package kz.bsbnb.usci.eav.util;

/**
 * Created by maksat on 8/4/15.
 */
public enum BatchStatuses implements IGlobal {
    WAITING,
    PROCESSING,
    ERROR,
    COMPLETED;

    @Override
    public String type() {
        return "BATCH_STATUS";
    }

    @Override
    public String code() {
        return name();
    }
}
