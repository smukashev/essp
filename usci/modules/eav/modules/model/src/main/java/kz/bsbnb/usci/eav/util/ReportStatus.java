package kz.bsbnb.usci.eav.util;

/**
 * Created by n.seitkozhayev on 3/3/15.
 */
public enum ReportStatus implements IGlobal {

    IN_PROGRESS(90L),
    WE(76L),
    WOE(77L),
    ORGANIZATION_APPROVED(128L),
    COMPLETED(92L);

    private Long statusId;

    private ReportStatus(Long statusId) {
        this.statusId = statusId;
    }

    public Long getStatusId() {
        return statusId;
    }

    @Override
    public String type() {
        return "REPORT_STATUS";
    }

    @Override
    public String code() {
        return name();
    }

}
