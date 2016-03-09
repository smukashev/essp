package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IPackageVersion;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.Date;

/**
 * @author abukabayev
 */
public class PackageVersion extends Persistable implements IPackageVersion {
    private Date reportDate;
    private long packageId;
    private String packageName;

    public PackageVersion() {

    }

    public PackageVersion(String packageName, Date date) {
        this.reportDate = date;
        this.packageName = packageName;
    }

    public PackageVersion(Date repDate, long package_id){
       this.reportDate = repDate;
       this.packageId = package_id;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setName(String name) {
        this.packageName = name;
    }

    @Override
    public String toString() {
        return "id: " + id + ", packageName: " + packageName + ", package_id: " + packageId;
    }
}
