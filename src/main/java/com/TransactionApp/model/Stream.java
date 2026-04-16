package com.TransactionApp.model;

import java.util.List;

public class Stream {
    public int id;
    public String name;
    public String source;
    public List<Destination> destinations;
    public String type;
    public int PCP;
    public int size;
    public int period;
    public int redundancy;
}

