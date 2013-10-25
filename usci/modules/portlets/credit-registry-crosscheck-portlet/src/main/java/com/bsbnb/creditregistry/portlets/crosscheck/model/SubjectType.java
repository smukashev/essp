package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Сущность для работы со справочником "Тип организации"
 * @author alexandr.motov
 */
/*
@Entity
@Table(name="SUBJECT_TYPE",schema="REF")
@SequenceGenerator(name="SUBJECT_TYPE_SEQUENCE", sequenceName="SUBJECT_TYPE_SEQ", schema="REF", allocationSize=1, initialValue=1000)
*/
public class SubjectType  {
    private static final long serialVersionUID = 2115312865112267609L;

    //<editor-fold defaultstate="collapsed" desc="Simple fields">
//    @Id
//    @GeneratedValue(generator="SUBJECT_TYPE_SEQUENCE", strategy=GenerationType.SEQUENCE)
//    @Column(name="ID", unique=true, nullable=false)
    private BigInteger id;
//    @Column(name="CODE")
    private String code;
//    @Column(name="NAME_RU")
    private String nameRu;
//    @Column(name="NAME_KZ")
    private String nameKz;
//    @Column(name="REPORT_PERIOD_DURATION_MONTHS")	
    private Integer reportPeriodDurationMonths;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fields to relationship">
//    @JoinColumn(name="PARENT_ID", referencedColumnName="ID")
//    @ManyToOne(fetch=FetchType.LAZY)
    private SubjectType parent;
        
//    @OneToMany(mappedBy="parent", fetch=FetchType.EAGER)
    private List<SubjectType> childList;
    
//    @JoinColumn(name="KIND_ID", referencedColumnName="ID")
//    @ManyToOne(fetch=FetchType.EAGER)
    private Shared kind;

//    @OneToMany(mappedBy="subjectType", fetch=FetchType.LAZY)
    private List<Creditor> creditorList;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    /**
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(BigInteger id) {
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="toString, hashCode, equals">
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{id=");
        sb.append(getId());
        
        if (getParent() != null) {
            sb.append(", parent={id=");
            sb.append(getParent().getId());
            sb.append("}");
        } else 
            sb.append(", parent=null");
        
        if (getKind() != null) {
            sb.append(", kind={id=");
            sb.append(getKind().getId());
            sb.append("}");
        } else 
            sb.append(", kind=null");
        
        sb.append(", code=");
        sb.append(getCode());
        sb.append(", nameRu=");
        sb.append(getNameRu());
        sb.append(", nameKz=");
        sb.append(getNameKz());
        sb.append(", reportPeriodDurationMonths=");
        sb.append(getReportPeriodDurationMonths());
        sb.append("}");

        return sb.toString();
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
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    //</editor-fold>
    
}
