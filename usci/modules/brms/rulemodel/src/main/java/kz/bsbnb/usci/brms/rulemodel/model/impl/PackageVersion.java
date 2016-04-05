package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IPackageVersion;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

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

    /*@Override
    public String toString() {
        return "id: " + id + ", packageName: " + packageName + ", package_id: " + packageId;
    }*/
}
