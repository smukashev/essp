package kz.bsbnb.usci.cr.model;

/**
 *
 * @author alexandr.motov
 */
public enum MessageType {
    
    CRITICAL_ERROR("CRITICAL_ERROR"),
    NON_CRITICAL_ERROR("NON_CRITICAL_ERROR"),
    SUCCESS("SUCCESS"),
    INFO("INFO");
    
    private String code;

    private MessageType(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
}
