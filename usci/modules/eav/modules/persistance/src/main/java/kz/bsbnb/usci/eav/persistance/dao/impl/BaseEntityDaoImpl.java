package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_SET_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

@Repository
public class BaseEntityDaoImpl extends JDBCSupport implements IBaseEntityDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Qualifier("metaClassRepositoryImpl")
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;

    @Autowired
    private IEavOptimizerDao eavOptimizerDao;

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
        deleteRecursive(persistable.getId());
    }

    public IBaseEntity loadMock(long id) {
        MetaClass metaClass = (MetaClass) getMetaClass(id);
        return new BaseEntity(id, metaClass);
    }

    @Override
    public IBaseEntity load(long id, Date existingReportDate, Date savingReportDate) {
        if (id < 1)
            throw new IllegalArgumentException(Errors.compose(Errors.E93));

        if (existingReportDate == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E94));

        IBaseEntity baseEntity = loadMock(id);
        IBaseEntityReportDate baseEntityReportDate = baseEntityReportDateDao.load(id, existingReportDate);
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
            //long baseValuesCount = baseValueCounts.get(baseValueClass);
            //if (baseValuesCount > 0) {
                IBaseEntityValueDao baseEntityValueDao = persistableDaoPool
                        .getPersistableDao(baseValueClass, IBaseEntityValueDao.class);

                baseEntityValueDao.loadBaseValues(baseEntity, existingReportDate, savingReportDate);
            //}
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
            throw new IllegalArgumentException(Errors.compose(Errors.E91, baseEntityId));

        if (rows.size() < 1)
            throw new IllegalStateException(Errors.compose(Errors.E92, baseEntityId));

        Map<String, Object> row = rows.get(0);

        long classId = ((BigDecimal) row.get(EAV_BE_ENTITIES.as(tableAlias).CLASS_ID.getName())).longValue();
        return metaClassRepository.getMetaClass(classId);
    }

    @Override
    @Transactional
    public boolean deleteRecursive(long baseEntityId) {
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
            IBaseValueDao baseValueDao = persistableDaoPool.getPersistableDao(baseValueClass, IBaseValueDao.class);
            baseValueDao.deleteAll(baseEntityId);
        }

        IBaseEntityReportDateDao baseEntityReportDateDao = persistableDaoPool
                .getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        baseEntityReportDateDao.deleteAll(baseEntityId);

        eavOptimizerDao.delete(baseEntityId);

        String tableAlias = "e";
        Delete delete = context
                .delete(EAV_BE_ENTITIES.as(tableAlias))
                .where(EAV_BE_ENTITIES.as(tableAlias).ID.equal(baseEntityId));

        logger.debug(delete.toString());
        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count > 1)
            throw new IllegalStateException(Errors.compose(Errors.E89, baseEntityId));

        if (count < 1)
            throw new IllegalStateException(Errors.compose(Errors.E90, baseEntityId));

        return true;
    }

    @Override
    public boolean isUsed(long baseEntityId) {
        final String tableAliasCV = "cv";
        final String tableAliasCSV = "csv";

        Select selectCV = context
                .select(EAV_BE_COMPLEX_VALUES.as(tableAliasCV).ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_VALUES.as(tableAliasCV))
                .where(EAV_BE_COMPLEX_VALUES.as(tableAliasCV).ENTITY_VALUE_ID.eq(baseEntityId));

        List listCV = queryForListWithStats(selectCV.getSQL(), selectCV.getBindValues().toArray());

        if (listCV.size() > 0)
            return true;

        Select selectCSV = context
                .select(EAV_BE_COMPLEX_SET_VALUES.as(tableAliasCSV).ENTITY_VALUE_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES.as(tableAliasCSV))
                .where(EAV_BE_COMPLEX_SET_VALUES.as(tableAliasCSV).ENTITY_VALUE_ID.eq(baseEntityId));

        List listCSV = queryForListWithStats(selectCSV.getSQL(), selectCSV.getBindValues().toArray());

        return listCSV.size() > 0;
    }
}
