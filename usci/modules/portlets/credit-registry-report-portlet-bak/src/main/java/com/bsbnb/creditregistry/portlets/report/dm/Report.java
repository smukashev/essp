package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

import static com.bsbnb.creditregistry.portlets.report.ReportApplication.getApplicationLocale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class Report implements Serializable {

    public Report() {
    }
    private static final long serialVersionUID = 1L;
    private long id;
    private String nameRu;
    private String nameKz;
    private String name;
    private String procedureName;
    private String type;
    private int orderNumber;
    private Set<ReportInputParameter> inputParameters = new HashSet<ReportInputParameter>();
    
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
        Locale locale = getApplicationLocale();
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
