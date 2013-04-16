package kz.bsbnb.usci.eav.model.json;

/**
 * @author k.tulbassiyev
 */
public class ContractStatusJModel {
    private Long index;
    private String protocol;
    private String description;

    public ContractStatusJModel(Long index, String protocol, String description) {
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

    @Override
    public String toString() {
        return "ContractStatusJModel{" +
                "index=" + index +
                ", protocol='" + protocol + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
