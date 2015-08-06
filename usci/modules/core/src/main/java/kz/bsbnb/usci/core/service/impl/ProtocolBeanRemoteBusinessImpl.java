package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.cr.model.Message;
import kz.bsbnb.usci.cr.model.Protocol;
import kz.bsbnb.usci.cr.model.Shared;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
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

    private Date parseContractDate(String sDate) {
        synchronized (contractDateFormat) {
            try {
                return contractDateFormat.parse(sDate);
            } catch (Exception e) {
//                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public List<Protocol> getProtocolsBy_InputInfo(InputInfo inputInfoId) {
        ArrayList<Protocol> list = new ArrayList<Protocol>();

        long batchId = inputInfoId.getId().longValue();

        batchService.getBatch(batchId);

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
        protocol.setPackNo(entityStatus.getIndex());
        protocol.setInputInfo(inputInfoId);

        Message message = new Message();
        message.setCode("A");
        message.setNameKz(entityStatus.getDescription());
        message.setNameRu(entityStatus.getDescription());

        Shared type = new Shared();
        type.setCode(entityStatus.getStatus().code());
        type.setNameRu(entityStatus.getStatus().code());
        type.setNameKz(entityStatus.getStatus().code());

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
                protocol.setNote(entityStatus.getDescription());
            }

            Map<String, String> entityStatusParams = batchService.getEntityStatusParams(entityStatus.getId());

            fillTypeDescription(entityStatusParams, protocol);

            protocol.setPrimaryContractDate(
                    parseContractDate(
                            entityStatusParams.get(StatusProperties.CONTRACT_DATE)
                    )
            );
        }

        list.add(protocol);
    }

    private void fillTypeDescription(Map<String, String> entityStatusParams, Protocol protocol) {
        if (entityStatusParams.get(StatusProperties.CONTRACT_NO) != null) {
            protocol.setTypeDescription(entityStatusParams.get(StatusProperties.CONTRACT_NO));
        } else if (entityStatusParams.get(StatusProperties.REF_NAME) != null) {
            protocol.setTypeDescription("Наименование: " + entityStatusParams.get(StatusProperties.REF_NAME));
        }
    }
}

