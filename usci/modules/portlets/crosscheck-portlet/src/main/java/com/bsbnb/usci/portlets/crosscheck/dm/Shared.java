package com.bsbnb.usci.portlets.crosscheck.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "R_REF_SHARED#")
public class Shared implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigDecimal id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "CODE")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 256)
    @Column(name = "NAME_RU")
    private String nameRu;
    @Size(max = 256)
    @Column(name = "NAME_KZ")
    private String nameKz;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "TYPE_")
    private String type;
    @Size(max = 500)
    @Column(name = "NOTE")
    private String note;
    @Column(name = "ORDER_NUM")
    private BigInteger orderNum;
    @OneToMany(mappedBy = "kindId")
    private List<SubjectType> subjectTypeList;

    public Shared() {
    }

    public Shared(BigDecimal id) {
        this.id = id;
    }

    public Shared(BigDecimal id, String code, String nameRu, String type) {
        this.id = id;
        this.code = code;
        this.nameRu = nameRu;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigInteger getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(BigInteger orderNum) {
        this.orderNum = orderNum;
    }

    @XmlTransient
    public List<SubjectType> getSubjectTypeList() {
        return subjectTypeList;
    }

    public void setSubjectTypeList(List<SubjectType> subjectTypeList) {
        this.subjectTypeList = subjectTypeList;
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
        if (!(object instanceof Shared)) {
            return false;
        }
        Shared other = (Shared) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Shared[ id=" + id + " ]";
    }

}
