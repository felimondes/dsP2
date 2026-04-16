package com.TransactionApp.SimulationTool;

import java.io.File;
import java.util.Collections;

import com.TransactionApp.model.Stream;
import com.TransactionApp.model.StreamsFile;
import com.TransactionApp.parser.JsonParser;
import com.TransactionApp.parser.TestCaseData;

import junit.framework.TestCase;

public class HyperPeriodCalculatorTest extends TestCase {

    public void testComputeHyperPeriodFromExampleTestCase1() throws Exception {
        JsonParser parser = new JsonParser();
        String testCaseDir = new File("src/main/resources/examples/test_case_1").getPath();

        TestCaseData data = parser.parseTestCase(testCaseDir);

        long hyperPeriod = HyperPeriodCalculator.computeHyperPeriod(data.streams);

        assertEquals(2000L, hyperPeriod);
    }

    public void testComputeHyperPeriodWithSingleStream() {
        StreamsFile streamsFile = new StreamsFile();
        Stream stream = new Stream();
        stream.period = 500;
        streamsFile.streams = Collections.singletonList(stream);

        long hyperPeriod = HyperPeriodCalculator.computeHyperPeriod(streamsFile);

        assertEquals(500L, hyperPeriod);
    }
}
