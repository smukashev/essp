package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;

/**
 *
 */
public interface IBeSetValueDao extends IBeValueDao {

    public long save(IBaseSet baseSet);

    public void remove(IBaseSet baseSet);

}