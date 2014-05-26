package com.bsbnb.creditregistry.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "R_REF_CREDITOR")
public class Creditor implements Serializable {

    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigInteger id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "NAME")
    private String name;
    @Size(max = 200)
    @Column(name = "SHORT_NAME")
    private String shortName;
    @Size(max = 10)
    @Column(name = "CODE")
    private String code;
    @Column(name = "SHUTDOWN_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date shutdownDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CHANGE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date changeDate;
    @JoinColumn(name = "SUBJECT_TYPE", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private SubjectType subjectType;
    @OneToMany(mappedBy = "mainOfficeId")
    private List<Creditor> creditorList;
    @JoinColumn(name = "MAIN_OFFICE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Creditor mainOfficeId;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "EAV_A_CREDITOR_USER",
            joinColumns =
            @JoinColumn(name = "CREDITOR_ID"),
            inverseJoinColumns =
            @JoinColumn(name = "USER_ID"))
    private List<PortalUser> portalUserList;

    public Creditor() {
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getShutdownDate() {
        return shutdownDate;
    }

    public void setShutdownDate(Date shutdownDate) {
        this.shutdownDate = shutdownDate;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    @XmlTransient
    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    public void setCreditorList(List<Creditor> creditorList) {
        this.creditorList = creditorList;
    }

    public Creditor getMainOfficeId() {
        return mainOfficeId;
    }

    public void setMainOfficeId(Creditor mainOfficeId) {
        this.mainOfficeId = mainOfficeId;
    }
    
    public List<PortalUser> getPortalUserList() {
        return portalUserList;
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
        if (!(object instanceof Creditor)) {
            return false;
        }
        Creditor other = (Creditor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.portlets.crosscheck.dm.Creditor[ id=" + id + " ]";
    }
}
