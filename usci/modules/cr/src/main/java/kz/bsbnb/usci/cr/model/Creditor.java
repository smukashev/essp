package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Creditor implements Serializable {
    private static final long serialVersionUID = 2115312865111267610L;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getShutdownDate() {
        return shutdownDate;
    }

    public void setShutdownDate(Date shutdownDate) {
        this.shutdownDate = shutdownDate;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getBIN() {
        return BIN;
    }

    public void setBIN(String BIN) {
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

    public Creditor getMainOffice() {
        return mainOffice;
    }

    public void setMainOffice(Creditor mainOffice) {
        this.mainOffice = mainOffice;
    }

    public List<Creditor> getBranchList() {
        return branchList;
    }

    public void setBranchList(List<Creditor> branchList) {
        this.branchList = branchList;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Creditor creditor = (Creditor) o;

        return getId().equals(creditor.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
