package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import org.mvel2.ast.Proto;
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

    private final List<EntityStatuses> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED);

    private final List<EntityStatuses> protocolStatisticsToDisplay = Arrays.asList(ACTUAL_COUNT);


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

        return list;
    }

    @Override
    public List<Protocol> getProtocolStatisticsBy_InputInfo(InputInfo inputInfoId)
    {
        ArrayList<Protocol> list = new ArrayList<>();
        long batchId = inputInfoId.getId().longValue();
        Batch batch = batchService.getBatch(batchId);
        List<EntityStatus> entityStatusList = batchService.getEntityStatusList(batchId);

        for (EntityStatus entityStatus : entityStatusList) {
            if (protocolStatisticsToDisplay.contains(entityStatus.getStatus())) {
                fillProtocol(entityStatus, inputInfoId, list, batch);
            }
        }

        fillSuccessCount(list, inputInfoId, batch);
        fillErrorCount(list, inputInfoId, batch);

        return list;
    }


    private void fillProtocol(EntityStatus entityStatus, InputInfo inputInfoId, ArrayList<Protocol> list, Batch batch) {

        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(entityStatus.getIndex() != null ? entityStatus.getIndex() : -1l);
        protocol.setInputInfo(inputInfoId);

        String errTitle = "";
        String errDescription = "";

        if (entityStatus.getErrorCode() != null) {
            errTitle = Errors.getTitle(entityStatus.getErrorCode());

            if (entityStatus.getDevDescription() != null) {
                errDescription = Errors.decompose(entityStatus.getErrorCode() + "|~~~|" + entityStatus.getDevDescription());
            } else {
                errDescription = Errors.decompose(entityStatus.getErrorCode());
            }
        }

        Message message = new Message();
        message.setCode("A");

        if (errTitle == null) {
            message.setNameKz(entityStatus.getErrorCode());
            message.setNameRu(entityStatus.getErrorCode());
        } else {
            message.setNameKz(errTitle);
            message.setNameRu(errTitle);
        }

        protocol.setMessage(message);
        protocol.setNote(errDescription);

        Shared type = new Shared();

        type.setCode(entityStatus.getStatus().code());
        type.setNameRu(entityStatus.getStatusDescription());
        type.setNameKz(entityStatus.getStatusDescription());

        protocol.setProtocolType(type);
        protocol.setMessageType(type);

        if (TOTAL_COUNT == entityStatus.getStatus()) {
            return;
        } else if (ACTUAL_COUNT == entityStatus.getStatus()) {
            message.setNameRu("Всего:");
            message.setNameKz("Всего:");
            protocol.setNote("" + batch.getActualCount());
        } else {
            if (COMPLETED == entityStatus.getStatus()) {

                if (entityStatus.getOperation() != null && entityStatus.getOperation().equals(OperationType.INSERT.name())) {
                    message.setNameRu("Информация по займу успешно загружена");
                    message.setNameKz("Информация по займу успешно загружена");
                } else if (entityStatus.getOperation() != null && entityStatus.getOperation().equals(OperationType.UPDATE.name())) {
                    message.setNameRu("Информация по займу успешно обновлена");
                    message.setNameKz("Информация по займу успешно обновлена");
                }else{
                    message.setNameRu("Без операций");
                    message.setNameKz("Без операций:");
                }

                protocol.setNote(entityStatus.getEntityId() + "");
            }

            protocol.setTypeDescription(entityStatus.getDescription());
        }

        list.add(protocol);
    }


    private void fillSuccessCount(ArrayList<Protocol> list, InputInfo inputInfoId, Batch batch)
    {
        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(-1l);
        protocol.setInputInfo(inputInfoId);
        Message message = new Message();
        message.setCode("A");
        message.setNameRu("Загруженно:");
        message.setNameKz("Загруженно:");

        Shared type = new Shared();
        type.setCode("");
        type.setNameRu("");
        type.setNameKz("");

        protocol.setMessage(message);
        protocol.setProtocolType(type);
        protocol.setMessageType(type);

        int errorsCount = batchService.getErrorEntityStatusCount(batch);

        protocol.setNote(String.valueOf(batch.getActualCount()-errorsCount));

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
        message.setNameRu("Ошибочных при загрузке:");
        message.setNameKz("Ошибочных при загрузке:");

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

