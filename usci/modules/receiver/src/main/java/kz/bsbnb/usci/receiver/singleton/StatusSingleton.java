package kz.bsbnb.usci.receiver.singleton;

import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import kz.bsbnb.usci.eav.model.json.StatusJModel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author k.tulbassiyev
 */
@Component
public class StatusSingleton {
    private Map<Long, StatusJModel> map =
            Collections.synchronizedMap(new HashMap<Long, StatusJModel>());

    public void startBatch(Long batchId) {
        map.put(batchId, new StatusJModel());
    }

    public void addBatchStatus(Long batchId, BatchStatusJModel batchStatusJModel) {
        map.get(batchId).getBatchStatuses().add(batchStatusJModel);
    }

    public void addContractStatus(Long batchId, ContractStatusJModel contractStatusJModel) {
        map.get(batchId).getContractStatuses().add(contractStatusJModel);
    }

    public StatusJModel endBatch(Long batchId) {
        return map.remove(batchId);
    }
}
