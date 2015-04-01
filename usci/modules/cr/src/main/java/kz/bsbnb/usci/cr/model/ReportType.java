package kz.bsbnb.usci.cr.model;

import java.util.HashMap;

/**
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a> 
 */
public enum ReportType implements SharedCode {

    RECIPIENCY_IN_PROGRESS("RECIPIENCY_IN_PROGRESS"),
    CROSS_CHECK_ERROR("CROSS_CHECK_ERROR"),
    CONTROL_WITH_ERRORS("WE"),
    CONTROL_WITHOUT_ERRORS("WOE"),
    RECIPIENCY_COMPLETED("RECIPIENCY_COMPLETED"),
    ORGANIZATION_APPROVED("ORGANIZATION_APPROVED"),
    ORGANIZATION_APPROVING("ORGANIZATION_APPROVING");

    public final static HashMap<String, Long> STATUS_CODE_ID_MAP = new HashMap<String, Long>();

    static {
        STATUS_CODE_ID_MAP.put("RECIPIENCY_IN_PROGRESS", 90l);
        STATUS_CODE_ID_MAP.put("CROSS_CHECK_ERROR", 91l);
        STATUS_CODE_ID_MAP.put("RECIPIENCY_COMPLETED",92l);
        STATUS_CODE_ID_MAP.put("IR",74l);
        STATUS_CODE_ID_MAP.put("CR",75l);
        STATUS_CODE_ID_MAP.put("WE",76l);
        STATUS_CODE_ID_MAP.put("WOE", 77l);
        STATUS_CODE_ID_MAP.put("ORGANIZATION_APPROVED",128l);
        STATUS_CODE_ID_MAP.put("ORGANIZATION_APPROVING",127l);
    }
    
    private String code;

    private ReportType(String code){
        this.code = code;
    }
    
    @Override
    public String getCode() {
        return this.code;
    }
    
}
