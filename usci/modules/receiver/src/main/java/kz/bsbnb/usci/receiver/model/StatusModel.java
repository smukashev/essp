package kz.bsbnb.usci.receiver.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class StatusModel {
    private Long batchId;
    private List<BatchStatusModel> batchStatusModelList = new ArrayList<BatchStatusModel>();
    private List<ContractStatusModel> contractStatusModelList = new ArrayList<ContractStatusModel>();

    public StatusModel(Long batchId) {
        this.batchId = batchId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public List<BatchStatusModel> getBatchStatusModelList() {
        return batchStatusModelList;
    }

    public void setBatchStatusModelList(List<BatchStatusModel> batchStatusModelList) {
        this.batchStatusModelList = batchStatusModelList;
    }

    public List<ContractStatusModel> getContractStatusModelList() {
        return contractStatusModelList;
    }

    public void setContractStatusModelList(List<ContractStatusModel> contractStatusModelList) {
        this.contractStatusModelList = contractStatusModelList;
    }
}
