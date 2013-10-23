package com.bsbnb.creditregistry.portlets.report.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import kz.bsbnb.usci.core.service.RemoteCreditorBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CustomDataSource extends IndexedContainer implements JRDataSource {
    private RmiProxyFactoryBean remoteCreditorBusinessFactoryBean;
    private RemoteCreditorBusiness creditorBusiness;

    private int currentRecordIndex = -1;
    private ArrayList<Object[]> dataSet;
    private String[] columnNames;
    private HashMap<String, Integer> columnIndicesMap;
    private int columnCount;

    public CustomDataSource() {
        dataSet = new ArrayList<Object[]>();
        columnNames = new String[]{"NAME", "STATUS-NAME", "BUTTON-COLUMN-CAPTION", "ACTUAL-COUNT",
                "ID", "STATUS-ID", "CREDITOR-ID"};
        columnCount = columnNames.length;
        columnIndicesMap = new HashMap<String, Integer>();
        columnIndicesMap.put("NAME", 0);
        columnIndicesMap.put("STATUS-NAME", 1);
        columnIndicesMap.put("BUTTON-COLUMN-CAPTION", 2);
        columnIndicesMap.put("ACTUAL-COUNT", 3);
        //columnIndicesMap.put("BEGIN-DATE", 4);
        //columnIndicesMap.put("END-DATE", 5);

        columnIndicesMap.put("ID", 4);
        columnIndicesMap.put("STATUS-ID", 5);
        columnIndicesMap.put("CREDITOR-ID", 6);



        remoteCreditorBusinessFactoryBean = new RmiProxyFactoryBean();
        remoteCreditorBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/remoteCreditorBusiness");
        remoteCreditorBusinessFactoryBean.setServiceInterface(RemoteCreditorBusiness.class);

        remoteCreditorBusinessFactoryBean.afterPropertiesSet();
        creditorBusiness = (RemoteCreditorBusiness) remoteCreditorBusinessFactoryBean.getObject();

        List<Creditor> mainOfficeCreditors = creditorBusiness.findMainOfficeCreditors();

        for (Creditor cred : mainOfficeCreditors) {
            int c_count = creditorBusiness.contractCount(cred);

            if (c_count <= 0)
                continue;

            Object[] values = new Object[columnCount];

            values[0] = cred.getName();
            values[1] = "Проверка правил пройдена";
            values[2] = "Button";
            values[3] = c_count;
            //values[4] = new Date();
            //values[5] = new Date();
            values[4] = new Integer((int)cred.getId());
            values[5] = creditorBusiness.creditorApproved(cred) ? new Integer(ConstantValues.REPORT_STATUS_OK) :
                    new Integer(ConstantValues.REPORT_STATUS_IN_PROGRESS);
            values[6] = new Integer((int)cred.getId());

            dataSet.add(values);
        }

        addContainerProperty(columnNames[0], String.class, null);
        addContainerProperty(columnNames[1], String.class, null);
        addContainerProperty(columnNames[2], String.class, null);
        addContainerProperty(columnNames[3], Integer.class, null);
        //addContainerProperty(columnNames[4], Date.class, null);
        //addContainerProperty(columnNames[5], Date.class, null);
        addContainerProperty(columnNames[4], Integer.class, null);
        addContainerProperty(columnNames[5], Integer.class, null);
        addContainerProperty(columnNames[6], Integer.class, null);

        for(Object[] record : dataSet) {
            Item newTableItem = addItem(record);
            for(int columnIndex=0; columnIndex<columnCount; columnIndex++) {
                Property itemProperty = newTableItem.getItemProperty(columnNames[columnIndex]);
                itemProperty.setValue(record[columnIndex]);
            }
        }

        /*try {
            ResultSetMetaData rsmd = rs.getMetaData();
            columnCount = rsmd.getColumnCount();
            columnNames = new String[columnCount];
            columnIndicesMap = new HashMap<String, Integer>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnNames[columnIndex - 1] = rsmd.getColumnName(columnIndex);
                columnIndicesMap.put(columnNames[columnIndex - 1], columnIndex - 1);
                log.log(Level.INFO, "Column #{0}: {1}", new Object[]{columnIndex, columnNames[columnIndex - 1]});
            }
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                Class columnClass = String.class;
                if(rsmd.getColumnType(columnIndex+1)==Types.TIMESTAMP) {
                    columnClass = Date.class;
                } else if(rsmd.getColumnType(columnIndex+1)==Types.NUMERIC) {
                    columnClass = Double.class;
                }
                addContainerProperty(columnNames[columnIndex], columnClass, null);
            }
            dataSet = new ArrayList<Object[]>();
            while (rs.next()) {
                Object[] record = new Object[columnCount];
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    Object obj = rs.getObject(columnIndex);
                    if(rsmd.getColumnType(columnIndex)==Types.TIMESTAMP&&obj!=null) {
                        record[columnIndex-1] = rs.getDate(columnIndex);
                    } else if(rsmd.getColumnType(columnIndex)==Types.NUMERIC&&obj!=null) {
                        record[columnIndex-1] = rs.getDouble(columnIndex);
                    } else {
                        record[columnIndex - 1] = rs.getString(columnIndex);
                    }
                }
                dataSet.add(record);
                Item newTableItem = addItem(record);
                for(int columnIndex=0; columnIndex<columnCount; columnIndex++) {
                    Property itemProperty = newTableItem.getItemProperty(columnNames[columnIndex]);
                    itemProperty.setValue(record[columnIndex]); 
                }
            }

        } catch (SQLException sqle) {
            log.log(Level.SEVERE, "SQL Exception occured while parsing report result set", sqle);
        }*/
    }

    public boolean next() throws JRException {
        currentRecordIndex++;
        return currentRecordIndex < dataSet.size();
    }

    public Object getFieldValue(JRField jrf) throws JRException {
        if (currentRecordIndex >= dataSet.size()) {
            throw new JRException("No more records");
        }
        String name = jrf.getName();
        try {
            return getFieldValue(name);
        } catch (NoSuchFieldException nsfe) {
            throw new JRException("No such field: " + name);
        }
    }

    public Object getFieldValue(String fieldName) throws NoSuchFieldException {
        Integer index = columnIndicesMap.get(fieldName);
        if (index == null) {
            if ("ROWNUM".equalsIgnoreCase(fieldName)) {
                return (currentRecordIndex + 1);
            }
            throw new NoSuchFieldException(fieldName);
        }
        return dataSet.get(currentRecordIndex)[index];
    }

    public void reset() {
        currentRecordIndex = 0;
    }

    public String[] getColumnNames() {
        return columnNames.clone();
    }
}
