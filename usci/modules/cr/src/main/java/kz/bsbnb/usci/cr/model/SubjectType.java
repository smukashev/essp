package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.List;

/**
 * Сущность для работы со справочником "Тип организации"
 * @author alexandr.motov
 */
public class SubjectType implements Serializable {
    private static final long serialVersionUID = 2115312865112267610L;

    private Long id;
    private String code;
    private String nameRu;
    private String nameKz;
    private Integer reportPeriodDurationMonths;
    private SubjectType parent;
        
    private List<SubjectType> childList;
    
    private Shared kind;

    private List<Creditor> creditorList;

    public SubjectType() {
    }

    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
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
     * @return the parent
     */
    public SubjectType getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(SubjectType parent) {
        this.parent = parent;
    }

    /**
     * @return the childList
     */
    public List<SubjectType> getChildList() {
        return childList;
    }

    /**
     * @param childList the childList to set
     */
    public void setChildList(List<SubjectType> childList) {
        this.childList = childList;
    }

    /**
     * @return the kind
     */
    public Shared getKind() {
        return kind;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(Shared kind) {
        this.kind = kind;
    }

    /**
     * @return the creditorList
     */
    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    /**
     * @param creditorList the creditorList to set
     */
    public void setCreditorList(List<Creditor> creditorList) {
        this.creditorList = creditorList;
    }

    /**
     * @return the reportPeriodDurationMonths
     */
    public Integer getReportPeriodDurationMonths() {
        return reportPeriodDurationMonths;
    }

    /**
     * @param reportPeriodDurationMonths the reportPeriodDurationMonths to set
     */
    public void setReportPeriodDurationMonths(Integer reportPeriodDurationMonths) {
        this.reportPeriodDurationMonths = reportPeriodDurationMonths;
    }
    
    public String getStringIdentifier() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubjectType other = (SubjectType) obj;
        return id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
