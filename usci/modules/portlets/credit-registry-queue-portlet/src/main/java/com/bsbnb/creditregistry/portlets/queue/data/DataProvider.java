package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {

    public List<Creditor> getCreditors(long userId, boolean isAdmin);

    public List<QueueFileInfo> getQueue(List<Creditor> creditors);

    public Sysconfig getConfig(String key) throws ConfigurationException;

    public void saveConfig(Sysconfig config) throws ConfigurationException;

    /*
     * Removes input info with specified id from processing queue
     * @throws InputInfoNotInQueueException if input info is not present in queue at the moment
     */
    public void rejectInputInfo(int inputInfoId) throws InputInfoNotInQueueException;
}
