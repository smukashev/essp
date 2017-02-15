package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.ISQLGenerator;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
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

    @Autowired
    SQLQueriesStats sqlStats;

    private ReentrantReadWriteLock semaphore = new ReentrantReadWriteLock();


    //List<IBaseEntity> queue = new LinkedList<>();
    Map<CacheEntry, CacheEntry> cache = new ConcurrentHashMap<>();
    private long totalHitCount = 0;
    private long cacheRemoveCount;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public class CacheEntry {
        private IBaseEntity baseEntity;
        private long hitCount;

        public CacheEntry(IBaseEntity entity) {
            this.baseEntity = entity;
            hitCount = 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(baseEntity.getId(), baseEntity.getBaseEntityReportDate().getReportDate());
            //return (int) baseEntity.getId() * 1234 + baseEntity.getBaseEntityReportDate().getReportDate().hashCode();
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
        long t1 = System.currentTimeMillis();

        if (entity.getId() <= 0L || entity.getBaseEntityReportDate().getReportDate() == null)
            throw new IllegalStateException(Errors.compose(Errors.E102));

        CacheEntry ce = new CacheEntry(entity);
        lock.readLock().lock();

        try {
            CacheEntry ret = cache.get(ce);

            if (ret == null) {
                IBaseEntity entityLoaded = loadDao.loadByMaxReportDate(entity.getId(), entity.getReportDate());
                //refactor null policy
                if (entityLoaded == null) {
                    throw new RuntimeException("no ref from db");
                }
                //entity.getBaseEntityReportDate().setReportDate(entity.getReportDate());
                ret = new CacheEntry(entityLoaded);

                cache.put(ce, ret);
            } else {
                totalHitCount++;
            }

            //queue.add(ce.baseEntity);

            /*if(queue.size() > CACHE_MONITOR_SIZE) {
                CacheEntry leftEntry = cache.get(new CacheEntry(queue.remove(0)));
                //System.out.println(leftEntry.hitCount);
                leftEntry.hitCount--;

                if(leftEntry.hitCount == 0) {
                    cache.remove(leftEntry);
                    cacheRemoveCount++;
                }

                if(leftEntry.hitCount < 0 )
                    throw new RuntimeException("negative value !!! " + leftEntry.hitCount);
            }*/

            ret.hitCount++;
            return ret.baseEntity;
        } finally {
            lock.readLock().unlock();
            sqlStats.put("cache.get() ", (System.currentTimeMillis()- t1) );
        }
    }

    public String getStatus(){
        PriorityQueue<CacheEntry> pq = new PriorityQueue<>(10, new Comparator<CacheEntry>() {
            @Override
            public int compare(CacheEntry o1, CacheEntry o2) {
                return o1.hitCount < o2.hitCount ? -1 : 1;
            }
        });

        for (CacheEntry cacheEntry : cache.values()) {
            pq.add(cacheEntry);
            if(pq.size() > 10) {
                pq.remove();
            }
        }

        String top10 = "";

        while(pq.size() != 0) {
            CacheEntry cacheEntry = pq.remove();
            top10 += cacheEntry.baseEntity + "," + cacheEntry.hitCount + "\n";
        }

        return "cacheHitCount = "  + totalHitCount + ", cache size = " + cache.size() + ", cacheRemoveCount = " + cacheRemoveCount + "\n" + top10;
    }

    @Override
    public void invalidate(IBaseEntity baseEntity) {
        lock.writeLock().lock();
        for (CacheEntry cacheEntry : cache.keySet()) {
            if(cacheEntry.baseEntity.getId() == baseEntity.getId())
                cache.remove(cacheEntry);
        }
        lock.writeLock().unlock();
    }

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
