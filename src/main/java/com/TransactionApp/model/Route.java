package com.TransactionApp.model;

import java.util.List;

public class Route {
    public int flow_id;
    public List<List<NodePort>> paths;
    public double min_e2e_delay;
}

