package com.TransactionApp.model;

import java.util.List;

public class Topology {
    public String delay_units;
    public int default_bandwidth_mbps;
    public List<Switch> switches;
    public List<EndSystem> end_systems;
    public List<Link> links;
}

