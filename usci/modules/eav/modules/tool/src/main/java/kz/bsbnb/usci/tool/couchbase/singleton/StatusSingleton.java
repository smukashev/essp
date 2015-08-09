package kz.bsbnb.usci.tool.couchbase.singleton;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.exceptions.BatchNotFoundException;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.tool.couchbase.BatchStatuses;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.factory.ICouchbaseClientFactory;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Deprecated
//@Component
public class StatusSingleton {
    @Autowired(required = true)
    private ICouchbaseClientFactory clientFactory;

    @Autowired
    private CouchbaseClientManager couchbaseClientManager;

    CouchbaseClient client;

    private static final boolean readOnly = false;

    private static Gson gson = new Gson();

    @PostConstruct
    public void init()
    {
        client = couchbaseClientManager.get();
    }

    @Deprecated
    public boolean isEntityCompleted(long batchId, long index) {
        Object contractStatus = client.get("entity_status:" + batchId + ":" + index);

        EntityStatusArrayJModel cStatuses;

        if (contractStatus != null) {
            cStatuses = gson.fromJson(contractStatus.toString(), EntityStatusArrayJModel.class);

            for (EntityStatusJModel status : cStatuses.getEntityStatuses()) {
                if (status.getProtocol().equals(EntityStatuses.COMPLETED)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Deprecated
    public synchronized void startBatch(Long batchId, BatchFullJModel batchFullJModel, BatchInfo batchInfo) {
        if (readOnly)
            return;

        BatchSign batchSign = new BatchSign();

        batchSign.setUserId(batchInfo.getUserId());
        batchSign.setFileName(batchFullJModel.getFileName());
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

        createBatchStatus(batchId, batchFullJModel.getFileName());
    }

    @Deprecated
    public synchronized void createBatchStatus(Long batchId, String fileName) {
        if (readOnly)
            return;

        Object batchStatus = client.get("batch_status:" + batchId);

        BatchStatusArrayJModel bStatuses;

        if (batchStatus == null) {
            bStatuses = new BatchStatusArrayJModel();
        } else {
            bStatuses = gson.fromJson(batchStatus.toString(), BatchStatusArrayJModel.class);//(BatchStatusArrayJModel)batchStatus;
        }

        bStatuses.setFileName(fileName);

        client.set("batch_status:" + batchId, 0, gson.toJson(bStatuses));
    }

    @Deprecated
    public synchronized void addBatchStatus(Long batchId, BatchStatusJModel batchStatusJModel) {
        if (readOnly)
            return;

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

    @Deprecated
    public synchronized BatchStatusArrayJModel getBatchStatus(Long batchId) {
        Object batchStatus = client.get("batch_status:" + batchId);

        BatchStatusArrayJModel bStatuses;

        if (batchStatus == null) {
            bStatuses = new BatchStatusArrayJModel();
        } else {
            bStatuses = gson.fromJson(batchStatus.toString(), BatchStatusArrayJModel.class);
        }

        return bStatuses;
    }

    @Deprecated
    public synchronized void addContractStatus(Long batchId, EntityStatusJModel contractStatusJModel) {
        if (readOnly)
            return;

        int i = 0;

        for(i = 0; i < 10; i++) {
            try {
                Object contractStatus = client.get("entity_status:" + batchId + ":" + contractStatusJModel.getIndex());

                EntityStatusArrayJModel cStatuses;

                if (contractStatus == null) {
                    cStatuses = new EntityStatusArrayJModel(batchId, contractStatusJModel.getIndex());
                } else {
                    cStatuses = gson.fromJson(contractStatus.toString(), EntityStatusArrayJModel.class);//(BatchStatusArrayJModel)batchStatus;
                }

                cStatuses.getEntityStatuses().add(contractStatusJModel);

                client.set("entity_status:" + batchId + ":" + contractStatusJModel.getIndex(), 0, gson.toJson(cStatuses));

                break;
            } catch (OperationTimeoutException ex) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (i >= 10) {
            throw new IllegalStateException("Couchbase is unreachable.");
        }
    }

    @Deprecated
    public synchronized void endBatch(Long batchId, Long userId) {
        if (readOnly)
            return;

        addBatchStatus(batchId, new BatchStatusJModel(
                BatchStatuses.COMPLETED, null, new Date(), userId));
    }

    @Deprecated
    public BatchFullJModel getBatch(long batchId) throws BatchNotFoundException {
        Object batch = client.get("batch:" + batchId);
        if(batch == null)
            throw new BatchNotFoundException("batchId:" + batchId);
        BatchFullJModel ret = gson.fromJson(batch.toString(), BatchFullJModel.class);
        return ret;
    }
}
