package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.*;

import java.util.*;

public class RouteResolver {

    private final Map<String, Link> egressToLink = new HashMap<>();

    public RouteResolver(TopologyWrapper topologyWrapper) {
        for (Link link : topologyWrapper.topology.links) {
            String key = key(link.source, link.sourcePort);
            egressToLink.put(key, link);
        }
    }

    public List<Link> resolvePath(List<NodePort> path) {
        List<Link> links = new ArrayList<>();

        // Last node is destination, not an egress hop
        for (int i = 0; i < path.size() - 1; i++) {
            NodePort hop = path.get(i);
            Link link = egressToLink.get(key(hop.node, hop.port));

            if (link == null) {
                throw new IllegalArgumentException(
                        "No link found for egress " + hop.node + ":" + hop.port
                );
            }

            links.add(link);
        }

        return links;
    }

    private String key(String node, int port) {
        return node + ":" + port;
    }
}