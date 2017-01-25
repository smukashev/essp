package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityReportDateDao;
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
            throw new IllegalStateException(Errors.compose(Errors.E102));

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, savingReportDate);

        if (maxReportDate == null)
            throw new RuntimeException(Errors.compose(Errors.E103, id, DataTypes.formatDate(savingReportDate)));

        return load(id, maxReportDate, savingReportDate);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date savingReportDate) {
        if (id <= 0L || savingReportDate == null)
            throw new IllegalStateException(Errors.compose(Errors.E102));

        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, savingReportDate);

        if (minReportDate == null)
            throw new RuntimeException(Errors.compose(Errors.E103, id, DataTypes.formatDate(savingReportDate)));

        return load(id, minReportDate, savingReportDate);
    }

    @Override
    public IBaseEntity load(long id) {
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);

        if (maxReportDate == null)
            throw new UnsupportedOperationException(Errors.compose(Errors.E101, id));

        return load(id, maxReportDate, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date existingReportDate, Date savingReportDate) {
        return baseEntityDao.load(id, existingReportDate, savingReportDate);
    }
}
