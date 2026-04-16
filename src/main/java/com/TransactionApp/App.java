package com.TransactionApp;

import com.TransactionApp.parser.JsonParser;
import com.TransactionApp.parser.TestCaseData;

public class App {
    public static void main(String[] args) throws Exception {

        JsonParser jsonParser = new JsonParser();
        TestCaseData testCase = jsonParser.parseTestCase("src/main/resources/examples/test_case_1");

        assertCondition(testCase != null, "Parsed test case is null");
        assertCondition(testCase.streams != null, "Streams section is null");
        assertCondition(testCase.routes != null, "Routes section is null");
        assertCondition(testCase.topology != null, "Topology section is null");

        assertCondition(testCase.streams.streams.size() == 10, "Expected 10 streams");
        assertCondition(testCase.routes.routes.size() == 10, "Expected 10 routes");
        assertCondition(testCase.topology.topology.switches.size() == 2, "Expected 2 switches");
        assertCondition(testCase.topology.topology.links.size() == 6, "Expected 6 links");

        System.out.println("Main smoke test PASSED: "
                + testCase.streams.streams.get(0).name
                + ", routes=" + testCase.routes.routes.size()
                + ", links=" + testCase.topology.topology.links.size());
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Main smoke test FAILED: " + message);
        }
    }
}
