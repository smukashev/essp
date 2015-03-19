package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

//import com.bsbnb.creditregistry.dm.Credit;
//import com.bsbnb.creditregistry.dm.maintenance.PortalUser;

/**
 * Сущность для работы со справочником "БВУ/НО"
 * @author alexandr.motov
 */
public class Creditor implements Serializable {
    private Long id;
    private String name;
    private String shortName;
    private String code;
    private Date shutdownDate;
    private Date changeDate;
    private String BIN;
    private String RNN;
    private String BIK;

    private Creditor mainOffice;

    private List<Creditor> branchList;

    private SubjectType subjectType;

    public Creditor() {
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

    /*
     * Method only takes ID into account. Should be reimplemented if one needs to consider other fields.
     */
    @Override
    public boolean equals(Object another) {
        if(another==null) {
            return false;
        }
        if(another==this) {
            return true;
        }
        if(!(another instanceof Creditor)) {
            return false;
        }
        Creditor anotherCreditor = (Creditor) another;
        return id == anotherCreditor.getId();
    }
    
    /*
     * Method only takes ID into account. Should be reimplemented if one needs to consider other fields.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

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

    public String getBIN()
    {
        return BIN;
    }

    public void setBIN(String BIN)
    {
        this.BIN = BIN;
    }

    public String getRNN() {
        return RNN;
    }

    public void setRNN(String RNN) {
        this.RNN = RNN;
    }

    public String getBIK() {
        return BIK;
    }

    public void setBIK(String BIK) {
        this.BIK = BIK;
    }

    @Override
    public String toString() {
        return "Creditor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", BIN='" + BIN + '\'' +
                ", RNN='" + RNN + '\'' +
                '}';
    }
}
