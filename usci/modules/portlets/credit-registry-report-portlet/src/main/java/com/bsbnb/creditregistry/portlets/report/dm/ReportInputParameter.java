package com.bsbnb.creditregistry.portlets.report.dm;

import com.bsbnb.creditregistry.portlets.report.ReportApplication;
import java.io.Serializable;
import java.util.Locale;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "REPORT_INPUT_PARAMETER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ReportInputParameter.findAll", query = "SELECT rip FROM ReportInputParameter rip"),
    @NamedQuery(name = "ReportInputParameter.findById", query = "SELECT rip FROM ReportInputParameter rip WHERE rip.id = :id")})
public class ReportInputParameter implements Serializable{
    public ReportInputParameter() {
        
    }
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "ID")
    private long id;
    @Basic(optional = false)
    @Column(name = "NAME_RU")
    private String nameRu;
    @Basic(optional = false)
    @Column(name="NAME_KZ") 
    private String nameKz;
    @Column(name="TYPE")
    private String type;
    @Column(name="PROCEDURE_NAME")
    private String procedureName;
    @Column(name="MINIMUM")
    private int minimum;
    @Column(name="MAXIMUM")
    private int maximum;
    @Column(name="NAME")
    private String parameterName;
    @Column(name="ORDER_NUMBER")
    private Integer orderNumber;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="REPORT_ID")
    private Report report;

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
     * @return the nameRu
     */
    public String getNameRu() {
        return nameRu;
    }

    /**
     * @param nameRu the nameRu to set
     */
    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    /**
     * @return the nameKz
     */
    public String getNameKz() {
        return nameKz;
    }

    /**
     * @param nameKz the nameKz to set
     */
    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the procedureName
     */
    public String getProcedureName() {
        return procedureName;
    }

    /**
     * @param procedureName the procedureName to set
     */
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    /**
     * @return the minimum
     */
    public int getMinimum() {
        return minimum;
    }

    /**
     * @param minimum the minimum to set
     */
    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    /**
     * @return the maximum
     */
    public int getMaximum() {
        return maximum;
    }

    /**
     * @param maximum the maximum to set
     */
    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * @param report the report to set
     */
    public void setReport(Report report) {
        this.report = report;
    }

    @Transient
    private ParameterType parameterType;
    
    /**
     * @return the parameterType
     */
    public ParameterType getParameterType() {
        if(parameterType==null) {
            parameterType = ParameterType.fromString(type);
        }
        return parameterType;
    }

    /**
     * @param parameterType the parameterType to set
     */
    public void setParameterType(ParameterType parameterType) {
        this.parameterType = parameterType;
    }
    
    public String getLocalizedName() {
        Locale locale = ReportApplication.getApplicationLocale();
        if(locale.getLanguage().equalsIgnoreCase("KZ")) {
            return nameKz;
        }
        return nameRu;
    }

    /**
     * @return the name
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * @param parameterName the name to set
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * @return the orderNumber
     */
    public Integer getOrderNumber() {
        return orderNumber;
    }

    /**
     * @param orderNumber the orderNumber to set
     */
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
}
