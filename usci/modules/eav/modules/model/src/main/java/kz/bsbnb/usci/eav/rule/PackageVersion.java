package kz.bsbnb.usci.eav.rule;

import java.io.Serializable;
import java.util.Date;

/**
 * @author abukabayev
 */
public class PackageVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date reportDate;

    RulePackage rulePackage;

    public PackageVersion() {
        super();
    }

    public PackageVersion(RulePackage rulePackage, Date date) {
        this.reportDate = date;
        this.rulePackage = rulePackage;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public RulePackage getRulePackage() {
        return rulePackage;
    }

    public void setRulePackage(RulePackage rulePackage) {
        this.rulePackage = rulePackage;
    }
}
