package kz.bsbnb.usci.receiver.model;

/**
 * @author k.tulbassiyev
 */
public class ContractStatusModel {
    private Long index;
    private String status;
    private String statusDescription;

    public ContractStatusModel(Long id, String status, String statusDescription) {
        this.index = id;
        this.status = status;
        this.statusDescription = statusDescription;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
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
