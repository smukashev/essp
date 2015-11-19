package kz.bsbnb.usci.receiver.queue.impl;

import kz.bsbnb.usci.receiver.queue.JobInfo;

import java.util.*;

/**
 * Created by bauyrzhan.makhambeto on 19/10/2015.
 */
public class CreditorCycleOrder extends AbstractQueueOrder {
    Map<Long, Long> creditorsTime = new HashMap<>();
    Long time = 0L;

    @Override
    public int compare(JobInfo o1, JobInfo o2) {
        return (int) (o1.getBatchId() - o2.getBatchId());
    }

    @Override
    public JobInfo getNextFile(List<JobInfo> files) {
        JobInfo result = null;
        Long minTime = Long.MAX_VALUE;
        for(JobInfo file : files) {
            if(result == null) {
                result = file;
                minTime = getNullSafe(creditorsTime.get(file.getBatchInfo().getCreditorId()));
            }
            else {
                Long creditorLastTime  = getNullSafe(creditorsTime.get(file.getBatchInfo().getCreditorId()));

                if(minTime > creditorLastTime) {
                    minTime = creditorLastTime;
                    result = file;
                }
            }
        }

        if(result != null)
            creditorsTime.put(result.getBatchInfo().getCreditorId(), time++);

        return result;
    }

    public Long getNullSafe(Long val){
        return val == null ? -1L : val;
    }
}
