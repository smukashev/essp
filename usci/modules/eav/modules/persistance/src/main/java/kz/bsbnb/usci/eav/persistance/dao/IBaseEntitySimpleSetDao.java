package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

/**
 * @author alexandr.motov
 */
public interface IBaseEntitySimpleSetDao extends IBaseEntityValueDao {

    public Set<Long> getChildBaseSetIds(long parentBaseEntityId);

}
