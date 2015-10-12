package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Report implements Serializable {

    public final static String INITIAL_REPORT_DATE_STR = "01/04/2013";

    public final static HashMap<Long, String> STATUS_NAME_MAP = new HashMap<>();
    public final static HashMap<Long, String> STATUS_CODE_MAP = new HashMap<>();

    static {
        STATUS_NAME_MAP.put(90l, "В процессе");
        STATUS_NAME_MAP.put(91l, "Ошибка межформенного контроля");
        STATUS_NAME_MAP.put(92l, "Завершен/Утвержден");
        STATUS_NAME_MAP.put(74l, "Отчитались не полностью");
        STATUS_NAME_MAP.put(75l, "Отчитались полностью");
        STATUS_NAME_MAP.put(76l, "Отконтроллирован с ошибками");
        STATUS_NAME_MAP.put(77l, "Отконтроллирован без ошибок");
        STATUS_NAME_MAP.put(128l, "Утвержден организацией");
        STATUS_NAME_MAP.put(127l, "Идет подтверждение");


        STATUS_CODE_MAP.put(90l, "RECIPIENCY_IN_PROGRESS");
        STATUS_CODE_MAP.put(91l, "CROSS_CHECK_ERROR");
        STATUS_CODE_MAP.put(92l, "RECIPIENCY_COMPLETED");
        STATUS_CODE_MAP.put(74l, "IR");
        STATUS_CODE_MAP.put(75l, "CR");
        STATUS_CODE_MAP.put(76l, "WE");
        STATUS_CODE_MAP.put(77l, "WOE");
        STATUS_CODE_MAP.put(128l, "ORGANIZATION_APPROVED");
        STATUS_CODE_MAP.put(127l, "ORGANIZATION_APPROVING");
    }

    private Long id;
    private Creditor creditor;
    private Long statusId;
    private Long totalCount;
    private Long actualCount;
    private Date reportDate;
    private Date beginningDate;
    private Date endDate;
    private Date lastManualEditDate;
    private transient Shared status;

    public Report() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getActualCount() {
        return actualCount;
    }

    public void setActualCount(Long actualCount) {
        this.actualCount = actualCount;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Date getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(Date beginningDate) {
        this.beginningDate = beginningDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getLastManualEditDate() {
        return lastManualEditDate;
    }

    public void setLastManualEditDate(Date lastManualEditDate) {
        this.lastManualEditDate = lastManualEditDate;
    }

    public Shared getStatus() {
        if (statusId != null && (status == null || status.getId() != statusId)) {
            status = new Shared();
            status.setId(statusId);
            status.setCode(STATUS_CODE_MAP.get(statusId));
            status.setNameRu(STATUS_NAME_MAP.get(statusId));
            status.setNameKz(STATUS_NAME_MAP.get(statusId));
        }
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

        StringBuilder sb = new StringBuilder();

        sb.append("{id=");
        sb.append(getId());

        if (getCreditor() != null) {
            sb.append(", creditor={id=");
            sb.append(getCreditor().getId());
            sb.append("}");
        } else {
            sb.append(", creditor=null");
        }

        sb.append(", actualCount=");
        sb.append(getActualCount());
        sb.append(", totalCount=");
        sb.append(getTotalCount());

        if (getBeginningDate() != null) {
            sb.append(", beginingDate=");
            sb.append(dateFormat.format(getBeginningDate()));
        }

        if (getEndDate() != null) {
            sb.append(", endDate=");
            sb.append(dateFormat.format(getEndDate()));
        }
        
        if (getStatusId() != null) {
            sb.append(", status={id=");
            sb.append(getStatusId());
            sb.append("}");
        } else {
            sb.append(", status=null");
        }

        sb.append("}");

        return sb.toString();
    }
}
