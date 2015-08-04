package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.Date;

/**
 * Created by maksat on 8/3/15.
 */
public class EntityStatus extends Persistable {
    private long batchId;
    private long entityId;
    private long statusId;
    private String description;
    private Date receiptDate;
    private Long index;

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
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

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
