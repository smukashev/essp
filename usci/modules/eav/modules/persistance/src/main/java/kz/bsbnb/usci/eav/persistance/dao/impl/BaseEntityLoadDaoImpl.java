package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.Errors;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Repository
public class BaseEntityLoadDaoImpl implements IBaseEntityLoadDao, InitializingBean {
    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    @Value("${refs.cache.enabled}")
    private boolean isReferenceCacheEnabled;

    @Autowired
    private IRefRepository refRepositoryDao;

    private DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isReferenceCacheEnabled)
            refRepositoryDao.fillRefRepository();
    }

    public IBaseEntity loadByMaxReportDate(long id, Date savingReportDate) {
        if (id == 0L || savingReportDate == null)
            throw new IllegalStateException(Errors.E102 + "");

        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, savingReportDate);
        if (maxReportDate == null)
            throw new RuntimeException(Errors.E103 + "|" + id + "|" + df.format(savingReportDate));

        return load(id, maxReportDate, savingReportDate);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date savingReportDate) {
        if (id == 0L || savingReportDate == null)
            throw new IllegalStateException(Errors.E102 + "");

        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, savingReportDate);
        if (minReportDate == null)
            throw new RuntimeException(Errors.E103 + "|" + id + "|" + df.format(savingReportDate));

        return load(id, minReportDate, savingReportDate);
    }

    @Override
    public IBaseEntity load(long id) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        if (maxReportDate == null)
            throw new UnsupportedOperationException(Errors.E101 + "|" + id);

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
