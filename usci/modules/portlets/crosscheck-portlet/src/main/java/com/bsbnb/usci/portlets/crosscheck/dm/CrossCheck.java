package com.bsbnb.usci.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "CROSS_CHECK")
public class CrossCheck implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigDecimal id;
    @Column(name = "DATE_BEGIN")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateBegin;
    @Column(name = "DATE_END")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnd;
    @JoinColumn(name = "CREDITOR_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Creditor creditor;
    @Basic(optional = false)
    @NotNull
    @Column(name = "REPORT_DATE")
    @Temporal(TemporalType.DATE)
    private Date reportDate;
    @Column(name = "STATUS_ID")
    private Integer status;   //Shared*/
    @Column(name = "STATUS_Name")
    private String statusname;
    @Column(name = "USER_NAME")
    private String username;

    public CrossCheck() {
    }

    public CrossCheck(BigDecimal id) {
        this.id = id;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

   
    public void setStatus(Integer status) {
        this.status = status;
    }

     public Integer getStatus() {
        return status;
    }
     
     public void setStatusName(String statusname){
         this.statusname = statusname;
     }
     
     public String getStatusName() {
          return status==null ? "" : statusname;
     }

       
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getCreditorName() {
        return creditor==null ? "" : creditor.getName();
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
        if (!(object instanceof CrossCheck)) {
            return false;
        }
        CrossCheck other = (CrossCheck) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CrossCheck[ id=" + id + " ]";
    }

}
