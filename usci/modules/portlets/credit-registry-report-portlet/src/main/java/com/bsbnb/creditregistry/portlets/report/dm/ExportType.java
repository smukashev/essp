/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Aidar.Myrzahanov
 */
@XmlRootElement
public class ExportType implements Serializable {
    public static final String TABLE_VAADIN = "TABLE_VAADIN";
    public static final String JASPER_XLS = "JASPER_XLS";
    public static final String TEMPLATE_XLS = "TEMPLATE_XLS";
    
    private static final long serialVersionUID = 1L;
    private BigDecimal id;
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
