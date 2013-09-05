package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

import java.util.Map;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeValueDao {

    public IBaseValue save(IBaseEntity baseEntity, String attribute);

    public Map<String, IBaseValue> save(IBaseEntity baseEntity, Set<String> attributes);

    public IBaseValue update(IBaseEntity baseEntityLoaded, IBaseEntity baseEntityForSave, String attribute);

    void remove(IBaseEntity baseEntity, String attribute);

}
