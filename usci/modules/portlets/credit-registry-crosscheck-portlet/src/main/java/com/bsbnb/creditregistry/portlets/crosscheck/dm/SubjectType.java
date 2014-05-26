package com.bsbnb.creditregistry.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "R_REF_SUBJECT_TYPE")
public class SubjectType implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigDecimal id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "CODE")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "NAME_RU")
    private String nameRu;
    @Size(max = 200)
    @Column(name = "NAME_KZ")
    private String nameKz;
    @Basic(optional = false)
    @NotNull
    @Column(name = "OPEN_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date openDate;
    @Column(name = "CLOSE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closeDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "IS_LAST")
    private short isLast;
    @Column(name = "REPORT_PERIOD_DURATION_MONTHS")
    private int reportPeriodDurationMonths;
    @OneToMany(mappedBy = "parentId")
    private List<SubjectType> subjectTypeList;
    @JoinColumn(name = "PARENT_ID", referencedColumnName = "ID")
    @ManyToOne
    private SubjectType parentId;
    @JoinColumn(name = "KIND_ID", referencedColumnName = "ID")
    @ManyToOne
    private Shared kindId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "subjectType")
    private List<Creditor> creditorList;

    public SubjectType() {
    }

    public SubjectType(BigDecimal id) {
        this.id = id;
    }

    public SubjectType(BigDecimal id, String code, String nameRu, Date openDate, short isLast) {
        this.id = id;
        this.code = code;
        this.nameRu = nameRu;
        this.openDate = openDate;
        this.isLast = isLast;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameKz() {
        return nameKz;
    }

    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public short getIsLast() {
        return isLast;
    }

    public void setIsLast(short isLast) {
        this.isLast = isLast;
    }

    public int getReportPeriodDurationMonths() {
        return reportPeriodDurationMonths;
    }

    public void setReportPeriodDurationMonths(int reportPeriodDurationMonths) {
        this.reportPeriodDurationMonths = reportPeriodDurationMonths;
    }

    @XmlTransient
    public List<SubjectType> getSubjectTypeList() {
        return subjectTypeList;
    }

    public void setSubjectTypeList(List<SubjectType> subjectTypeList) {
        this.subjectTypeList = subjectTypeList;
    }

    public SubjectType getParentId() {
        return parentId;
    }

    public void setParentId(SubjectType parentId) {
        this.parentId = parentId;
    }

    public Shared getKindId() {
        return kindId;
    }

    public void setKindId(Shared kindId) {
        this.kindId = kindId;
    }

    @XmlTransient
    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    public void setCreditorList(List<Creditor> creditorList) {
        this.creditorList = creditorList;
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
        if (!(object instanceof SubjectType)) {
            return false;
        }
        SubjectType other = (SubjectType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.portlets.crosscheck.dm.SubjectType[ id=" + id + " ]";
    }

}
