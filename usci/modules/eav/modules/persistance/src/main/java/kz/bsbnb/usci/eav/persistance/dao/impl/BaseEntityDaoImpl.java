package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

/**
 * @author alexandr.motov
 */
@Repository
public class BaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao {

    private final Logger logger = LoggerFactory.getLogger(BaseEntityDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Override
    public long insert(IPersistable persistable) {
        IBaseEntity baseEntity = (IBaseEntity)persistable;
        long baseEntityId =
                insert(
                        baseEntity.getMeta().getId()
                );
        baseEntity.setId(baseEntityId);

        return baseEntityId;
    }

    protected long insert(long metaClassId) {
        Insert insert = context
                .insertInto(EAV_BE_ENTITIES)
                .set(EAV_BE_ENTITIES.CLASS_ID, metaClassId);

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(IPersistable persistable) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void delete(IPersistable persistable) {
        IBaseEntity baseEntity = (BaseEntity)persistable;
        if(baseEntity.getOperation() == OperationType.DELETE){
            Update update = context
                    .update(EAV_BE_ENTITIES)
                    .set(EAV_BE_ENTITIES.DELETED, DataUtils.convert(true))
                    .where(EAV_BE_ENTITIES.ID.equal(baseEntity.getId()));

            logger.debug(update.toString());
            int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());
            if (count > 1)
            {
                throw new RuntimeException("DELETE operation should update only one record. ID: " + baseEntity.getId());
            }
            if (count < 1)
            {
                logger.warn("DELETE operation should update a record. ID: " + baseEntity.getId());
            }
        }else
            delete(persistable.getId());
    }

    protected void delete(long id)
    {
        String tableAlias = "e";
        Delete delete = context
                .delete(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());
        if (count > 1)
        {
            throw new RuntimeException("DELETE operation should be delete only one record. ID: " + id);
        }
        if (count < 1)
        {
            logger.warn("DELETE operation should delete a record. ID: " + id);
        }
    }

    @Override
    public boolean isDeleted(long id)
    {
        Select select = context.select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.ID.eq(id))
                .and(EAV_BE_ENTITIES.DELETED.eq(DataUtils.convert(true)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
        return rows.size() > 0;
    }

    public IBaseEntity load(long id)
    {
        MetaClass metaClass = (MetaClass)getMetaClass(id);
        return new BaseEntity(id, metaClass);
    }

    @Override
    public IBaseEntity load(long id, Date reportDate, Date actualReportDate)
    {
        if(id < 1)
        {
            throw new IllegalArgumentException("Does not have id. Can't load.");
        }

        if (reportDate == null)
        {
            throw new IllegalArgumentException("To load instance of BaseEntity must always " +
                    "be specified report date.");
        }

        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        IBaseEntity baseEntity = load(id);
        IBaseEntityReportDate baseEntityReportDate =
                baseEntityReportDateDao.load(id, reportDate);
        baseEntity.setBaseEntityReportDate(baseEntityReportDate);

        Map<Class<? extends IBaseValue>, Long> baseValueCounts =
                new HashMap<Class<? extends IBaseValue>, Long>();
        baseValueCounts.put(BaseEntityIntegerValue.class, baseEntityReportDate.getIntegerValuesCount());
        baseValueCounts.put(BaseEntityDateValue.class, baseEntityReportDate.getDateValuesCount());
        baseValueCounts.put(BaseEntityStringValue.class, baseEntityReportDate.getStringValuesCount());
        baseValueCounts.put(BaseEntityBooleanValue.class, baseEntityReportDate.getBooleanValuesCount());
        baseValueCounts.put(BaseEntityDoubleValue.class, baseEntityReportDate.getDoubleValuesCount());
        baseValueCounts.put(BaseEntityComplexValue.class, baseEntityReportDate.getComplexValuesCount());
        baseValueCounts.put(BaseEntitySimpleSet.class, baseEntityReportDate.getSimpleSetsCount());
        baseValueCounts.put(BaseEntityComplexSet.class, baseEntityReportDate.getComplexSetsCount());

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        boolean last = DataTypeUtil.compareBeginningOfTheDay(maxReportDate, reportDate) == 0;

        for (Class<? extends IBaseValue> baseValueClass: baseValueCounts.keySet())
        {
            long baseValuesCount = baseValueCounts.get(baseValueClass);
            if (baseValuesCount > 0)
            {
                IBaseEntityValueDao baseEntityValueDao = persistableDaoPool
                        .getPersistableDao(baseValueClass, IBaseEntityValueDao.class);
                int compare = DataTypeUtil.compareBeginningOfTheDay(actualReportDate, reportDate);
                baseEntityValueDao.loadBaseValues(baseEntity, compare == -1 ? reportDate : actualReportDate, last);
            }
        }

        return baseEntity;
    }

    @Override
    public IMetaClass getMetaClass(long baseEntityId) {
        String tableAlias = "e";
        Select select = context
                .select(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID)
                .from(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(baseEntityId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new IllegalArgumentException("More then one instance of BaseEntity found.");
        }

        if (rows.size() < 1)
        {
            throw new IllegalStateException("Instance of BaseEntity was not found.");
        }

        Map<String, Object> row = rows.get(0);
        if(row != null)
        {
            long classId = ((BigDecimal)row.get(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID.getName())).longValue();
            return metaClassRepository.getMetaClass(classId);

        }
        else
        {
            logger.error("Can't load instance of BaseEntity, empty data set.");
        }
        return null;
    }

    @Override
    public boolean isUsed(long baseEntityId)
    {
        String complexValuesTableAlias = "cv";
        String complexSetValuesTableAlias = "csv";

        //TODO: refactor, remove dual, make selectOne()
        Select select = context
                .select(DSL.val(1L).as("ex_flag"))
                .from("dual")
                .where(DSL.exists(context
                        .select(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ID)
                        .from(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias))
                        .where(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ENTITY_VALUE_ID.equal(baseEntityId))))
                .or(DSL.exists(context
                        .select(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ID)
                        .from(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias))
                        .where(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ENTITY_VALUE_ID.equal(baseEntityId))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.size() > 0;
    }

    @Override
    //@Transactional
    public boolean deleteRecursive(long baseEntityId)
    {
        IMetaClass metaClass = getMetaClass(baseEntityId);
        return deleteRecursive(baseEntityId, metaClass);
    }

    public boolean deleteRecursive(long baseEntityId, IMetaClass metaClass)
    {
        boolean baseEntityUsed = isUsed(baseEntityId);
        if (baseEntityUsed || metaClass.isReference())
        {
            return false;
        }

        Set<Class<? extends IBaseValue>> baseValueClasses =
                new HashSet<Class<? extends IBaseValue>>();
        baseValueClasses.add(BaseEntityBooleanValue.class);
        baseValueClasses.add(BaseEntityDateValue.class);
        baseValueClasses.add(BaseEntityDoubleValue.class);
        baseValueClasses.add(BaseEntityIntegerValue.class);
        baseValueClasses.add(BaseEntityStringValue.class);
        baseValueClasses.add(BaseEntityComplexValue.class);
        baseValueClasses.add(BaseEntitySimpleSet.class);
        baseValueClasses.add(BaseEntityComplexSet.class);

        for (Class<? extends IBaseValue> baseValueClass: baseValueClasses)
        {
            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueClass, IBaseValueDao.class);
            baseValueDao.deleteAll(baseEntityId);
        }

        IBaseEntityReportDateDao baseEntityReportDateDao = persistableDaoPool
                .getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        baseEntityReportDateDao.deleteAll(baseEntityId);
        delete(baseEntityId);

        return true;
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityId)
    {
        Set<Long> allChildBaseEntitiesIds = new HashSet<Long>();

        // Complex values
        IBaseEntityComplexValueDao baseEntityComplexValueDao = persistableDaoPool
                .getPersistableDao(BaseEntityComplexValue.class, IBaseEntityComplexValueDao.class);
        Set<Long> complexValuesBaseEntitiesIds = baseEntityComplexValueDao
                .getChildBaseEntityIdsWithoutRefs(parentBaseEntityId);
        for (Long complexValuesBaseEntitiesId: complexValuesBaseEntitiesIds)
        {
            Set<Long> childBaseEntitiesIds = getChildBaseEntityIds(complexValuesBaseEntitiesId);
            allChildBaseEntitiesIds.addAll(childBaseEntitiesIds);
        }
        allChildBaseEntitiesIds.addAll(complexValuesBaseEntitiesIds);

        // Complex sets
        IBaseEntityComplexSetDao baseEntityComplexSetDao = persistableDaoPool
                .getPersistableDao(BaseEntityComplexSet.class, IBaseEntityComplexSetDao.class);
        Set<Long> complexSetsBaseEntitiesIds = baseEntityComplexSetDao
                .getChildBaseEntityIdsWithoutRefs(parentBaseEntityId);
        for (Long complexSetsBaseEntitiesId: complexSetsBaseEntitiesIds)
        {
            Set<Long> childBaseEntitiesIds = getChildBaseEntityIds(complexSetsBaseEntitiesId);
            allChildBaseEntitiesIds.addAll(childBaseEntitiesIds);
        }
        allChildBaseEntitiesIds.addAll(complexSetsBaseEntitiesIds);

        return allChildBaseEntitiesIds;
    }


    @Override
    public long getRandomBaseEntityId(IMetaClass metaClass)
    {
        return getRandomBaseEntityId(metaClass.getId());
    }

    @Override
    public long getRandomBaseEntityId(long metaClassId)
    {
        String tableAlias = "e";
        Select select = context
                .select(EAV_BE_ENTITIES.as(tableAlias).ID)
                .from(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID.equal(metaClassId))
                .limit(1);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            return ((BigDecimal)(rows.get(0).get(EAV_BE_ENTITIES.ID.getName()))).longValue();
        }

        return 0;
    }

}
