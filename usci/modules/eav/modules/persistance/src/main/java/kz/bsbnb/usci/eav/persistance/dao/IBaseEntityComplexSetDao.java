package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

public interface IBaseEntityComplexSetDao extends IBaseEntityValueDao {
    Set<Long> getChildBaseSetIds(long parentBaseEntityId);

    Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);
}
