package kz.bsbnb.usci.eav.persistance.impl.db.postgresql.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

/**
 * @author a.motov
 */
@Repository
public class PostgreSQLBaseEntityDaoImpl extends JDBCSupport
            implements IBaseEntityDao {
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLBaseEntityDaoImpl.class);

    @PostConstruct
    public void init()
    {

    }

    @Override
    public BaseEntity load(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long save(BaseEntity persistable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(BaseEntity persistable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseEntity load(BaseEntity baseEntity, boolean eager) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
