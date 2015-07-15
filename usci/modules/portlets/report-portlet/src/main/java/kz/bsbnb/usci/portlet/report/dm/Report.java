package kz.bsbnb.usci.portlet.report.dm;

import kz.bsbnb.usci.portlet.report.ReportApplication;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

;

/**
 *
 * @author Aidar.Myrzahanov
 */
@Entity
@Table(name = "REPORT")
@NamedQueries({
    @NamedQuery(name = "Report.findAll", query = "SELECT r FROM Report r"),
    @NamedQuery(name = "Report.findById", query = "SELECT r FROM Report r WHERE r.id = :id")})
public class Report implements Serializable {

    public Report() {
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
    @Column(name = "NAME_KZ")
    private String nameKz;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PROCEDURE_NAME")
    private String procedureName;
    @Column(name="TYPE")
    private String type;
    @Column(name="ORDER_NUMBER") 
    private int orderNumber;
    @OneToMany(targetEntity = ReportInputParameter.class, cascade = CascadeType.ALL, mappedBy = "report")
    @OrderBy(value="orderNumber")
    private Set<ReportInputParameter> inputParameters;
    
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name="REPORT_EXPORT_TYPE",
               joinColumns=@JoinColumn(name="REPORT_ID"),
               inverseJoinColumns=@JoinColumn(name="EXPORT_TYPE_ID"))
        private List<ExportType> exportTypesList;

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
     * @return the fileName
     */
    public String getName() {
        return name;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setName(String fileName) {
        this.name = fileName;
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
     * @return the inputParameters
     */
    public List<ReportInputParameter> getInputParameters() {
        return new ArrayList<ReportInputParameter>(inputParameters);
    }

    /**
     * @param inputParameters the inputParameters to set
     */
    public void setInputParameters(List<ReportInputParameter> inputParameters) {
        this.inputParameters = new HashSet<ReportInputParameter>(inputParameters);
    }

    public String getLocalizedName() {
        Locale locale = ReportApplication.getApplicationLocale();
        if ("KZ".equalsIgnoreCase(locale.getLanguage())) {
            return getNameKz();
        }
        return getNameRu();
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof Report) {
            Report otherReport = (Report) other;
            return id==otherReport.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
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
     * @return the exportTypesList
     */
    public List<ExportType> getExportTypesList() {
        return exportTypesList;
    }


    public void setExportTypeList(List<ExportType> exportTypeList){ this.exportTypesList = exportTypeList;}

    /**
     * @return the orderNumber
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * @param orderNumber the orderNumber to set
     */
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
