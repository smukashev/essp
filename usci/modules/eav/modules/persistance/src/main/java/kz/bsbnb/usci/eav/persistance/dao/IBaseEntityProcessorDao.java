package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;
import java.util.Set;

public interface IBaseEntityProcessorDao {
    long search(IBaseEntity baseEntity, long creditorId);

    IBaseEntity prepare(IBaseEntity baseEntity, long creditorId);

    IBaseEntity process(IBaseEntity baseEntity);

    List<Long> getEntityIDsByMetaClass(long metaClassId);

    List<BaseEntity> getEntityByMetaClass(MetaClass meta);

    boolean isApproved(long creditorId);

    boolean remove(long baseEntityId);

    IBaseEntityLoadDao getBaseEntityLoadDao();
}
