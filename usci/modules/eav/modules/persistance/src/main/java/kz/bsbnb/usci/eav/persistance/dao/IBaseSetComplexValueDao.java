package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Set;

public interface IBaseSetComplexValueDao extends IBaseSetValueDao {
    Set<Long> getChildBaseEntityIds(long baseSetId);
}
