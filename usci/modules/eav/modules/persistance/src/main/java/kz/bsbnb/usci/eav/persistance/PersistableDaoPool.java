package kz.bsbnb.usci.eav.persistance;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alexandr.Motov on 18.03.14.
 */
@Repository
public class PersistableDaoPool implements IPersistableDaoPool {

    @Autowired
    private ApplicationContext applicationContext;

    private Map<Class<? extends IPersistable>, Class<? extends IPersistableDao>> persistableDaoMap =
            new HashMap<Class<? extends IPersistable>, Class<? extends IPersistableDao>>();

    public PersistableDaoPool() {
        persistableDaoMap.put(BaseEntity.class, IBeEntityDao.class);
        persistableDaoMap.put(BaseEntityReportDate.class, IBeEntityReportDateDao.class);
        persistableDaoMap.put(BaseEntityBooleanValue.class, IBeBooleanValueDao.class);
        persistableDaoMap.put(BaseEntityDateValue.class, IBeDateValueDao.class);
        persistableDaoMap.put(BaseEntityDoubleValue.class, IBeDoubleValueDao.class);
        persistableDaoMap.put(BaseEntityIntegerValue.class, IBeIntegerValueDao.class);
        persistableDaoMap.put(BaseEntityStringValue.class, IBeStringValueDao.class);
        persistableDaoMap.put(BaseEntityComplexValue.class, IBeComplexValueDao.class);
        persistableDaoMap.put(BaseSet.class, IBeSetDao.class);
        persistableDaoMap.put(BaseEntitySimpleSet.class, IBeEntitySimpleSetDao.class);
        persistableDaoMap.put(BaseEntityComplexSet.class, IBeEntityComplexSetDao.class);
        persistableDaoMap.put(BaseSetBooleanValue.class, IBeBooleanSetValueDao.class);
        persistableDaoMap.put(BaseSetDateValue.class, IBeDateSetValueDao.class);
        persistableDaoMap.put(BaseSetDoubleValue.class, IBeDoubleSetValueDao.class);
        persistableDaoMap.put(BaseSetIntegerValue.class, IBeIntegerSetValueDao.class);
        persistableDaoMap.put(BaseSetStringValue.class, IBeStringSetValueDao.class);
        persistableDaoMap.put(BaseSetComplexValue.class, IBeComplexSetValueDao.class);
    }

    public IPersistableDao getPersistableDao(Class<? extends IPersistable> persistableClass)
    {
        if (persistableClass == null)
        {
            throw new RuntimeException("Persistable class can not be null.");
        }

        Class<? extends IPersistableDao> persistableDaoClass =  persistableDaoMap.get(persistableClass);
        if (persistableDaoClass != null)
        {
            return applicationContext.getBean(persistableDaoClass);
        }
        throw new RuntimeException("Not found appropriate interface for persistable class " + persistableClass.getClass().getName());
    }

    public <T extends IPersistableDao> T getPersistableDao(Class<? extends IPersistable> persistableClass, Class<T> extendedPersistableDaoClass)
    {
        if (persistableClass == null)
        {
            throw new RuntimeException("Persistable class can not be null.");
        }

        Class<? extends IPersistableDao> persistableDaoClass =  persistableDaoMap.get(persistableClass);
        if (persistableDaoClass != null && extendedPersistableDaoClass.getClass().isAssignableFrom(persistableDaoClass))
        {
            return (T)applicationContext.getBean(persistableDaoClass);
        }
        throw new RuntimeException("Not found appropriate interface for persistable class " + persistableClass.getClass().getName());
    }

}
