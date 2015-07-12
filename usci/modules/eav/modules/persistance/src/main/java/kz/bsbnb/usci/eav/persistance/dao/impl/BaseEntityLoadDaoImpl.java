package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class BaseEntityLoadDaoImpl implements IBaseEntityLoadDao {
    @Autowired
    IPersistableDaoPool persistableDaoPool;

    public IBaseEntity loadByMaxReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (maxReportDate == null)
            throw new RuntimeException("No data found on report date " + actualReportDate + ".");

        return load(id, maxReportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity loadByMaxReportDate(long id, Date reportDate) {
        return loadByMaxReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date reportDate) {
        return loadByMinReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, actualReportDate);
        if (minReportDate == null)
            throw new RuntimeException("No data found on report date " + actualReportDate + ".");

        return load(id, minReportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity loadByReportDate(long id, Date reportDate) {
        return loadByReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date reportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (reportDate == null) {
            reportDate = baseEntityReportDateDao.getMinReportDate(id, actualReportDate);
            if (reportDate == null)
                throw new RuntimeException("No data found on report date " + actualReportDate + ".");
        }

        return load(id, reportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity load(long id) {
        return load(id, false);
    }

    @Override
    public IBaseEntity load(long id, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        if (maxReportDate == null)
            throw new UnsupportedOperationException("Not found appropriate report date.");

        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        if (baseEntityDao.isDeleted(id))
            return baseEntityDao.load(id);

        /*
        if (caching)
            return baseEntityCacheDao.getBaseEntity(id, maxReportDate);
        */

        return load(id, maxReportDate, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching) {
        /*
        if (caching)
            return baseEntityCacheDao.getBaseEntity(id, actualReportDate);
        */

        return load(id, maxReportDate, actualReportDate);
    }

    public IBaseEntity load(long id, Date reportDate, Date actualReportDate) {
        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.load(id, reportDate, actualReportDate);
    }
}
