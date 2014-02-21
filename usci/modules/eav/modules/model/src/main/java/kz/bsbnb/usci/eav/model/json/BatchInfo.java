package kz.bsbnb.usci.eav.model.json; /**
 * @author abukabayev
 */
import java.util.Date;
import java.util.HashMap;

public class BatchInfo {
    private String batchType;
    private String batchName;
    private Long size;
    private Long userId;
    private Date repDate;
    private HashMap<String, String> additionalParams;

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
}
