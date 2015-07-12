package kz.bsbnb.usci.eav.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Scope(value = "singleton")
public class BaseEntityRepositoryImpl implements IBaseEntityRepository {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityRepositoryImpl.class);

    private class BaseEntityKey {
        private long id;
        private Date reportDate;

        public BaseEntityKey(long id, Date reportDate) {
            this.id = id;
            this.reportDate = reportDate;
        }

        public long getId() {
            return id;
        }

        public Date getReportDate() {
            return reportDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BaseEntityKey that = (BaseEntityKey) o;

            if (id != that.id) return false;
            if (!reportDate.equals(that.reportDate)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + reportDate.hashCode();
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
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    // TODO: Maybe use LinkedHashMap
    LoadingCache<BaseEntityKey, IBaseEntity> cache;

    @Override
    public IBaseEntityProcessorDao getBaseEntityProcessorDao() {
        return this.baseEntityProcessorDao;
    }

    public BaseEntityRepositoryImpl() {
        if (enabled) {
            initialize();
        }
    }

    public synchronized void initialize() {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .softValues()
                .maximumSize(maximumSize)
                .expireAfterAccess(duration, timeUnit)
                .build(
                        new CacheLoader<BaseEntityKey, IBaseEntity>() {
                            public IBaseEntity load(BaseEntityKey key) throws Exception {
                                //TODO: Dates messed up
                                return baseEntityLoadDao.loadByMaxReportDate(key.getId(), key.getReportDate());
                            }
                        });
    }

    public IBaseEntity getBaseEntity(long id, Date reportDate) {
        if (id < 1) {
            throw new IllegalArgumentException("Unable to load a instance of BaseEntity without ID.");
        }

        if (reportDate == null) {
            throw new IllegalArgumentException("Unable to load a instance of BaseEntity without report date.");
        }

        if (enabled) {
            BaseEntityKey key = new BaseEntityKey(id, reportDate);
            try {
                return cache.get(key);
            } catch (ExecutionException e) {
                throw new RuntimeException("When receiving instance of BaseEntity unexpected error occurred.");
            }
        } else {
            //TODO: Dates messed up
            return baseEntityLoadDao.loadByMaxReportDate(id, reportDate);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setConcurrencyLevel(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

}
