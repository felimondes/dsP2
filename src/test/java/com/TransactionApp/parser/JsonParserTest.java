package com.TransactionApp.parser;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class JsonParserTest extends TestCase {

    public void testParseTestCaseLoadsExpectedData() throws Exception {
        JsonParser parser = new JsonParser();
        String testCaseDir = new File("src/main/resources/examples/test_case_1").getPath();

        TestCaseData data = parser.parseTestCase(testCaseDir);

        assertNotNull(data);
        assertNotNull(data.streamsFile);
        assertNotNull(data.routesFile);
        assertNotNull(data.topologyWrapper);

        assertEquals("MICRO_SECOND", data.streamsFile.delay_units);
        assertEquals(10, data.streamsFile.streams.size());
        assertEquals("Stream0", data.streamsFile.streams.get(0).name);

        assertEquals(10, data.routesFile.routes.size());
        assertEquals(0, data.routesFile.routes.get(0).flow_id);

        assertEquals(2, data.topologyWrapper.topology.switches.size());
        assertEquals(6, data.topologyWrapper.topology.links.size());
    }

    public void testParseTestCaseHandlesTrailingPathSeparator() throws Exception {
        JsonParser parser = new JsonParser();
        String basePath = new File("src/main/resources/examples/test_case_1").getPath();

        TestCaseData withoutSeparator = parser.parseTestCase(basePath);
        TestCaseData withSeparator = parser.parseTestCase(basePath + File.separator);

        assertEquals(withoutSeparator.streamsFile.streams.size(), withSeparator.streamsFile.streams.size());
        assertEquals(withoutSeparator.routesFile.routes.size(), withSeparator.routesFile.routes.size());
        assertEquals(withoutSeparator.topologyWrapper.topology.links.size(), withSeparator.topologyWrapper.topology.links.size());
    }

    public void testParseTestCaseThrowsForMissingDirectory() {
        JsonParser parser = new JsonParser();

        try {
            parser.parseTestCase("src/main/resources/examples/does_not_exist");
            fail("Expected IOException for missing test case directory");
        } catch (IOException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}

