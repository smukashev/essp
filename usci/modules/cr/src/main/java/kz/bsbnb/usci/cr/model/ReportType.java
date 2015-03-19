package kz.bsbnb.usci.cr.model;

/**
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a> 
 */
public enum ReportType implements SharedCode {

    RECIPIENCY_IN_PROGRESS("RECIPIENCY_IN_PROGRESS"),
    CROSS_CHECK_ERROR("CROSS_CHECK_ERROR"),
    CONTROL_WITH_ERRORS("WE"),
    CONTROL_WITHOUT_ERRORS("WOE"),
    RECIPIENCY_COMPLETED("RECIPIENCY_COMPLETED"),
    ORGANIZATION_APPROVED("ORGANIZATION_APPROVED");
    
    private String code;

    private ReportType(String code){
        this.code = code;
    }
    
    @Override
    public String getCode() {
        return this.code;
    }
    
}
