package kz.bsbnb.usci.eav.persistance.dao.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeStorageDao;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
@Scope(value = "singleton")
public class BeStorageDaoImpl implements IBeStorageDao {

    private class BaseEntityKey
    {
        private long id;
        private Date reportDate;
        private boolean withClosedValues;

        public BaseEntityKey(long id, Date reportDate, boolean withClosedValues)
        {
            this.id = id;
            this.reportDate = reportDate;
            this.withClosedValues = withClosedValues;
        }

        public long getId()
        {
            return id;
        }

        public Date getReportDate()
        {
            return reportDate;
        }

        public boolean isWithClosedValues()
        {
            return withClosedValues;
        }

    }

    public static final int CONCURRENCY_LEVEL = 4;
    public static final long MAXIMUM_SIZE  = 10000;
    public static final long DURATION  = 10;
    public static final TimeUnit UNIT  = TimeUnit.MINUTES;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    // TODO: Maybe use LinkedHashMap
    LoadingCache<BaseEntityKey, IBaseEntity> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(CONCURRENCY_LEVEL)
            .weakKeys()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterAccess(DURATION, UNIT)
            .build(
                    new CacheLoader<BaseEntityKey, IBaseEntity>() {
                        public IBaseEntity load(BaseEntityKey key) throws Exception {
                            if (key.getReportDate() == null)
                            {
                                return baseEntityDao.load(key.getId(), key.isWithClosedValues());
                            }
                            else
                            {
                                return baseEntityDao.load(key.getId(), key.getReportDate(), key.isWithClosedValues());
                            }
                        }
                    });

    public IBaseEntity getBaseEntity(long id, Date reportDate)
    {
        // TODO:
        return getBaseEntity(id, reportDate, false);
    }

    public IBaseEntity getBaseEntity(long id, Date reportDate, boolean withClosedValues)
    {
        BaseEntityKey key = new BaseEntityKey(id, reportDate, withClosedValues);
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            throw new RuntimeException("When receiving instance of BaseEntity unexpected error occurred.");
        }
    }

    public IBaseEntity getBaseEntity(long id)
    {
        return getBaseEntity(id, null, false);
    }

    public IBaseEntity getBaseEntity(long id, boolean withClosedValues)
    {
        return getBaseEntity(id, null, withClosedValues);
    }

    public void clean()
    {
        cache.asMap().clear();
    }

}
