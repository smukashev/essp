package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Date;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBaseEntityComplexValueDao extends IBaseEntityValueDao {

    public Set<Long> getChildBaseEntityIds(long parentBaseEntityId);

    public Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);

    public IBaseValue getNextBaseValue(IBaseValue baseValue, boolean currentReportDate);

    public IBaseValue getPreviousBaseValue(IBaseValue baseValue, boolean currentReportDate);

    public IBaseValue getLastBaseValue(IBaseValue baseValue, boolean currentReportDate);

}
