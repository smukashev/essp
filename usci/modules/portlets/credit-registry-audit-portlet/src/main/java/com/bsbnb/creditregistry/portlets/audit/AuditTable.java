package com.bsbnb.creditregistry.portlets.audit;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditTable extends Table {
    
    private ResourceBundle bundle;
    private SimpleDateFormat dateFormat;
    
    public AuditTable(String caption, Container container, ResourceBundle bundle) {
        super(caption, container);
        this.bundle = bundle;
        dateFormat = new SimpleDateFormat("d MMMMM yyyy HH:mm:ss", bundle.getLocale());
        setVisibleColumns(getColumns());
        setColumnHeaders(getHeaders());
//        if(bundle.getLocale().getLanguage().compareToIgnoreCase("kz")==0) noteColumnName = "noteKz";
//        else noteColumnName = "noteRu";
//        setColumnWidth(noteColumnName, 200);
    }
    
    private static final String[] COLUMNS_ORDER = new String[] {"username", "actionName", "tableName", "info", "beginDate", "endDate"};
    private String noteColumnName;
    
    /**
     * Метод возвращает список колонок для отображения в таблице аудита
     */
    private String[] getColumns() {
        return COLUMNS_ORDER;
    }
    
    /**
     * Метод возвращает локализованные названия колонок
     * @return 
     */
    private String[] getHeaders() {
        String[] columns = getColumns();
        int columnsNumber = columns.length;
        String[] headers = new String[columnsNumber];
        for(int columnIndex=0; columnIndex<columnsNumber; columnIndex++) {
            headers[columnIndex] = bundle.getString(columns[columnIndex]);
        }
        return headers;
    }
    
    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        Object value = property.getValue();
        if (value instanceof Date) {
            return dateFormat.format((Date) value);
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
}
