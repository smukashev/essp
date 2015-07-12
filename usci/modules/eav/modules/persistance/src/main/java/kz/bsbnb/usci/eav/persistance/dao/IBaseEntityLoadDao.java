package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;

public interface IBaseEntityLoadDao {
    IBaseEntity loadByMaxReportDate(long id, Date reportDate);

    IBaseEntity loadByMaxReportDate(long id, Date reportDate, boolean caching);

    IBaseEntity loadByMinReportDate(long id, Date reportDate);

    IBaseEntity loadByMinReportDate(long id, Date reportDate, boolean caching);

    IBaseEntity loadByReportDate(long id, Date actualReportDate, boolean caching);

    IBaseEntity loadByReportDate(long id, Date actualReportDate);

    IBaseEntity load(long id);

    IBaseEntity load(long id, boolean caching);

    IBaseEntity load(long id, Date maxReportDate, Date actualReportDate);

    IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching);

}
