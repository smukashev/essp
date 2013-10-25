package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.io.Serializable;
import java.util.Date;



/**
 *
 * @author Aidar.Myrzahanov
 */
//@Entity
//@Table(name = "CROSS_CHECK", schema = "MAINTENANCE")
public class CrossCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    public CrossCheck() {
        
    }
    
//    @Id
//    @Basic(optional = false)
//    @Column(name = "ID")
    private long id;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "STATUS_ID", nullable = false)
    private Shared status;
    
//    @Basic(optional = false)
//    @Column(name = "USER_NAME")
    private String username;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "CREDITOR_ID", nullable = false)
    private Creditor creditor;
//    
//    @Basic(optional = false)
//    @Column(name = "DATE_BEGIN")
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateBegin;
//    
//    @Basic(optional = false)
//    @Column(name = "DATE_END")
//    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateEnd;
//    
//    @Basic(optional = false)
//    @Column(name = "REPORT_DATE")
//    @Temporal(javax.persistence.TemporalType.DATE)
    private Date reportDate;

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
     * @return the status
     */
    public Shared getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Shared status) {
        this.status = status;
    }

    /**
     * @return the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String username) {
        this.username = username;
    }

    /**
     * @return the creditor
     */
    public Creditor getCreditor() {
        return creditor;
    }

    /**
     * @param creditor the creditor to set
     */
    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    /**
     * @return the dateBegin
     */
    public Date getDateBegin() {
        return dateBegin;
    }

    /**
     * @param dateBegin the dateBegin to set
     */
    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    /**
     * @return the dateEnd
     */
    public Date getDateEnd() {
        return dateEnd;
    }

    /**
     * @param dateEnd the dateEnd to set
     */
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * @return the reportDate
     */
    public Date getReportDate() {
        return reportDate;
    }

    /**
     * @param reportDate the reportDate to set
     */
    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
    
    public String getCreditorName() {
        return creditor.getName();
    }
    
}
