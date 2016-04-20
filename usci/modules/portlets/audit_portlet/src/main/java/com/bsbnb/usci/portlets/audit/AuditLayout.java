/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.usci.portlets.audit;


import com.bsbnb.usci.portlets.audit.dm.AuditController;
import com.bsbnb.usci.portlets.audit.dm.AuditTableRecord;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditLayout extends VerticalLayout {
    
    //PENDING: Столбец "Действие" в таблице аудита
    //TODO: Многострочные ячейки в таблице
    
    private AuditFilterComponent filterForm;
    private Table table;
    private ResourceBundle bundle;
    private PopupDateField deleteDateField;
    private Button deleteButton;  
    
    public AuditLayout(Locale locale) {
        bundle = ResourceBundle.getBundle("content.Language", new Locale("ru", "RU"));
    }
    
    public String getString(String key) {
        return bundle.getString(key);
    }
    
    @Override
    public void attach() {    
        AuditController controller = new AuditController();
        List<AuditTableRecord> records = controller.getAuditRecords();
        BeanItemContainer<AuditTableRecord> container = new BeanItemContainer<AuditTableRecord>(AuditTableRecord.class,records);
        table = new AuditTable(getString("AuditTableName"),container, bundle);
        table.setWidth("100%");
        filterForm = new AuditFilterComponent(container,bundle);
        container.addContainerFilter(filterForm.getFilter());       
        addComponent(filterForm);
        addComponent(table);
    }
}
