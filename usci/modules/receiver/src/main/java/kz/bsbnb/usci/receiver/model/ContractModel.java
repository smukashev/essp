package kz.bsbnb.usci.receiver.model;

/**
 * @author k.tulbassiyev
 */
public class ContractModel {
    private Long id;
    private String status;
    private String statusDescription;

    public ContractModel(Long id, String status) {
        this.id = id;
        this.status = status;
    }

    public ContractModel(Long id, String status, String statusDescription) {
        this.id = id;
        this.status = status;
        this.statusDescription = statusDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractModel)) return false;

        ContractModel that = (ContractModel) o;

        if (!id.equals(that.id)) return false;
        if (!status.equals(that.status)) return false;
        if (statusDescription != null ? !statusDescription.equals(that.statusDescription) :
                that.statusDescription != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + (statusDescription != null ? statusDescription.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ContractModel{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                '}';
    }
}
