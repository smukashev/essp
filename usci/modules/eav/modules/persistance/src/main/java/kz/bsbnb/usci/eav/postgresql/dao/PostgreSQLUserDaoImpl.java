package kz.bsbnb.usci.eav.postgresql.dao;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.cr.model.SubjectType;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IUserDao;
import kz.bsbnb.usci.eav.persistance.impl.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 *
 */
@Repository
public class PostgreSQLUserDaoImpl extends JDBCSupport implements IUserDao
{
    private final Logger logger = LoggerFactory.getLogger(PostgreSQLUserDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBaseEntityDao postgreSQLBaseEntityDaoImpl;

    @Override
    public boolean hasPortalUserCreditor(long userId, long creditorId)
    {
        SelectForUpdateStep select;



        select = context.select(
                CREDITOR_USER.ID
        ).from(CREDITOR_USER).
                where(CREDITOR_USER.USER_ID.eq(userId)).and(CREDITOR_USER.CREDITOR_ID.eq(creditorId));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        if (rows.size() > 0)
            return true;

        return false;
    }

    @Override
    public void setPortalUserCreditors(long userId, long creditorId)
    {
        try
        {
            InsertOnDuplicateStep insert = context.insertInto(
                    CREDITOR_USER,
                    CREDITOR_USER.USER_ID,
                    CREDITOR_USER.CREDITOR_ID
            ).values(userId, creditorId);

            logger.debug(insert.toString());
            insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        }
        catch (DuplicateKeyException e)
        {
            logger.error("Duplicate ids: " + userId + " " + creditorId);
            throw new IllegalArgumentException("Duplicate ids: " + userId + " " + creditorId);
        }
    }

    @Override
    public void unsetPortalUserCreditors(long userId, long creditorId)
    {
        DeleteConditionStep deleteFilter =
                context.delete(CREDITOR_USER).
                        where(CREDITOR_USER.USER_ID.eq(userId)).and(CREDITOR_USER.CREDITOR_ID.eq(creditorId));

        long t = 0;
        if(sqlStats != null)
        {
            t = System.nanoTime();
        }

        jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

        if(sqlStats != null)
        {
            sqlStats.put(deleteFilter.getSQL(), (System.nanoTime() - t) / 1000000);
        }
    }

    @Override
    public List<Creditor> getPortalUserCreditorList(long userId)
    {
        ArrayList<Creditor> creditors = new ArrayList<Creditor>();

        SelectForUpdateStep select;



        select = context.select(
                CREDITOR_USER.CREDITOR_ID
        ).from(CREDITOR_USER).
                where(CREDITOR_USER.USER_ID.eq(userId));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        if (rows.size() < 1)
            return creditors;

        for (Map<String, Object> row : rows){
            Long id = ((BigDecimal)row.get(CREDITOR_USER.CREDITOR_ID.getName())).longValue();

            BaseEntity entity = (BaseEntity) postgreSQLBaseEntityDaoImpl.load(id);

            Creditor creditor = new Creditor();

            creditor.setId(entity.getId());
            BaseValue value = (BaseValue)entity.getBaseValue("name");
            if (value != null)
                creditor.setName((String)value.getValue());
            else
                creditor.setName("none");

            value = (BaseValue)entity.getBaseValue("short_name");
            if (value != null)
                creditor.setShortName((String)value.getValue());
            else
                creditor.setShortName("none");

            value = (BaseValue)entity.getBaseValue("code");
            if (value != null)
                creditor.setCode((String)value.getValue());
            else
                creditor.setCode("none");

            SubjectType st = new SubjectType();
            BaseValue val = (BaseValue)entity.getBaseValue("subject_type");
            BaseEntity stEntity = val == null ? null : (BaseEntity)val.getValue();
            if (stEntity != null) {

            } else {
                st.setCode("NONE");
                st.setNameKz("NONE");
                st.setNameRu("NONE");
            }

            creditor.setSubjectType(st);


            creditors.add(creditor);
        }

        return creditors;
    }

    @Override
    public void synchronize(List<PortalUser> users)
    {
        HashMap<Long, PortalUser> usersFromDB = new HashMap<Long, PortalUser>();
        HashMap<Long, PortalUser> usersFromPortal = new HashMap<Long, PortalUser>();

        SelectForUpdateStep select;



        select = context.select(
                USERS.ID,
                USERS.USER_ID,
                USERS.FIRST_NAME,
                USERS.LAST_NAME,
                USERS.MIDDLE_NAME,
                USERS.SCREEN_NAME,
                USERS.EMAIL,
                USERS.MODIFIED_DATE
        ).from(USERS);


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());


        for (Map<String, Object> row : rows){
            PortalUser user = new PortalUser();

            user.setId((BigInteger)row.get(USERS.ID.getName()));
            user.setUserId(((BigInteger) row.get(USERS.ID.getName())).longValue());
            user.setFirstName((String)row.get(USERS.FIRST_NAME.getName()));
            user.setLastName((String) row.get(USERS.LAST_NAME.getName()));
            user.setMiddleName((String) row.get(USERS.MIDDLE_NAME.getName()));
            user.setScreenName((String)row.get(USERS.SCREEN_NAME.getName()));
            user.setEmailAddress((String)row.get(USERS.EMAIL.getName()));
            user.setModifiedDate(DataUtils.convert((Timestamp)row.get(USERS.MODIFIED_DATE.getName())));

            usersFromDB.put(user.getUserId(), user);
        }

        for (PortalUser user : users) {
            usersFromPortal.put(user.getUserId(), user);
        }

        Set<Long> toDelete = SetUtils.difference(usersFromDB.keySet(), usersFromPortal.keySet());
        Set<Long> toAdd = SetUtils.difference(usersFromPortal.keySet(), usersFromDB.keySet());
        Set<Long> toUpdate = SetUtils.intersection(usersFromPortal.keySet(), usersFromDB.keySet());

        for (Long id : toDelete) {
            DeleteConditionStep deleteFilter =
                    context.delete(CREDITOR_USER).
                            where(CREDITOR_USER.USER_ID.eq(id));

            long t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

            if(sqlStats != null)
            {
                sqlStats.put(deleteFilter.getSQL(), (System.nanoTime() - t) / 1000000);
            }

            deleteFilter =
                    context.delete(USERS).
                            where(USERS.USER_ID.eq(id));

            t = 0;
            if(sqlStats != null)
            {
                t = System.nanoTime();
            }

            jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

            if(sqlStats != null)
            {
                sqlStats.put(deleteFilter.getSQL(), (System.nanoTime() - t) / 1000000);
            }
        }

        for (Long id : toAdd) {
            try
            {
                PortalUser pu = usersFromPortal.get(id);

                InsertOnDuplicateStep insert = context.insertInto(
                        USERS,
                        USERS.USER_ID,
                        USERS.FIRST_NAME,
                        USERS.LAST_NAME,
                        USERS.MIDDLE_NAME,
                        USERS.SCREEN_NAME,
                        USERS.EMAIL,
                        USERS.MODIFIED_DATE
                ).values(
                        pu.getUserId(),
                        pu.getFirstName(),
                        pu.getLastName(),
                        pu.getMiddleName(),
                        pu.getScreenName(),
                        pu.getEmailAddress(),
                        DataUtils.convert(pu.getModifiedDate())
                );

                logger.debug(insert.toString());
                insertWithId(insert.getSQL(), insert.getBindValues().toArray());
            }
            catch (DuplicateKeyException e)
            {
                logger.error("Duplicate id: " + id);
                throw new IllegalArgumentException("Duplicate id: " + id);
            }
        }

        for (Long id : toUpdate) {
            PortalUser pu = usersFromPortal.get(id);

            Update update = context
                    .update(USERS)
                    .set(USERS.USER_ID, pu.getUserId())
                    .set(USERS.FIRST_NAME, pu.getFirstName())
                    .set(USERS.LAST_NAME, pu.getLastName())
                    .set(USERS.MIDDLE_NAME, pu.getMiddleName())
                    .set(USERS.SCREEN_NAME, pu.getScreenName())
                    .set(USERS.EMAIL, pu.getEmailAddress())
                    .set(USERS.MODIFIED_DATE, DataUtils.convert(pu.getModifiedDate()))
                    .where(USERS.USER_ID.equal(id))
                    .and(USERS.MODIFIED_DATE.lessThan(DataUtils.convert(pu.getModifiedDate())));

            logger.debug(update.toString());
            updateWithStats(update.getSQL(), update.getBindValues().toArray());
        }
    }
}
