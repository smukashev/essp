package kz.bsbnb.usci.eav.persistance.dao.pool.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PersistableDaoPool implements IPersistableDaoPool {
    @Autowired
    private ApplicationContext applicationContext;

    private Map<Class<? extends IPersistable>, Class<? extends IPersistableDao>> persistableDaoMap =
            new HashMap<>();

    public PersistableDaoPool() {
        persistableDaoMap.put(BaseEntity.class, IBaseEntityDao.class);
        persistableDaoMap.put(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        persistableDaoMap.put(BaseEntityBooleanValue.class, IBaseEntityBooleanValueDao.class);
        persistableDaoMap.put(BaseEntityDateValue.class, IBaseEntityDateValueDao.class);
        persistableDaoMap.put(BaseEntityDoubleValue.class, IBaseEntityDoubleValueDao.class);
        persistableDaoMap.put(BaseEntityIntegerValue.class, IBaseEntityIntegerValueDao.class);
        persistableDaoMap.put(BaseEntityStringValue.class, IBaseEntityStringValueDao.class);
        persistableDaoMap.put(BaseEntityComplexValue.class, IBaseEntityComplexValueDao.class);
        persistableDaoMap.put(BaseSet.class, IBaseSetDao.class);
        persistableDaoMap.put(BaseEntitySimpleSet.class, IBaseEntitySimpleSetDao.class);
        persistableDaoMap.put(BaseEntityComplexSet.class, IBaseEntityComplexSetDao.class);
        persistableDaoMap.put(BaseSetBooleanValue.class, IBaseSetBooleanValueDao.class);
        persistableDaoMap.put(BaseSetDateValue.class, IBaseSetDateValueDao.class);
        persistableDaoMap.put(BaseSetDoubleValue.class, IBaseSetDoubleValueDao.class);
        persistableDaoMap.put(BaseSetIntegerValue.class, IBaseSetIntegerValueDao.class);
        persistableDaoMap.put(BaseSetStringValue.class, IBaseSetStringValueDao.class);
        persistableDaoMap.put(BaseSetComplexValue.class, IBaseSetComplexValueDao.class);
    }

    public IPersistableDao getPersistableDao(Class<? extends IPersistable> persistableClass) {
        if (persistableClass == null)
            throw new RuntimeException(Errors.getMessage(Errors.E172));

        Class<? extends IPersistableDao> persistableDaoClass = persistableDaoMap.get(persistableClass);

        if (persistableDaoClass != null)
            return applicationContext.getBean(persistableDaoClass);

        throw new RuntimeException(Errors.getMessage(Errors.E173, persistableClass.getName()));
    }

    @SuppressWarnings("unchecked")
    public <T extends IPersistableDao> T getPersistableDao(Class<? extends IPersistable> persistableClass,
                                                           Class<T> extendedPersistableDaoClass) {
        if (persistableClass == null)
            throw new RuntimeException(Errors.getMessage(Errors.E172));

        Class<? extends IPersistableDao> persistableDaoClass = persistableDaoMap.get(persistableClass);

        if (persistableDaoClass != null && extendedPersistableDaoClass.isAssignableFrom(persistableDaoClass))
            return (T) applicationContext.getBean(persistableDaoClass);

        throw new RuntimeException(Errors.getMessage(Errors.E173, persistableClass.getName()));
    }

}
