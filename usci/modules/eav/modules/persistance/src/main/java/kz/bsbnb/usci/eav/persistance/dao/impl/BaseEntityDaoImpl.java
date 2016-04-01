package kz.bsbnb.usci.eav.persistance.dao.impl;

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
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class BaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;

    @Autowired
    private IRefRepository refRepository;

    @Override
    public long insert(IPersistable persistable) {
        IBaseEntity baseEntity = (IBaseEntity) persistable;

        Insert insert = context
                .insertInto(EAV_BE_ENTITIES)
                .set(EAV_BE_ENTITIES.CLASS_ID, baseEntity.getMeta().getId());

        logger.debug(insert.toString());
        long baseEntityId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        baseEntity.setId(baseEntityId);

        return baseEntityId;
    }

    @Override
    public void update(IPersistable persistable) {
        throw new UnsupportedOperationException(Errors.compose(Errors.E2));
    }

    @Override
    public void delete(IPersistable persistable) {
        delete(persistable.getId());
        refRepository.delRef(persistable.getId());
    }

    protected void delete(long id) {
        String tableAlias = "e";
        Delete delete = context
                .delete(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count > 1)
            throw new IllegalStateException(Errors.compose(Errors.E89,id));

        if (count < 1)
            throw new IllegalStateException(Errors.compose(Errors.E90,id));

        refRepository.delRef(id);
    }

    public IBaseEntity loadMock(long id) {
        MetaClass metaClass = (MetaClass) getMetaClass(id);
        return new BaseEntity(id, metaClass);
    }

    @Override
    public IBaseEntity load(long id, Date reportDate) {
        if (id < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E93));

        if (reportDate == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E94));

        if (refRepository.getRef(id, reportDate) != null)
            return refRepository.getRef(id, reportDate);

        IBaseEntity baseEntity = loadMock(id);
        IBaseEntityReportDate baseEntityReportDate = baseEntityReportDateDao.load(id, reportDate);
        baseEntity.setBaseEntityReportDate(baseEntityReportDate);

        baseEntityReportDate.setBaseEntity(baseEntity);

        Map<Class<? extends IBaseValue>, Long> baseValueCounts = new HashMap<>();

        baseValueCounts.put(BaseEntityIntegerValue.class, baseEntityReportDate.getIntegerValuesCount());
        baseValueCounts.put(BaseEntityDateValue.class, baseEntityReportDate.getDateValuesCount());
        baseValueCounts.put(BaseEntityStringValue.class, baseEntityReportDate.getStringValuesCount());
        baseValueCounts.put(BaseEntityBooleanValue.class, baseEntityReportDate.getBooleanValuesCount());
        baseValueCounts.put(BaseEntityDoubleValue.class, baseEntityReportDate.getDoubleValuesCount());
        baseValueCounts.put(BaseEntityComplexValue.class, baseEntityReportDate.getComplexValuesCount());
        baseValueCounts.put(BaseEntitySimpleSet.class, baseEntityReportDate.getSimpleSetsCount());
        baseValueCounts.put(BaseEntityComplexSet.class, baseEntityReportDate.getComplexSetsCount());

        for (Class<? extends IBaseValue> baseValueClass : baseValueCounts.keySet()) {
            long baseValuesCount = baseValueCounts.get(baseValueClass);
            if (baseValuesCount > 0) {
                IBaseEntityValueDao baseEntityValueDao = persistableDaoPool
                        .getPersistableDao(baseValueClass, IBaseEntityValueDao.class);

                baseEntityValueDao.loadBaseValues(baseEntity, reportDate);
            }
        }

        if (baseEntity.getMeta().isReference())
            refRepository.setRef(id, reportDate, baseEntity);

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
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntityId));

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E92, baseEntityId));

        Map<String, Object> row = rows.get(0);

        long classId = ((BigDecimal) row.get(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID.getName())).longValue();
        return metaClassRepository.getMetaClass(classId);
    }

    @Override
    public boolean isUsed(long baseEntityId, long exceptContainingId) {
        String complexValuesTableAlias = "cv";
        String complexSetValuesTableAlias = "csv";
        String complexSetsTableAlias = "cs";

        Select select = context
                .selectOne()
                .where(DSL.exists(context.select(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ID)
                                .from(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias))
                                .where(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ENTITY_VALUE_ID.equal(baseEntityId))
                                .and(EAV_BE_COMPLEX_VALUES.as(complexValuesTableAlias).ENTITY_ID.notEqual(exceptContainingId))
                        )
                ).or(DSL.exists(context.select(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ID)
                        .from(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias)
                                .join(EAV_BE_ENTITY_COMPLEX_SETS.as(complexSetsTableAlias)).on(
                                        EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).SET_ID
                                                .equal(EAV_BE_ENTITY_COMPLEX_SETS.as(complexSetsTableAlias).ID))
                        ).where(EAV_BE_COMPLEX_SET_VALUES.as(complexSetValuesTableAlias).ENTITY_VALUE_ID.
                                equal(baseEntityId))
                        .and(EAV_BE_ENTITY_COMPLEX_SETS.as(complexSetsTableAlias).ENTITY_ID.
                                notEqual(exceptContainingId))));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return rows.size() > 0;
    }

    @Override
    public boolean isUsed(long baseEntityId) {
        String complexValuesTableAlias = "cv";
        String complexSetValuesTableAlias = "csv";

        Select select = context
                .selectOne()
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
    public boolean deleteRecursive(long baseEntityId) {
        IMetaClass metaClass = getMetaClass(baseEntityId);
        return deleteRecursive(baseEntityId, metaClass);
    }

    public boolean deleteRecursive(long baseEntityId, IMetaClass metaClass) {
        boolean baseEntityUsed = isUsed(baseEntityId);
        if (baseEntityUsed || metaClass.isReference()) {
            return false;
        }

        Set<Class<? extends IBaseValue>> baseValueClasses = new HashSet<>();
        baseValueClasses.add(BaseEntityBooleanValue.class);
        baseValueClasses.add(BaseEntityDateValue.class);
        baseValueClasses.add(BaseEntityDoubleValue.class);
        baseValueClasses.add(BaseEntityIntegerValue.class);
        baseValueClasses.add(BaseEntityStringValue.class);
        baseValueClasses.add(BaseEntityComplexValue.class);
        baseValueClasses.add(BaseEntitySimpleSet.class);
        baseValueClasses.add(BaseEntityComplexSet.class);

        for (Class<? extends IBaseValue> baseValueClass : baseValueClasses) {
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
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityId) {
        Set<Long> allChildBaseEntitiesIds = new HashSet<>();

        // Complex values
        IBaseEntityComplexValueDao baseEntityComplexValueDao = persistableDaoPool
                .getPersistableDao(BaseEntityComplexValue.class, IBaseEntityComplexValueDao.class);
        Set<Long> complexValuesBaseEntitiesIds = baseEntityComplexValueDao
                .getChildBaseEntityIdsWithoutRefs(parentBaseEntityId);
        for (Long complexValuesBaseEntitiesId : complexValuesBaseEntitiesIds) {
            Set<Long> childBaseEntitiesIds = getChildBaseEntityIds(complexValuesBaseEntitiesId);
            allChildBaseEntitiesIds.addAll(childBaseEntitiesIds);
        }
        allChildBaseEntitiesIds.addAll(complexValuesBaseEntitiesIds);

        // Complex sets
        IBaseEntityComplexSetDao baseEntityComplexSetDao = persistableDaoPool
                .getPersistableDao(BaseEntityComplexSet.class, IBaseEntityComplexSetDao.class);
        Set<Long> complexSetsBaseEntitiesIds = baseEntityComplexSetDao
                .getChildBaseEntityIdsWithoutRefs(parentBaseEntityId);
        for (Long complexSetsBaseEntitiesId : complexSetsBaseEntitiesIds) {
            Set<Long> childBaseEntitiesIds = getChildBaseEntityIds(complexSetsBaseEntitiesId);
            allChildBaseEntitiesIds.addAll(childBaseEntitiesIds);
        }
        allChildBaseEntitiesIds.addAll(complexSetsBaseEntitiesIds);

        return allChildBaseEntitiesIds;
    }
}
