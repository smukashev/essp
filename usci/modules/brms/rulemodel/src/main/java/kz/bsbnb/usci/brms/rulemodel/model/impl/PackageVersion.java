package kz.bsbnb.usci.brms.rulemodel.model.impl;

import kz.bsbnb.usci.brms.rulemodel.model.IPackageVersion;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.util.Date;

/**
 * @author abukabayev
 */
public class PackageVersion extends Persistable implements IPackageVersion {
    private Date openDate;
    private long packageId;
    private String name;

    public PackageVersion(){

    }

    public PackageVersion(Date repDate, long package_id){
       this.openDate = repDate;
       this.packageId = package_id;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "id: " + id + ", name: " + name + ", package_id: " + packageId;
    }
}
