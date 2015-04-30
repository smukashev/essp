package kz.bsbnb.usci.cr.model;

/**
 * Created by n.seitkozhayev on 3/3/15.
 */
public enum ReportStatus {

    IN_PROGRESS(90l),
    ORGANIZATION_APPROVED(128l),
    COMPLETED(92l);

    private Long statusId;

    private ReportStatus(Long statusId) {
        this.statusId = statusId;
    }

    public Long getStatusId() {
        return statusId;
    }
}
