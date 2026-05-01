package com.TransactionApp.SimulationTool;

import com.TransactionApp.model.Link;
import com.TransactionApp.model.Stream;

import java.util.List;

public class Frame {
    public final int streamId;
    public final int instanceId;
    public final int priority;
    public final int sizeBytes;
    public final double releaseTime;
    public final List<Link> path;

    public int nextHopIndex = 0;
    public double finishTime = -1.0;

    public Frame(Stream stream, int instanceId, double releaseTime, List<Link> path) {
        this.streamId = stream.id;
        this.instanceId = instanceId;
        this.priority = stream.PCP;
        this.sizeBytes = stream.size;
        this.releaseTime = releaseTime;
        this.path = path;
    }

    public boolean hasNextHop() {
        return nextHopIndex < path.size();
    }

    public Link getNextLink() {
        return path.get(nextHopIndex);
    }

    public void advanceHop() {
        nextHopIndex++;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "streamId=" + streamId +
                ", instanceId=" + instanceId +
                ", priority=" + priority +
                ", sizeBytes=" + sizeBytes +
                ", releaseTime=" + releaseTime +
                ", pathLength=" + path.size() +
                '}';
    }
}