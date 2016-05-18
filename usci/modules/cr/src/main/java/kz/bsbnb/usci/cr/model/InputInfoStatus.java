package kz.bsbnb.usci.cr.model;

public enum InputInfoStatus implements SharedCode {
    BATCH_PROCESSING_IN_PROGRESS("BATCH_PROCESSING_IN_PROGRESS"),
    BATCH_PROCESSING_COMPLETED("BATCH_PROCESSING_COMPLETED"),
    IN_QUEUE("IN_QUEUE"),
    WAITING_FOR_SIGNATURE("WAITING_FOR_SIGNATURE"),
    REJECTED("REJECTED"),
    MAINTENANCE_REQUEST("MAINTENANCE_REQUEST");

    private final String code;

    private InputInfoStatus(String code){
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}