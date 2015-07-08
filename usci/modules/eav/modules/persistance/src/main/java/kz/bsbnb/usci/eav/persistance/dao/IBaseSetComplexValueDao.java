package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Set;

public interface IBaseSetComplexValueDao extends IBaseSetValueDao {
    Set<Long> getChildBaseEntityIds(long baseSetId);

    IBaseValue getNextBaseValue(IBaseValue baseValue, boolean currentReportDate);

    IBaseValue getPreviousBaseValue(IBaseValue baseValue, boolean currentReportDate);

    IBaseValue getLastBaseValue(IBaseValue baseValue, boolean currentReportDate);

    boolean isSingleBaseValue(IBaseValue baseValue);
}
