package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.BatchStatuses;

import java.util.Date;

/**
 * Created by maksat on 8/3/15.
 */
public class BatchStatus extends Persistable {

    private long batchId;
    private long statusId;
    private String description;
    private Date receiptDate;

    private BatchStatuses status;

    public long getBatchId() {
        return batchId;
    }

    public BatchStatus setBatchId(long batchId) {
        this.batchId = batchId;
        return this;
    }

    public long getStatusId() {
        return statusId;
    }

    public BatchStatus setStatusId(long statusId) {
        this.statusId = statusId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BatchStatus setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public BatchStatus setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
        return this;
    }

    public BatchStatuses getStatus() {
        return status;
    }

    public BatchStatus setStatus(BatchStatuses status) {
        this.status = status;
        return this;
    }
}
