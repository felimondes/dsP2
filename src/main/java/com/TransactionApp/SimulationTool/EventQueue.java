package com.TransactionApp.SimulationTool;

import java.util.PriorityQueue;

public class EventQueue {

    private final PriorityQueue<SimulationEvent> events = new PriorityQueue<>();

    public void add(SimulationEvent event) {
        events.add(event);
    }

    public SimulationEvent poll() {
        return events.poll();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }
}