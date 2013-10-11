package kz.bsbnb.usci.receiver.singleton;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.receiver.factory.ICouchbaseClientFactory;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Component
public class StatusSingleton {
//    private Map<Long, ContractStatusArrayJModel> map =
//            Collections.synchronizedMap(new HashMap<Long, ContractStatusArrayJModel>());

    @Autowired(required = true)
    private ICouchbaseClientFactory clientFactory;

    CouchbaseClient client;

    private static Gson gson = new Gson();

    @PostConstruct
    public void init()
    {
        client = clientFactory.getCouchbaseClient();
    }

    public synchronized void startBatch(Long batchId, BatchFullJModel batchFullJModel, BatchInfo batchInfo) {
        //map.put(batchId, new ContractStatusArrayJModel());

        OperationFuture<Boolean> result = client.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        while(true) if(result.isDone()) break; // must be completed

        OperationFuture<Boolean> result1 = client.set("manifest:" + batchId, 0, gson.toJson(batchInfo));

        while(true) if(result1.isDone()) break; // must be completed
    }

    public synchronized void addBatchStatus(Long batchId, BatchStatusJModel batchStatusJModel) {
        //map.get(batchId).getBatchStatuses().add(batchStatusJModel);
        Object batchStatus = client.get("batch_status:" + batchId);

        BatchStatusArrayJModel bStatuses;

        if (batchStatus == null) {
            bStatuses = new BatchStatusArrayJModel();
        } else {
            bStatuses = gson.fromJson(batchStatus.toString(), BatchStatusArrayJModel.class);//(BatchStatusArrayJModel)batchStatus;
        }

        bStatuses.getBatchStatuses().add(batchStatusJModel);

        client.set("batch_status:" + batchId, 0, gson.toJson(bStatuses));
    }

    public synchronized void addContractStatus(Long batchId, ContractStatusJModel contractStatusJModel) {
        //map.get(batchId).getContractStatuses().add(contractStatusJModel);

        Object contractStatus = client.get("contract_status:" + batchId + ":" + contractStatusJModel.getIndex());

        ContractStatusArrayJModel cStatuses;

        if (contractStatus == null) {
            cStatuses = new ContractStatusArrayJModel(batchId, contractStatusJModel.getIndex());
        } else {
            cStatuses = gson.fromJson(contractStatus.toString(), ContractStatusArrayJModel.class);//(BatchStatusArrayJModel)batchStatus;
        }

        cStatuses.getContractStatuses().add(contractStatusJModel);

        client.set("contract_status:" + batchId + ":" + contractStatusJModel.getIndex(), 0, gson.toJson(cStatuses));
    }

    public synchronized void endBatch(Long batchId) {
        //return map.remove(batchId);


    }
}
