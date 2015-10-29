package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.EntityStatuses;

import java.util.Date;

public class EntityStatus extends Persistable {
    private long batchId;
    private long entityId;
    private long statusId;
    private String description;
    private Date receiptDate;
    private Long index;

    private EntityStatuses status;

    public long getBatchId() {
        return batchId;
    }

    public EntityStatus setBatchId(long batchId) {
        this.batchId = batchId;
        return this;
    }

    public long getEntityId() {
        return entityId;
    }

    public EntityStatus setEntityId(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public long getStatusId() {
        return statusId;
    }

    public EntityStatus setStatusId(long statusId) {
        this.statusId = statusId;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public EntityStatus setDescription(String description) {
        if (description != null) {
            if (description.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 512) {
                this.description = description;
            } else {
                this.description = description.substring(0, 256);
            }
        }
        return this;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public EntityStatus setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
        return this;
    }

    public Long getIndex() {
        return index;
    }

    public EntityStatus setIndex(Long index) {
        this.index = index;
        return this;
    }

    public EntityStatuses getStatus() {
        return status;
    }

    public EntityStatus setStatus(EntityStatuses status) {
        this.status = status;
        return this;
    }
}
