package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;
import java.util.Set;

public interface IBaseEntityProcessorDao {
    long search(IBaseEntity baseEntity);

    IBaseEntity prepare(IBaseEntity baseEntity);

    IBaseEntity process(IBaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long metaClassId);

    List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    boolean isApproved(long id);

    boolean remove(long baseEntityId);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);
}
