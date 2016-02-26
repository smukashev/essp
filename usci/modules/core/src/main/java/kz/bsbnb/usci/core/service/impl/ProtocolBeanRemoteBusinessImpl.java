package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        for (EntityStatus entityStatus : entityStatusList) {
            if (protocolsToDisplay.contains(entityStatus.getStatus())) {
                fillProtocol(entityStatus, inputInfoId, list);
            }
        }

        return list;
    }

    private void fillProtocol(EntityStatus entityStatus, InputInfo inputInfoId, ArrayList<Protocol> list) {

        Protocol protocol = new Protocol();
        protocol.setId(1L);
        protocol.setPackNo(entityStatus.getIndex() != null ? entityStatus.getIndex() : -1l);
        protocol.setInputInfo(inputInfoId);
        String err="";

        if(entityStatus.getErrorCode()!=null) {
            err = Errors.getError(entityStatus.getErrorCode());
            if(entityStatus.getDevDescription()!=null) {
                String[] params = entityStatus.getDevDescription().split("\\|");
                String[] words = err.split(" ");
                for(String param:params) {
                    for(int i = 0; i<words.length; i++){
                        if(words[i].startsWith("#")){
                            words[i] = param;
                            break;
                        }
                    }
                }

                err = StringUtils.join(Arrays.copyOfRange(words, 0, words.length), " ");
            }
        }
        Message message = new Message();
        message.setCode("A");

        if (err == null) {
            message.setNameKz(entityStatus.getErrorCode());
            message.setNameRu(entityStatus.getErrorCode());
        } else {
            message.setNameKz(entityStatus.getDescription() + "| " + err);
            message.setNameRu(entityStatus.getDescription() + "| " +err);
        }

        Shared type = new Shared();

        type.setCode(entityStatus.getStatus().code());
        type.setNameRu(entityStatus.getStatusDescription());
        type.setNameKz(entityStatus.getStatusDescription());

        protocol.setMessage(message);
        protocol.setMessageType(type);
        protocol.setProtocolType(type);

        if (TOTAL_COUNT == entityStatus.getStatus()) {
            message.setNameRu("Общее количество:");
            message.setNameKz("Общее количество:");
            protocol.setNote(entityStatus.getDescription());
        } else if (ACTUAL_COUNT == entityStatus.getStatus()) {
            message.setNameRu("Заявленное количество:");
            message.setNameKz("Заявленное количество:");
            protocol.setNote(entityStatus.getDescription());
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
}

