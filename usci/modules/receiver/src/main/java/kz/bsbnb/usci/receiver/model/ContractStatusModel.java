package kz.bsbnb.usci.receiver.model;

/**
 * @author k.tulbassiyev
 */
public class ContractStatusModel {
    private Long index;
    private String protocol;
    private String description;

    public ContractStatusModel(Long index, String protocol, String description) {
        this.index = index;
        this.protocol = protocol;
        this.description = description;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
