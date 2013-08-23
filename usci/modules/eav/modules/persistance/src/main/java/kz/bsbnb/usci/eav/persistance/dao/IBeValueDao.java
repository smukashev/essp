package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.sql.Date;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeValueDao {

    long save(IBaseEntity baseEntity, String attribute);

    void save(IBaseEntity baseEntity, Set<String> attributes);

    void update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute);

    void remove(IBaseEntity baseEntity, String attribute);

}
