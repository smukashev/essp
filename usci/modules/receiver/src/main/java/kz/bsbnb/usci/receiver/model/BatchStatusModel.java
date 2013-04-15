package kz.bsbnb.usci.receiver.model;

/**
 * @author k.tulbassiyev
 */
public class BatchStatusModel {
    private String protocol;
    private String description;

    public BatchStatusModel(String protocol, String description) {
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
}
