package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.Stream;
import com.TransactionApp.model.StreamsFile;

public class HyperPeriodCalculator {

    public static long computeHyperPeriod(StreamsFile streamsFile) {
        long lcm = 1;

        for (Stream s : streamsFile.streams) {
            lcm = lcm(lcm, s.period);
        }

        return lcm;
    }

    private static long lcm(long a, long b) {
        return (a * b) / gcd(a, b);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
