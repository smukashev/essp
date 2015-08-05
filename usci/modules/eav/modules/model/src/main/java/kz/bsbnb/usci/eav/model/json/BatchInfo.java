package kz.bsbnb.usci.eav.model.json;

import java.util.Date;
import java.util.HashMap;

/**
 * @author abukabayev
 */
public class BatchInfo {
    private String batchType;
    private String batchName;
    private Long size;
    private Long userId;
    private Date repDate;
    private HashMap<String, String> additionalParams = new HashMap<String, String>();

    private long reportId;
    private long totalCount;
    private long actualCount;

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

    public HashMap<String, String> getAdditionalParams()
    {
        return additionalParams;
    }

    public void setAdditionalParams(HashMap<String, String> additionalParams)
    {
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

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public long getActualCount() {
        return actualCount;
    }

    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }
}
