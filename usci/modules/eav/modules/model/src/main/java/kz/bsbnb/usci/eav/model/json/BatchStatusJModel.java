package kz.bsbnb.usci.eav.model.json;

/**
 * @author k.tulbassiyev
 */
public class BatchStatusJModel {
    private String protocol;
    private String description;

    public BatchStatusJModel(String protocol, String description) {
        this.protocol = protocol;
        this.description = description;
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
        return "BatchStatusJModel{" +
                "protocol='" + protocol + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
