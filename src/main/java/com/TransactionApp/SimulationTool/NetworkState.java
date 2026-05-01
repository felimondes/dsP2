package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.Link;
import com.TransactionApp.model.TopologyWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NetworkState {

    private final Map<String, OutputPort> outputPortsByLinkId = new HashMap<>();

    public NetworkState(TopologyWrapper topologyWrapper) {
        int defaultBandwidthMbps =
                topologyWrapper.topology.default_bandwidth_mbps;

        for (Link link : topologyWrapper.topology.links) {
            outputPortsByLinkId.put(
                    link.id,
                    new OutputPort(link, defaultBandwidthMbps)
            );
        }
    }

    public OutputPort getOutputPort(Link link) {
        OutputPort outputPort = outputPortsByLinkId.get(link.id);

        if (outputPort == null) {
            throw new IllegalArgumentException(
                    "No output port found for link " + link.id
            );
        }

        return outputPort;
    }

    public Collection<OutputPort> getAllOutputPorts() {
        return outputPortsByLinkId.values();
    }

    public void updateAllCredits(double currentTime) {
        for (OutputPort outputPort : outputPortsByLinkId.values()) {
            outputPort.updateCredits(currentTime);
        }
    }
}