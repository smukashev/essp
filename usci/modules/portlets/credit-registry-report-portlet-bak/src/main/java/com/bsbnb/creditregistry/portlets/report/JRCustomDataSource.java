package com.bsbnb.creditregistry.portlets.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.logging.Level;
import net.sf.jasperreports.engine.JRDataSource;
import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class JRCustomDataSource implements JRDataSource {

    private int currentRecordIndex = -1;
    private ArrayList<String[]> dataSet;
    private String[] columnNames;
    private HashMap<String, Integer> columnIndicesMap;
    private int columnCount;

    public JRCustomDataSource(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            columnCount = rsmd.getColumnCount();
            columnNames = new String[columnCount];
            columnIndicesMap = new HashMap<String, Integer>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnNames[columnIndex - 1] = rsmd.getColumnName(columnIndex);
                columnIndicesMap.put(columnNames[columnIndex - 1], columnIndex - 1);
            }
            dataSet = new ArrayList<String[]>();
            while (rs.next()) {
                String[] record = new String[columnCount];
                for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                    record[columnIndex - 1] = rs.getString(columnIndex);
                }
                dataSet.add(record);
            }
        } catch (SQLException sqle) {
            log.log(Level.SEVERE, "SQL Exception occured while parsing report result set", sqle);
        }
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
        Integer index = columnIndicesMap.get(name);
        if (index == null) {
            if ("ROWNUM".equalsIgnoreCase(name)) {
                return currentRecordIndex + 1;
            }
            throw new JRException("No such field: " + name);
        }
        return dataSet.get(currentRecordIndex)[index];
    }
}
