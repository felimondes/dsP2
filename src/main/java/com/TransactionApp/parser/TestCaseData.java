package com.TransactionApp.parser;

import com.TransactionApp.model.RoutesFile;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.model.TopologyWrapper;

public class TestCaseData {
    public final StreamsFile streamsFile;
    public final RoutesFile routesFile;
    public final TopologyWrapper topologyWrapper;

    public TestCaseData(StreamsFile streams, RoutesFile routes, TopologyWrapper topology) {
        this.streamsFile = streams;
        this.routesFile = routes;
        this.topologyWrapper = topology;
    }
}

