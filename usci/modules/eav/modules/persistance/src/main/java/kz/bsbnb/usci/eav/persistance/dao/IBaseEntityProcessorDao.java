package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IBaseEntityProcessorDao {
    long search(IBaseEntity baseEntity);

    List<Long> search(long metaClassId);

    List<Long> search(String className);

    IBaseEntity prepare(IBaseEntity baseEntity);

    IBaseEntity process(IBaseEntity baseEntity);

    List<Long> getEntityIDsByMetaclass(long metaClassId);

    List<BaseEntity> getEntityByMetaclass(MetaClass meta);

    boolean isApproved(long id);

    boolean remove(long baseEntityId);

    Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);
}
