package com.bsbnb.creditregistry.portlets.queue.thread;

import kz.bsbnb.usci.eav.util.QueueOrderType;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface QueueConfiguration {

    //int getFilesInProcessing() throws ConfigurationException;

    //long getLastLaunchMillis() throws ConfigurationException;

    QueueOrderType getOrderType();

    //int getParallelLimit() throws ConfigurationException;

    List<Integer> getPriorityCreditorIds();

    //String getWsdlLocation() throws ConfigurationException;

    //void setLastLaunchMillis(long millis) throws ConfigurationException;

    void setOrderType(QueueOrderType orderType);

    void setPriorityCreditorIds(List<Integer> ids);
}
