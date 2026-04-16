package com.TransactionApp.parser;

import com.TransactionApp.model.RoutesFile;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.model.TopologyWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonParser {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private StreamsFile parseStreams(String path) throws IOException {
        return mapper.readValue(new File(path), StreamsFile.class);
    }

    private RoutesFile parseRoutes(String path) throws IOException {
        return mapper.readValue(new File(path), RoutesFile.class);
    }

    private TopologyWrapper parseTopology(String path) throws IOException {
        return mapper.readValue(new File(path), TopologyWrapper.class);
    }

    public TestCaseData parseTestCase(String directoryPath) throws IOException {
        String normalized = directoryPath.endsWith("/") || directoryPath.endsWith("\\")
                ? directoryPath.substring(0, directoryPath.length() - 1)
                : directoryPath;

        StreamsFile streams = parseStreams(normalized + File.separator + "streams.json");
        RoutesFile routes = parseRoutes(normalized + File.separator + "routes.json");
        TopologyWrapper topology = parseTopology(normalized + File.separator + "topology.json");

        return new TestCaseData(streams, routes, topology);
    }
}

