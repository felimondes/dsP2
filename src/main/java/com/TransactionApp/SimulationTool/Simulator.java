package com.TransactionApp.SimulationTool;



import java.io.IOException;

import com.TransactionApp.model.RoutesFile;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.model.TopologyWrapper;
import com.TransactionApp.parser.JsonParser;
import com.TransactionApp.parser.TestCaseData;

import static com.TransactionApp.SimulationTool.HyperPeriodCalculator.computeHyperPeriod;

public class Simulator {

    public static void main(String[] args) throws IOException {

        // 1. Parse input (adjust path as needed)
        JsonParser parser = new JsonParser();
        TestCaseData testCase = parser.parseTestCase("src/main/resources/examples/test_case_1");

        final StreamsFile streams = testCase.streams;

        final RoutesFile routes = testCase.routes;

        final TopologyWrapper topology = testCase.topology;

        // 2. Compute hyperPeriod
        long hyperPeriod = computeHyperPeriod(streams);

        // 3. Print result
        System.out.println("HyperPeriod: " + hyperPeriod);
    }

}
