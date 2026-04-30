package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.*;

import java.util.*;

public class FrameReleaseGenerator {

    private final StreamsFile streamsFile;
    private final RoutesFile routesFile;
    private final RouteResolver routeResolver;

    public FrameReleaseGenerator(
            StreamsFile streamsFile,
            RoutesFile routesFile,
            RouteResolver routeResolver
    ) {
        this.streamsFile = streamsFile;
        this.routesFile = routesFile;
        this.routeResolver = routeResolver;
    }

    public List<Frame> generateFrames(long simulationDuration) {
        List<Frame> frames = new ArrayList<>();

        Map<Integer, Route> routeByStreamId = buildRouteMap();

        for (Stream stream : streamsFile.streams) {
            Route route = routeByStreamId.get(stream.id);

            if (route == null) {
                throw new IllegalArgumentException(
                        "No route found for stream " + stream.id
                );
            }

            // For now, use the first path only.
            // This matches the simple non-redundant case.
            List<NodePort> nodePortPath = route.paths.get(0);
            List<Link> linkPath = routeResolver.resolvePath(nodePortPath);

            int instanceId = 0;

            for (long releaseTime = 0;
                 releaseTime < simulationDuration;
                 releaseTime += stream.period) {

                frames.add(new Frame(stream, instanceId, releaseTime, linkPath));
                instanceId++;
            }
        }

        frames.sort(Comparator.comparingLong(frame -> frame.releaseTime));

        return frames;
    }

    private Map<Integer, Route> buildRouteMap() {
        Map<Integer, Route> routeByStreamId = new HashMap<>();

        for (Route route : routesFile.routes) {
            routeByStreamId.put(route.flow_id, route);
        }

        return routeByStreamId;
    }
}