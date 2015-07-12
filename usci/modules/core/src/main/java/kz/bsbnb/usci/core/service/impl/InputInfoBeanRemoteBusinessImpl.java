package kz.bsbnb.usci.core.service.impl;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.*;
import com.google.gson.Gson;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.tool.couchbase.singleton.CouchbaseClientManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

import static kz.bsbnb.usci.tool.couchbase.BatchStatuses.COMPLETED;
import static kz.bsbnb.usci.tool.couchbase.BatchStatuses.ERROR;

@Service
public class InputInfoBeanRemoteBusinessImpl implements InputInfoBeanRemoteBusiness {
    @Autowired
    private CouchbaseClientManager couchbaseClientManager;

    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(InputInfoBeanRemoteBusinessImpl.class);

    private Gson gson = new Gson();

    private final List<String> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED);

    @PostConstruct
    public void init() {
        couchbaseClient = couchbaseClientManager.get();
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

        if (rows.hasNext()) {
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

    private long getCreditorId(long batchId) {
        View view = couchbaseClient.getView("batch", "batch_creditor");
        Query query = new Query();
        query.setDescending(true);
        query.setKey("" + batchId);

        ViewResponse response = couchbaseClient.query(view, query);

        Iterator<ViewRow> rows = response.iterator();

        if (rows.hasNext()) {
            return Long.parseLong(rows.next().getValue());
        }

        return 0;
    }

    @Override
    public List<InputInfo> getAllInputInfos(List<Creditor> creditorsList, Date reportDate) {
        ArrayList<InputInfo> list = new ArrayList<InputInfo>();

        View view = couchbaseClient.getView("batch", "batch");
        Query query = new Query();
        //query.setLimit(20);
        //query.setGroup(true);
        //query.setGroupLevel(1);

        ViewResponse viewResponse = couchbaseClient.query(view, query);

        Gson gson = new Gson();

        HashMap<Long, Creditor> inputCreditors = new HashMap<Long, Creditor>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        BatchFullStatusJModel batchFullStatusJModel = null;

        if (viewResponse != null) {
            for (ViewRow row : viewResponse) {
                ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) row;

                if (batchFullStatusJModel == null) {
                    batchFullStatusJModel =
                            gson.fromJson(viewRowNoDocs.getValue(), BatchFullStatusJModel.class);
                } else {
                    Long id = batchFullStatusJModel.getId();

                    Long creditorId = getCreditorId(id);

                    Creditor currentCreditor = inputCreditors.get(creditorId);

                    if (currentCreditor == null) {
                        batchFullStatusJModel = null;
                        continue;
                    }

                    BatchStatusArrayJModel statusArrayJModel =
                            gson.fromJson(viewRowNoDocs.getValue(), BatchStatusArrayJModel.class);

                    List<BatchStatusJModel> statusesList = statusArrayJModel.getBatchStatuses();

                    InputInfo ii = new InputInfo();

                    String lastStatus = "";

                    ii.setBatchStatuses(new ArrayList<Protocol>());

                    for (BatchStatusJModel statusModel : statusesList) {
                        if (protocolsToDisplay.contains(statusModel.getProtocol())) {
                            fillProtocol(statusModel, ii);
                        }
                        lastStatus = fillDates(statusModel, ii);
                    }

                    BatchInfo manifest = getManifest(id);

                    ii.setUserId(manifest.getUserId());
                    ii.setReportDate(manifest.getRepDate());

                    ii.setTotal(10L);


                    ii.setId(BigInteger.valueOf(id));

                    ii.setCreditor(currentCreditor);
                    ii.setFileName(batchFullStatusJModel.getFileName());

                    Shared s = new Shared();
                    s.setCode("S");
                    s.setNameRu(lastStatus);
                    s.setNameKz(lastStatus);

                    ii.setReceiverType(s);
                    ii.setStatus(s);

                    if (reportDate == null || DataTypeUtil.compareBeginningOfTheDay(ii.getReportDate(), reportDate) == 0) {
                        list.add(ii);
                    }

                    batchFullStatusJModel = null;
                }
            }
        }

        return list;
    }

    private String fillDates(BatchStatusJModel statusModel, InputInfo inputInfo) {
        String lastStatus = statusModel.getProtocol();

        if (lastStatus.equals("PROCESSING")) {
            inputInfo.setStartedDate(statusModel.getReceived());
        } else if (lastStatus.equals("COMPLETED")) {
            inputInfo.setCompletionDate(statusModel.getReceived());
        } else if (lastStatus.equals("WAITING")) {
            inputInfo.setReceiverDate(statusModel.getReceived());
        } else if (lastStatus.equals("ERROR") &&
                inputInfo.getReceiverDate() == null) {
            inputInfo.setReceiverDate(statusModel.getReceived());
        }
        return lastStatus;
    }

    private void fillProtocol(BatchStatusJModel statusModel, InputInfo inputInfo) {
        Protocol protocol = new Protocol();
        protocol.setId(0L);
        {
            Message message = new Message();
            message.setCode("A");
            message.setNameKz(statusModel.getDescription());
            message.setNameRu(statusModel.getDescription());
            protocol.setMessage(message);
        }
        {
            Shared s = new Shared();
            s.setCode("S");
            s.setNameRu(statusModel.getProtocol());
            s.setNameKz(statusModel.getProtocol());
            protocol.setMessageType(s);
            protocol.setProtocolType(s);
        }

        inputInfo.getBatchStatuses().add(protocol);
    }

    @Override
    public List<InputInfo> getPendingBatches(List<Creditor> creditorsList) {
        ArrayList<InputInfo> list = new ArrayList<>();

        View view = couchbaseClient.getView("batch", "batch_pending");
        Query query = new Query();
        query.setStale(Stale.FALSE);
        query.setDescending(true);
        query.setLimit(10000);
        //query.setGroup(true);
        //query.setGroupLevel(1);

        ViewResponse viewResponse = couchbaseClient.query(view, query);

        Gson gson = new Gson();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        BatchFullStatusJModel batchFullStatusJModel = null;

        if (viewResponse != null) {
            for (ViewRow row : viewResponse) {
                ViewRowNoDocs viewRowNoDocs = (ViewRowNoDocs) row;

                batchFullStatusJModel = gson.fromJson(couchbaseClient.get("batch:" +
                        viewRowNoDocs.getKey()).toString(), BatchFullStatusJModel.class);

                Long key = Long.parseLong(viewRowNoDocs.getKey());

                Long creditorId;
                if (batchFullStatusJModel.getCreditorId() != null)
                    creditorId = batchFullStatusJModel.getCreditorId();
                else
                    creditorId = getCreditorId(key.longValue());

                if (inputCreditors.get(creditorId) != null) {
                    InputInfo inputInfo = new InputInfo();
                    inputInfo.setFileName(batchFullStatusJModel.getFileName());
                    inputInfo.setId(BigInteger.valueOf(key));
                    inputInfo.setCreditor(inputCreditors.get(creditorId));

                    Shared s = new Shared();
                    s.setCode("S");
                    s.setNameRu("В обработке");
                    s.setNameKz("В обработке");
                    inputInfo.setStatus(s);

                    inputInfo.setUserId(batchFullStatusJModel.getUserId());
                    inputInfo.setReceiverDate(batchFullStatusJModel.getReceived());

                    list.add(inputInfo);
                }

            }
        }

        return list;
    }

    @Override
    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        Object obj = couchbaseClient.get("batch:" + batchId);
        return gson.fromJson(obj.toString(), BatchFullJModel.class);
    }

}
