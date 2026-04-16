package com.TransactionApp.parser;

import com.TransactionApp.model.RoutesFile;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.model.TopologyWrapper;

public class TestCaseData {
    public final StreamsFile streams;
    public final RoutesFile routes;
    public final TopologyWrapper topology;

    public TestCaseData(StreamsFile streams, RoutesFile routes, TopologyWrapper topology) {
        this.streams = streams;
        this.routes = routes;
        this.topology = topology;
    }
}

