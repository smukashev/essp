package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

public interface IBaseEntityComplexSetDao extends IBaseEntityValueDao {
    Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);
}
