package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.EntityStatuses;

import java.util.Date;

public class EntityStatus extends Persistable {
    private long batchId;
    private long entityId;
    private long statusId;
    private String operation;
    private String description;
    private String errorCode;
    private String devDescription;
    private Date receiptDate;
    private Long index;
    private String contractNumber;

    private EntityStatuses status;

    public long getBatchId() {
        return batchId;
    }

    public EntityStatus setBatchId(long batchId) {
        this.batchId = batchId;
        return this;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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
        } else {
            this.description = null;
        }
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public EntityStatus setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getDevDescription() {
        return devDescription;
    }

    public EntityStatus setDevDescription(String devDescription) {
        this.devDescription = devDescription;
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

    public String getStatusDescription() {
        if (status == EntityStatuses.COMPLETED) {
            return "Завершён";
        } else if (status == EntityStatuses.CHECK_IN_CORE) {
            return "Прверка";
        } else if (status == EntityStatuses.CHECK_IN_PARSER) {
            return "Провекра";
        } else if (status == EntityStatuses.ERROR) {
            return "Ошибка";
        } else if (status == EntityStatuses.PARSING) {
            return "Парсинг";
        } else if (status == EntityStatuses.PROCESSING) {
            return "В обработке";
        } else if (status == EntityStatuses.TOTAL_COUNT) {
            return "";
        } else if (status == EntityStatuses.ACTUAL_COUNT) {
            return "";
        } else {
            return "Неизвестный тип";
        }
    }

    public EntityStatus setStatus(EntityStatuses status) {
        this.status = status;
        return this;
    }
}
