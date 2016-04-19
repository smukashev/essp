package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

public interface IBaseEntityLoadDao {
    IBaseEntity loadByMaxReportDate(long id, Date savingReportDate);

    IBaseEntity loadByMinReportDate(long id, Date savingReportDate);

    IBaseEntity load(long id);

    IBaseEntity load(long id, Date existingReportDate, Date savingReportDate);
}
