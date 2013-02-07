package kz.bsbnb.usci.eav.model.metadata.impl;

import kz.bsbnb.usci.eav.model.metadata.IMetaClassRepository;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class BasicMetaClassRepositoryImpl implements IMetaClassRepository
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
}
