package com.bsbnb.creditregistry.portlets.queue.thread;

import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrderType;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestQueueConfiguration implements QueueConfiguration {

    private int filesInProcessing;
    private long lastLaunchMillis;
    private QueueOrderType orderType;
    private int parallelLimit;
    private List<Integer> priorityCreditorIds;
    private String wsdlLocation;

    @Override
    public int getFilesInProcessing() throws ConfigurationException {
        return filesInProcessing;
    }

    @Override
    public long getLastLaunchMillis() throws ConfigurationException {
        return lastLaunchMillis;
    }

    @Override
    public QueueOrderType getOrderType() {
        return orderType;
    }

    @Override
    public int getParallelLimit() throws ConfigurationException {
        return parallelLimit;
    }

    @Override
    public List<Integer> getPriorityCreditorIds() {
        return Collections.unmodifiableList(priorityCreditorIds);
    }

    @Override
    public String getWsdlLocation() throws ConfigurationException {
        return wsdlLocation;
    }

    @Override
    public void setLastLaunchMillis(long millis) throws ConfigurationException {
        lastLaunchMillis = millis;
    }

    @Override
    public void setOrderType(QueueOrderType orderType) {
        this.orderType = orderType;
    }

    @Override
    public void setPriorityCreditorIds(List<Integer> ids) {
        priorityCreditorIds = ids;
    }

    /**
     * @param filesInProcessing the filesInProcessing to set
     */
    public void setFilesInProcessing(int filesInProcessing) {
        this.filesInProcessing = filesInProcessing;
    }

    /**
     * @param parallelLimit the parallelLimit to set
     */
    public void setParallelLimit(int parallelLimit) {
        this.parallelLimit = parallelLimit;
    }

    /**
     * @param wsdlLocation the wsdlLocation to set
     */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }
}
