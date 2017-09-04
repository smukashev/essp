package com.bsbnb.usci.portlets.audit;

import com.bsbnb.usci.portlets.audit.dm.AuditTableRecord;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditFilterComponent extends FormLayout{   
    private TextField infoField;
    private PopupDateField dateBeforeField;
    private PopupDateField dateAfterField;
    private TextField tableNameField;
    private Button applyButton;
    private AuditFilter filter;
    private BeanItemContainer<AuditTableRecord> container;
    private ResourceBundle bundle;
    
    public AuditFilterComponent(BeanItemContainer<AuditTableRecord> container, ResourceBundle bundle) {
        filter = new AuditFilter();
        this.container = container;
        this.bundle = bundle;
    }
    
    public String getString(String key) {
        return bundle.getString(key);
    }
    
    @Override
    public void attach() {
        infoField = new TextField(getString("FilterNoteField"));
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, bundle.getLocale());
        String datePattern = ((SimpleDateFormat) dateFormat).toPattern();
        System.out.println("Pattern: "+datePattern);
        dateBeforeField = new PopupDateField(getString("FilterDateBeforeField"));
        dateBeforeField.setDateFormat(datePattern);
        dateAfterField = new PopupDateField(getString("FilterDateAfterField"));
        dateAfterField.setDateFormat(datePattern);
        tableNameField = new TextField(getString("FilterTableNameField"));
        applyButton = new Button(getString("ApplyFilter"), new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                filter.setStartDate((Date) dateBeforeField.getValue());
                filter.setFinishDate((Date) dateAfterField.getValue());
                filter.setInfo(infoField.getValue().toString());
                filter.setTableName(tableNameField.getValue().toString());
                if(container!=null) {
                    container.removeAllContainerFilters();
                    container.addContainerFilter(filter);
                }
            }
        });
        addComponent(infoField);
        addComponent(dateBeforeField);
        addComponent(dateAfterField);
        addComponent(tableNameField);
        addComponent(applyButton);
    }
    
    public AuditFilter getFilter() {
        return filter;
    }
}
