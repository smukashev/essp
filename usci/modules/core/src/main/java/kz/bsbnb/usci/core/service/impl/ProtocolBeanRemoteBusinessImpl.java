package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kz.bsbnb.usci.eav.util.EntityStatuses.*;

@Service
public class ProtocolBeanRemoteBusinessImpl implements ProtocolBeanRemoteBusiness {

    @Autowired
    private IBatchService batchService;

    private final DateFormat contractDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private final List<EntityStatuses> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED, TOTAL_COUNT, ACTUAL_COUNT);

    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId) {
        ArrayList<Protocol> list = new ArrayList<>();

        long batchId = inputInfoId.getId().longValue();

        List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId);

        Batch batch = batchService.getBatch(batchId);

        for (EntityStatus entityStatus : entityStatusList) {
            if (protocolsToDisplay.contains(entityStatus.getStatus())) {
                fillProtocol(entityStatus, inputInfoId, list, batch);
            }
        }

        fillErrorCount(list, inputInfoId, batch);

        return list;
    }

    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId, int firstIndex, int count) {
        ArrayList<Protocol> list = new ArrayList<>();

        long batchId = inputInfoId.getId().longValue();

        List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId, firstIndex, count);

        Batch batch = batchService.getBatch(batchId);

        for (EntityStatus entityStatus : entityStatusList) {
            if (protocolsToDisplay.contains(entityStatus.getStatus())) {
                fillProtocol(entityStatus, inputInfoId, list, batch);
            }
        }

        fillErrorCount(list, inputInfoId, batch);

        return list;
    }

    @Override
    public int countProtocolsByInputInfo(InputInfo inputInfoId) {
        return batchService.getEntityStatusCount(inputInfoId.getId().longValue());
    }

    private void fillProtocol(EntityStatus entityStatus, InputInfo inputInfoId, ArrayList<Protocol> list, Batch batch) {

        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(entityStatus.getIndex() != null ? entityStatus.getIndex() : -1l);
        protocol.setInputInfo(inputInfoId);
        String err="";

        if(entityStatus.getErrorCode()!=null) {
            if(entityStatus.getDevDescription()!=null)
                err = Errors.decompose(entityStatus.getErrorCode()+"|~~~|"+entityStatus.getDevDescription());
            else
                err = Errors.decompose(entityStatus.getErrorCode());
        }
        Message message = new Message();
        message.setCode("A");

        if (err == null) {
            message.setNameKz(entityStatus.getErrorCode());
            message.setNameRu(entityStatus.getErrorCode());
        } else {
            message.setNameKz(err);
            message.setNameRu(err);
        }

        Shared type = new Shared();

        type.setCode(entityStatus.getStatus().code());
        type.setNameRu(entityStatus.getStatusDescription());
        type.setNameKz(entityStatus.getStatusDescription());

        protocol.setMessage(message);
        protocol.setProtocolType(type);
        protocol.setMessageType(type);

        if (TOTAL_COUNT == entityStatus.getStatus()) {
            message.setNameRu("Заявленное количество:");
            message.setNameKz("Заявленное количество:");
            protocol.setNote("" + batch.getTotalCount());
        } else if (ACTUAL_COUNT == entityStatus.getStatus()) {
            message.setNameRu("Количество загруженных:");
            message.setNameKz("Количество загруженных:");
            protocol.setNote("" + batch.getActualCount());
        } else {
            if (COMPLETED == entityStatus.getStatus()) {
                message.setNameRu("Идентификатор сущности:");
                message.setNameKz("Идентификатор сущности:");
                protocol.setNote(entityStatus.getEntityId() + "");
            }

            protocol.setTypeDescription(entityStatus.getDescription());
        }

        list.add(protocol);
    }


    private void fillErrorCount(ArrayList<Protocol> list, InputInfo inputInfoId, Batch batch)
    {
        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(-1l);
        protocol.setInputInfo(inputInfoId);
        Message message = new Message();
        message.setCode("A");
        message.setNameRu("Количество загруженных с ошибкой:");
        message.setNameKz("Количество загруженных с ошибкой:");

        Shared type = new Shared();
        type.setCode("");
        type.setNameRu("");
        type.setNameKz("");

        protocol.setMessage(message);
        protocol.setProtocolType(type);
        protocol.setMessageType(type);

        int errorsCount = batchService.getErrorEntityStatusCount(batch);

        protocol.setNote(String.valueOf(errorsCount));

        list.add(protocol);
    }
}

