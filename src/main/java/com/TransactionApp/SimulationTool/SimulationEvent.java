package com.TransactionApp.SimulationTool;

public class SimulationEvent implements Comparable<SimulationEvent> {

    public final double time;
    public final EventType type;
    public final Frame frame;
    public final OutputPort outputPort;

    public SimulationEvent(double time, EventType type, Frame frame) {
        this.time = time;
        this.type = type;
        this.frame = frame;
        this.outputPort = null;
    }

    public SimulationEvent(double time, EventType type, OutputPort outputPort) {
        this.time = time;
        this.type = type;
        this.frame = null;
        this.outputPort = outputPort;
    }

    @Override
    public int compareTo(SimulationEvent other) {
        int byTime = Double.compare(this.time, other.time);

        if (byTime != 0) {
            return byTime;
        }

        int byEventPriority =
                Integer.compare(eventPriority(this.type), eventPriority(other.type));

        if (byEventPriority != 0) {
            return byEventPriority;
        }

        if (this.type == EventType.FRAME_RELEASE
                && other.type == EventType.FRAME_RELEASE
                && this.frame != null
                && other.frame != null) {

            // Higher PCP should be processed first.
            int byFramePriority =
                    Integer.compare(other.frame.priority, this.frame.priority);

            if (byFramePriority != 0) {
                return byFramePriority;
            }

            int byStreamId =
                    Integer.compare(this.frame.streamId, other.frame.streamId);

            if (byStreamId != 0) {
                return byStreamId;
            }

            return Integer.compare(this.frame.instanceId, other.frame.instanceId);
        }

        return 0;
    }

    private int eventPriority(EventType type) {
        return switch (type) {
            case TRANSMISSION_END -> 0;
            case FRAME_RELEASE -> 1;
            case CREDIT_RECOVERY -> 2;
        };
    }
}