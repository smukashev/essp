package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Caches crud operations with MetaClass objects.
 */
@Repository
public class MetaClassRepositoryImpl implements IMetaClassRepository
{
    private HashMap<String, MetaClass> cache = new HashMap<String, MetaClass>();
    private HashMap<Long, String> names = new HashMap<Long, String>();

    @Autowired
    IMetaClassDao metaClassDao;

    @Override
    public MetaClass getMetaClass(String className)
    {
        MetaClass metaClass = cache.get(className);

        if(metaClass == null)
        {
            synchronized(this) {
                metaClass = metaClassDao.load(className);
                if(metaClass != null) {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), className);
                }
            }
        }

        return metaClass;
    }

    @Override
    public MetaClass getMetaClass(long id) {
        String className = names.get(id);
        MetaClass metaClass = null;

        if (className != null) {
            metaClass = cache.get(className);
        }

        if(metaClass == null)
        {
            synchronized(this) {
                metaClass = metaClassDao.load(id);
                if(metaClass != null) {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), metaClass.getClassName());
                }
            }
        }

        return metaClass;
    }

    @Override
    public List<MetaClass> getMetaClasses() {
        return metaClassDao.loadAll();
    }

    @Override
    //TODO: does not check names
    synchronized public void saveMetaClass(MetaClass meta)
    {
        long id = metaClassDao.save(meta);

        for (String name : cache.keySet()) {
            cache.get(name).recursiveSet(meta);
        }

        cache.put(meta.getClassName(), meta);
    }

    @Override
    synchronized public void resetCache() {
        cache.clear();
        names.clear();
    }

    @Override
    public List<MetaClassName> getMetaClassesNames()
    {
        return metaClassDao.getMetaClassesNames();
    }


    @Override
    public List<MetaClassName> getRefNames() {
        return metaClassDao.getRefNames();
    }

    @Override
    //TODO: does not erase from names
    public boolean delMetaClass(String className)
    {
        MetaClass meta = getMetaClass(className);

        if (meta != null) {
            metaClassDao.remove(meta);
            return true;
        } else {
            return false;
        }
    }
}
