package com.bsbnb.creditregistry.portlets.queue.thread.logic;

import com.bsbnb.creditregistry.portlets.queue.ui.Localization;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public enum QueueOrderType {

    CHRONOLOGICAL("CHRONOLOGICAL", Localization.CHRONOLOGICAL_ORDER_DESCRIPTION, new OrderFactory() {
        public QueueOrder createOrder() {
            return new ChronologicalOrder();
        }
    }),
    CREDITOR_CYCLE("CREDITOR_CYCLE", Localization.CYCLE_ORDER_DESCRIPTION, new OrderFactory() {
        public QueueOrder createOrder() {
            return new CreditorCycleOrder();
        }
    }),
    MINIMAL_WEIGHT_FIRST("MINIMUM_WEIGHT", Localization.MINIMUM_WEIGHT_ORDER_DESCRIPTION, new OrderFactory() {
        public QueueOrder createOrder() {
            return new MinimumWeightOrder();
        }
    });

    private static interface OrderFactory {
        public QueueOrder createOrder();
    }
    private final OrderFactory factory;
    private final String code;
    private final Localization description;

    private QueueOrderType(String code, Localization description, OrderFactory factory) {
        this.code = code;
        this.description = description;
        this.factory = factory;
    }
    private static final Map<String, QueueOrderType> ORDER_TYPES_BY_CODE = new HashMap<String, QueueOrderType>();

    static {
        for (QueueOrderType type : QueueOrderType.values()) {
            ORDER_TYPES_BY_CODE.put(type.getCode(), type);
        }
    }

    public static QueueOrderType getQueueOrderTypeByCode(String code) {
        return ORDER_TYPES_BY_CODE.get(code);
    }

    public String getCode() {
        return code;
    }

    public Localization getDescription() {
        return description;
    }

    public QueueOrder createOrder() {
        return factory.createOrder();
    }
}
