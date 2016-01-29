package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

import static kz.bsbnb.usci.eav.util.BatchStatuses.*;

@Service
public class InputInfoBeanRemoteBusinessImpl implements InputInfoBeanRemoteBusiness {

    @Autowired
    private IBatchService batchService;

    @Autowired
    private IGlobalService globalService;

    private List<BatchStatuses> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED);

    private Map<Long, EavGlobal> globalMap = new HashMap<>();

    @Override
    public List<InputInfo>  getAllInputInfos(List<Creditor> creditorsList, Date reportDate) {
        ArrayList<InputInfo> list = new ArrayList<>();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        List<Batch> batchList = batchService.getAll(reportDate);

        for (Batch batch : batchList) {
            Creditor currentCreditor = inputCreditors.get(batch.getCreditorId());

            if (currentCreditor == null) {
                continue;
            }

            List<BatchStatus> batchStatusList = batchService.getBatchStatusList(batch.getId());

            InputInfo ii = new InputInfo();

            String lastStatus = "";

            ii.setBatchStatuses(new ArrayList<Protocol>());

            for (BatchStatus batchStatus : batchStatusList) {
                if (protocolsToDisplay.contains(batchStatus.getStatus())) {
                    if (StringUtils.isEmpty(lastStatus)) {
                        lastStatus = batchStatus.getStatus().code();
                    }
                    fillProtocol(batchStatus, ii);
                }
                fillDates(batchStatus, ii);
            }

            ii.setUserId(batch.getUserId());
            ii.setReportDate(batch.getRepDate());

            ii.setTotal(10L);

            ii.setId(BigInteger.valueOf(batch.getId()));

            ii.setCreditor(currentCreditor);
            ii.setFileName(batch.getFileName());

            if (lastStatus.equals("COMPLETED")) {
                lastStatus = "Завершён";
            } else if (lastStatus.equals("ERROR")) {
                lastStatus = "Ошибка";
            }

            Shared s = new Shared();
            s.setCode("S");
            s.setNameRu(lastStatus);
            s.setNameKz(lastStatus);

            ii.setReceiverType(s);
            ii.setStatus(s);

            if (reportDate == null || DataUtils.compareBeginningOfTheDay(ii.getReportDate(), reportDate) == 0) {
                list.add(ii);
            }
        }

        return list;
    }

    private String fillDates(BatchStatus batchStatus, InputInfo inputInfo) {
        BatchStatuses lastStatus = batchStatus.getStatus();

        if (lastStatus == PROCESSING) {
            inputInfo.setStartedDate(batchStatus.getReceiptDate());
        } else if (lastStatus == COMPLETED) {
            inputInfo.setCompletionDate(batchStatus.getReceiptDate());
        } else if (lastStatus == WAITING) {
            inputInfo.setReceiverDate(batchStatus.getReceiptDate());
        } else if (lastStatus == ERROR && inputInfo.getReceiverDate() == null) {
            inputInfo.setReceiverDate(batchStatus.getReceiptDate());
        }

        return lastStatus.code();
    }

    private void fillProtocol(BatchStatus batchStatus, InputInfo inputInfo) {
        Protocol protocol = new Protocol();
        protocol.setId(0L);
        {
            Message message = new Message();
            message.setCode("A");
            message.setNameKz(batchStatus.getDescription());
            message.setNameRu(batchStatus.getDescription());
            protocol.setMessage(message);
        }
        {
            Shared s = new Shared();
            s.setCode(batchStatus.getStatus().code());

            switch (s.getCode()) {
                case "ERROR":
                    s.setNameRu("Ошибка");
                    s.setNameKz("Ошибка");
                    break;
                case "COMPLETED":
                    s.setNameRu("Завершен");
                    s.setNameKz("Завершен");
                    break;
                default:
                    s.setNameRu(batchStatus.getStatus().code());
                    s.setNameKz(batchStatus.getStatus().code());
                    break;
            }

            protocol.setMessageType(s);
            protocol.setProtocolType(s);
        }

        inputInfo.getBatchStatuses().add(protocol);
    }

    @Override
    public List<InputInfo> getPendingBatches(List<Creditor> creditorsList) {
        ArrayList<InputInfo> list = new ArrayList<>();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        List<Batch> pendingBatchList = batchService.getPendingBatchList();

        for (Batch batch : pendingBatchList) {
            Creditor creditor = inputCreditors.get(batch.getCreditorId());

            if (creditor != null) {
                InputInfo inputInfo = new InputInfo();
                inputInfo.setFileName(batch.getFileName());
                inputInfo.setId(BigInteger.valueOf(batch.getId()));
                inputInfo.setCreditor(creditor);

                Shared s = new Shared();

                EavGlobal g;
                if(globalMap.containsKey(batch.getStatusId())) {
                    g = globalMap.get(batch.getStatusId());
                } else {
                    g = globalService.getGlobal(batch.getStatusId());
                    if(g!= null)
                        globalMap.put(g.getId(), g);
                }
                s.setCode("S");
                if(g != null) {
                    s.setNameRu(g.getDescription());
                    s.setNameKz(g.getDescription());
                }
                inputInfo.setStatus(s);

                inputInfo.setUserId(batch.getUserId());
                inputInfo.setReceiverDate(batch.getReceiptDate());
                inputInfo.setActualCount(batch.getActualCount());

                list.add(inputInfo);
            }

        }

        return list;
    }

    @Override
    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        Batch batch = batchService.getBatch(batchId.longValue());

        return new BatchFullJModel(
                batch.getId(),
                batch.getFileName(),
                batch.getContent(),
                batch.getReceiptDate(),
                batch.getUserId(),
                batch.getCreditorId()
        );
    }
}
