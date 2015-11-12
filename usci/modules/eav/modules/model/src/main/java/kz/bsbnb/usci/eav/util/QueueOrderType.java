package kz.bsbnb.usci.eav.util;

import java.util.HashMap;
import java.util.Map;

public enum QueueOrderType implements IGlobal {
    CHRONOLOGICAL("CHRONOLOGICAL-ORDER-DESCRIPTION"),
    CREDITOR_CYCLE("CYCLE-ORDER-DESCRIPTION"),
    MINIMUM_WEIGHT("MINIMUM-WEIGHT-ORDER-DESCRIPTION");

    public String description;

    private QueueOrderType(String description){
        this.description = description;
    }

    @Override
    public String type() {
        return "QUEUE_ALGO_OPTION";
    }

    @Override
    public String code() {
        return name();
    }

    private static final Map<String, QueueOrderType> ORDER_TYPES_BY_CODE = new HashMap<>();

    static {
        for (QueueOrderType type : QueueOrderType.values()) {
            ORDER_TYPES_BY_CODE.put(type.code(), type);
        }
    }

    public static QueueOrderType getQueueOrderTypeByCode(String code) {
        return ORDER_TYPES_BY_CODE.get(code);
    }


    public String getDescription(){
        return this.description;
    }
}
