package com.bsbnb.creditregistry.portlets.audit.dm;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditEvent implements Serializable{
    
    public AuditEvent() {
        
    }
    
    public AuditEvent(AuditEvent event) {
        errorCode = event.getErrorCode();
        errorMessage = event.getErrorMessage();
        beginDate = event.getBeginDate();
        endDate = event.getEndDate();
        id = event.getId();
        info = event.getInfo();
        kind = event.getKind();
        success = event.isSuccess() ? 1 : 0;
        tableName = event.getTableName();
        userId = event.getUserId();
    }
    
    private static final long serialVersionUID = 1L;
    private long id;
    private long userId;
    private Date beginDate;
    private Date endDate;
    private int success;
    private int errorCode;
    private String errorMessage;
    private String info;
    private String tableName;
    
    private AuditEventKind kind;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the userId
     */
    public long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /**
     * @return the eventBegin
     */
    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * @param eventBegin the eventBegin to set
     */
    public void setBeginDate(Date eventBegin) {
        this.beginDate = eventBegin;
    }

    /**
     * @return the eventEnd
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param eventEnd the eventEnd to set
     */
    public void setEndDate(Date eventEnd) {
        this.endDate = eventEnd;
    }

    /**
     * @return the isSuccess
     */
    public boolean isSuccess() {
        return success!=0;
    }

    public void setSuccess(boolean success) {
        this.success = success ? 1 : 0;
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
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

    /**
     * @return the kind
     */
    public AuditEventKind getKind() {
        return kind;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(AuditEventKind kind) {
        this.kind = kind;
    }
    
}
