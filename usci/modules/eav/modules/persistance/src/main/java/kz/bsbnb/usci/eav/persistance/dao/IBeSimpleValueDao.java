package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeSimpleValueDao extends IBeValueDao {

    void save(BaseEntity baseEntity, Set<String> attributes, DataTypes dataType);

    void save(BaseEntity baseEntity, String attribute, DataTypes dataType);

}
