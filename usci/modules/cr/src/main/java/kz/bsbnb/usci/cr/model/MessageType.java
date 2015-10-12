package kz.bsbnb.usci.cr.model;

public enum MessageType {
    
    CRITICAL_ERROR("CRITICAL_ERROR"),
    NON_CRITICAL_ERROR("NON_CRITICAL_ERROR"),
    SUCCESS("SUCCESS"),
    INFO("INFO");
    
    private String code;

    MessageType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
 }
