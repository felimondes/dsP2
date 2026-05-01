package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.Link;

import java.util.ArrayDeque;
import java.util.Queue;

public class OutputPort {

    private Boolean debugPrints = false;

    public final Link link;

    private final int defaultBandwidthMbps;

    private static final double EPSILON = 1e-9;

    private final Queue<Frame> classAQueue = new ArrayDeque<>();
    private final Queue<Frame> classBQueue = new ArrayDeque<>();
    private final Queue<Frame> bestEffortQueue = new ArrayDeque<>();

    private Frame currentlyTransmittingFrame = null;
    private TrafficClass currentlyTransmittingClass = null;

    private double creditA = 0.0;
    private double creditB = 0.0;

    private Double pendingCreditRecoveryTime = null;

    private final double idleSlopeA = 0.5;
    private final double sendSlopeA = -0.5;

    private final double idleSlopeB = 0.5;
    private final double sendSlopeB = -0.5;

    private double lastCreditUpdateTime = 0.0;

    public OutputPort(Link link, int defaultBandwidthMbps) {
        this.link = link;
        this.defaultBandwidthMbps = defaultBandwidthMbps;
    }

    public void enqueue(Frame frame) {
        TrafficClass trafficClass = TrafficClass.fromPriority(frame.priority);

        switch (trafficClass) {
            case CLASS_A -> classAQueue.add(frame);
            case CLASS_B -> classBQueue.add(frame);
            case BEST_EFFORT -> bestEffortQueue.add(frame);
        }
    }

    public void updateCredits(double currentTime) {
        double elapsed = currentTime - lastCreditUpdateTime;

        if (elapsed <= 0) {
            return;
        }

        updateClassACredit(elapsed);
        updateClassBCredit(elapsed);

        resetPositiveCreditIfQueueIdle();
        clampTinyCreditsToZero();

        lastCreditUpdateTime = currentTime;
    }

    private void updateClassACredit(double elapsed) {
        if (currentlyTransmittingClass == TrafficClass.CLASS_A) {
            creditA += sendSlopeA * elapsed;
            return;
        }

        if (!classAQueue.isEmpty()) {
            creditA += idleSlopeA * elapsed;
            return;
        }

        if (creditA < 0) {
            creditA = Math.min(0, creditA + idleSlopeA * elapsed);
        }
    }

    private void updateClassBCredit(double elapsed) {
        if (currentlyTransmittingClass == TrafficClass.CLASS_B) {
            creditB += sendSlopeB * elapsed;
            return;
        }

        if (!classBQueue.isEmpty()) {
            creditB += idleSlopeB * elapsed;
            return;
        }

        if (creditB < 0) {
            creditB = Math.min(0, creditB + idleSlopeB * elapsed);
        }
    }

    private void resetPositiveCreditIfQueueIdle() {
        if (classAQueue.isEmpty()
                && currentlyTransmittingClass != TrafficClass.CLASS_A
                && creditA > 0) {
            creditA = 0;
        }

        if (classBQueue.isEmpty()
                && currentlyTransmittingClass != TrafficClass.CLASS_B
                && creditB > 0) {
            creditB = 0;
        }
    }

    public boolean isBusy() {
        return currentlyTransmittingFrame != null;
    }

    public void clearCurrentTransmission() {
        currentlyTransmittingFrame = null;
        currentlyTransmittingClass = null;
    }

