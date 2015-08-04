package kz.bsbnb.usci.eav.util;

/**
 * Created by maksat on 8/4/15.
 */
public enum EntityStatuses implements IGlobal {
    ERROR,
    PARSING,
    CHECK_IN_PARSER,
    WAITING,
    PROCESSING,
    CHECK_IN_CORE,
    SAVING,
    COMPLETED,
    TOTAL_COUNT,
    ACTUAL_COUNT;

    @Override
    public String type() {
        return "ENTITY_STATUS";
    }

    @Override
    public String code() {
        return name();
    }
}
