package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Date;

public interface IBaseValueDao<T extends IBaseContainer> extends IPersistableDao {
    IBaseValue getNextBaseValue(IBaseValue baseValue);

    IBaseValue getPreviousBaseValue(IBaseValue baseValue);

    IBaseValue getClosedBaseValue(IBaseValue baseValue);

    IBaseValue getLastBaseValue(IBaseValue baseValue);

    IBaseValue getExistingBaseValue(IBaseValue baseValue);

    void loadBaseValues(T baseContainer, Date existingReportDate, Date savingReportDate);

    void deleteAll(long baseContainerId);
}
