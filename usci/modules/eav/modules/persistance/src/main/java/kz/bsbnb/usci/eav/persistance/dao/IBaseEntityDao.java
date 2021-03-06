package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;

import java.util.Date;
import java.util.Set;

public interface IBaseEntityDao extends IPersistableDao {
    IBaseEntity loadMock(long id);

    IBaseEntity load(long id, Date reportDate, Date savingReportDate);

    IMetaClass getMetaClass(long baseEntityId);

    boolean deleteRecursive(long baseEntityId);

    boolean isUsed (long baseEntityId);
}