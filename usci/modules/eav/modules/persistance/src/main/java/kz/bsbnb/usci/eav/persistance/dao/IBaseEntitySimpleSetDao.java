package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
public interface IBaseEntitySimpleSetDao extends IBaseEntityValueDao {

    public Set<Long> getChildBaseSetIds(long parentBaseEntityId);

}
