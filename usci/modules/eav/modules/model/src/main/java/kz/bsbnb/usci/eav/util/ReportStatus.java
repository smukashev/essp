package kz.bsbnb.usci.eav.util;

/**
 * Created by n.seitkozhayev on 3/3/15.
 */
public enum ReportStatus implements IGlobal {

    IN_PROGRESS,
    WE,
    WOE,
    CROSS_CHECK_ERROR,
    COMPLETED,
    ORGANIZATION_APPROVED,
    ORGANIZATION_APPROVING;

    @Override
    public String type() {
        return "REPORT_STATUS";
    }

    @Override
    public String code() {
        return name();
    }

}
