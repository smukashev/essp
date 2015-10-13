package kz.bsbnb.usci.cr.model;

//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * TODO javadoc 
 * @author &lt;a href="mailto:dmitriy.zakomirnyy@bsbnb.kz"&gt;Dmitriy Zakomirnyy&lt;/a&gt;
 */

public class InputInfo implements Serializable {
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

    /**
     * @return the total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * @return the totalInserted
     */
    public long getTotalInserted() {
        return totalInserted;
    }

    /**
     * @param totalInserted the totalInserted to set
     */
    public void setTotalInserted(long totalInserted) {
        this.totalInserted = totalInserted;
    }

    /**
     * @return the totalUpdated
     */
    public long getTotalUpdated() {
        return totalUpdated;
    }

    /**
     * @param totalUpdated the totalUpdated to set
     */
    public void setTotalUpdated(long totalUpdated) {
        this.totalUpdated = totalUpdated;
    }

    /**
     * @return the totalInsertFailed
     */
    public long getTotalInsertFailed() {
        return totalInsertFailed;
    }

    /**
     * @param totalInsertFailed the totalInsertFailed to set
     */
    public void setTotalInsertFailed(long totalInsertFailed) {
        this.totalInsertFailed = totalInsertFailed;
    }

    /**
     * @return the totalUpdateFailed
     */
    public long getTotalUpdateFailed() {
        return totalUpdateFailed;
    }

    /**
     * @param totalUpdateFailed the totalUpdateFailed to set
     */
    public void setTotalUpdateFailed(long totalUpdateFailed) {
        this.totalUpdateFailed = totalUpdateFailed;
    }

    /**
     * @return the totalInsertLost
     */
    public long getTotalInsertLost() {
        return totalInsertLost;
    }

    /**
     * @param totalInsertLost the totalInsertLost to set
     */
    public void setTotalInsertLost(long totalInsertLost) {
        this.totalInsertLost = totalInsertLost;
    }

    /**
     * @return the totalUpdateLost
     */
    public long getTotalUpdateLost() {
        return totalUpdateLost;
    }

    /**
     * @param totalUpdateLost the totalUpdateLost to set
     */
    public void setTotalUpdateLost(long totalUpdateLost) {
        this.totalUpdateLost = totalUpdateLost;
    }

	public List<Protocol> getBatchStatuses() {
		return batchStatuses;
	}

	public void setBatchStatuses(List<Protocol> batchStatuses) {
		this.batchStatuses = batchStatuses;
	}

	public int getActualCount() {
		return actualCount;
	}

	public void setActualCount(long actualCount) {
		if (actualCount > Integer.MAX_VALUE)
				this.actualCount = Integer.MAX_VALUE;
		else this.actualCount = (int) actualCount;
	}
}