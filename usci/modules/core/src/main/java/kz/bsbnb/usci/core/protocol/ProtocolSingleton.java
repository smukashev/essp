package kz.bsbnb.usci.core.protocol;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.core.factory.ICouchbaseClientFactory;
import net.spy.memcached.internal.OperationFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author k.tulbassiyev
 */
@Component
public class ProtocolSingleton
{
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
}
