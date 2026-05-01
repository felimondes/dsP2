package com.TransactionApp.analyticalTool;

import com.TransactionApp.model.Link;
import com.TransactionApp.model.NodePort;
import com.TransactionApp.model.Route;
import com.TransactionApp.model.Stream;
import com.TransactionApp.parser.TestCaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WCRTTool {


    double sizeToBit = 8.0;
    Map<Integer, List<Stream>> streamByPriority;
    Map<Integer, Stream> streamById;
    Map<Integer, Route> routeByFlowId;
    Map<String, Link> linksBySourceAndDestination;
    TestCaseData testCaseData;



    Map<Integer, Double> aPlus = new HashMap<>();
    Map<Integer, Double> aMinus = new HashMap<>();


    public WCRTTool(TestCaseData testCaseData) {
        streamById = indexStreams(testCaseData);
        routeByFlowId = indexRoutes(testCaseData);
        streamByPriority = indexStreamsByPriority(testCaseData);
        linksBySourceAndDestination = makeLinksBySourceAndDestination(testCaseData);
        this.testCaseData = testCaseData;

        aPlus = new HashMap<>();
        aMinus = new HashMap<>();
        aPlus.put(1, 0.5);
        aMinus.put(1, 0.5);
        aPlus.put(2, 0.5);
        aMinus.put(2, 0.5);
    }


    public Map<Integer, Double> calculateWCRT() throws Exception {



        Map <Integer, Double> result = new HashMap<>();


        for (Map.Entry<Integer, Stream> entry : streamById.entrySet()) {
            Integer key = entry.getKey();
            Stream stream = entry.getValue();
            Route route = routeByFlowId.get(key);
            boolean isBE = stream.PCP == 0; //BE is priority 0
            if (isBE) {
                result.put(stream.id, Double.POSITIVE_INFINITY); // BE streams have infinite WCRT
                continue;
            }
            double WCRTPerStream = calculateWCRTPerStream(stream, route.paths);
            result.put(stream.id, WCRTPerStream);
        }
        return result;
    }

    private double calculateWCRTPerStream(Stream stream, List<List<NodePort>> paths) throws Exception {
        boolean isAssumptionWrong = paths.size() > 1; //more than one path, means that prolly ith stream has ith route.
        if (isAssumptionWrong) {
            throw new Exception("Assumption is wrong for stream " + stream.id);
        }

        List<NodePort> path = paths.get(0); //theres always just 1 path in these test cases.
        double result = 0;

        for (int i = 0; i < path.size()-1; i++) {
            String source = path.get(i).node;
            String destination = path.get(i+1).node;
            Link link = getLink(source, destination);
            result += calculateWCRTPerLink(stream, link);
        }
        return result;
    }

    private Link getLink(String source, String destination) {
        String linkKey = source + ":" + destination;
        Link link = linksBySourceAndDestination.get(linkKey);
        if (link == null) {
            throw new RuntimeException("Link not found for source: " + source + " and destination: " + destination);
        }
        return link;
    }

    private double calculateWCRTPerLink(Stream stream, Link link) {
        double BWD = link.bandwidth_mbps;
        BWD = 100;
        double C = ((double) stream.size * sizeToBit) / (BWD);
        double SPI = SPI(stream, BWD);
        double HPI = HPI(stream, BWD);
        double LPI = LPI(stream, BWD);

        double msWCRT = (C + SPI + HPI + LPI);
        return msWCRT;
    }

    private double SPI(Stream stream, double BWD) {
        List<Stream> samePriorityStreams = streamByPriority.get(stream.PCP);
        double SPI = 0;
        for (Stream s : samePriorityStreams) {
            if (s.id == stream.id) continue; // skip the stream itself
            double transmission = ((double) s.size * sizeToBit) / (BWD);
            double factor = 1.0 + (aPlus.get(stream.PCP) / aMinus.get(stream.PCP));
            SPI += transmission * factor;
        }
        return SPI;
    }
    private double LPI(Stream stream, double BWD) {
        int lowestPriority = 0; //
        if (stream.PCP == lowestPriority) {
            return 0;
        }

        double max_C = 0; //find the maximum C among all higher priority streams
        int currentPriority = stream.PCP - 1;
        while (currentPriority >= lowestPriority) {
            List<Stream> lowerPriorityStreams = streamByPriority.get(currentPriority);
            for (Stream s : lowerPriorityStreams) {
                double C = ((double) s.size * 8.0) / (BWD);
                if (C > max_C) {
                    max_C = C;
                }
            }
            currentPriority--;
        }

        return max_C;
    }
    private double HPI(Stream stream, double BWD) {

        int highestPriority = streamByPriority.size() - 1; //
        if (stream.PCP == highestPriority) {
            return 0;
        }

        double max_C = 0; //find the maximum C among all higher priority streams
        int currentPriority = stream.PCP + 1;
        while (currentPriority <= highestPriority) {
            List<Stream> higherPriorityStreams = streamByPriority.get(currentPriority);
            for (Stream s : higherPriorityStreams) {
                double C = ((double) s.size * 8.0) / (BWD);
                if (C > max_C) {
                    max_C = C;
                }
            }
            currentPriority++;
        }


        double lpi = LPI(stream, BWD);
        double alphaMinus = aMinus.get(stream.PCP);
        double alphaPlus = aPlus.get(stream.PCP);
        double factor = alphaPlus / alphaMinus;

        return (lpi * factor) + max_C;
    }

    private Map<Integer, List<Stream>> indexStreamsByPriority(TestCaseData testCaseData) {
        Map<Integer, List<Stream>> streamByPriority = new HashMap<>();

        List<Stream> streams = testCaseData.streamsFile.streams;
        for (Stream stream : streams) {
            boolean isOddStream = (stream.id % 2) == 1;
            if (isOddStream) continue;
            streamByPriority
                    .computeIfAbsent(stream.PCP, k -> new ArrayList<>())
                    .add(stream);
        }

        return streamByPriority;
    }
    private Map<Integer, Stream> indexStreams(TestCaseData testCaseData) {
        Map<Integer, Stream> streamById = new HashMap<>();
        for (Stream stream : testCaseData.streamsFile.streams) {
            boolean isOddStream = (stream.id % 2) == 1;
            if (isOddStream) continue;
            streamById.put(stream.id, stream);
        }
        return streamById;
    }
    private Map<Integer, Route> indexRoutes(TestCaseData testCaseData) {
        Map<Integer, Route> routeByFlowId = new HashMap<>();
        for (Route route : testCaseData.routesFile.routes) {
            boolean isOddRoute = (route.flow_id % 2) == 1;
            if (isOddRoute) continue;
            routeByFlowId.put(route.flow_id, route);
        }
        return routeByFlowId;
    }

    private Map<String, Link> makeLinksBySourceAndDestination(TestCaseData testCaseData) {
        Map<String, Link> linksBySourceAndDestination = new HashMap<>();
        for (Link link : testCaseData.topologyWrapper.topology.links) {
            String key = link.source + ":" + link.destination;
            linksBySourceAndDestination.put(key, link);
        }
        return linksBySourceAndDestination;
    }
}
