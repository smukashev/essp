package kz.bsbnb.usci.eav.persistance.dao.pool;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IPersistableDao;

/**
 * @author alexandr.motov
 */
public interface IPersistableDaoPool {

    public IPersistableDao getPersistableDao(Class<? extends IPersistable> persistableClass);

    public <T extends IPersistableDao> T getPersistableDao(Class<? extends IPersistable> persistableClass,
                                                            Class<T> persistableDaoClass);

}
