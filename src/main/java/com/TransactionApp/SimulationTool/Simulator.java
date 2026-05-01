package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.RoutesFile;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.model.TopologyWrapper;
import com.TransactionApp.parser.JsonParser;
import com.TransactionApp.parser.TestCaseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.TransactionApp.SimulationTool.HyperPeriodCalculator.computeHyperPeriod;

public class Simulator {

    public static void main(String[] args) throws IOException {


        Boolean debugPrints = false;

        JsonParser parser = new JsonParser();

        TestCaseData testCase = parser.parseTestCase(
                "src/main/resources/examples/test_case_hand_2"
        );

        final StreamsFile streams = testCase.streamsFile;
        final RoutesFile routes = testCase.routesFile;
        final TopologyWrapper topology = testCase.topologyWrapper;

        long hyperPeriod = computeHyperPeriod(streams);

        int simulationHyperperiods = 10;
        double releaseHorizon = simulationHyperperiods * (double) hyperPeriod;

        RouteResolver routeResolver = new RouteResolver(topology);

        FrameReleaseGenerator frameReleaseGenerator =
                new FrameReleaseGenerator(
                        streams,
                        routes,
                        routeResolver
                );

        List<Frame> frames =
                frameReleaseGenerator.generateFrames(releaseHorizon);

        EventQueue eventQueue = new EventQueue();

        for (Frame frame : frames) {
            eventQueue.add(new SimulationEvent(
                    frame.releaseTime,
                    EventType.FRAME_RELEASE,
                    frame
            ));
        }

        NetworkState networkState = new NetworkState(topology);
        List<Frame> deliveredFrames = new ArrayList<>();

        System.out.println("HyperPeriod: " + hyperPeriod);
        System.out.println("Release horizon: " + releaseHorizon);
        System.out.println("Generated frames: " + frames.size());
        System.out.println("Initial events: " + eventQueue.size());
        System.out.println();

        while (!eventQueue.isEmpty()) {
            SimulationEvent event = eventQueue.poll();

            networkState.updateAllCredits(event.time);

            switch (event.type) {
                case FRAME_RELEASE -> handleFrameRelease(
                        event,
                        networkState,
                        eventQueue,
                        debugPrints
                );

                case TRANSMISSION_END -> handleTransmissionEnd(
                        event,
                        networkState,
                        eventQueue,
                        deliveredFrames,
                        debugPrints
                );

                case CREDIT_RECOVERY -> handleCreditRecovery(
                        event,
                        eventQueue
                );
            }
        }

        System.out.println();
        System.out.println("Delivered frames: " + deliveredFrames.size());

        printObservedResponseTimes(deliveredFrames);
    }

    private static void handleFrameRelease(
            SimulationEvent event,
            NetworkState networkState,
            EventQueue eventQueue,
            Boolean debugPrints
    ) {
        Frame frame = event.frame;

        if (!frame.hasNextHop()) {
            throw new IllegalStateException(
                    "Released frame has no path: " + frame
            );
        }

        OutputPort outputPort =
                networkState.getOutputPort(frame.getNextLink());

        outputPort.enqueue(frame);

        TrafficClass trafficClass =
                TrafficClass.fromPriority(frame.priority);

        if(debugPrints){
            System.out.println(
                    "At time " + event.time +
                            ": enqueued stream " + frame.streamId +
                            ", instance " + frame.instanceId +
                            " on " + frame.getNextLink().id +
                            " in " + trafficClass +
                            " queue. Queues now: " +
                            outputPort.queueSummary()
            );
        }


        // Important:
        // Since a new frame arrived, the port may now be able to transmit.
        outputPort.tryStartTransmission(event.time, eventQueue);
    }

    private static void handleTransmissionEnd(
            SimulationEvent event,
            NetworkState networkState,
            EventQueue eventQueue,
            List<Frame> deliveredFrames,
            Boolean debugPrints
    ) {
        Frame frame = event.frame;

        OutputPort completedOutputPort =
                networkState.getOutputPort(frame.getNextLink());

        completedOutputPort.clearCurrentTransmission();

        if(debugPrints){
            System.out.println(
                    "At time " + event.time +
                            ": finished transmitting stream " + frame.streamId +
                            ", instance " + frame.instanceId +
                            " on " + frame.getNextLink().id
            );
        }


        frame.advanceHop();

        if (frame.hasNextHop()) {
            OutputPort nextOutputPort =
                    networkState.getOutputPort(frame.getNextLink());

            nextOutputPort.enqueue(frame);

            if(debugPrints){
                System.out.println(
                        "At time " + event.time +
                                ": moved stream " + frame.streamId +
                                ", instance " + frame.instanceId +
                                " to next link " + frame.getNextLink().id +
                                ". Queues now: " +
                                nextOutputPort.queueSummary()
                );
            }


            // Important:
            // The frame just arrived at this next port,
            // so this next port may now be able to transmit.
            nextOutputPort.tryStartTransmission(event.time, eventQueue);

        } else {
            frame.finishTime = event.time;
            deliveredFrames.add(frame);

            if(debugPrints){
                System.out.println(
                        "At time " + event.time +
                                ": delivered stream " + frame.streamId +
                                ", instance " + frame.instanceId +
                                ". Response time = " +
                                (frame.finishTime - frame.releaseTime)
                );
            }

        }

        // Important:
        // The old port just became free,
        // so it may be able to transmit another queued frame.
        completedOutputPort.tryStartTransmission(event.time, eventQueue);
    }

    private static void printObservedResponseTimes(List<Frame> deliveredFrames) {
        Map<Integer, SimulationResult> resultsByStream = new TreeMap<>();

        for (Frame frame : deliveredFrames) {
            resultsByStream
                    .computeIfAbsent(
                            frame.streamId,
                            SimulationResult::new
                    )
                    .record(frame);
        }

        System.out.println();
        System.out.println("Observed maximum response times:");

        for (SimulationResult result : resultsByStream.values()) {
            System.out.println(result);
        }
    }

    private static void handleCreditRecovery(
            SimulationEvent event,
            EventQueue eventQueue
    ) {
        OutputPort outputPort = event.outputPort;

        if (outputPort == null) {
            throw new IllegalStateException(
                    "Credit recovery event has no output port"
            );
        }

        boolean isValidRecoveryEvent =
                outputPort.consumeCreditRecoveryEvent(event.time);

        if (!isValidRecoveryEvent) {
            return;
        }

        outputPort.tryStartTransmission(event.time, eventQueue);
    }


}


