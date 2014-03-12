package kz.bsbnb.usci.tool.couchbase.singleton;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.tool.couchbase.BatchStatuses;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.factory.ICouchbaseClientFactory;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class StatusSingleton {
    @Autowired(required = true)
    private ICouchbaseClientFactory clientFactory;

    CouchbaseClient client;

    private static Gson gson = new Gson();

    @PostConstruct
    public void init()
    {
        client = clientFactory.getCouchbaseClient();
    }

    public boolean isEntityCompleted(long batchId, long index) {
        Object contractStatus = client.get("contract_status:" + batchId + ":" + index);

        ContractStatusArrayJModel cStatuses;

        if (contractStatus != null) {
            cStatuses = gson.fromJson(contractStatus.toString(), ContractStatusArrayJModel.class);

            for (ContractStatusJModel status : cStatuses.getContractStatuses()) {
                if (status.getProtocol().equals(EntityStatuses.COMPLETED)) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized void startBatch(Long batchId, BatchFullJModel batchFullJModel, BatchInfo batchInfo) {
        BatchSign batchSign = new BatchSign();

        batchSign.setUserId(batchInfo.getUserId());
        batchSign.setFileName(batchInfo.getBatchName());
        batchSign.setBatchId(batchId);

        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            batchSign.setMd5(md5.digest(batchFullJModel.getContent()).toString());
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }


        OperationFuture<Boolean> result = client.set("batch:" + batchId, 0, gson.toJson(batchFullJModel));

        while(true) if(result.isDone()) break; // must be completed

        OperationFuture<Boolean> result1 = client.set("manifest:" + batchId, 0, gson.toJson(batchInfo));

        while(true) if(result1.isDone()) break; // must be completed

        OperationFuture<Boolean> result2 = client.set("sign:" + batchId, 0, gson.toJson(batchSign));

        while(true) if(result2.isDone()) break; // must be completed
    }

    public synchronized void addBatchStatus(Long batchId, BatchStatusJModel batchStatusJModel) {
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

    public synchronized void endBatch(Long batchId, Long userId) {
        addBatchStatus(batchId, new BatchStatusJModel(
                BatchStatuses.COMPLETED, null, new Date(), userId));
    }
}
