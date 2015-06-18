package kz.bsbnb.usci.brms.rulesvr.model;

import kz.bsbnb.usci.brms.rulesvr.persistable.IPersistable;

import java.util.Date;

/**
 * @author abukabayev
 */
public interface IBatchVersion  extends IPersistable
{
    public Date getOpenDate();

    public void setOpenDate(Date reportDate);

    public long getPackageId();

    public void setPackageId(long packageId);

    public String getName();

    public void setName(String name);
}
