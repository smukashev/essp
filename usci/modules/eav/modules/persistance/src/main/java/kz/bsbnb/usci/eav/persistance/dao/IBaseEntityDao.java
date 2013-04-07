package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

/**
 *
 * @author a.motov
 * @since 1.0
 * @version 1.0
 */
public interface IBaseEntityDao extends IDao<BaseEntity>
{

    /**
     * Search BaseEntity on key fields in the DB. In case
     * if the search found more than one instance, it will
     * return the first in the list. If the search has not
     * been found a single instance, it returns a null value.
     *
     * @param baseEntity instance of the BaseEntity for search
     * @return obtained instance of the BaseEntity by the search.
     * @since 1.0
     */
    public BaseEntity search(BaseEntity baseEntity);

    public long saveOrUpdate(BaseEntity baseEntity);

    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);

    public boolean isUsed(long baseEntityId);

}
