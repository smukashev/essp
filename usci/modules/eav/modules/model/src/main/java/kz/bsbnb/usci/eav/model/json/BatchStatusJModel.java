package kz.bsbnb.usci.eav.model.json;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class BatchStatusJModel {
    private String protocol;
    private String description;
    private Date received;

    public BatchStatusJModel(String protocol, String description, Date received) {
        this.protocol = protocol;
        this.description = description;
        this.received = received;
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

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
