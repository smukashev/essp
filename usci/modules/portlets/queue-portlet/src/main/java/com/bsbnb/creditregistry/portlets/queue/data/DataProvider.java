package com.bsbnb.creditregistry.portlets.queue.data;

import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.util.QueueOrderType;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface DataProvider {
    List<Creditor> getCreditors(long userId, boolean isAdmin);

    List<QueueFileInfo> getQueue(List<Creditor> creditors);

    public String getConfig(String type, String code) throws ConfigurationException;

    public void saveConfig(EavGlobal config) throws ConfigurationException;

    /*
     * Removes input info with specified id from processing queue
     * @throws InputInfoNotInQueueException if input info is not present in queue at the moment
     */
    //public void rejectInputInfo(int inputInfoId) throws InputInfoNotInQueueException;

    List<QueueFileInfo> getPreviewQueue(List<Creditor> creditors, List<Integer> selectedCreditorIds, QueueOrderType selectedOrder);

    BatchFullJModel getBatchFullModel(BigInteger batchId);

    List<InputInfoDisplayBean> getMaintenanceInfo(List<Creditor> creditors, Date reportDate);

    void approveAndSend(List<Long> approvedInputInfos);
}
