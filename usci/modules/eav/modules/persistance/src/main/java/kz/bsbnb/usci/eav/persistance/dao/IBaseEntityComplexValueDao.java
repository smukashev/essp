package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Set;

public interface IBaseEntityComplexValueDao extends IBaseEntityValueDao {
    Set<Long> getChildBaseEntityIds(long parentBaseEntityId);

    Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);
}
