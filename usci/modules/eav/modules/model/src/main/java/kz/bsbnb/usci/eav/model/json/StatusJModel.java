package kz.bsbnb.usci.eav.model.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class StatusJModel {
    private List<BatchStatusJModel> batchStatuses = new ArrayList<BatchStatusJModel>();
    private List<ContractStatusJModel> contractStatuses = new ArrayList<ContractStatusJModel>();

    public List<BatchStatusJModel> getBatchStatuses() {
        return batchStatuses;
    }

    public void setBatchStatuses(List<BatchStatusJModel> batchStatuses) {
        this.batchStatuses = batchStatuses;
    }

    public List<ContractStatusJModel> getContractStatuses() {
        return contractStatuses;
    }

    public void setContractStatuses(List<ContractStatusJModel> contractStatuses) {
        this.contractStatuses = contractStatuses;
    }

    @Override
    public String toString() {
        return "StatusJModel{" +
                "batchStatuses=" + batchStatuses +
                ", contractStatuses=" + contractStatuses +
                '}';
    }
}
