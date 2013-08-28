package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeSimpleValueDao extends IBeValueDao {

    void save(IBaseEntity baseEntity, Set<String> attributes, DataTypes dataType);

    long save(IBaseEntity baseEntity, String attribute, DataTypes dataType);

}
