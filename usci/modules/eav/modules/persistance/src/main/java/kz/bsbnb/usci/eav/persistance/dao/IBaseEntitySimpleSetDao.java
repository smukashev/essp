package kz.bsbnb.usci.eav.persistance.dao;

import java.util.Set;

public interface IBaseEntitySimpleSetDao extends IBaseEntityValueDao {
    Set<Long> getChildBaseSetIds(long parentBaseEntityId);
}
