package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author a.motov
 */
public interface IBeValueDao {

    public void insert(IPersistable persistable);

    public void update(IPersistable persistable);

    public void delete(IPersistable persistable);

}
