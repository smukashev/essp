package kz.bsbnb.usci.receiver.singleton;

import kz.bsbnb.usci.receiver.model.BatchStatusModel;
import kz.bsbnb.usci.receiver.model.ContractStatusModel;
import kz.bsbnb.usci.receiver.model.StatusModel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author k.tulbassiyev
 */
@Component
public class StatusSingleton {
    private Map<Long, StatusModel> map =
            Collections.synchronizedMap(new HashMap<Long, StatusModel>());

    public void startBatch(Long batchId) {
        map.put(batchId, new StatusModel());
    }

    public void addBatchStatus(Long batchId, BatchStatusModel batchStatusModel) {
        map.get(batchId).getBatchStatuses().add(batchStatusModel);
    }

    public void addContractStatus(Long batchId, ContractStatusModel contractStatusModel) {
        map.get(batchId).getContractStatuses().add(contractStatusModel);
    }

    public StatusModel endBatch(Long batchId) {
        return map.remove(batchId);
    }
}
