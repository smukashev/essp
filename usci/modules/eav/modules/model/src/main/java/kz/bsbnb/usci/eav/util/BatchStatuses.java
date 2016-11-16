package kz.bsbnb.usci.eav.util;

public enum BatchStatuses implements IGlobal {
    WAITING,
    PROCESSING,
    ERROR,
    WAITING_FOR_SIGNATURE,
    COMPLETED,
    CANCELLED,
    MAINTENANCE_REQUEST,
    MAINTENANCE_DECLINED;


    @Override
    public String type() {
        return "BATCH_STATUS";
    }

    @Override
    public String code() {
        return name();
    }
}
