package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.eav.persistance.generated.tables.EavACreditorState;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
       // try {
            lock.readLock().lock();
            List<MetaClass> metaClassList = metaClassDao.loadAll();
            lock.readLock().unlock();

            lock.writeLock().lock();
            Iterator<MetaClass> concurrentIterator = metaClassList.iterator();

            while(concurrentIterator.hasNext()) {
                MetaClass tmpMeta = concurrentIterator.next();

                cache.put(tmpMeta.getClassName(), tmpMeta);
                names.put(tmpMeta.getId(), tmpMeta.getClassName());
            }

            lock.writeLock().unlock();
       // }
       // finally {
         //   lock.readLock().unlock();
         //   lock.writeLock().unlock();
       // }
    }

    @Override
    public MetaClass getMetaClass(String className) {
           MetaClass metaClass = cache.get(className);

           if (metaClass == null) {
               lock.readLock().lock();
               try {
                   metaClass = metaClassDao.load(className);
               }
               finally {
                   lock.readLock().unlock();
               }


               if (metaClass != null) {
                   lock.writeLock().lock();
                   try {
                       cache.put(className, metaClass);
                       names.put(metaClass.getId(), className);
                   }
                   finally {
                       lock.writeLock().unlock();
                   }


               }
           }

           return metaClass;
    }

    @Override
    public MetaClass getMetaClass(long id) {
           String className = names.get(id);
           MetaClass metaClass = null;

           if (className != null)
               metaClass = cache.get(className);

           if (metaClass == null) {
               lock.readLock().lock();
               try {
                   metaClass = metaClassDao.load(id);
               }
               finally {
                   lock.readLock().unlock();
               }

               if (metaClass != null) {
                   lock.writeLock().lock();
                   try {
                       cache.put(className, metaClass);
                       names.put(metaClass.getId(), metaClass.getClassName());
                   }
                   finally {
                       lock.writeLock().unlock();
                   }
               }
           }

           return metaClass;
    }

    @Override
    public List<MetaClass> getMetaClasses() {

        List<MetaClass> metaClassList;
            lock.readLock().lock();
            try {
                metaClassList = metaClassDao.loadAll();
            }
            finally {
                lock.readLock().unlock();

            }
            return metaClassList;
    }

    @Override
    public void saveMetaClass(MetaClass meta) {
            long id;
            lock.readLock().lock();
            try {
                id = metaClassDao.save(meta);
            }
            finally {
                lock.readLock().unlock();
            }



        /*for (String name : cache.keySet()) {
            cache.get(name).recursiveSet(meta);
        }*/

            lock.writeLock().lock();
            try {
                names.put(id, meta.getClassName());
                cache.put(meta.getClassName(), meta);
            }
            finally {
                lock.writeLock().unlock();
            }

    }

    @Override
    public void resetCache() {
            lock.writeLock().lock();
            try {
                cache.clear();
                names.clear();
            }
            finally {
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
        MetaClass meta;
        lock.readLock().lock();
            try {
                meta = getMetaClass(className);
            }
            finally {
                lock.readLock().unlock();
            }

            if (meta != null) {
                lock.writeLock().lock();
                try {
                    metaClassDao.remove(meta);
                    cache.remove(className);
                    names.remove(meta.getId());
                }
                finally {
                    lock.writeLock().unlock();
                }
                return true;
            }
            return false;
    }

}
