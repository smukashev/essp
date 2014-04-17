package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

/**
 *
 */
public interface IBaseEntityComplexSetDao extends IBaseEntityValueDao {

    public Set<Long> getChildBaseSetIds(long parentBaseEntityId);

    public Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);

}
