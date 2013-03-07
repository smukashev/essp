package kz.bsbnb.usci.eav_persistance.repository.impl;

import kz.bsbnb.usci.eav_persistance.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * Caches crud operations with MetaClass objects.
 */
@Repository
public class MetaClassRepositoryImpl implements IMetaClassRepository
{
    private HashMap<String, MetaClass> cache = new HashMap<String, MetaClass>();

    @Autowired
    IMetaClassDao storage;

    @Override
    public MetaClass getMetaClass(String className)
    {
        MetaClass metaClass = cache.get(className);

        if(metaClass == null)
            metaClass = storage.load(className);

        return metaClass;
    }

    @Override
    public MetaClass getMetaClass(long id) {
        // todo: implement
        return null;
    }

}
