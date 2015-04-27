package kz.bsbnb.usci.eav.repository.impl;

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
    }

    @Override
    public MetaClass getMetaClass(String className) {
        MetaClass metaClass = cache.get(className);

        if (metaClass == null) {
            lock.readLock().lock();
            metaClass = metaClassDao.load(className);
            lock.readLock().unlock();

            if (metaClass != null) {
                lock.writeLock().lock();
                cache.put(className, metaClass);
                names.put(metaClass.getId(), className);
                lock.writeLock().unlock();
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
            metaClass = metaClassDao.load(id);
            lock.readLock().unlock();

            if (metaClass != null) {
                lock.writeLock().lock();
                cache.put(className, metaClass);
                names.put(metaClass.getId(), metaClass.getClassName());
                lock.writeLock().unlock();
            }
        }

        return metaClass;
    }

    @Override
    public List<MetaClass> getMetaClasses() {
        lock.readLock().lock();
        List<MetaClass> metaClassList = metaClassDao.loadAll();
        lock.readLock().unlock();

        return metaClassList;
    }

    @Override
    public void saveMetaClass(MetaClass meta) {
        lock.readLock().lock();
        long id = metaClassDao.save(meta);
        lock.readLock().unlock();

        /*for (String name : cache.keySet()) {
            cache.get(name).recursiveSet(meta);
        }*/

        lock.writeLock().lock();
        names.put(id, meta.getClassName());
        cache.put(meta.getClassName(), meta);
        lock.writeLock().unlock();
    }

    @Override
    public void resetCache() {
        lock.writeLock().lock();
        cache.clear();
        names.clear();
        lock.writeLock().unlock();

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
        lock.readLock().lock();
        MetaClass meta = getMetaClass(className);
        lock.readLock().unlock();

        if(meta != null) {
            lock.writeLock().lock();
            metaClassDao.remove(meta);

            cache.remove(className);
            names.remove(meta.getId());
            lock.writeLock().unlock();

            return true;
        }

        return false;
    }
}
