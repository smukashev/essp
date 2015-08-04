package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.Date;

/**
 * Created by maksat on 8/3/15.
 */
public class BatchStatus extends Persistable {

    private long batchId;
    private long statusId;
    private String description;
    private Date receiptDate;

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public long getStatusId() {
        return statusId;
    }

    public void setStatusId(long statusId) {
        this.statusId = statusId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }
}
