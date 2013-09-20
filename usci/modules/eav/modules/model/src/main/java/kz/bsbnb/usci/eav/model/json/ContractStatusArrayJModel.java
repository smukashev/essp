package kz.bsbnb.usci.eav.model.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class ContractStatusArrayJModel
{
    private String type = "contract_status";

    private Long batchId;
    private Long index;

    private List<ContractStatusJModel> contractStatuses = new ArrayList<ContractStatusJModel>();

    public ContractStatusArrayJModel(Long batchId, Long index)
    {
        this.batchId = batchId;
        this.index = index;
    }

    public List<ContractStatusJModel> getContractStatuses() {
        return contractStatuses;
    }

    public void setContractStatuses(List<ContractStatusJModel> contractStatuses) {
        this.contractStatuses = contractStatuses;
    }

    @Override
    public String toString() {
        return "ContractStatusArrayJModel{" +
                "contractStatuses=" + contractStatuses +
                '}';
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getIndex()
    {
        return index;
    }

    public void setIndex(Long index)
    {
        this.index = index;
    }
}
