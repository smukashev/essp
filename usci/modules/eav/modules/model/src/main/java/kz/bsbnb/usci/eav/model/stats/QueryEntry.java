package kz.bsbnb.usci.eav.model.stats;

import java.io.Serializable;

public class QueryEntry implements Serializable {
    public long maxTime;
    public long minTime;
    public long totalTime;
    public long count;
}
