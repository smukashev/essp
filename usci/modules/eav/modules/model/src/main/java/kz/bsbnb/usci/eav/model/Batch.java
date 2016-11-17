package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.DataUtils;

import java.util.Date;

public class Batch extends Persistable {

    private static final long serialVersionUID = 1L;

    private Date receiptDate;
    private Date repDate;
    private Long userId;
    private String fileName;
    private byte[] content;
    private String hash;
    private String sign;
    private String batchType;
    private Long totalCount;
    private Long actualCount;
    private Long reportId;
    private Long creditorId;
    private Long statusId;
    private boolean maintenance;
    private Boolean maintenanceApproved;
    private Boolean maintenanceDeclined;
    private Creditor creditor;

    public Batch() {
        super();
    }

    public Batch(Date reportDate, Long userId) {
        super();

        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = new Date();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
        this.userId = userId;
    }

    public Batch(Date reportDate) {
        super();

        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = new Date();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
    }

    public Batch(Date receiptDate, Date reportDate) {
        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = (Date) receiptDate.clone();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        if (receiptDate == null) {
            this.receiptDate = null;
            return;
        }

        Date newReceiptDate = (Date) receiptDate.clone();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.receiptDate = newReceiptDate;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Batch)) return false;
        if (!super.equals(o)) return false;

        Batch batch = (Batch) o;

        return receiptDate.equals(batch.receiptDate);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + receiptDate.hashCode();
        return result;
    }

    public Date getRepDate() {
        return repDate;
    }

    public void setRepDate(Date reportDate) {
        if (reportDate == null) {
            this.repDate = null;
            return;
        }

        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.repDate = newReportDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFormattedFileName(){
        if(fileName == null)
            return "без имени";

        if(fileName.contains("\\"))
            return fileName.substring(fileName.lastIndexOf('\\') + 1);

        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
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

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(Long creditorId) {
        this.creditorId = creditorId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", repDate=" + repDate +
                ", creditorId=" + creditorId + '}';
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenanceApproved(Boolean maintenanceApproved) {
        this.maintenanceApproved = maintenanceApproved;
    }

    public Boolean isMaintenanceApproved() {
        return maintenanceApproved;
    }

    public void setMaintenanceDeclined(Boolean maintenanceDeclined) {
        this.maintenanceDeclined = maintenanceDeclined;
    }

    public Boolean isMaintenanceDeclined() {
        return maintenanceDeclined;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }
}
