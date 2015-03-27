package com.bsbnb.usci.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "R_CORE_REPORT#")
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigInteger id;
    @JoinColumn(name = "CREDITOR_ID", referencedColumnName = "ID")
    private Creditor creditor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TOTAL_COUNT")
    private BigInteger totalCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ACTUAL_COUNT")
    private BigInteger actualCount;
    @Column(name = "BEG_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date begDate;
    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "REPORT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "STATUS_ID")
    private Shared status;
    @Size(max = 80)
    @Column(name = "USERNAME")
    private String username;

    public Report() {
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Creditor getCreditorId() {
        return creditor;
    }

    public void setCreditorId(Creditor creditor) {
        this.creditor = creditor;
    }

    public BigInteger getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(BigInteger totalCount) {
        this.totalCount = totalCount;
    }

    public BigInteger getActualCount() {
        return actualCount;
    }

    public void setActualCount(BigInteger actualCount) {
        this.actualCount = actualCount;
    }

    public Date getBegDate() {
        return begDate;
    }

    public void setBegDate(Date begDate) {
        this.begDate = begDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Shared getStatus() {
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Report)) {
            return false;
        }
        Report other = (Report) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Report[ id=" + id + " ]";
    }

}
