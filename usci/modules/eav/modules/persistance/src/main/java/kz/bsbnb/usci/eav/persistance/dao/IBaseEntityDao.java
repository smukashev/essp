package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.util.Date;
import java.util.Set;

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

    public BaseEntity load(long id, Date reportDate);

    public BaseEntity prepare(BaseEntity baseEntity);

    public BaseEntity apply(BaseEntity baseEntity);

    public BaseEntity process(BaseEntity baseEntity);

    public long saveOrUpdate(BaseEntity baseEntity);

    public void update(BaseEntity baseEntityForSave, BaseEntity baseEntityLoaded);

    public boolean isUsed(long baseEntityId);

    public Set<Date> getAvailableReportDates(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId);

}
