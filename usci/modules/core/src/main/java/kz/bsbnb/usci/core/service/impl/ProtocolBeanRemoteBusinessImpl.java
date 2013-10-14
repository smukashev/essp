package kz.bsbnb.usci.core.service.impl;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusArrayJModel;
import kz.bsbnb.usci.eav.model.json.ContractStatusJModel;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness
{
    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(ProtocolBeanRemoteBusinessImpl.class);
    private Gson gson = new Gson();

    @PostConstruct
    public void init() {
        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            couchbaseClient = new CouchbaseClient(nodes, "test", "");
        } catch (Exception e) {
            logger.error("Error connecting to Couchbase: " + e.getMessage());
        }
    }

    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId)
    {
        ArrayList<Protocol> list = new ArrayList<Protocol>();

        String batchId = "" + inputInfoId.getId();

        if(batchId != null) {
            View view = couchbaseClient.getView("batch", "contract_status");
            Query query = new Query();
            query.setDescending(true);
            query.setRangeEnd("\"" + batchId + "_0\"");
            query.setRangeStart("\"" + batchId + "_9\"");


            ViewResponse response = couchbaseClient.query(view, query);

            Iterator<ViewRow> rows = response.iterator();

            ArrayList<ContractStatusArrayJModel> csList = new ArrayList<ContractStatusArrayJModel>();
            while(rows.hasNext()) {
                ViewRow viewRowNoDocs = rows.next();

                ContractStatusArrayJModel batchFullStatusJModel =
                        gson.fromJson(viewRowNoDocs.getValue(), ContractStatusArrayJModel.class);

                //csList.add(batchFullStatusJModel);

                for (ContractStatusJModel csajm : batchFullStatusJModel.getContractStatuses()) {
                    Protocol prot = new Protocol();
                    prot.setId(1L);
                    Message m = new Message();

                    m.setCode("A");
                    m.setNameKz(csajm.getDescription());
                    m.setNameRu(csajm.getDescription());
                    prot.setMessage(m);

                    Shared s = new Shared();
                    s.setCode("S");
                    s.setNameRu(csajm.getProtocol());
                    s.setNameKz(csajm.getProtocol());
                    prot.setMessageType(s);


                    prot.setNote("присвоено " + csajm.getReceived());
                    prot.setPackNo(csajm.getIndex());
                    prot.setPrimaryContractDate(csajm.getContractDate());
                    prot.setProtocolType(s);


                    prot.setTypeDescription(csajm.getContractNo());

                    list.add(prot);
                }
            }
        }

        return list;
    }
}

