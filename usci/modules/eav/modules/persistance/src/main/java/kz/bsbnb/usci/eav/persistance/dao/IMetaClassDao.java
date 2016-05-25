package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.jooq.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IMetaClassDao extends IDao<MetaClass> {
    MetaClass load(String className);

    MetaClass loadDisabled(String className);

    MetaClass load(String className, Date beginDate);

    List<MetaClass> loadAll();

    long save(MetaClass meta);

    List<MetaClassName> getMetaClassesNames();

    List<MetaClassName> getRefNames();

    List<Long> loadContaining(long id);

    void remove(MetaClass metaClass);

    List<Map<String, Object>> getSimpleResult(long metaId);

    Select getSimpleSelect(long metaId);
}
