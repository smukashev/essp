package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;

import java.util.Date;
import java.util.Set;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
public interface IBaseEntityReportDateDao extends IPersistableDao {

    public IBaseEntityReportDate load(long baseEntityId, Date reportDate);

    public void deleteAll(long baseSetId);

    public Set<Date> getAvailableReportDates(long baseEntityId);

    public Date getMinReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId);

    public Date getMaxReportDate(long baseEntityId, Date reportDate);

}