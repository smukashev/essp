package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Set;

/**
 *
 */
public interface IBaseSetComplexValueDao extends IBaseSetValueDao {

    public Set<Long> getChildBaseEntityIds(long baseSetId);

    public IBaseValue getNextBaseValue(IBaseValue baseValue, boolean currentReportDate);

    public IBaseValue getPreviousBaseValue(IBaseValue baseValue, boolean currentReportDate);

    public IBaseValue getLastBaseValue(IBaseValue baseValue, boolean currentReportDate);

}
