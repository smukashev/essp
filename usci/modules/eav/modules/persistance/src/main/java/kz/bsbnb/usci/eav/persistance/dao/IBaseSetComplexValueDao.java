package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

/**
 *
 */
public interface IBaseSetComplexValueDao extends IBaseSetValueDao {

    public Set<Long> getChildBaseEntityIds(long baseSetId);

}
