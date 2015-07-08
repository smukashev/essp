package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;

import java.util.Date;
import java.util.Set;

public interface IBaseEntityReportDateDao extends IPersistableDao {
    IBaseEntityReportDate load(long baseEntityId, Date reportDate);

    void deleteAll(long baseSetId);

    Set<Date> getAvailableReportDates(long baseEntityId);

    Date getMinReportDate(long baseEntityId);

    Date getMinReportDate(long baseEntityId, Date reportDate);

    Date getMaxReportDate(long baseEntityId);

    Date getMaxReportDate(long baseEntityId, Date reportDate);

    boolean isLastReportDate(long baseEntityId, Date reportDate);

    Date getNextReportDate(long baseEntityId, Date reportDate);

    Date getPreviousReportDate(long baseEntityId, Date reportDate);

    boolean exists(long baseEntityId, Date reportDate);
}
