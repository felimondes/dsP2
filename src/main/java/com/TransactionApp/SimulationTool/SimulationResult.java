package com.TransactionApp.SimulationTool;

public class SimulationResult {

    public final int streamId;
    public double maxResponseTime = 0.0;
    public int deliveredFrames = 0;

    public SimulationResult(int streamId) {
        this.streamId = streamId;
    }

    public void record(Frame frame) {
        double responseTime = frame.finishTime - frame.releaseTime;

        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }

        deliveredFrames++;
    }

    @Override
    public String toString() {
        return "Stream " + streamId +
                ": max observed response time = " +
                String.format("%.3f", maxResponseTime) +
                " us, delivered frames = " + deliveredFrames;
    }
}