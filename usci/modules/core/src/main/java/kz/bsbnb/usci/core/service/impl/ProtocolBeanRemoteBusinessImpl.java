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
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.CouchbaseClientManager;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

import static kz.bsbnb.usci.tool.couchbase.EntityStatuses.*;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness
{
    @Autowired
    private CouchbaseClientManager couchbaseClientManager;

    private CouchbaseClient couchbaseClient;
    private Logger logger = Logger.getLogger(ProtocolBeanRemoteBusinessImpl.class);
    private Gson gson = new Gson();

    private final DateFormat gsonDateFormat
            = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);

    private final List<String> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED, TOTAL_COUNT, ACTUAL_COUNT);

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
        couchbaseClient = couchbaseClientManager.get();
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

            while(rows.hasNext()) {
                ViewRow viewRowNoDocs = rows.next();

                EntityStatusArrayJModel batchFullStatusJModel =
                        gson.fromJson(viewRowNoDocs.getValue(), EntityStatusArrayJModel.class);

                for (EntityStatusJModel entityStatus : batchFullStatusJModel.getEntityStatuses()) {
                    if (protocolsToDisplay.contains(entityStatus.getProtocol())) {
                        fillProtocol(entityStatus, inputInfoId, list);
                    }
                }
            }
        }

        return list;
    }

    private void fillProtocol(EntityStatusJModel entityStatus, InputInfo inputInfoId, ArrayList<Protocol> list) {
        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(entityStatus.getIndex());
        protocol.setInputInfo(inputInfoId);

        Message message = new Message();
        message.setCode("A");
        message.setNameKz(entityStatus.getDescription());
        message.setNameRu(entityStatus.getDescription());

        Shared type = new Shared();
        type.setCode("S");
        type.setNameRu(entityStatus.getProtocol());
        type.setNameKz(entityStatus.getProtocol());

        protocol.setMessage(message);
        protocol.setMessageType(type);
        protocol.setProtocolType(type);

        if (EntityStatuses.TOTAL_COUNT.equals(entityStatus.getProtocol())) {
            message.setNameRu("Общее количество:");
            message.setNameKz("Общее количество:");
            protocol.setNote(entityStatus.getDescription());
        } else if (EntityStatuses.ACTUAL_COUNT.equals(entityStatus.getProtocol())) {
            message.setNameRu("Заявленное количество:");
            message.setNameKz("Заявленное количество:");
            protocol.setNote(entityStatus.getDescription());
        } else {
            if (EntityStatuses.COMPLETED.equals(entityStatus.getProtocol())) {
                message.setNameRu("Идентификатор сущности:");
                message.setNameKz("Идентификатор сущности:");
                protocol.setNote(entityStatus.getDescription());
            }
            protocol.setPrimaryContractDate(
                    parseDateFromGson((String) entityStatus.getProperty(StatusProperties.CONTRACT_DATE))
            );
            protocol.setTypeDescription((String) entityStatus.getProperty(StatusProperties.CONTRACT_NO));
        }

        list.add(protocol);
    }
}

