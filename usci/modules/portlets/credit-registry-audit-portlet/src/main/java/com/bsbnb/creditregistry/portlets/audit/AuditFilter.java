package com.bsbnb.creditregistry.portlets.audit;

import com.bsbnb.creditregistry.portlets.audit.dm.AuditEvent;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditFilter implements Filter{
    
    private Date startDate;
    private Date finishDate;
    private String info;
    private String tableName;
    
    public AuditFilter() {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.set(1500, 1, 1);
        startDate = calendar.getTime();
        calendar.set(2100, 1, 1);
        finishDate = calendar.getTime();
        info = "";
        tableName = "";
    }
    
    public AuditFilter(Date startDate, Date finishDate, String info, String tableName) {
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.info = info;
        this.tableName = tableName;
    }

    @Override
    public boolean passesFilter(Object itemId, Item item) {
        BeanItem beanItem = (BeanItem) item;
        AuditEvent record = (AuditEvent) beanItem.getBean();
        if(record==null) return false;
        return isValidAuditDate(record.getBeginDate())&&
                isValidInfo(record.getInfo())&&
                isValidTableName(record.getTableName());
    }

    public boolean isValidAuditDate(Date auditDate) {
        if(auditDate==null) return true;
        if(startDate!=null&&startDate.after(auditDate)) return false;
        if(finishDate!=null&&finishDate.before(auditDate)) return false;
        return true;
    }
    
    public boolean isValidInfo(String value) {
        if(info==null||info.length()==0) return true;
        if(value==null) return false;
        return value.toLowerCase().contains(info.toLowerCase());
    }
       
    public boolean isValidTableName(String value) {
        if(tableName==null||tableName.length()==0) return true;
        if(value==null) return false;
        return value.toLowerCase().contains(tableName.toLowerCase());
    }
    
    @Override
    public boolean appliesToProperty(Object propertyId) {
        return true;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the finishDate
     */
    public Date getFinishDate() {
        return finishDate;
    }

    /**
     * @param finishDate the finishDate to set
     */
    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getInfo() {
        return this.info;
    }    
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    
}
