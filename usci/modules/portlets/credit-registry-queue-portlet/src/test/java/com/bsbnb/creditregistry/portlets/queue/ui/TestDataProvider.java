package com.bsbnb.creditregistry.portlets.queue.ui;

import com.bsbnb.creditregistry.dm.maintenance.InputInfo;
import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import com.bsbnb.creditregistry.dm.ref.Creditor;
import com.bsbnb.creditregistry.dm.ref.Shared;
import com.bsbnb.creditregistry.dm.ref.SubjectType;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.data.InputInfoNotInQueueException;
import com.bsbnb.creditregistry.portlets.queue.data.QueueFileInfo;
import com.bsbnb.creditregistry.portlets.queue.thread.ConfigurationException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class TestDataProvider implements DataProvider {
    private static final String WSDL_LOCATION_CODE = "WSDL_Location";
    private static final String PARALLEL_LIMIT_CODE = "PARALLEL_LIMIT";
    private static final String FILES_IN_PROCESSING_CODE = "FILES_IN_PROCESSING";
    private static final String LAST_QUEUE_LAUNCH_MILLIS_CODE = "LAST_QUEUE_LAUNCH_MILLIS";
    private static final String QUEUE_ORDER_CODE = "QUEUE_ORDER";
    private static final String CHRONOLOGICAL_ORDER_VALUE = "CHRONOLOGICAL";
    public static final String CREDITOR_CYCLE_ORDER_VALUE = "CREDITOR_CYCLE";
    public static final String MINIMUM_WEIGHT_ORDER_VALUE = "MINIMUM_WEIGHT";
    public static final String PRIORITY_CREDITOR_IDS_CODE = "PRIORITY_CREDITOR_IDS";
    private static final Map<String, String> configurationMap = new HashMap<String, String>();

    static {
        configurationMap.put(WSDL_LOCATION_CODE, "location");
        configurationMap.put(PARALLEL_LIMIT_CODE, "1");
        configurationMap.put(FILES_IN_PROCESSING_CODE, "0");
        configurationMap.put(LAST_QUEUE_LAUNCH_MILLIS_CODE, "-1");
        configurationMap.put(QUEUE_ORDER_CODE, CHRONOLOGICAL_ORDER_VALUE);
        configurationMap.put(PRIORITY_CREDITOR_IDS_CODE, "");
    }
    
    public List<Creditor> getCreditors(long userId, boolean isUserAdmin) {
        Creditor testCreditor = new Creditor();
        testCreditor.setId(BigInteger.ONE);
        testCreditor.setName("Test bank");
        SubjectType subjectType = new SubjectType();
        subjectType.setNameRu("Test subject type");
        testCreditor.setSubjectType(subjectType);
        return Arrays.asList(testCreditor);
    }
    
    public List<QueueFileInfo> getQueue(List<Creditor> creditors) {
        QueueFileInfo testQueueInfo = new QueueFileInfo();
        testQueueInfo.setRownum(1);
        testQueueInfo.setFilename("hello");
        return Arrays.asList(testQueueInfo);
    }
    
    public void setInputInfoStatusProcessing(long inputInfoId) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setWsdlLocation(String wsdlLocation) {
        configurationMap.put(WSDL_LOCATION_CODE, wsdlLocation);
    }
    
    public void setParallelLimit(int parallelLimit) {
        configurationMap.put(PARALLEL_LIMIT_CODE, parallelLimit+"");
    }
    
    public void setFilesInProcessing(int filesInProcessing) {
        configurationMap.put(FILES_IN_PROCESSING_CODE, filesInProcessing+"");
    }
    
    public void setLastQueueLaunchMillis(long lastQueueLaunchMillis) {
        configurationMap.put(LAST_QUEUE_LAUNCH_MILLIS_CODE, lastQueueLaunchMillis+"");
    }
    
    public void setQueueOrder(String code) {
        configurationMap.put(QUEUE_ORDER_CODE, code);
    }
    
    public void setPriorityCreditorIds(String ids) {
        configurationMap.put(PRIORITY_CREDITOR_IDS_CODE, ids);
    }

    public Sysconfig getConfig(String key) throws ConfigurationException {
        Sysconfig config = new Sysconfig();
        config.setValue(configurationMap.get(key));
        return config;
    }

    public void saveConfig(Sysconfig config) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InputInfo getInputInfoById(int inputInfoId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveInputInfo(InputInfo inputInfo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Shared getInputInfoRejectedStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void rejectInputInfo(int inputInfoId) throws InputInfoNotInQueueException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
