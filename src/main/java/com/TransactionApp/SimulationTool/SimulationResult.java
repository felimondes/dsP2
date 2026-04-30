package com.TransactionApp.SimulationTool;

public class SimulationResult {

    public final int streamId;
    public long maxResponseTime = 0;
    public int deliveredFrames = 0;

    public SimulationResult(int streamId) {
        this.streamId = streamId;
    }

    public void record(Frame frame) {
        long responseTime = frame.finishTime - frame.releaseTime;

        if (responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }

        deliveredFrames++;
    }

    @Override
    public String toString() {
        return "Stream " + streamId +
                ": max observed response time = " + maxResponseTime +
                " us, delivered frames = " + deliveredFrames;
    }
}