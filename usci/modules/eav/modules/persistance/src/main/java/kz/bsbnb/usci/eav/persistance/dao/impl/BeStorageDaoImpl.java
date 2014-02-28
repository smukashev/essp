package kz.bsbnb.usci.eav.persistance.dao.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBeStorageDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Aspect
@Component
@Scope(value = "singleton")
public class BeStorageDaoImpl implements IBeStorageDao {

    private final Logger logger = LoggerFactory.getLogger(BeStorageDaoImpl.class);

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BaseEntityKey that = (BaseEntityKey) o;

            if (id != that.id) return false;
            if (withClosedValues != that.withClosedValues) return false;
            if (reportDate != null ? !reportDate.equals(that.reportDate) : that.reportDate != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (reportDate != null ? reportDate.hashCode() : 0);
            result = 31 * result + (withClosedValues ? 1 : 0);
            return result;
        }
    }

    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_CONCURRENCY_LEVEL = 50;
    public static final long DEFAULT_MAXIMUM_SIZE = 10000;
    public static final long DEFAULT_DURATION = 10;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    private boolean enabled = DEFAULT_ENABLED;
    private int concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
    private long maximumSize = DEFAULT_MAXIMUM_SIZE;
    private long duration = DEFAULT_DURATION;
    private TimeUnit timeUnit = DEFAULT_TIME_UNIT;

    @Autowired
    private IBaseEntityDao baseEntityDao;

    // TODO: Maybe use LinkedHashMap
    LoadingCache<BaseEntityKey, IBaseEntity> cache;

    public void reinitialize()
    {
        cache = CacheBuilder.newBuilder()
            .concurrencyLevel(concurrencyLevel)
            .softValues()
            .maximumSize(maximumSize)
            .expireAfterAccess(duration, timeUnit)
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
    }

    public IBaseEntity getBaseEntity(long id, Date reportDate)
    {
        // TODO:
        return getBaseEntity(id, reportDate, false);
    }

    public IBaseEntity getBaseEntity(long id, Date reportDate, boolean withClosedValues)
    {
        if (enabled)
        {
            if (cache == null)
            {
                reinitialize();
            }
            BaseEntityKey key = new BaseEntityKey(id, reportDate, withClosedValues);
            try {
                return cache.get(key);
            } catch (ExecutionException e) {
                throw new RuntimeException("When receiving instance of BaseEntity unexpected error occurred.");
            }
        }
        else
        {
            if (reportDate == null)
            {
                return baseEntityDao.load(id, withClosedValues);
            }
            else
            {
                return baseEntityDao.load(id, reportDate, withClosedValues);
            }
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

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean getEnabled()
    {
        return enabled;
    }

    public void setConcurrencyLevel(int concurrencyLevel)
    {
        this.concurrencyLevel = concurrencyLevel;
    }

    public int getConcurrencyLevel()
    {
        return concurrencyLevel;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setTimeUnit(TimeUnit timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit()
    {
        return timeUnit;
    }

    @Around("execution(* kz.bsbnb.usci.eav.postgresql.dao.PostgreSQLBaseEntityDaoImpl.process(..))")
    public Object processAround(ProceedingJoinPoint pjp) throws Throwable
    {
        if (enabled)
        {
            Exception exception = null;
            IBaseEntity outputBaseEntity = null;

            try
            {
                outputBaseEntity = (IBaseEntity)pjp.proceed();
            }
            catch(Exception ex)
            {
                exception = ex;
            }

            if (exception != null)
            {
                throw exception;
            }
            else
            {
                refreshBaseEntity(outputBaseEntity);
            }

            return outputBaseEntity;
        }
        else
        {
            return pjp.proceed();
        }
    }

    private void refreshBaseEntity(IBaseEntity baseEntity)
    {
        IMetaClass metaClass = baseEntity.getMeta();

        Iterator it = baseEntity.getIdentifiers().iterator();

        while (it.hasNext())
        {
            String identifier = (String)it.next();
            IMetaType metaType = metaClass.getMemberType(identifier);

            if (!metaType.isSet() && metaType.isComplex() && (!metaType.isReference() & !metaType.isImmutable()))
            {
                IMetaClass childMetaClass = (IMetaClass)metaType;
                if (childMetaClass.isSearchable())
                {
                    IBaseValue baseValue = baseEntity.getBaseValue(identifier);
                    if (baseValue.getValue() != null)
                    {
                        refreshBaseEntity((IBaseEntity)baseValue.getValue());
                    }
                }
            }

        }

        BaseEntityKey key = new BaseEntityKey(baseEntity.getId(), baseEntity.getReportDate(), baseEntity.isWithClosedValues());
        if (cache == null)
        {
            reinitialize();
        }
        cache.put(key, baseEntity);
    }

}
