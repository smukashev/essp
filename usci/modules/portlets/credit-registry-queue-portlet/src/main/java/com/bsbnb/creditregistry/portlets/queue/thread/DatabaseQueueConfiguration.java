package com.bsbnb.creditregistry.portlets.queue.thread;

import com.bsbnb.creditregistry.dm.maintenance.Sysconfig;
import static com.bsbnb.creditregistry.portlets.queue.QueueApplication.log;
import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import com.bsbnb.creditregistry.portlets.queue.thread.logic.QueueOrderType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DatabaseQueueConfiguration implements QueueConfiguration {

    private static final String WSDL_LOCATION_CODE = "WSDL_Location";
    private static final String PARALLEL_LIMIT_CODE = "PARALLEL_LIMIT";
    private static final String FILES_IN_PROCESSING_CODE = "FILES_IN_PROCESSING";
    private static final String LAST_QUEUE_LAUNCH_MILLIS_CODE = "LAST_QUEUE_LAUNCH_MILLIS";
    private static final String QUEUE_ORDER_CODE = "QUEUE_ORDER";
    private static final String PRIORITY_CREDITOR_IDS_CODE = "PRIORITY_CREDITOR_IDS";
    private DataProvider dataProvider;

    public DatabaseQueueConfiguration(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public String getWsdlLocation() throws ConfigurationException {
        return dataProvider.getConfig(WSDL_LOCATION_CODE).getValue();
    }

    @Override
    public int getFilesInProcessing() throws ConfigurationException {
        try {
            return Integer.parseInt(dataProvider.getConfig(FILES_IN_PROCESSING_CODE).getValue());
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException("Couldn't parse " + FILES_IN_PROCESSING_CODE + " configuration value", nfe);
        }
    }

    @Override
    public int getParallelLimit() throws ConfigurationException {
        try {
            return Integer.parseInt(dataProvider.getConfig(PARALLEL_LIMIT_CODE).getValue());
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException("Couldn't parse " + PARALLEL_LIMIT_CODE + " configuration value", nfe);
        }
    }

    @Override
    public long getLastLaunchMillis() throws ConfigurationException {
        try {
            return Long.parseLong(dataProvider.getConfig(LAST_QUEUE_LAUNCH_MILLIS_CODE).getValue());
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException("Couldn't parse " + LAST_QUEUE_LAUNCH_MILLIS_CODE + " configuration value", nfe);
        }
    }

    @Override
    public void setLastLaunchMillis(long millis) throws ConfigurationException {
        Sysconfig config = dataProvider.getConfig(LAST_QUEUE_LAUNCH_MILLIS_CODE);
        config.setValue(millis + "");
        dataProvider.saveConfig(config);
    }

    @Override
    public QueueOrderType getOrderType() {
        try {
            String orderCode = dataProvider.getConfig(QUEUE_ORDER_CODE).getValue();
            return QueueOrderType.getQueueOrderTypeByCode(orderCode);
        } catch (ConfigurationException ce) {
            log.log(Level.WARNING, "", ce);
        }

        return QueueOrderType.CHRONOLOGICAL;
    }

    @Override
    public void setOrderType(QueueOrderType orderType) {
        try {
            Sysconfig config = dataProvider.getConfig(QUEUE_ORDER_CODE);
            config.setValue(orderType.getCode());
            dataProvider.saveConfig(config);
        } catch (ConfigurationException ce) {
            log.log(Level.WARNING, "", ce);
        }
    }

    @Override
    public List<Integer> getPriorityCreditorIds() {
        try {
            String dbValue = dataProvider.getConfig(PRIORITY_CREDITOR_IDS_CODE).getValue();
            String[] idValues = dbValue.split(",");
            ArrayList<Integer> ids = new ArrayList<Integer>(idValues.length);
            for (String idValue : idValues) {
                try {
                    ids.add(Integer.parseInt(idValue));
                }catch (NumberFormatException nfe) {
                    throw new ConfigurationException("Parse error", nfe);
                }
            }
            return ids;
        } catch (ConfigurationException ce) {
            log.log(Level.WARNING, "Exception on sysconfig: " + PRIORITY_CREDITOR_IDS_CODE, ce);
        }

        return new ArrayList<Integer>();
    }

    @Override
    public void setPriorityCreditorIds(List<Integer> ids) {
        try {
            if (ids.isEmpty()) {
                ids.add(-1);
            }
            StringBuilder value = new StringBuilder(ids.size() * 2);
            for (int id : ids) {
                value.append(id);
                value.append(",");
            }
            if (value.length() > 0) {
                value.setLength(value.length() - 1);
            }
            Sysconfig config = dataProvider.getConfig(PRIORITY_CREDITOR_IDS_CODE);
            config.setValue(value.toString());
            dataProvider.saveConfig(config);
        } catch (ConfigurationException ce) {
            log.log(Level.WARNING, "Exception on writing sysconfig: " + PRIORITY_CREDITOR_IDS_CODE, ce);
        }
    }
}
