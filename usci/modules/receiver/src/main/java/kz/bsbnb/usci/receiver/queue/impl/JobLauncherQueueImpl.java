package kz.bsbnb.usci.receiver.queue.impl;

import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputInfo;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.BatchInfo;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import kz.bsbnb.usci.receiver.queue.JobInfo;
import kz.bsbnb.usci.receiver.queue.JobLauncherQueue;
import kz.bsbnb.usci.receiver.queue.QueueOrder;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

@Component
@Scope(value = "singleton")
public class JobLauncherQueueImpl implements JobLauncherQueue {

    Logger logger = LoggerFactory.getLogger(JobLauncherQueueImpl.class);

    @Autowired
    IServiceRepository serviceRepository;

    IGlobalService globalService;

    private final String QUEUE_SETTING = "QUEUE_SETTING";
    private final String QUEUE_ALGO = "QUEUE_ALGO";
    private final String PRIORITY_CREDITOR_IDS = "PRIORITY_CREDITOR_IDS";

    @Value("${concurrencyLimit}")
    private int concurrencyLimit;
    private int activeJobCount;
    Set<Long> creditorsWithPriority = new HashSet<>();
    QueueOrderType currentOrderType = null;
    QueueOrder order;
    List<JobInfo> queue = new LinkedList<>();

    @PostConstruct
    public void init(){
        globalService = serviceRepository.getGlobalService();
        reloadConfig();
    }

    @Override
    public JobInfo getNextJob() {
        if(activeJobCount < concurrencyLimit) {
            synchronized (this) {
                if(queue.size() > 0) {
                    activeJobCount++;
                    JobInfo ret = next(queue);
                    queue.remove(ret);
                    return ret;
                }
            }
        }
        return null;
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("JobLauncherStatus:\n");
        sb.append("queue size = " + queue.size() + ", order(Impl):" + order.getClass().getName() + ", " +
                "activeJobCount = " + activeJobCount);

        sb.append("queue state:\n");
        if(queue.size() == 0) sb.append("(empty)");

        for(JobInfo jobInfo : queue) {
            sb.append("batchId = " + jobInfo.getBatchId());
            sb.append(", batchName = " + jobInfo.getBatchInfo().getBatchName());
            sb.append(", size = " + jobInfo.getBatchInfo().getContentSize());
            sb.append("\n");
        }

        return sb.toString();
    }


    @Override
    public synchronized void jobFinished(){
        activeJobCount --;
    }

    @Override
    public synchronized void addJob(long batchId, BatchInfo batchInfo) {
        queue.add(new JobInfo(batchId, batchInfo));
    }

    public JobInfo next(List<JobInfo> queue){
        return next(queue, creditorsWithPriority);
    }

    public JobInfo next(List<JobInfo> queue, Set<Long> creditorsWithPriority) {
        return next(queue, creditorsWithPriority, order);
    }

    public JobInfo next(List<JobInfo> queue, Set<Long> creditorsWithPriority, QueueOrder order) {
        //Сначала определяем первые файлы по каждому кредитору
        /*List<JobInfo> firstFilesByEachCreditor = new ArrayList<JobInfo>();
        Set<Long> creditorIds = new HashSet<>();*/
        Map<Long, JobInfo> firstFilesByEachCreditor = new HashMap<>();
        for (JobInfo jobInfo : queue) {
            if (!firstFilesByEachCreditor.containsKey(jobInfo.getBatchInfo().getCreditorId())) {
                firstFilesByEachCreditor.put(jobInfo.getBatchInfo().getCreditorId(), jobInfo);
            } else {
                JobInfo o = firstFilesByEachCreditor.get(jobInfo.getBatchInfo().getCreditorId());
                if(order.compare(jobInfo, o) < 0) {
                    firstFilesByEachCreditor.put(jobInfo.getBatchInfo().getCreditorId(), jobInfo);
                }
            }
        }

        //Из полученных выбираем файлы приоритетных кредиторов
        List<JobInfo> priorityCreditorsFiles = new ArrayList<JobInfo>(firstFilesByEachCreditor.size());
        Set<Long> priorityCreditorIds = new HashSet<>();
        for (Long creditorId : creditorsWithPriority) {
            priorityCreditorIds.add(creditorId);
        }
        for (JobInfo jobInfo : firstFilesByEachCreditor.values()) {
            if (priorityCreditorIds.contains(jobInfo.getBatchInfo().getCreditorId())) {
                priorityCreditorsFiles.add(jobInfo);
            }
        }
        //если таковых нет, то пропускаем всех остальных
        if (priorityCreditorsFiles.isEmpty()) {
            priorityCreditorsFiles = new ArrayList<>(firstFilesByEachCreditor.values());
        }

        return order.getNextFile(priorityCreditorsFiles);
    }

    @Override
    public List<InputInfo> getOrderedFiles(List<Creditor> creditors, Set<Long> creditorsWithPriority, QueueOrderType queueOrderType){
        Map<Long, Creditor> creditorMap = new HashMap<>();
        QueueOrder order = getImplementationByEnum(queueOrderType);

        for(Creditor c: creditors) {
            creditorMap.put(c.getId(), c);
        }

        List<JobInfo> tempFiles = new LinkedList<> ();
        List<InputInfo> ret = new LinkedList<>();
        for(JobInfo jobInfo : queue) {
            tempFiles.add(jobInfo);
        }
        int n = tempFiles.size();

        for(int i=0;i<n;i++) {
            JobInfo j = next(tempFiles, creditorsWithPriority, order);
            tempFiles.remove(j);
            InputInfo inputInfo = new InputInfo();
            inputInfo.setId(BigInteger.valueOf(j.getBatchId()));
            inputInfo.setFileName(j.getBatchInfo().getBatchName());
            inputInfo.setCreditor(creditorMap.get(j.getBatchInfo().getCreditorId()));
            inputInfo.setReceiverDate(j.getBatchInfo().getReceiptDate());
            inputInfo.setUserId(j.getBatchInfo().getUserId());
            inputInfo.setActualCount(0L);
            ret.add(inputInfo);
        }
        return ret;
    }

    @Override
    public void changeQueueType(QueueOrderType orderType) {
        try {
            if (currentOrderType != orderType) {
                globalService.update(QUEUE_SETTING, QUEUE_ALGO, orderType.code());
                currentOrderType = orderType;
                order = getImplementationByEnum(orderType);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public QueueOrder getImplementationByEnum(QueueOrderType queueOrderType){
        switch (queueOrderType) {
            case CREDITOR_CYCLE:
                return new CreditorCycleOrder();
            case MINIMUM_WEIGHT:
                return new MinimumWeightOrder();
            default:
                return new ChronologicalOrder();
        }
    }

    @Override
    public void reloadConfig() {
        order = getImplementationByEnum(QueueOrderType.valueOf(globalService.getValue(QUEUE_SETTING, QUEUE_ALGO)));
        String priorityCreditors = globalService.getValue(QUEUE_SETTING, PRIORITY_CREDITOR_IDS);
        creditorsWithPriority = new HashSet<>();
        try {
            String[] creditors = priorityCreditors.split(",");
            for(String str : creditors) {
                creditorsWithPriority.add(Long.parseLong(str));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }
}
