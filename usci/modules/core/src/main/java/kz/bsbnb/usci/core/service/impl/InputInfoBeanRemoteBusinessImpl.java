package kz.bsbnb.usci.core.service.impl;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.json.BatchFullStatusJModel;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.eav.model.json.BatchStatusJModel;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class InputInfoBeanRemoteBusinessImpl implements InputInfoBeanRemoteBusiness
{
    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(InputInfoBeanRemoteBusinessImpl.class);

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

    private List<BatchStatusJModel> getBatchStatuses(long batchId) {
        Gson gson = new Gson();

        View view = couchbaseClient.getView("batch", "batch");
        Query query = new Query();
        query.setGroup(true);
        query.setGroupLevel(1);
        query.setKey("\"" + batchId + "\"");

        ViewResponse response = couchbaseClient.query(view, query);

        Iterator<ViewRow> rows = response.iterator();

        if(rows.hasNext()) {
            ViewRowReduced viewRowNoDocs = (ViewRowReduced) rows.next();

            System.out.println("==================");
            System.out.println(viewRowNoDocs.getValue());
            System.out.println("==================");

            BatchFullStatusJModel batchFullStatusJModel =
                    gson.fromJson(viewRowNoDocs.getValue(), BatchFullStatusJModel.class);

            return batchFullStatusJModel.getStatus().getBatchStatuses();
        }

        return new ArrayList<BatchStatusJModel>();
    }

    private BatchInfo getManifest(long id) {
        Gson gson = new Gson();
        return gson.fromJson(couchbaseClient.get("manifest:" + id).toString(), BatchInfo.class);
    }

    @Override
    public List<InputInfo> getAllInputInfosBy_Creditors_By_RepDateSortedBy_Id_Desc(List<Creditor> creditorsList, Date reportDate)
    {
        ArrayList<InputInfo> list = new ArrayList<InputInfo>();

        View view = couchbaseClient.getView("batch", "batch");
        Query query = new Query();
        query.setLimit(20);
        query.setGroup(true);
        query.setGroupLevel(1);

        ViewResponse viewResponse = couchbaseClient.query(view, query);

        Gson gson = new Gson();

        if(viewResponse != null) {
            for(ViewRow row : viewResponse) {
                ViewRowReduced viewRowNoDocs = (ViewRowReduced) row;

                BatchFullStatusJModel batchFullStatusJModel =
                        gson.fromJson(viewRowNoDocs.getValue(), BatchFullStatusJModel.class);

                Long id = batchFullStatusJModel.getId();

                List<BatchStatusJModel> statusesList = batchFullStatusJModel.getStatus().getBatchStatuses();

                InputInfo ii = new InputInfo();

                String lastStatus = "";

                for (BatchStatusJModel statusModel : statusesList) {
                    lastStatus = statusModel.getProtocol();
                    if (statusModel.getProtocol().equals("PROCESSING")) {
                        ii.setStartedDate(statusModel.getReceived());
                    } else if(statusModel.getProtocol().equals("COMPLETED")) {
                        ii.setCompletionDate(statusModel.getReceived());
                    } else if(statusModel.getProtocol().equals("WAITING")) {
                        ii.setReceiverDate(statusModel.getReceived());
                    }
                }

                BatchInfo manifest = getManifest(id);

                ii.setUserId(manifest.getUserId());
                ii.setReportDate(manifest.getRepDate());

                ii.setTotal(10L);


                ii.setId(BigInteger.valueOf(id));
                ii.setCreditor(creditorsList.get(0));
                ii.setFileName(batchFullStatusJModel.getFileName());

                Shared s = new Shared();
                s.setCode("S");
                s.setNameRu(lastStatus);
                s.setNameKz(lastStatus);

                ii.setReceiverType(s);
                ii.setStatus(s);

                list.add(ii);
            }
        }

        return list;
    }
}
