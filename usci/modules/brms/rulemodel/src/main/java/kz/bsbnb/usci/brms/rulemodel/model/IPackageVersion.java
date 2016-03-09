package kz.bsbnb.usci.brms.rulemodel.model;

import kz.bsbnb.usci.brms.rulemodel.persistable.IPersistable;

import java.util.Date;

/**
 * @author abukabayev
 */
public interface IPackageVersion extends IPersistable
{
    public Date getOpenDate();

    public void setOpenDate(Date reportDate);

    public long getPackageId();

    public void setPackageId(long packageId);

    public String getName();

    public void setName(String name);
}
