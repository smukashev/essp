package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.PortalUser;
import kz.bsbnb.usci.cr.model.SubjectType;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IUserDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class UserDaoImpl extends JDBCSupport implements IUserDao {
    private final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Override
    public boolean hasPortalUserCreditor(long userId, long creditorId) {
        SelectForUpdateStep select;

        select = context.select(EAV_A_CREDITOR_USER.ID).from(EAV_A_CREDITOR_USER).
                where(EAV_A_CREDITOR_USER.USER_ID.eq(userId)).and(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(creditorId));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.size() > 0;
    }

    @Override
    public void setPortalUserCreditors(long userId, long creditorId) {
        InsertOnDuplicateStep insert = context.insertInto(
                EAV_A_CREDITOR_USER,
                EAV_A_CREDITOR_USER.USER_ID,
                EAV_A_CREDITOR_USER.CREDITOR_ID
        ).values(userId, creditorId);

        insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void unsetPortalUserCreditors(long userId, long creditorId) {
        DeleteConditionStep deleteFilter = context.delete(EAV_A_CREDITOR_USER).
                where(EAV_A_CREDITOR_USER.USER_ID.eq(userId)).
                and(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(creditorId));

        jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());
    }

    @Override
    public List<Creditor> getPortalUserCreditorList(long userId) {
        ArrayList<Creditor> creditors = new ArrayList<>();

        SelectForUpdateStep select;

        select = context.select(EAV_A_CREDITOR_USER.CREDITOR_ID).from(EAV_A_CREDITOR_USER).
                where(EAV_A_CREDITOR_USER.USER_ID.eq(userId));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() < 1)
            return creditors;

        for (Map<String, Object> row : rows) {
            Long id = ((BigDecimal) row.get(EAV_A_CREDITOR_USER.CREDITOR_ID.getName())).longValue();

            IBaseEntity entity = baseEntityLoadDao.load(id);

            Creditor creditor = new Creditor();

            creditor.setId(entity.getId());
            BaseValue value = (BaseValue) entity.getBaseValue("name");
            if (value != null)
                creditor.setName((String) value.getValue());
            else
                creditor.setName("none");

            value = (BaseValue) entity.getBaseValue("short_name");
            if (value != null)
                creditor.setShortName((String) value.getValue());
            else
                creditor.setShortName("none");

            value = (BaseValue) entity.getBaseValue("code");
            if (value != null)
                creditor.setCode((String) value.getValue());
            else
                creditor.setCode("none");

            SubjectType st = new SubjectType();
            BaseValue val = (BaseValue) entity.getBaseValue("subject_type");
            BaseEntity stEntity = val == null ? null : (BaseEntity) val.getValue();
            if (stEntity != null) {
                st.setId(stEntity.getId());

                for (String s : stEntity.getAttributes()) {
                    Object obj = stEntity.getBaseValue(s).getValue();
                    if (obj == null) {
                        continue;
                    }
                    if (s.equals("code")) {
                        st.setCode((String) obj);
                    } else if (s.equals("name_ru")) {
                        st.setNameRu((String) obj);
                    } else if (s.equals("name_kz")) {
                        st.setNameKz((String) obj);
                    } else if (s.equals("kind_id")) {
//                        Shared kind = new Shared();
//                        kind.setId(((Integer) obj).longValue());
//                        st.setKind(kind);
                    } else if (s.equals("report_period_duration_months")) {
                        st.setReportPeriodDurationMonths((Integer) obj);
                    }
                }
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
    public void synchronize(List<PortalUser> users) {
        HashMap<Long, PortalUser> usersFromDB = new HashMap<>();
        HashMap<Long, PortalUser> usersFromPortal = new HashMap<>();

        SelectForUpdateStep select;

        select = context.select(
                EAV_A_USER.ID,
                EAV_A_USER.USER_ID,
                EAV_A_USER.FIRST_NAME,
                EAV_A_USER.LAST_NAME,
                EAV_A_USER.MIDDLE_NAME,
                EAV_A_USER.SCREEN_NAME,
                EAV_A_USER.EMAIL,
                EAV_A_USER.MODIFIED_DATE
        ).from(EAV_A_USER);

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            PortalUser user = new PortalUser();

            user.setId(BigInteger.valueOf(Long.parseLong(row.get(EAV_A_USER.ID.getName()).toString())));
            user.setUserId(Long.parseLong(row.get(EAV_A_USER.USER_ID.getName()).toString()));
            user.setFirstName((String) row.get(EAV_A_USER.FIRST_NAME.getName()));
            user.setLastName((String) row.get(EAV_A_USER.LAST_NAME.getName()));
            user.setMiddleName((String) row.get(EAV_A_USER.MIDDLE_NAME.getName()));
            user.setScreenName((String) row.get(EAV_A_USER.SCREEN_NAME.getName()));
            user.setEmailAddress((String) row.get(EAV_A_USER.EMAIL.getName()));
            user.setModifiedDate(DataUtils.convert((Timestamp) row.get(EAV_A_USER.MODIFIED_DATE.getName())));

            usersFromDB.put(user.getUserId(), user);
        }

        for (PortalUser user : users) usersFromPortal.put(user.getUserId(), user);

        Set<Long> toDelete = SetUtils.difference(usersFromDB.keySet(), usersFromPortal.keySet());
        Set<Long> toAdd = SetUtils.difference(usersFromPortal.keySet(), usersFromDB.keySet());
        Set<Long> toUpdate = SetUtils.intersection(usersFromPortal.keySet(), usersFromDB.keySet());

        for (Long id : toDelete) {
            DeleteConditionStep deleteFilter =
                    context.delete(EAV_A_CREDITOR_USER).
                            where(EAV_A_CREDITOR_USER.USER_ID.eq(id));

            jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());

            deleteFilter = context.delete(EAV_A_USER).where(EAV_A_USER.USER_ID.eq(id));

            jdbcTemplate.update(deleteFilter.getSQL(), deleteFilter.getBindValues().toArray());
        }

        for (Long id : toAdd) {
            PortalUser pu = usersFromPortal.get(id);

            InsertOnDuplicateStep insert = context.insertInto(
                    EAV_A_USER,
                    EAV_A_USER.USER_ID,
                    EAV_A_USER.FIRST_NAME,
                    EAV_A_USER.LAST_NAME,
                    EAV_A_USER.MIDDLE_NAME,
                    EAV_A_USER.SCREEN_NAME,
                    EAV_A_USER.EMAIL,
                    EAV_A_USER.MODIFIED_DATE
            ).values(
                    pu.getUserId(),
                    pu.getFirstName(),
                    pu.getLastName(),
                    pu.getMiddleName(),
                    pu.getScreenName(),
                    pu.getEmailAddress(),
                    DataUtils.convert(pu.getModifiedDate())
            );

            insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        }

        for (Long id : toUpdate) {
            PortalUser pu = usersFromPortal.get(id);

            Update update = context
                    .update(EAV_A_USER)
                    .set(EAV_A_USER.USER_ID, pu.getUserId())
                    .set(EAV_A_USER.FIRST_NAME, pu.getFirstName())
                    .set(EAV_A_USER.LAST_NAME, pu.getLastName())
                    .set(EAV_A_USER.MIDDLE_NAME, pu.getMiddleName())
                    .set(EAV_A_USER.SCREEN_NAME, pu.getScreenName())
                    .set(EAV_A_USER.EMAIL, pu.getEmailAddress())
                    .set(EAV_A_USER.MODIFIED_DATE, DataUtils.convert(pu.getModifiedDate()))
                    .where(EAV_A_USER.USER_ID.equal(id))
                    .and(EAV_A_USER.MODIFIED_DATE.lessThan(DataUtils.convert(pu.getModifiedDate())));

            updateWithStats(update.getSQL(), update.getBindValues().toArray());
        }
    }

    @Override
    public List<String> getAllowedClasses(long portalUserId) {
        Select select = context.select(EAV_A_USER_CLASS.META_NAME)
                .from(EAV_A_USER_CLASS)
                .where(EAV_A_USER_CLASS.USER_ID.eq(portalUserId));

        List<String> list = new ArrayList<>();

        List<Map<String, Object>> ret = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> m : ret) list.add((String) m.get(EAV_A_USER_CLASS.META_NAME.getName()));

        return list;
    }

    @Override
    public List<Long> getAllowedRefs(long portalUserId, String meta) {
        Select select = context.select(EAV_A_USER_REF.ENTITY_ID)
                .from(EAV_A_USER_REF)
                .where(EAV_A_USER_REF.USER_ID.eq(portalUserId))
                .and(EAV_A_USER_REF.META_NAME.eq(meta));

        List<Long> list = new ArrayList<>();

        List<Map<String, Object>> ret = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> m : ret)
            list.add(((BigDecimal) m.get(EAV_A_USER_REF.ENTITY_ID.getName())).longValue());

        return list;
    }

    @Override
    public PortalUser getUser(long userId) {
        Select select = context.select().from(EAV_A_USER).where(EAV_A_USER.USER_ID.eq(userId));

        List<Map<String, Object>> list = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (list == null || list.isEmpty())
            return null;

        Map<String, Object> row = list.get(0);

        PortalUser portalUser = new PortalUser();

        portalUser.setId(BigInteger.valueOf(Long.parseLong(row.get(EAV_A_USER.ID.getName()).toString())));
        portalUser.setUserId(Long.parseLong(row.get(EAV_A_USER.ID.getName()).toString()));
        portalUser.setScreenName((String) row.get(EAV_A_USER.SCREEN_NAME.getName()));
        portalUser.setEmailAddress((String) row.get(EAV_A_USER.EMAIL.getName()));
        portalUser.setFirstName((String) row.get(EAV_A_USER.FIRST_NAME.getName()));
        portalUser.setLastName((String) row.get(EAV_A_USER.LAST_NAME.getName()));
        portalUser.setMiddleName((String) row.get(EAV_A_USER.MIDDLE_NAME.getName()));
        portalUser.setModifiedDate(DataUtils.convert((Timestamp) row.get(EAV_A_USER.MODIFIED_DATE.getName())));
        portalUser.setActive(((BigDecimal) row.get(EAV_A_USER.IS_ACTIVE.getName())).longValue() == 1L);

        portalUser.setCreditorList(getPortalUserCreditorList(portalUser.getUserId()));

        return portalUser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PortalUser> getPortalUsersHavingAccessToCreditor(Creditor creditor) {
        Table root = EAV_A_USER.as("root");
        Table creditors = EAV_A_CREDITOR_USER.as("creditors");


        Select select = context.select(root.field(EAV_A_USER.ID),
                root.field(EAV_A_USER.USER_ID),
                root.field(EAV_A_USER.SCREEN_NAME),
                root.field(EAV_A_USER.EMAIL),
                root.field(EAV_A_USER.FIRST_NAME),
                root.field(EAV_A_USER.LAST_NAME),
                root.field(EAV_A_USER.MIDDLE_NAME),
                root.field(EAV_A_USER.MODIFIED_DATE),
                root.field(EAV_A_USER.IS_ACTIVE)
        ).from(root).join(creditors)
                .on(root.field(EAV_A_USER.USER_ID).eq(creditors.field(EAV_A_CREDITOR_USER.USER_ID)))
                .where(root.field(EAV_A_USER.IS_ACTIVE).eq(DataUtils.convert(true))
                        .and(creditors.field(EAV_A_CREDITOR_USER.CREDITOR_ID).eq(creditor.getId())));


        List<PortalUser> ret = jdbcTemplate.query(select.getSQL(),
                select.getBindValues().toArray(), new BeanPropertyRowMapper(PortalUser.class));

        return ret;
    }
}