    public void tryStartTransmission(double currentTime, EventQueue eventQueue) {
        updateCredits(currentTime);

        if (isBusy()) {
            return;
        }

        Frame nextFrame = dequeueCBS();

        if (nextFrame == null) {
            scheduleCreditRecoveryIfNeeded(currentTime, eventQueue);
            return;
        }

        currentlyTransmittingFrame = nextFrame;
        currentlyTransmittingClass = TrafficClass.fromPriority(nextFrame.priority);

        pendingCreditRecoveryTime = null;

        double transmissionTime = computeTransmissionTimeMicroseconds(nextFrame);
        double endTime = currentTime + transmissionTime;

        eventQueue.add(new SimulationEvent(
                endTime,
                EventType.TRANSMISSION_END,
                nextFrame
        ));

        if(debugPrints){
            System.out.println(
                    "At time " + currentTime +
                            ": started transmitting stream " + nextFrame.streamId +
                            ", instance " + nextFrame.instanceId +
                            " on " + link.id +
                            " as " + currentlyTransmittingClass +
                            ". Transmission ends at " + endTime +
                            ". Credits: " + creditSummary()
            );
        }

    }

    private Frame dequeueCBS() {
        if (!classAQueue.isEmpty() && creditA >= -EPSILON) {
            creditA = Math.max(creditA, 0.0);
            return classAQueue.poll();
        }

        if (!classBQueue.isEmpty() && creditB >= -EPSILON) {
            creditB = Math.max(creditB, 0.0);
            return classBQueue.poll();
        }

        if (!bestEffortQueue.isEmpty()) {
            return bestEffortQueue.poll();
        }

        return null;
    }

    private void scheduleCreditRecoveryIfNeeded(
            double currentTime,
            EventQueue eventQueue
    ) {
        Double nextRecoveryTime = null;

        if (!classAQueue.isEmpty() && creditA < -EPSILON) {
            double recoveryTime = currentTime + (-creditA / idleSlopeA);
            nextRecoveryTime = recoveryTime;
        }

        if (!classBQueue.isEmpty() && creditB < -EPSILON) {
            double recoveryTime = currentTime + (-creditB / idleSlopeB);

            if (nextRecoveryTime == null || recoveryTime < nextRecoveryTime) {
                nextRecoveryTime = recoveryTime;
            }
        }

        if (nextRecoveryTime == null) {
            return;
        }

        // Important: never schedule a recovery event at the same time.
        if (nextRecoveryTime <= currentTime + EPSILON) {
            return;
        }

        if (pendingCreditRecoveryTime != null
                && pendingCreditRecoveryTime <= nextRecoveryTime + EPSILON) {
            return;
        }

        pendingCreditRecoveryTime = nextRecoveryTime;

        eventQueue.add(new SimulationEvent(
                nextRecoveryTime,
                EventType.CREDIT_RECOVERY,
                this
        ));

        if (debugPrints) {
            System.out.println(
                    "At time " + currentTime +
                            ": scheduled credit recovery on " + link.id +
                            " at time " + nextRecoveryTime +
                            ". Credits: " + creditSummary()
            );
        }
    }

    private double computeTransmissionTimeMicroseconds(Frame frame) {
        int bandwidthMbps = getBandwidthMbps();

        return (frame.sizeBytes * 8.0) / bandwidthMbps;
    }

    private int getBandwidthMbps() {
        if (link.bandwidth_mbps > 0) {
            return link.bandwidth_mbps;
        }

        return defaultBandwidthMbps;
    }

    public String queueSummary() {
        return "A=" + classAQueue.size()
                + ", B=" + classBQueue.size()
                + ", BE=" + bestEffortQueue.size();
    }

    public String creditSummary() {
        return "creditA=" + String.format("%.2f", creditA)
                + ", creditB=" + String.format("%.2f", creditB);
    }

    public boolean consumeCreditRecoveryEvent(double eventTime) {
        if (pendingCreditRecoveryTime == null) {
            return false;
        }

        if (Math.abs(pendingCreditRecoveryTime - eventTime) > EPSILON) {
            return false;
        }

        pendingCreditRecoveryTime = null;
        return true;
    }

    private void clampTinyCreditsToZero() {
        if (Math.abs(creditA) < EPSILON) {
            creditA = 0.0;
        }

        if (Math.abs(creditB) < EPSILON) {
            creditB = 0.0;
        }
    }


}