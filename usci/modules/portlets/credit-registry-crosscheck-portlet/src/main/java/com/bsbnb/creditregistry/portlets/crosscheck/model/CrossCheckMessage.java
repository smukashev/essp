package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.io.Serializable;

/**
 *
 * @author Aidar.Myrzahanov
 */
//@Entity
//@Table(name="CROSS_CHECK_MESSAGE", schema="MAINTENANCE")
public class CrossCheckMessage implements Serializable {
//    @Id
//    @Basic(optional = false)
//    @Column(name = "ID")
    private long id;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "MESSAGE_ID", nullable = true)
    private Message message;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "CROSS_CHECK_ID", nullable = true)
    private CrossCheck crossCheck;
    
//    @Basic(optional = false)
//    @Column(name = "INNER_VALUE")
    private String innerValue;
//    
//    @Basic(optional = false)
//    @Column(name = "OUTER_VALUE")
    private String outerValue;
    
//    @Basic(optional = false)
//    @Column(name = "DESCRIPTION")
    private String description;
    
//    @Basic(optional = false)
//    @Column(name = "IS_ERROR")
    private Integer isError;
    
//    @Basic(optional = false)
//    @Column(name = "DIFF")
    private String difference;

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
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * @return the crosscheck
     */
    public CrossCheck getCrossCheck() {
        return crossCheck;
    }

    /**
     * @param crossCheck the crosscheck to set
     */
    public void setCrossCheck(CrossCheck crossCheck) {
        this.crossCheck = crossCheck;
    }

    /**
     * @return the innerValue
     */
    public String getInnerValue() {
        return innerValue;
    }

    /**
     * @param innerValue the innerValue to set
     */
    public void setInnerValue(String innerValue) {
        this.innerValue = innerValue;
    }

    /**
     * @return the outerValue
     */
    public String getOuterValue() {
        return outerValue;
    }

    /**
     * @param outerValue the outerValue to set
     */
    public void setOuterValue(String outerValue) {
        this.outerValue = outerValue;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /*
     * Метод нужен для вывода 7-значных номеров балансовых счетов в формате: **** ***
     */
    public String getFormattedDescription() {
        if(description==null||description.length()!=7) {
            return description;
        }
        return description.substring(0,4)+" "+description.substring(4);
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the isError
     */
    public Integer getIsError() {
        return isError;
    }

    /**
     * @param isError the isError to set
     */
    public void setIsError(Integer isError) {
        this.isError = isError;
    }

    /**
     * @return the difference
     */
    public String getDifference() {
        return difference;
    }

    /**
     * @param difference the difference to set
     */
    public void setDifference(String difference) {
        this.difference = difference;
    }
 
}
