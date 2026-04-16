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
        assertNotNull(data.streams);
        assertNotNull(data.routes);
        assertNotNull(data.topology);

        assertEquals("MICRO_SECOND", data.streams.delay_units);
        assertEquals(10, data.streams.streams.size());
        assertEquals("Stream0", data.streams.streams.get(0).name);

        assertEquals(10, data.routes.routes.size());
        assertEquals(0, data.routes.routes.get(0).flow_id);

        assertEquals(2, data.topology.topology.switches.size());
        assertEquals(6, data.topology.topology.links.size());
    }

    public void testParseTestCaseHandlesTrailingPathSeparator() throws Exception {
        JsonParser parser = new JsonParser();
        String basePath = new File("src/main/resources/examples/test_case_1").getPath();

        TestCaseData withoutSeparator = parser.parseTestCase(basePath);
        TestCaseData withSeparator = parser.parseTestCase(basePath + File.separator);

        assertEquals(withoutSeparator.streams.streams.size(), withSeparator.streams.streams.size());
        assertEquals(withoutSeparator.routes.routes.size(), withSeparator.routes.routes.size());
        assertEquals(withoutSeparator.topology.topology.links.size(), withSeparator.topology.topology.links.size());
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

