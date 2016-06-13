package kz.bsbnb.usci.receiver.reader.impl.beans;

import java.util.Date;
import java.util.HashMap;

public class ManifestData {

    private String type;
    private Long userId;
    private Integer size;
    private Date reportDate;
    private HashMap<String, String> additionalParams = new HashMap<>();
    private boolean maintenance;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public HashMap<String, String> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(HashMap<String, String> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public boolean isMaintenance() {
        return maintenance;
    }
}
