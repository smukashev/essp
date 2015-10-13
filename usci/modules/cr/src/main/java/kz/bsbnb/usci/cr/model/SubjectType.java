package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.List;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameKz() {
        return nameKz;
    }

    public void setNameKz(String nameKz) {
        this.nameKz = nameKz;
    }

    public SubjectType getParent() {
        return parent;
    }

    public void setParent(SubjectType parent) {
        this.parent = parent;
    }

    public List<SubjectType> getChildList() {
        return childList;
    }

    public void setChildList(List<SubjectType> childList) {
        this.childList = childList;
    }

    public Shared getKind() {
        return kind;
    }

    public void setKind(Shared kind) {
        this.kind = kind;
    }

    public List<Creditor> getCreditorList() {
        return creditorList;
    }

    public void setCreditorList(List<Creditor> creditorList) {
        this.creditorList = creditorList;
    }

    public Integer getReportPeriodDurationMonths() {
        return reportPeriodDurationMonths;
    }

    public void setReportPeriodDurationMonths(Integer reportPeriodDurationMonths) {
        this.reportPeriodDurationMonths = reportPeriodDurationMonths;
    }
    
    public String getStringIdentifier() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final SubjectType other = (SubjectType) obj;

        return id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
