package com.bsbnb.usci.portlets.audit.dm;

import java.io.Serializable;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class AuditEventKind implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String name;
    private int isAlwaysAuditable;
    private int isActive;
    private String code;

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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the isAlwaysAuditable
     */
    public int getIsAlwaysAuditable() {
        return isAlwaysAuditable;
    }

    /**
     * @param isAlwaysAuditable the isAlwaysAuditable to set
     */
    public void setIsAlwaysAuditable(int isAlwaysAuditable) {
        this.isAlwaysAuditable = isAlwaysAuditable;
    }

    /**
     * @return the isActive
     */
    public int getIsActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

}
