package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.sql.Date;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeValueDao {

    void save(BaseEntity baseEntity, String attribute);

    void save(BaseEntity baseEntity, Set<String> attributes);

    void update(BaseEntity baseEntityLoaded, BaseEntity baseEntityForSave, String attribute);

    void remove(BaseEntity baseEntity, String attribute);

}
