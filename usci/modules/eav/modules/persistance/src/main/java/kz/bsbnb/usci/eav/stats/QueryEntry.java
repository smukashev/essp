package kz.bsbnb.usci.eav.stats;

import java.io.Serializable;

public class QueryEntry implements Serializable {
    public double maxTime;
    public double minTime;
    public double totalTime;
    public long count;
}
