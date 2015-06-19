package kz.bsbnb.usci.core.service.impl;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.json.EntityStatusArrayJModel;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness
{
    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(ProtocolBeanRemoteBusinessImpl.class);
    private Gson gson = new Gson();

    private final DateFormat gsonDateFormat
            = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);

    private Date parseDateFromGson(String sDate) {
        synchronized (gsonDateFormat) {
            try {
                return gsonDateFormat.parse(sDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @PostConstruct
    public void init() {
        System.setProperty("viewmode", "production");
        //System.setProperty("viewmode", "development");

        ArrayList<URI> nodes = new ArrayList<URI>();
        nodes.add(URI.create("http://172.17.110.114:8091/pools"));

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
            View view = couchbaseClient.getView("batch", "entity_status");
            Query query = new Query();
            query.setDescending(true);
            query.setRangeEnd("[" + batchId + ",0]");
            query.setRangeStart("[" + batchId + ",999999999]");


            ViewResponse response = couchbaseClient.query(view, query);

            Iterator<ViewRow> rows = response.iterator();

            ArrayList<EntityStatusArrayJModel> csList = new ArrayList<EntityStatusArrayJModel>();
            while(rows.hasNext()) {
                ViewRow viewRowNoDocs = rows.next();

                EntityStatusArrayJModel batchFullStatusJModel =
                        gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

                //csList.add(batchFullStatusJModel);

                for (EntityStatusJModel csajm : batchFullStatusJModel.getEntityStatuses()) {
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

                    prot.setPrimaryContractDate(
                        parseDateFromGson((String) csajm.getProperty(StatusProperties.CONTRACT_DATE))
                    );

                    prot.setProtocolType(s);

                    prot.setTypeDescription((String)csajm.getProperty(StatusProperties.CONTRACT_NO));
                    prot.setInputInfo(inputInfoId);
                    list.add(prot);
                }
            }
        }

        return list;
    }
}

