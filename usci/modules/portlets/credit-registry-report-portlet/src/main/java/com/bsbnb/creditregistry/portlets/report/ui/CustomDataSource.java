package com.bsbnb.creditregistry.portlets.report.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class CustomDataSource extends IndexedContainer implements JRDataSource {

    private int currentRecordIndex = -1;
    private ArrayList<Object[]> dataSet;
    private String[] columnNames;
    private HashMap<String, Integer> columnIndicesMap;
    private int columnCount;

    public CustomDataSource() {
        dataSet = new ArrayList<Object[]>();
        columnCount = 1;
        columnNames = new String[columnCount];
        columnNames[0] = "column1";
        columnIndicesMap = new HashMap<String, Integer>();
        columnIndicesMap.put("column1", 0);

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
