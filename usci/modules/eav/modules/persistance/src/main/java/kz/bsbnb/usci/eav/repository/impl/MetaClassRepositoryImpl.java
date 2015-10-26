package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Repository
public class MetaClassRepositoryImpl implements IMetaClassRepository, InitializingBean {
    @Autowired
    IMetaClassDao metaClassDao;

    private HashMap<String, MetaClass> cache = new HashMap<>();
    private HashMap<Long, String> names = new HashMap<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void afterPropertiesSet() throws Exception {
        lock.readLock().lock();
        List<MetaClass> metaClassList;
        try {
            metaClassList = metaClassDao.loadAll();
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            for (MetaClass tmpMeta : metaClassList) {
                cache.put(tmpMeta.getClassName(), tmpMeta);
                names.put(tmpMeta.getId(), tmpMeta.getClassName());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public MetaClass getDisabledMetaClass(String className) {
        lock.readLock().lock();
        MetaClass metaClass;
        try {
            metaClass = cache.get(className);
        } finally {
            lock.readLock().unlock();
        }

        if (metaClass == null) {
            lock.readLock().lock();
            try {
                metaClass = metaClassDao.loadDisabled(className);
            } finally {
                lock.readLock().unlock();
            }

            if (metaClass != null) {
                lock.writeLock().lock();
                try {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), className);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return metaClass;
    }

    @Override
    public MetaClass getMetaClass(String className) {
        lock.readLock().lock();
        MetaClass metaClass;
        try {
            metaClass = cache.get(className);
        } finally {
            lock.readLock().unlock();
        }

        if (metaClass == null) {
            lock.readLock().lock();
            try {
                metaClass = metaClassDao.load(className);
            } finally {
                lock.readLock().unlock();
            }

            if (metaClass != null) {
                lock.writeLock().lock();
                try {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), className);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return metaClass;
    }

    @Override
    public MetaClass getMetaClass(long id) {
        String className;
        lock.readLock().lock();
        try {
            className = names.get(id);
        } finally {
            lock.readLock().unlock();
        }

        MetaClass metaClass = null;

        if (className != null) {
            lock.readLock().lock();
            try {
                metaClass = cache.get(className);
            } finally {
                lock.readLock().unlock();
            }
        }

        if (metaClass == null) {
            lock.readLock().lock();
            try {
                metaClass = metaClassDao.load(id);
            } finally {
                lock.readLock().unlock();
            }

            if (metaClass != null) {
                lock.writeLock().lock();
                try {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), metaClass.getClassName());
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return metaClass;
    }

    @Override
    public List<MetaClass> getMetaClasses() {
        List<MetaClass> metaClassList = new ArrayList<>();
        lock.readLock().lock();
        try {
            for (Map.Entry<String, MetaClass> entry : cache.entrySet())
                metaClassList.add(entry.getValue());
        } finally {
            lock.readLock().unlock();
        }
        return metaClassList;
    }

    @Override
    public void saveMetaClass(MetaClass meta) {
        long id;
        lock.writeLock().lock();
        try {
            id = metaClassDao.save(meta); // TODO: simple array attribute doesn't contain ID
            // TODO: remove tmp solution
            meta = metaClassDao.load(id);
            names.put(id, meta.getClassName());
            cache.put(meta.getClassName(), meta);
            List<Long> classIds= metaClassDao.loadContaining(id);
            for(long classId: classIds)
            {
                meta = metaClassDao.load(classId);
                names.put(meta.getId(), meta.getClassName());
                cache.put(meta.getClassName(), meta);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void resetCache() {
        lock.writeLock().lock();
        try {
            cache.clear();
            names.clear();
        } finally {
            lock.writeLock().unlock();
        }

        try {
            afterPropertiesSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<MetaClassName> getMetaClassesNames() {
        return metaClassDao.getMetaClassesNames();
    }

    @Override
    public List<MetaClassName> getRefNames() {
        return metaClassDao.getRefNames();
    }

    @Override
    public boolean delMetaClass(String className) {
        MetaClass meta = getMetaClass(className);

        if (meta != null) {
            lock.writeLock().lock();
            try {
                metaClassDao.remove(meta);
                cache.remove(className);
                names.remove(meta.getId());
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

}
