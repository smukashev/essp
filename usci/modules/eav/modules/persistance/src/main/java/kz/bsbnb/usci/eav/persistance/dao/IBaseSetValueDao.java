package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseSet;

import java.util.Date;

public interface IBaseSetValueDao extends IBaseValueDao<IBaseSet> {
    Date getNextReportDate(long baseSetId, Date reportDate);

    Date getPreviousReportDate(long baseSetId, Date reportDate);
}
