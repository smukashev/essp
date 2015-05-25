package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface IBaseEntityRepository {
    IBaseEntity getBaseEntity(long id, Date reportDate);

    void initialize();

    void setEnabled(boolean enabled);

    boolean getEnabled();

    void setConcurrencyLevel(int concurrencyLevel);

    int getConcurrencyLevel();

    void setDuration(long duration);

    long getDuration();

    void setTimeUnit(TimeUnit timeUnit);

    TimeUnit getTimeUnit();

    IBaseEntityProcessorDao getBaseEntityProcessorDao();
}
