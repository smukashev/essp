package kz.bsbnb.usci.eav.model.meta;

/**
 * Created by emles on 16.09.17
 */
public enum HistoryType {

    COMMON((byte) 1),
    ADVANCED((byte) 2);

    byte type = 1;

    HistoryType(byte type) {
        this.type = type;
    }

    public static Byte valueOf(HistoryType historyType) {
        return historyType.type;
    }

    public static HistoryType valueOf(byte type) {
        for (HistoryType historyType : HistoryType.values()) {
            if (historyType.type == type) return historyType;
        }
        return null;
    }

}



