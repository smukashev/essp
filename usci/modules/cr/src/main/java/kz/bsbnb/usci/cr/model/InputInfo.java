package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class InputInfo implements Serializable {
    private static final long serialVersionUID = 2115412865112267120L;

    private BigInteger id;

    private Long userId;

    private Creditor creditor;

    private String fileName;

    private Date receiverDate;

    private Shared receiverType;

    private Date startedDate;

    private Date completionDate;

    private Date reportDate;

    private Shared status;

    private long total;

	private int actualCount;

    private int processedCount;

    private long totalInserted;

    private long totalUpdated;

    private long totalInsertFailed;

    private long totalUpdateFailed;

    private long totalInsertLost;

    private long totalUpdateLost;

    private List<Protocol> batchStatuses;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getReceiverDate() {
        return receiverDate;
    }

    public void setReceiverDate(Date receiverDate) {
        this.receiverDate = receiverDate;
    }

    public Shared getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(Shared receiverType) {
        this.receiverType = receiverType;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Shared getStatus() {
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalInserted() {
        return totalInserted;
    }

    public void setTotalInserted(long totalInserted) {
        this.totalInserted = totalInserted;
    }

    public long getTotalUpdated() {
        return totalUpdated;
    }

    public void setTotalUpdated(long totalUpdated) {
        this.totalUpdated = totalUpdated;
    }

    public long getTotalInsertFailed() {
        return totalInsertFailed;
    }

    public void setTotalInsertFailed(long totalInsertFailed) {
        this.totalInsertFailed = totalInsertFailed;
    }

    public long getTotalUpdateFailed() {
        return totalUpdateFailed;
    }

    public void setTotalUpdateFailed(long totalUpdateFailed) {
        this.totalUpdateFailed = totalUpdateFailed;
    }

    public long getTotalInsertLost() {
        return totalInsertLost;
    }

    public void setTotalInsertLost(long totalInsertLost) {
        this.totalInsertLost = totalInsertLost;
    }

    public long getTotalUpdateLost() {
        return totalUpdateLost;
    }

    public void setTotalUpdateLost(long totalUpdateLost) {
        this.totalUpdateLost = totalUpdateLost;
    }

    public List<Protocol> getBatchStatuses() {
        return batchStatuses;
    }

	public int getActualCount() {
		return actualCount;
	}

	public void setActualCount(long actualCount) {
		if (actualCount > Integer.MAX_VALUE)
				this.actualCount = Integer.MAX_VALUE;
		else this.actualCount = (int) actualCount;
	}

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public void setBatchStatuses(List<Protocol> batchStatuses) {
        this.batchStatuses = batchStatuses;
    }
}