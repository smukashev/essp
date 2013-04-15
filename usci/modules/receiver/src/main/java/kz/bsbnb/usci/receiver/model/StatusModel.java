package kz.bsbnb.usci.receiver.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class StatusModel {
    private List<BatchStatusModel> batchStatuses = new ArrayList<BatchStatusModel>();
    private List<ContractStatusModel> contractStatuses = new ArrayList<ContractStatusModel>();

    public List<BatchStatusModel> getBatchStatuses() {
        return batchStatuses;
    }

    public void setBatchStatuses(List<BatchStatusModel> batchStatuses) {
        this.batchStatuses = batchStatuses;
    }

    public List<ContractStatusModel> getContractStatuses() {
        return contractStatuses;
    }

    public void setContractStatuses(List<ContractStatusModel> contractStatuses) {
        this.contractStatuses = contractStatuses;
    }
}
