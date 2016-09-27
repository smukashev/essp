package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.DataUtils;
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

    private List<BatchStatuses> protocolsToDisplay = Arrays.asList(ERROR, COMPLETED, WAITING_FOR_SIGNATURE, WAITING, PROCESSING, CANCELLED, MAINTENANCE_REQUEST);

    private Map<Long, EavGlobal> globalMap = new HashMap<>();

    @Override
    public List<InputInfo>  getAllInputInfos(List<Creditor> creditorsList, Date reportDate) {
        ArrayList<InputInfo> list = new ArrayList<>();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        List<Batch> batchList = batchService.getAll(reportDate, creditorsList);

        List<Long> bathsIds = new LinkedList<>();
        for (Batch batch : batchList) {
            bathsIds.add(batch.getId());
        }

        List<BatchStatus> batchStatuses = batchService.getBatchStatuses(bathsIds);

        Map<Long, List<BatchStatus> > batchStatusMap = new HashMap<>();

        for (BatchStatus batchStatus : batchStatuses) {
            if(!batchStatusMap.containsKey(batchStatus.getBatchId())) {
                batchStatusMap.put(batchStatus.getBatchId(), new LinkedList<BatchStatus>());
            }

            batchStatusMap.get(batchStatus.getBatchId()).add(batchStatus);
        }

        for (Batch batch : batchList) {
            Creditor currentCreditor = inputCreditors.get(batch.getCreditorId());

            if (currentCreditor == null) {
                continue;
            }

            //List<BatchStatus> batchStatusList = batchService.getBatchStatusList(batch.getId());
            List<BatchStatus> batchStatusList = batchStatusMap.get(batch.getId());


            InputInfo ii = getInputInfo(batch, currentCreditor, batchStatusList);

            if (reportDate == null || DataUtils.compareBeginningOfTheDay(ii.getReportDate(), reportDate) == 0) {
                list.add(ii);
            }
        }

        return list;
    }

    @Override
    public List<InputInfo>  getAllInputInfos(List<Creditor> creditorsList, Date reportDate, int firstIndex, int count) {
        ArrayList<InputInfo> list = new ArrayList<>();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditorsList) {
            inputCreditors.put(cred.getId(), cred);
        }

        List<Batch> batchList = batchService.getAll(reportDate, creditorsList, firstIndex, count);

        List<Long> bathsIds = new LinkedList<>();
        for (Batch batch : batchList) {
            bathsIds.add(batch.getId());
        }

        List<BatchStatus> batchStatuses = batchService.getBatchStatuses(bathsIds);

        Map<Long, List<BatchStatus> > batchStatusMap = new HashMap<>();

        for (BatchStatus batchStatus : batchStatuses) {
            if(!batchStatusMap.containsKey(batchStatus.getBatchId())) {
                batchStatusMap.put(batchStatus.getBatchId(), new LinkedList<BatchStatus>());
            }

            batchStatusMap.get(batchStatus.getBatchId()).add(batchStatus);
        }

        for (Batch batch : batchList) {
            Creditor currentCreditor = inputCreditors.get(batch.getCreditorId());

            if (currentCreditor == null) {
                continue;
            }

            //List<BatchStatus> batchStatusList = batchService.getBatchStatusList(batch.getId());
            List<BatchStatus> batchStatusList = batchStatusMap.get(batch.getId());


            InputInfo ii = getInputInfo(batch, currentCreditor, batchStatusList);

            if (reportDate == null || DataUtils.compareBeginningOfTheDay(ii.getReportDate(), reportDate) == 0) {
                list.add(ii);
            }
        }

        return list;
    }

    private InputInfo getInputInfo(Batch batch, Creditor currentCreditor, List<BatchStatus> batchStatusList) {
        InputInfo ii = new InputInfo();

        String lastStatus = "";
        Date lastReceiptDate = null;

        ii.setBatchStatuses(new ArrayList<Protocol>());

        Boolean isCompleted = false;
        String completedStatus = null;

        for (BatchStatus batchStatus : batchStatusList) {
            if (protocolsToDisplay.contains(batchStatus.getStatus())) {
                if (lastReceiptDate == null || lastReceiptDate.compareTo(batchStatus.getReceiptDate()) < 0) {
                    lastReceiptDate = batchStatus.getReceiptDate();
                    lastStatus = batchStatus.getStatus().code();
                }

                fillProtocol(batchStatus, ii);

                if (batchStatus.getStatus().equals(COMPLETED) || batchStatus.getStatus().equals(ERROR)) {
                    isCompleted = true;
                    completedStatus = batchStatus.getStatus().code();
                }
            }
            fillDates(batchStatus, ii);
        }

        if (isCompleted)
            lastStatus = completedStatus;

        ii.setUserId(batch.getUserId());
        ii.setReportDate(batch.getRepDate());

        ii.setTotal(batch.getTotalCount());

        ii.setId(BigInteger.valueOf(batch.getId()));

        ii.setCreditor(currentCreditor);
        ii.setFileName(batch.getFileName());

        switch (lastStatus) {
            case "COMPLETED":
                lastStatus = "Завершён";
                break;
            case "ERROR":
                lastStatus = "Ошибка";
                break;
            case "WAITING_FOR_SIGNATURE":
                lastStatus = "Ожидает подписи";
                break;
            case "WAITING":
                lastStatus = "В очереди";
                break;
            case "PROCESSING":
                lastStatus = "В обработке";
                break;
            case "CANCELLED":
                lastStatus = "Отмена загрузки";
                break;
            case "MAINTENANCE_REQUEST":
                lastStatus = "Запрос на изменение за утвержденный период";
                break;
        }

        Shared s = new Shared();
        s.setCode("S");
        s.setNameRu(lastStatus);
        s.setNameKz(lastStatus);

        ii.setReceiverType(s);
        ii.setStatus(s);
        return ii;
    }

    private String fillDates(BatchStatus batchStatus, InputInfo inputInfo) {
        BatchStatuses lastStatus = batchStatus.getStatus();

        if (lastStatus == PROCESSING) {
            inputInfo.setStartedDate(batchStatus.getReceiptDate());
        } else if (lastStatus == COMPLETED) {
            inputInfo.setCompletionDate(batchStatus.getReceiptDate());
        } else if (lastStatus == WAITING || lastStatus == WAITING_FOR_SIGNATURE || lastStatus == MAINTENANCE_REQUEST) {
            inputInfo.setReceiverDate(batchStatus.getReceiptDate());
        } else if (lastStatus == ERROR) {
            inputInfo.setReceiverDate(batchStatus.getReceiptDate());
            inputInfo.setCompletionDate(batchStatus.getReceiptDate());
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
                case "WAITING_FOR_SIGNATURE":
                    s.setNameRu("Ожидает подписи");
                    s.setNameKz("Ожидает подписи");
                    break;
                case  "MAINTENANCE_REQUEST":
                    s.setNameRu("Запрос на изменение за утвержденный период");
                    s.setNameKz("Запрос на изменение за утвержденный период");
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

    @Override
    public List<InputInfo> getMaintenanceInfo(List<Creditor> creditors, Date reportDate) {
        List<Batch> maintenanceBatches = batchService.getMaintenanceBatches(reportDate);
        ArrayList<InputInfo> list = new ArrayList<>();

        HashMap<Long, Creditor> inputCreditors = new HashMap<>();

        for (Creditor cred : creditors) {
            inputCreditors.put(cred.getId(), cred);
        }

        Iterator<Batch> mIterator = maintenanceBatches.iterator();
        while(mIterator.hasNext()) {
            Batch batch = mIterator.next();
            if(!inputCreditors.containsKey(batch.getCreditorId()))
                mIterator.remove();



            InputInfo ii = getInputInfo(batch, inputCreditors.get(batch.getCreditorId())
                    , batchService.getBatchStatusList(batch.getId()));

            list.add(ii);
        }

        return list;
    }

    @Override
    public void approveMaintenance(List<Long> approvedBatchIds) {
        batchService.approveMaintenance(approvedBatchIds);
    }

    @Override
    public int countInputInfos(List<Creditor> creditors, Date reportDate) {
        return batchService.getBatchCount(creditors, reportDate);
    }
}
