package kz.bsbnb.usci.eav.model.json;

import kz.bsbnb.usci.eav.model.Batch;

import java.util.Date;
import java.util.HashMap;

public class BatchInfo {
    private String batchType;
    private String batchName;
    private Long size;
    private Long userId;
    private Date repDate;
    private HashMap<String, String> additionalParams = new HashMap<>();
    private Date receiptDate;
    private int contentSize;
    private Long creditorId;

    private long reportId;
    private long totalCount;
    private long actualCount;

    public BatchInfo() {
        super();
    }

    public BatchInfo(Batch batch) {
        setReportId(batch.getReportId());
        setRepDate(batch.getRepDate());
        setBatchType(batch.getBatchType());
        setSize(batch.getTotalCount());
        setTotalCount(batch.getTotalCount() != null ? batch.getTotalCount() : 0);
        setActualCount(batch.getActualCount() != null ? batch.getActualCount() : 0);
        setUserId(batch.getUserId());
        setCreditorId(batch.getCreditorId());
        setContentSize(batch.getContent().length);
        setReceiptDate(batch.getReceiptDate());
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchType() {
        return batchType;
    }

    public void setBatchType(String batchType) {
        this.batchType = batchType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getRepDate() {
        return repDate;
    }

    public void setRepDate(Date repDate) {
        this.repDate = repDate;
    }

    public HashMap<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(HashMap<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void addParam(String name, String value) {
        this.additionalParams.put(name, value);
    }


    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getActualCount() {
        return actualCount;
    }

    public void setActualCount(long actualCount) {
        this.actualCount = actualCount;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    public Long getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(Long creditorId) {
        this.creditorId = creditorId;
    }
}
