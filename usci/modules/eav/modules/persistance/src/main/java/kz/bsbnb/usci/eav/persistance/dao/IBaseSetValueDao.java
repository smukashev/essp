package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseSet;

import java.util.Date;

/**
 * @author alexandr.motov
 */
public interface IBaseSetValueDao extends IBaseValueDao<IBaseSet> {

    public Date getNextReportDate(long baseSetId, Date reportDate);

    public Date getPreviousReportDate(long baseSetId, Date reportDate);

}
