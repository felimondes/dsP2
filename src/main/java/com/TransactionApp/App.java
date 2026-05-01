package com.TransactionApp;

import com.TransactionApp.analyticalTool.WCRTTool;
import com.TransactionApp.parser.JsonParser;
import com.TransactionApp.parser.TestCaseData;

import java.util.Map;

public class App {



    public static void main(String[] args) throws Exception {

        JsonParser jsonParser = new JsonParser();
        TestCaseData testCaseData = jsonParser.parseTestCase("src/main/resources/examples/test_case_2");
        WCRTTool wcdTool = new WCRTTool(testCaseData);
        Map<Integer, Double> wcrt = wcdTool.calculateWCRT();

        //print values
        for (Map.Entry<Integer, Double> entry : wcrt.entrySet()) {
            System.out.println("Stream ID: " + entry.getKey() + ", WCRT: " + entry.getValue());
        }
    }
}
