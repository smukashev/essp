package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Date;

/**
 * Created by Alexandr.Motov on 20.03.14.
 */
public interface IBaseValueDao<T extends IBaseContainer> extends IPersistableDao {

    public IBaseValue getNextBaseValue(IBaseValue baseValue);

    public IBaseValue getPreviousBaseValue(IBaseValue baseValue);

    public IBaseValue getClosedBaseValue(IBaseValue baseValue);

    public IBaseValue getLastBaseValue(IBaseValue baseValue);

    public void loadBaseValues(T baseContainer, Date actualReportDate, boolean lastReportDate);

}
