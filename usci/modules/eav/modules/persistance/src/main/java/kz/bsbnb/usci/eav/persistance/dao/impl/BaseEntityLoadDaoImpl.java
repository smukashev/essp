package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class BaseEntityLoadDaoImpl implements IBaseEntityLoadDao {
    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    public IBaseEntity loadByMaxReportDate(long id, Date savingReportDate) {
        if (id <= 0L || savingReportDate == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E102));

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, savingReportDate);

        if (maxReportDate == null)
            throw new RuntimeException(Errors.getMessage(Errors.E103, id, DataUtils.dateFormatDot.format(savingReportDate)));

        return load(id, maxReportDate);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date savingReportDate) {
        if (id <= 0L || savingReportDate == null)
            throw new IllegalStateException(Errors.getMessage(Errors.E102));

        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, savingReportDate);

        if (minReportDate == null)
            throw new RuntimeException(Errors.getMessage(Errors.E103, id, DataUtils.dateFormatDot.format(savingReportDate)));

        return load(id, minReportDate);
    }

    @Override
    public IBaseEntity load(long id) {
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);

        if (maxReportDate == null)
            throw new UnsupportedOperationException(Errors.getMessage(Errors.E101, id));

        if (baseEntityDao.isDeleted(id))
            return null;

        return load(id, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date reportDate) {
        return baseEntityDao.load(id, reportDate);
    }
}
