package kz.bsbnb.usci.brms.rulesvr.model.impl;

import kz.bsbnb.usci.brms.rulesvr.model.IBatchVersion;
import kz.bsbnb.usci.brms.rulesvr.persistable.impl.Persistable;
import java.util.Date;

/**
 * @author abukabayev
 */
public class BatchVersion extends Persistable implements IBatchVersion {
    private Date report_date;
    private long package_id;
    private String name;

    public BatchVersion(){

    }

    public BatchVersion(Date repDate,long package_id){
       this.report_date = repDate;
       this.package_id = package_id;
    }

    public Date getReport_date() {
        return report_date;
    }

    public void setReport_date(Date report_date) {
        this.report_date = report_date;
    }

    public long getPackage_id() {
        return package_id;
    }

    public void setPackage_id(long package_id) {
        this.package_id = package_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "id: " + id + ", name: " + name + ", package_id: " + package_id;
    }
}
