package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

/**
 * @author a.motov
 */
public interface IBaseEntityComplexValueDao extends IBaseEntityValueDao {

    public Set<Long> getChildBaseEntityIds(long parentBaseEntityId);

    public Set<Long> getChildBaseEntityIdsWithoutRefs(long parentBaseEntityId);

}
