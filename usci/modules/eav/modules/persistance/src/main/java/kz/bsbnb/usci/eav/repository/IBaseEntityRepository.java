package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public interface IBaseEntityRepository {

    public IBaseEntity getBaseEntity(long id, Date reportDate);

    public void initialize();

    public void setEnabled(boolean enabled);

    public boolean getEnabled();

    public void setConcurrencyLevel(int concurrencyLevel);

    public int getConcurrencyLevel();

    public void setDuration(long duration);

    public long getDuration();

    public void setTimeUnit(TimeUnit timeUnit);

    public TimeUnit getTimeUnit();

}
