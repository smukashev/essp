package kz.bsbnb.usci.eav.persistance.dao.pool;

import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IPersistableDao;

public interface IPersistableDaoPool {
    IPersistableDao getPersistableDao(Class<? extends IPersistable> persistableClass);

    <T extends IPersistableDao> T getPersistableDao(Class<? extends IPersistable> persistableClass, Class<T> persistableDaoClass);
}
