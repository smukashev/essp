package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class BaseEntityLoadDaoImpl implements IBaseEntityLoadDao, InitializingBean {
    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Value("${refs.cache.enabled}")
    private boolean isReferenceCacheEnabled;

    @Autowired
    IRefRepository refRepositoryDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isReferenceCacheEnabled)
            refRepositoryDao.fillRefRepository();
    }

    public IBaseEntity loadByMaxReportDate(long id, Date savingReportDate) {
        IBaseEntity loadedEntity;
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, savingReportDate);
        if (maxReportDate == null)
            throw new RuntimeException("В базе нет данных для сущности(" + id + ") до отчетной даты(включительно): "
                    + savingReportDate + ";");

        if (isReferenceCacheEnabled) {
            loadedEntity = load(id, maxReportDate, savingReportDate);
        } else {
            loadedEntity = refRepositoryDao.getRef(id, maxReportDate);
            if (loadedEntity == null) {
                loadedEntity = load(id, maxReportDate, savingReportDate);
                if (loadedEntity.getMeta().isReference())
                    refRepositoryDao.setRef(id, savingReportDate, loadedEntity);
            }
        }

        return loadedEntity;
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date savingReportDate) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, savingReportDate);
        if (minReportDate == null)
            throw new RuntimeException("В базе нет данных для сущности(" + id + ") после отчетной даты(включительно): "
                    + savingReportDate + ";");

        return load(id, minReportDate, savingReportDate);
    }

    @Override
    public IBaseEntity load(long id) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        if (maxReportDate == null)
            throw new UnsupportedOperationException("В базе отсутсвует отчетная дата на ID: " + id + ";");

        IBaseEntityDao baseEntityDao = persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        if (baseEntityDao.isDeleted(id))
            return null;

        return load(id, maxReportDate, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date reportDate, Date savingReportDate) {
        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);

        return baseEntityDao.load(id, reportDate, savingReportDate);
    }
}
