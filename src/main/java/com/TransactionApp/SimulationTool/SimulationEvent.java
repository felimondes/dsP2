package com.TransactionApp.SimulationTool;

public class SimulationEvent implements Comparable<SimulationEvent> {

    public final long time;
    public final EventType type;
    public final Frame frame;
    public final OutputPort outputPort;

    public SimulationEvent(long time, EventType type, Frame frame) {
        this.time = time;
        this.type = type;
        this.frame = frame;
        this.outputPort = null;
    }

    public SimulationEvent(long time, EventType type, OutputPort outputPort) {
        this.time = time;
        this.type = type;
        this.frame = null;
        this.outputPort = outputPort;
    }

    @Override
    public int compareTo(SimulationEvent other) {
        int byTime = Long.compare(this.time, other.time);

        if (byTime != 0) {
            return byTime;
        }

        return Integer.compare(eventPriority(this.type), eventPriority(other.type));
    }

    private int eventPriority(EventType type) {
        return switch (type) {
            case TRANSMISSION_END -> 0;
            case FRAME_RELEASE -> 1;
            case CREDIT_RECOVERY -> 2;
        };
    }
}