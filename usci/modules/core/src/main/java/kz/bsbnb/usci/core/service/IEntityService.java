package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.stats.QueryEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author k.tulbassiyev
 */
public interface IEntityService {
    public BaseEntity load(long id);
    public void process(BaseEntity baseEntity);
    public BaseEntity search(BaseEntity baseEntity);
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad);
    public List<Long> getEntityIDsByMetaclass(long id);
    public List<RefListItem> getRefsByMetaclass(long metaClassId);
    public HashMap<String, QueryEntry> getSQLStats();
    public void clearSQLStats();
    public void remove(long id);
    public void removeAllByMetaClass(IMetaClass metaClass);
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds);
}
