package com.TransactionApp.SimulationTool;

public enum TrafficClass {
    CLASS_A,
    CLASS_B,
    BEST_EFFORT;

    public static TrafficClass fromPriority(int pcp) {
        return switch (pcp) {
            case 2 -> CLASS_A;
            case 1 -> CLASS_B;
            case 0 -> BEST_EFFORT;
            default -> throw new IllegalArgumentException(
                    "Unsupported PCP value: " + pcp
            );
        };
    }
}