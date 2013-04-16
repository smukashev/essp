package kz.bsbnb.usci.eav.model.json;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class ContractStatusJModel {
    private Long index;
    private String protocol;
    private String description;
    private Date received;

    public ContractStatusJModel(Long index, String protocol, String description, Date received) {
        this.index = index;
        this.protocol = protocol;
        this.description = description;
        this.received = received;
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

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
