package kz.bsbnb.usci.cr.model;

import java.util.HashMap;

/**
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a> 
 */
@Deprecated
public enum ReportType implements SharedCode { // TODO remove this enum

    RECIPIENCY_IN_PROGRESS("IN_PROGRESS"),
    CROSS_CHECK_ERROR("CROSS_CHECK_ERROR"),
    CONTROL_WITH_ERRORS("WE"),
    CONTROL_WITHOUT_ERRORS("WOE"),
    RECIPIENCY_COMPLETED("COMPLETED"),
    ORGANIZATION_APPROVED("ORGANIZATION_APPROVED"),
    ORGANIZATION_APPROVING("ORGANIZATION_APPROVING");

    private String code;

    private ReportType(String code){
        this.code = code;
    }

    @Override
    public String getCode() {
        return this.code;
    }

}
