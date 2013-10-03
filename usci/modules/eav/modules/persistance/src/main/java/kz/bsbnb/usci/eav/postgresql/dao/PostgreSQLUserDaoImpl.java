package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IUserDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Repository
public class PostgreSQLUserDaoImpl extends JDBCSupport implements IUserDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLUserDaoImpl.class);


    @Override
    public boolean hasPortalUserCreditor(long userId, long creditorId)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setPortalUserCreditors(long userId, long creditorId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unsetPortalUserCreditors(long userId, long creditorId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Creditor> getPortalUserCreditorList(long userId)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void synchronize(List<PortalUser> users)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
