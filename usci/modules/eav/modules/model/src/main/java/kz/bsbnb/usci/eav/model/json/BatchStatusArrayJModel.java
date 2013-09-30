package kz.bsbnb.usci.eav.model.json;

import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
public class BatchStatusArrayJModel
{
    private String type = "batch_status";
    private List<BatchStatusJModel> batchStatuses = new ArrayList<BatchStatusJModel>();

    public List<BatchStatusJModel> getBatchStatuses() {
        return batchStatuses;
    }

    public void setBatchStatuses(List<BatchStatusJModel> batchStatuses) {
        this.batchStatuses = batchStatuses;
    }

    @Override
    public String toString() {
        return "ContractStatusArrayJModel{" +
                "batchStatuses=" + batchStatuses +
                '}';
    }
}
