package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.ISQLGenerator;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RefRepositoryImpl implements IRefRepository, InitializingBean {
    private static final int CACHE_MONITOR_SIZE = 10000;
    private Map<Long, List<Map<String, Object>>> prepareMap = new HashMap<>();

    @Autowired
    private ISQLGenerator sqlGenerator;


    @Autowired
    private IBaseEntityLoadDao loadDao;

    @Qualifier("metaClassRepositoryImpl")
    @Autowired
    private IMetaClassRepository metaClassRepository;

    private ReentrantReadWriteLock semaphore = new ReentrantReadWriteLock();


    List<IBaseEntity> queue = new LinkedList<>();
    Map<CacheEntry, CacheEntry> cache = new ConcurrentHashMap<>();
    private long totalHitCount = 0;

    public class CacheEntry {
        private IBaseEntity baseEntity;
        private long hitCount;

        public CacheEntry(IBaseEntity entity) {
            this.baseEntity = entity;
            hitCount = 0;
        }

        @Override
        public int hashCode() {
            return (int) baseEntity.getId() * 1234 + baseEntity.getReportDate().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CacheEntry) {
                CacheEntry that = (CacheEntry) obj;
                return baseEntity.getId() == that.baseEntity.getId()
                        && (baseEntity.getReportDate().equals(that.baseEntity.getReportDate()));
            }

            return false;
        }
    }

    @Override
    public IBaseEntity get(IBaseEntity entity) {
        CacheEntry ce = new CacheEntry(entity);
        CacheEntry ret = cache.get(ce);

        if(ret == null) {
            IBaseEntity entityLoaded = loadDao.loadByMaxReportDate(entity.getId(), entity.getReportDate());
            //refactor null policy
            if(entityLoaded == null) {
                throw new RuntimeException("no ref from db");
            }
            //entity.getBaseEntityReportDate().setReportDate(entity.getReportDate());
            ret = new CacheEntry(entityLoaded);

            cache.put(ce, ret);
        } else {
            totalHitCount ++;
            if( (totalHitCount % 1000) == 0)
                System.out.println("cacheHitCount = "  + totalHitCount + ", queue size = " + queue.size());
        }

        ret.hitCount++;
        queue.add(ce.baseEntity);

        /*if(queue.size() > CACHE_MONITOR_SIZE) {
            CacheEntry leftEntry = cache.get(new CacheEntry(queue.remove(0)));
            //System.out.println(leftEntry.hitCount);
            leftEntry.hitCount--;

            if(leftEntry.hitCount == 0) {
                cache.remove(leftEntry);
                System.out.println("removed from cache !!!");
            }

            if(leftEntry.hitCount < 0 )
                throw new RuntimeException("negative value !!! " + leftEntry.hitCount);
        }*/

        return ret.baseEntity;
    }

    /*@Override
    public synchronized void invalidate(IBaseEntity baseEntity) {
        Iterator<IBaseEntity> iterator = queue.iterator();

        while(iterator.hasNext()) {
            IBaseEntity cachedEntity = iterator.next();
            if(cachedEntity.getId() == baseEntity.getId()) {
                iterator.remove();
                CacheEntry invEntry = cache.get(new CacheEntry(cachedEntity));
                invEntry.hitCount--;

                if(invEntry.hitCount == 0){
                    cache.remove(new CacheEntry(cachedEntity));
                    System.out.println("removed from cache due to invalidate !!!");
                }
            }
        }
    }*/

    @Override
    public void afterPropertiesSet() throws Exception {
        semaphore.writeLock().lock();
        try {
            long t1 = System.currentTimeMillis();
            for (MetaClass meta : metaClassRepository.getMetaClasses()) {
                if (meta.isReference())
                    prepareMap.put(meta.getId(), sqlGenerator.getSimpleResult(meta.getId(), true));
            }
            System.out.println("Caching time: " + (System.currentTimeMillis() - t1));
        } finally {
            semaphore.writeLock().unlock();
        }
    }

    @Override
    public long prepareRef(final IBaseEntity baseEntity) {
        semaphore.readLock().lock();
        try {
            List<Map<String, Object>> mapList=null;
            if(prepareMap.get(baseEntity.getMeta().getId())==null)
                prepareMap.put(baseEntity.getMeta().getId(), sqlGenerator.getSimpleResult(baseEntity.getMeta().getId(), true));
                mapList = prepareMap.get(baseEntity.getMeta().getId());

            List<Map<String, Object>> currentEntityMapList = convert(baseEntity);

            for (Map<String, Object> map : mapList) {
                for (Map<String, Object> current : currentEntityMapList) {
                    boolean found = true;

                    for (Map.Entry<String, Object> currentEntry : current.entrySet()) {
                        if (!currentEntry.getValue().equals(map.get(currentEntry.getKey()))) {
                            found = false;
                            break;
                        }
                    }

                    if (found)
                        return ((BigDecimal) map.get(baseEntity.getMeta().getClassName().toUpperCase() + "_ID")).longValue();
                }
            }

            return 0;
        } finally {
            semaphore.readLock().unlock();
        }
    }

    public void installRef(final IBaseEntity entity) {
        semaphore.writeLock().lock();
        try {
            List<Map<String, Object>> mapList = convert(entity);

            List<Map<String, Object>> preparedRef = prepareMap.get(entity.getMeta().getId());

            for (Map<String, Object> m : mapList) {
                m.put(entity.getMeta().getClassName().toUpperCase() + "_ID", new BigDecimal(entity.getId()));
                preparedRef.add(m);
            }
        } finally {
            semaphore.writeLock().unlock();
        }
    }

    private List<Map<String, Object>> convert (IBaseEntity baseEntity) {
        List<Map<String, Object>> mapList = new LinkedList<>();

        for (String attributeName : baseEntity.getMeta().getAttributeNames()) {
            IMetaAttribute metaAttribute = baseEntity.getMeta().getMetaAttribute(attributeName);
            IMetaType metaType = metaAttribute.getMetaType();

            if (!metaAttribute.isKey() && !metaAttribute.isOptionalKey())
                continue;

            if (metaType.isSet() && metaType.isComplex()) {
                final IBaseValue baseValue = baseEntity.getBaseValue(attributeName);

                if (baseValue == null)
                    continue;

                final BaseSet baseSet = (BaseSet) baseValue.getValue();

                for (IBaseValue childBaseValue : baseSet.get()) {
                    Map<String, Object> map = new HashMap<>();
                    IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();
                    map.put(childBaseEntity.getMeta().getClassName().toUpperCase() + "_ID", new BigDecimal(childBaseEntity.getId()));
                    mapList.add(map);
                }
            }
        }

        if (mapList.size() == 0) {
            mapList.add(new HashMap<String, Object>());
        }

        for (String attributeName : baseEntity.getMeta().getAttributeNames()) {
            IMetaAttribute metaAttribute = baseEntity.getMeta().getMetaAttribute(attributeName);
            IMetaType metaType = metaAttribute.getMetaType();

            if ((!metaAttribute.isKey() && !metaAttribute.isOptionalKey()) || metaType.isSet())
                continue;

            final String attrName = metaAttribute.getName().toUpperCase();
            final IBaseValue baseValue = baseEntity.getBaseValue(attributeName);

            if (metaAttribute.isOptionalKey() && (baseValue == null || baseValue.getValue() == null))
                continue;

            String attrNewName;
            Object value;

            if (metaType.isComplex()) {
                attrNewName = attrName + "_ID";
                value = new BigDecimal(((IBaseEntity) baseValue.getValue()).getId());
            } else {
                attrNewName = attrName;
                MetaValue metaValue = (MetaValue) metaType;

                switch(metaValue.getTypeCode()) {
                    case INTEGER:
                        value = new BigDecimal(String.valueOf(baseValue.getValue()));
                        break;
                    default:
                        value = baseValue.getValue();
                        break;
                }
            }

            for (Map<String, Object> entry : mapList) {
                entry.put(attrNewName, value);
            }
        }

        return mapList;
    }
}
