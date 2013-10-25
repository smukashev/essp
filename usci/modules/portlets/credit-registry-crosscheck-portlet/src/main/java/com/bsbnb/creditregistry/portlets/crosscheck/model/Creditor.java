package com.bsbnb.creditregistry.portlets.crosscheck.model;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * Сущность для работы со справочником "БВУ/НО"
 *
 * @author alexandr.motov
 */
//@Entity
//@Table(name="CREDITOR", schema="REF")
//@SequenceGenerator(name="CREDITOR_SEQUENCE", sequenceName="CREDITOR_SEQ", schema="REF", initialValue=1000, allocationSize=1)
public class Creditor {

    private static final long serialVersionUID = 8437865055762496380L;
    //<editor-fold defaultstate="collapsed" desc="Simple fields">
//    @Id
//    @GeneratedValue(generator="CREDITOR_SEQUENCE", strategy=GenerationType.SEQUENCE)
//    @Column(name="ID", unique=true, nullable=false)
    private BigInteger id;
//    @Column(name="NAME", nullable=false)
    private String name;
//    @Column(name="SHORT_NAME")
    private String shortName;
//    @Column(name="CODE")
    private String code;
//    @Column(name="SHUTDOWN_DATE")
//    @Temporal(TemporalType.DATE)
    private Date shutdownDate;
//    @Column(name="CHANGE_DATE", nullable=false)
//    @Temporal(TemporalType.DATE)
    private Date changeDate;
    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Fields to relationships">
//    @JoinColumn(name="MAIN_OFFICE_ID", referencedColumnName="ID")
//    @ManyToOne(fetch=FetchType.LAZY)
    private Creditor mainOffice;
    private SubjectType subjectType;
//
//    @OneToMany(mappedBy="mainOffice", fetch=FetchType.LAZY)
    private List<Creditor> branchList;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
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
     * @return the shutdownDate
     */
    public Date getShutdownDate() {
        return shutdownDate;
    }

    /**
     * @param shutdownDate the shutdownDate to set
     */
    public void setShutdownDate(Date shutdownDate) {
        this.shutdownDate = shutdownDate;
    }

    /**
     * @return the changeDate
     */
    public Date getChangeDate() {
        return changeDate;
    }

    /**
     * @param changeDate the changeDate to set
     */
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    /**
     * @return the mainOffice
     */
    public Creditor getMainOffice() {
        return mainOffice;
    }

    /**
     * @param mainOffice the mainOffice to set
     */
    public void setMainOffice(Creditor mainOffice) {
        this.mainOffice = mainOffice;
    }

    /**
     * @return the branchList
     */
    public List<Creditor> getBranchList() {
        return branchList;
    }

    /**
     * @param branchList the branchList to set
     */
    public void setBranchList(List<Creditor> branchList) {
        this.branchList = branchList;
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
        if (getMainOffice() != null) {
            sb.append(", mainOffice={id=");
            sb.append(getMainOffice().getId());
            sb.append("}");
        } else {
            sb.append(", mainOffice=null");
        }
        sb.append(", code=");
        sb.append(getCode());
        sb.append(", name=");
        sb.append(getName());
        sb.append(", shortName=");
        sb.append(getShortName());
        sb.append("}");

        return sb.toString();
    }

    /*
     * Method only takes ID into account. Should be reimplemented if one needs to consider other fields.
     */
    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (another == this) {
            return true;
        }
        if (!(another instanceof Creditor)) {
            return false;
        }
        if (id == null) {
            return false;
        }
        Creditor anotherCreditor = (Creditor) another;
        return id.equals(anotherCreditor.getId());
    }

    /*
     * Method only takes ID into account. Should be reimplemented if one needs to consider other fields.
     */
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="toXMLString">
    public String toXmlString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<model><model-name>");
        sb.append(Creditor.class.getName());
        sb.append("</model-name>");

        sb.append("<column><column-name>id</column-name><column-value><![CDATA[");
        sb.append(getId());
        sb.append("]]></column-value></column>");

        sb.append("<column><column-name>code</column-name><column-value><![CDATA[");
        sb.append(getCode());
        sb.append("]]></column-value></column>");

        sb.append("<column><column-name>name</column-name><column-value><![CDATA[");
        sb.append(getName());
        sb.append("]]></column-value></column>");

        sb.append("<column><column-name>shortName</column-name><column-value><![CDATA[");
        sb.append(getShortName());
        sb.append("]]></column-value></column>");

        sb.append("<column><column-name>shutdownDate</column-name><column-value><![CDATA[");
        sb.append(getShutdownDate());
        sb.append("]]></column-value></column>");

        sb.append("</model>");

        return sb.toString();
    }
    //</editor-fold>

    /**
     * @return the subjectType
     */
    public SubjectType getSubjectType() {
        return subjectType;
    }

    /**
     * @param subjectType the subjectType to set
     */
    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }
}
