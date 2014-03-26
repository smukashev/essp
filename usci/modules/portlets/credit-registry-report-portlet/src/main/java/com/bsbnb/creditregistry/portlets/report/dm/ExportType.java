package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "EXPORT_TYPE")
@XmlRootElement
public class ExportType implements Serializable {
    public static final String TABLE_VAADIN = "TABLE_VAADIN";
    public static final String JASPER_XLS = "JASPER_XLS";
    public static final String TEMPLATE_XLS = "TEMPLATE_XLS";
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private BigDecimal id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "NAME")
    private String name;

    public ExportType() {
    }

    public ExportType(BigDecimal id) {
        this.id = id;
    }

    public ExportType(BigDecimal id, String name) {
        this.id = id;
        this.name = name;
    }

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExportType)) {
            return false;
        }
        ExportType other = (ExportType) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.bsbnb.creditregistry.portlets.report.dm.ExportType[ id=" + id + " ]";
    }
    
}
