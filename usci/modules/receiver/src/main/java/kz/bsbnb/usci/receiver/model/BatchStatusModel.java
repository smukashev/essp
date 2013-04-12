package kz.bsbnb.usci.receiver.model;

/**
 * @author k.tulbassiyev
 */
public class BatchStatusModel {
    private String status;
    private String statusDescription;

    public BatchStatusModel(String status, String statusDescription) {
        this.status = status;
        this.statusDescription = statusDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
