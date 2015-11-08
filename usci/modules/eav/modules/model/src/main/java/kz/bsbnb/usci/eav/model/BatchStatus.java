package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.BatchStatuses;

import java.util.Date;

public class BatchStatus extends Persistable {
    private static final long serialVersionUID = 1L;
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
        if (description != null) {
            if (description.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 512) {
                this.description = description;
            } else {
                this.description = description.substring(0, 256);
            }
        } else {
            this.description = null;
        }

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

    public String getStatusDescription() {
        if (status == BatchStatuses.COMPLETED) {
            return "Завершён";
        } else if (status == BatchStatuses.ERROR) {
            return "Ошибка";
        } else if (status == BatchStatuses.PROCESSING) {
            return "В обработке";
        } else if (status == BatchStatuses.WAITING) {
            return "В ожиданий";
        } else {
            return "Неизвестный тип";
        }
    }

    public BatchStatus setStatus(BatchStatuses status) {
        this.status = status;
        return this;
    }
}
