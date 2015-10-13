package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.json.*;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.DataUtils;
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

    private List<BatchStatuses> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED);

    @Override
    public List<InputInfo> getAllInputInfos(List<Creditor> creditorsList, Date reportDate) {
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
            s.setCode("S");
            s.setNameRu(batchStatus.getStatus().code());
            s.setNameKz(batchStatus.getStatus().code());
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
                s.setCode("S");
                s.setNameRu("В обработке");
                s.setNameKz("В обработке");
                inputInfo.setStatus(s);

                inputInfo.setUserId(batch.getUserId());
                inputInfo.setReceiverDate(batch.getReceiptDate());

                list.add(inputInfo);
            }

        }

        return list;
    }

    @Override
    public BatchFullJModel getBatchFullModel(BigInteger batchId) {
        Batch batch = batchService.getBatch(batchId.longValue());

        BatchFullJModel batchFullJModel = new BatchFullJModel(
                batch.getId(),
                batch.getFileName(),
                batch.getContent(),
                batch.getReceiptDate(),
                batch.getUserId(),
                batch.getCreditorId()
        );

        return batchFullJModel;
    }

}
