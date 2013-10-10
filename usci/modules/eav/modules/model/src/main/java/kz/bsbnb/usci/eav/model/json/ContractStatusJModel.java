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
    private String contractNo;
    private Date contractDate;

    public ContractStatusJModel(Long index, String protocol, String description, Date received,
                                String contractNo, Date contractDate)
    {
        this.index = index;
        this.protocol = protocol;
        this.description = description;
        this.received = received;
        this.contractNo = contractNo;
        this.contractDate = contractDate;
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

    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String contractNo)
    {
        this.contractNo = contractNo;
    }

    public Date getContractDate()
    {
        return contractDate;
    }

    public void setContractDate(Date contractDate)
    {
        this.contractDate = contractDate;
    }
}
