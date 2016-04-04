package com.bsbnb.creditregistry.portlets.queue.thread;


import com.bsbnb.creditregistry.portlets.queue.data.DataProvider;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.QueueOrderType;
import org.apache.log4j.Logger;

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
    private static final String QUEUE_SETTING = "QUEUE_SETTING";
    private static final String QUEUE_ALGO = "QUEUE_ALGO";
    private static final String PRIORITY_CREDITOR_IDS_CODE = "PRIORITY_CREDITOR_IDS";
    private DataProvider dataProvider;
    public final Logger logger = Logger.getLogger(DatabaseQueueConfiguration.class);

    public DatabaseQueueConfiguration(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    /*
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
    }*/
    /*
    @Override
    public long getLastLaunchMillis() throws ConfigurationException {
        try {
            return Long.parseLong(dataProvider.getConfig(QUEUE_SETTING, LAST_QUEUE_LAUNCH_MILLIS_CODE));
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException("Couldn't parse " + LAST_QUEUE_LAUNCH_MILLIS_CODE + " configuration value", nfe);
        }
    }

    @Override
    public void setLastLaunchMillis(long millis) throws ConfigurationException {
        //Sysconfig config = dataProvider.getConfig(QUEUE_SETTING, LAST_QUEUE_LAUNCH_MILLIS_CODE);
        EavGlobal global = new EavGlobal(QUEUE_SETTING, LAST_QUEUE_LAUNCH_MILLIS_CODE, millis + "");
        //config.setValue(millis + "");
        dataProvider.saveConfig(global);
    }*/

    @Override
    public QueueOrderType getOrderType() {
        try {
            String orderCode = dataProvider.getConfig(QUEUE_SETTING, QUEUE_ALGO);
            return QueueOrderType.getQueueOrderTypeByCode(orderCode);
        } catch (ConfigurationException ce) {
            logger.warn(null, ce);
        }

        return QueueOrderType.CHRONOLOGICAL;
    }

    @Override
    public void setOrderType(QueueOrderType orderType) {
        try {
            EavGlobal global = new EavGlobal(QUEUE_SETTING, QUEUE_ALGO, orderType.name());
            dataProvider.saveConfig(global);
        } catch (Exception e) {
            logger.warn(null, e);
        }
    }

    @Override
    public List<Integer> getPriorityCreditorIds() {
        try {
            String dbValue = dataProvider.getConfig(QUEUE_SETTING, PRIORITY_CREDITOR_IDS_CODE);
            String[] idValues = dbValue.split(",");
            ArrayList<Integer> ids = new ArrayList<Integer>(idValues.length);
            for (String idValue : idValues) {
                try {
                    ids.add(Integer.parseInt(idValue));
                }catch (NumberFormatException nfe) {
                    logger.error(Errors.unmarshall(Errors.getMessage(Errors.E253, nfe)));
                    throw new ConfigurationException(Errors.getMessage(Errors.E253, nfe));
                }
            }
            return ids;
        } catch (ConfigurationException ce) {
            logger.warn("Exception on sysconfig: " + PRIORITY_CREDITOR_IDS_CODE, ce);
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
            //String config = dataProvider.getConfig(QUEUE_SETTING, PRIORITY_CREDITOR_IDS_CODE);
            EavGlobal global = new EavGlobal(QUEUE_SETTING, PRIORITY_CREDITOR_IDS_CODE, value.toString());
            //config.setValue(value.toString());
            dataProvider.saveConfig(global);
        } catch (ConfigurationException ce) {
            logger.warn("Exception on writing sysconfig: " + PRIORITY_CREDITOR_IDS_CODE, ce);
        }
    }
}
