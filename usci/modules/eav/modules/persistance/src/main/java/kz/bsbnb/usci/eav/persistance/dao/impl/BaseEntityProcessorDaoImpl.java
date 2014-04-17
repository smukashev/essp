package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.base.impl.value.*;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
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
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@SuppressWarnings("unchecked")
@Repository
public class BaseEntityProcessorDaoImpl extends JDBCSupport implements IBaseEntityProcessorDao
{
    private final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorDaoImpl.class);

    @Autowired
    IBatchRepository batchRepository;
    @Autowired
    IMetaClassRepository metaClassRepository;
    @Autowired
    IBaseEntityRepository baseEntityCacheDao;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    public IBaseEntity loadByMaxReportDate(long id, Date actualReportDate, boolean caching)
    {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (maxReportDate == null)
        {
            throw new RuntimeException("No data found on report date " + actualReportDate + ".");
        }
        return load(id, maxReportDate, actualReportDate, caching);
    }

    public IBaseEntity loadByMaxReportDate(long id, Date reportDate)
    {
        return loadByMaxReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity load(long id)
    {
        return load(id, false);
    }

    @Override
    public IBaseEntity load(long id, boolean caching)
    {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        if (maxReportDate == null)
        {
            throw new UnsupportedOperationException("Not found appropriate report date.");
        }

        if (caching)
        {
            return baseEntityCacheDao.getBaseEntity(id, maxReportDate);
        }

        return load(id, maxReportDate, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching)
    {
        if (caching)
        {
            return baseEntityCacheDao.getBaseEntity(id, actualReportDate);
        }

        return load(id, maxReportDate, actualReportDate);
    }

    public IBaseEntity load(long id, Date reportDate, Date actualReportDate)
    {
        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.load(id, reportDate, actualReportDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long search(IBaseEntity baseEntity)
    {
        IMetaClass metaClass = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName())
                .findSingle((BaseEntity)baseEntity);

        return baseEntityId == null ? 0 : baseEntityId;
    }

    public List<Long> search(long metaClassId)
    {
        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Long> baseEntityIds = new ArrayList<Long>();
        for (Map<String, Object> row: rows)
        {
            baseEntityIds.add(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return baseEntityIds;
    }

    public List<Long> search(String className)
    {
        MetaClass metaClass = metaClassRepository.getMetaClass(className);
        if (metaClass != null)
        {
            return search(metaClass.getId());
        }

        return new ArrayList<Long>();
    }


    public IBaseEntity prepare(IBaseEntity baseEntity)
    {
        IMetaClass metaClass = baseEntity.getMeta();
        for (String attribute: baseEntity.getAttributes())
        {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isComplex())
            {
                IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                if (baseValue.getValue() != null)
                {
                    if (metaType.isSet())
                    {
                        IMetaSet childMetaSet = (IMetaSet)metaType;
                        IMetaType childMetaType = childMetaSet.getMemberType();
                        if (childMetaType.isSet())
                        {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }
                        else
                        {
                            IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                            for (IBaseValue childBaseValue: childBaseSet.get())
                            {
                                IBaseEntity childBaseEntity = (IBaseEntity)childBaseValue.getValue();
                                if (childBaseEntity.getValueCount() != 0)
                                {
                                    prepare((IBaseEntity)childBaseValue.getValue());
                                }
                            }
                        }
                    }
                    else
                    {
                        IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                        if (childBaseEntity.getValueCount() != 0)
                        {
                            prepare((IBaseEntity)baseValue.getValue());
                        }
                    }
                }
            }
        }

        if (metaClass.isSearchable())
        {
            long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
            {
                baseEntity.setId(baseEntityId);
            }
        }

        return baseEntity;
    }

    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager)
    {
        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++)
        {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable insertedObject: insertedObjects)
                {
                    persistableDao.insert(insertedObject);
                }
            }
        }

        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++)
        {
                Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> updatedObjects = baseEntityManager.getUpdatedObjects(objectClass);
            if (updatedObjects != null && updatedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable updatedObject: updatedObjects)
                {
                    persistableDao.update(updatedObject);
                }
            }
        }

        for (int i = BaseEntityManager.CLASS_PRIORITY.size() - 1; i >= 0; i--)
        {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> deletedObjects = baseEntityManager.getDeletedObjects(objectClass);
            if (deletedObjects != null && deletedObjects.size() != 0)
            {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable deletedObject: deletedObjects)
                {
                    persistableDao.delete(deletedObject);
                }
            }
        }

        for (IBaseEntity unusedBaseEntity: baseEntityManager.getUnusedBaseEntities())
        {
            IBaseEntityDao baseEntityDao = persistableDaoPool
                    .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
            baseEntityDao.deleteRecursive(unusedBaseEntity.getId(), unusedBaseEntity.getMeta());
        }
    }

    private IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager)
    {
        if (baseEntityForSave.getId() < 1 || baseEntityForSave.getMeta().isSearchable() == false)
        {
            return applyBaseEntityBasic(baseEntityForSave, baseEntityManager);
        }
        else
        {
            Date reportDate = baseEntityForSave.getReportDate();
            IBaseEntityReportDateDao baseEntityReportDateDao =
                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
            Date maxReportDate = baseEntityReportDateDao
                    .getMaxReportDate(baseEntityForSave.getId(), reportDate);

            if (maxReportDate == null)
            {
                throw new UnsupportedOperationException("Not yet implemented. Entity ID: " + baseEntityForSave.getId());
                //return applyBaseEntityAdvanced(baseEntityForSave, baseEntityManager);
            }
            else
            {
                IBaseEntity baseEntityLoaded = load(baseEntityForSave.getId(), maxReportDate, reportDate);
                try {
                    return applyBaseEntityAdvanced(baseEntityForSave, baseEntityLoaded, baseEntityManager);
                } catch (IllegalStateException ex) {
                    logger.error("ILLEGAL_STATE_ERROR in applyBaseEntityAdvanced " + ex.getMessage() + "\n" +
                            "REPORT_DATE_EXISTS ERROR:\n" +
                            "DATA DUMP\n" +
                            "baseEntityForSave: \n" +
                            baseEntityForSave +
                            "baseEntityLoaded: \n" +
                            baseEntityLoaded);

                    throw ex;
                }
            }
        }
    }

    private IBaseEntity applyBaseEntityBasic(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager)
    {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null) {
            return foundProcessedBaseEntity;
        }

        IBaseEntity baseEntityApplied =
                new BaseEntity(
                        baseEntitySaving.getMeta(),
                        baseEntitySaving.getReportDate()
                );
        for (String attribute: baseEntitySaving.getAttributes())
        {
            IBaseValue baseValue = baseEntitySaving.getBaseValue(attribute);
            if (baseValue.getValue() != null)
            {
                applyBaseValueBasic(baseEntityApplied, baseValue, baseEntityManager);
            }
        }

        baseEntityApplied.calculateValueCount();
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    /*private IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager)
    {
        IBaseEntity baseEntityApplied =
                new BaseEntity(
                        baseEntitySaving.getId(),
                        baseEntitySaving.getMeta(),
                        baseEntitySaving.getReportDate()
                );
        for (String attribute: baseEntitySaving.getAttributes())
        {
            IBaseValue baseValue = baseEntitySaving.getBaseValue(attribute);
            applyBaseValueAdvanced(baseEntityApplied, baseValue, baseEntityManager);
        }

        baseEntityApplied.calculateValueCount();
        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);

        return baseEntityApplied;
    }*/



    private IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                                                IBaseEntityManager baseEntityManager)
    {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);
        if (foundProcessedBaseEntity != null) {
            return foundProcessedBaseEntity;
        }

        IMetaClass metaClass = baseEntitySaving.getMeta();
        IBaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded, baseEntitySaving.getReportDate());

        for (String attribute: metaClass.getAttributeNames())
        {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

            // Not present in saving instance of BaseEntity
            if (baseValueSaving == null)
            {
                continue;
            }

            IBaseContainer baseContainer = baseValueSaving.getBaseContainer();
            if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
            {
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "contained not in the instance of BaseEntity is not possible.");
            }

            IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
            if (metaAttribute == null)
            {
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "without meta data is not possible.");
            }

            IMetaType metaType = metaAttribute.getMetaType();
            if (metaType.isComplex())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                if (metaType.isSet())
                {
                    applyComplexSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
                else
                {
                    applyComplexValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
            }
            else
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                if (metaType.isSet())
                {
                    applySimpleSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
                else
                {
                    applySimpleValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                }
            }
        }

        // Calculate values count
        baseEntityApplied.calculateValueCount();

        // Register instance of BaseEntityReportDate
        IBaseEntityReportDate baseEntityReportDate =
                baseEntityApplied.getBaseEntityReportDate();

        Date reportDateSaving = baseEntitySaving.getReportDate();
        Date reportDateLoaded = baseEntityLoaded.getReportDate();
        int reportDateCompare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
        if (reportDateCompare == 0)
        {
            baseEntityManager.registerAsUpdated(baseEntityReportDate);
        }
        else
        {
            boolean reportDateExists = checkReportDateExists(baseEntityLoaded.getId(),
                    baseEntityReportDate.getReportDate());

            if (reportDateExists) {
                logger.error("REPORT_DATE_EXISTS ERROR:\n" +
                        "DATA DUMP\n" +
                        "baseEntitySaving: \n" +
                        baseEntitySaving +
                        "baseEntityLoaded: \n" +
                        baseEntityLoaded +
                        "baseEntityApplied: \n" +
                        baseEntityApplied);

                throw new IllegalStateException("Report date " + baseEntityReportDate.getReportDate() + " already exists");
            }

            baseEntityManager.registerAsInserted(baseEntityReportDate);
        }

        //TODO:Remove unused instances of BaseEntity

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    protected void applyBaseValueBasic(IBaseEntity baseEntityApplied, IBaseValue baseValue, IBaseEntityManager baseEntityManager)
    {
        if (baseValue.getValue() == null)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes with null value is not possible.");
        }

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes contained not in the instance " +
                    "of BaseEntity is not possible.");
        }

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null)
        {
            throw new RuntimeException("Basic applying instance of BaseValue changes without meta data is not possible.");
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex())
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();

                // TODO: Add implementation of immutable complex values in sets

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue: childBaseSet.get())
                {
                    IBaseEntity childBaseEntity = (IBaseEntity)childBaseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue childBaseValueApplied =
                            BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaSet.getMemberType(),
                                    childBaseValue.getBatch(),
                                    childBaseValue.getIndex(),
                                    new Date(baseValue.getRepDate().getTime()),
                                    childBaseEntityApplied,
                                    false,
                                    true);
                    childBaseSetApplied.put(childBaseValueApplied);
                    baseEntityManager.registerAsInserted(childBaseValueApplied);
                }

                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValue.getBatch(),
                        baseValue.getIndex(),
                        new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true
                );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
            else
            {
                /*boolean last = true;
                if (metaAttribute.isFinal())
                {
                    IBaseValueDao valueDao = persistableDaoPool
                            .getPersistableDao(baseValue.getClass(), IBaseValueDao.class);
                    IBaseValue lastBaseValue = valueDao.getLastBaseValue(baseValue);
                    if (lastBaseValue != null)
                    {

                    }
                }*/

                if (metaAttribute.isImmutable())
                {
                    IMetaClass childMetaClass = (IMetaClass)metaType;
                    IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                    if (childBaseEntity.getValueCount() != 0)
                    {
                        if (childBaseEntity.getId() < 1)
                        {
                            throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                    childBaseEntity.getMeta().getClassName() + "\n" + childBaseEntity.toString());
                        }

                        IBaseEntity childBaseEntityImmutable = loadByMaxReportDate(childBaseEntity.getId(),                                childBaseEntity.getReportDate(), childMetaClass.isReference());
                        if (childBaseEntityImmutable == null)
                        {
                            throw new RuntimeException(String.format("Instance of BaseEntity with id {0} not found in the DB.",
                                    childBaseEntity.getId()));
                        }

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                childBaseEntityImmutable,
                                false,
                                true
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {
                    IBaseEntity childBaseEntity = (IBaseEntity)baseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValue.getBatch(),
                            baseValue.getIndex(),
                            new Date(baseValue.getRepDate().getTime()),
                            childBaseEntityApplied,
                            false,
                            true
                    );

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
        else
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                IMetaValue childMetaValue = (IMetaValue)childMetaSet.getMemberType();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue: childBaseSet.get())
                {
                    IBaseValue childBaseValueApplied =
                            BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaSet.getMemberType(),
                                    childBaseValue.getBatch(),
                                    childBaseValue.getIndex(),
                                    new Date(baseValue.getRepDate().getTime()),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValue.getValue()).getTime()) :
                                            childBaseValue.getValue(),
                                    false,
                                    true);
                    childBaseSetApplied.put(childBaseValueApplied);
                    baseEntityManager.registerAsInserted(childBaseValueApplied);
                }

                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValue.getBatch(),
                        baseValue.getIndex(),
                        new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true
                );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
            else
            {
                IMetaValue metaValue = (IMetaValue)metaType;
                IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValue.getValue()).getTime()) :
                                        baseValue.getValue(),
                                false,
                                true
                        );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
        }
    }

    /*protected void applyBaseValueAdvanced(IBaseEntity baseEntityApplied, IBaseValue baseValue,
                                          IBaseEntityManager baseEntityManager)
    {
        if (baseValue.getValue() == null)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes with null value is not possible.");
        }

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer == null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes contained not in the instance " +
                    "of BaseEntity is not possible.");
        }

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null)
        {
            throw new RuntimeException("Advanced applying instance of BaseValue changes without meta data is not possible.");
        }

        IBaseValueDao valueDao = persistableDaoPool
                .getPersistableDao(baseValue.getClass(), IBaseValueDao.class);
        IBaseValue nextBaseValue = valueDao.getNextBaseValue(baseValue);

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex())
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }
            }
            else
            {

            }
        }
        else
        {
            if (metaType.isSet())
            {
                if (metaType.isSetOfSets())
                {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet)metaType;
                IMetaValue childMetaValue = (IMetaValue)childMetaSet.getMemberType();
                IBaseSet childBaseSet = (IBaseSet)baseValue.getValue();
                if (nextBaseValue != null)
                {
                    IBaseSet childNextBaseSet = (IBaseSet)nextBaseValue.getValue();
                    if (baseValue.equalsByValue(nextBaseValue))
                    {
                        IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());

                        boolean baseValueFound = false;
                        Set<UUID> processedUuids = new HashSet<UUID>();
                        for (IBaseValue childBaseValue: childBaseSet.get())
                        {
                            baseValueFound = false;
                            for (IBaseValue childNextBaseValue: childNextBaseSet.get())
                            {
                                if (processedUuids.contains(childNextBaseValue.getUuid()))
                                {
                                    continue;
                                }
                                else
                                {
                                    if (childBaseValue.equalsByValue(childMetaValue, childNextBaseValue))
                                    {
                                        // Mark as processed and found
                                        processedUuids.add(childNextBaseValue.getUuid());
                                        baseValueFound = true;

                                        // Set new report date
                                        childNextBaseValue.setRepDate(
                                                new Date(childBaseValue.getRepDate().getTime()));

                                        // Put in the result set and register as updated
                                        childBaseSetApplied.put(childNextBaseValue);
                                        baseEntityManager.registerAsUpdated(childNextBaseValue);

                                        break;
                                    }
                                }
                            }

                            if (baseValueFound)
                            {
                                continue;
                            }

                            IBaseValue childBaseValueApplied =
                                    BaseValueFactory.create(
                                            childMetaSet.getType(),
                                            childMetaSet.getMemberType(),
                                            childBaseValue.getBatch(),
                                            childBaseValue.getIndex(),
                                            new Date(baseValue.getRepDate().getTime()),
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date)childBaseValue.getValue()).getTime()) :
                                                    childBaseValue.getValue(),
                                            false,
                                            false);
                            childBaseSetApplied.put(childBaseValueApplied);
                            baseEntityManager.registerAsInserted(childBaseValueApplied);
                        }

                        baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                true
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {

                }
            }
            else
            {
                if (nextBaseValue != null)
                {
                    if (baseValue.equalsByValue(nextBaseValue))
                    {
                        nextBaseValue.setRepDate(new Date(baseValue.getRepDate().getTime()));
                        baseEntityApplied.put(metaAttribute.getName(), nextBaseValue);
                        baseEntityManager.registerAsUpdated(nextBaseValue);
                    }
                    else
                    {
                        IMetaValue metaValue = (IMetaValue)metaType;
                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValue.getBatch(),
                                baseValue.getIndex(),
                                new Date(baseValue.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValue.getValue()).getTime()) :
                                        baseValue.getValue(),
                                false,
                                false
                        );

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                else
                {
                    IMetaValue metaValue = (IMetaValue)metaType;
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValue.getBatch(),
                            baseValue.getIndex(),
                            new Date(baseValue.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValue.getValue()).getTime()) :
                                    baseValue.getValue(),
                            false,
                            true
                    );

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }*/

    protected void applySimpleSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                      IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        IMetaSet childMetaSet = (IMetaSet)metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();
        IMetaValue childMetaValue = (IMetaValue)childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet)baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;
        if (baseValueLoaded != null)
        {
            childBaseSetLoaded = (IBaseSet)baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0)
            {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;

                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            childBaseSetLoaded,
                            true,
                            baseValueLoaded.isLast());
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetLoaded,
                            true,
                            baseValueLoaded.isLast());
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                childBaseSetLoaded,
                                baseValueLoaded.isClosed(),
                                false);
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }
                }
            }
            else
            {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        childBaseSetApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
        }
        else
        {
            if (childBaseSetSaving == null)
            {
                return;
            }

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null)
            {
                childBaseSetLoaded = (IBaseSet)baseValueClosed.getValue();
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueClosed.getId(),
                        baseValueClosed.getBatch(),
                        baseValueClosed.getIndex(),
                        new Date(baseValueClosed.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        baseValueClosed.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsUpdated(baseValueApplied);
            }
            else
            {
                IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                if (baseValuePrevious != null)
                {
                    childBaseSetLoaded = (IBaseSet)baseValuePrevious.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            baseValuePrevious.isLast());
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValuePrevious.isLast())
                    {
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
                else
                {
                    childBaseSetApplied = new BaseSet(childMetaType);
                    baseEntityManager.registerAsInserted(childBaseSetApplied);

                    // TODO: Check next value

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            true);
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }

        Set<UUID> processedUuids = new HashSet<UUID>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0)
        {
            boolean baseValueFound;
            for (IBaseValue childBaseValueSaving: childBaseSetSaving.get())
            {

                if (childBaseSetLoaded != null)
                {
                    baseValueFound = false;
                    for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
                    {
                        if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                        {
                            continue;
                        }

                        if (childBaseValueSaving.equalsByValue(childMetaValue, childBaseValueLoaded))
                        {
                            // Mark as processed and found
                            processedUuids.add(childBaseValueLoaded.getUuid());
                            baseValueFound = true;

                            IBaseValue baseValueApplied = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getBatch(),
                                    childBaseValueLoaded.getIndex(),
                                    new Date(childBaseValueLoaded.getRepDate().getTime()),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                            childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    childBaseValueLoaded.isLast());
                            childBaseSetApplied.put(baseValueApplied);
                            break;
                        }
                    }

                    if (baseValueFound)
                    {
                        continue;
                    }
                }

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);

                // Check closed value
                IBaseValue baseValueForSearch = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        new Date(childBaseValueSaving.getRepDate().getTime()),
                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        childBaseValueSaving.isClosed(),
                        childBaseValueSaving.isLast());
                baseValueForSearch.setBaseContainer(childBaseSetApplied);

                IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);
                if (childBaseValueClosed != null)
                {
                    childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsDeleted(childBaseValueClosed);

                    IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                    if (childBaseValueClosed.isLast())
                    {
                        childBaseValuePrevious.setIndex(childBaseValueSaving.getIndex());
                        childBaseValuePrevious.setBatch(childBaseValueSaving.getBatch());
                        childBaseValuePrevious.setLast(true);

                        childBaseSetApplied.put(childBaseValuePrevious);
                        baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                    }
                    else
                    {
                        childBaseSetApplied.put(childBaseValuePrevious);
                    }

                    continue;
                }

                // Check next value
                IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                if (childBaseValueNext != null)
                {
                    childBaseValueNext.setBatch(childBaseValueSaving.getBatch());
                    childBaseValueNext.setIndex(childBaseValueSaving.getIndex());
                    childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                    childBaseSetApplied.put(childBaseValueNext);
                    baseEntityManager.registerAsUpdated(childBaseValueNext);
                    continue;
                }


                IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                if (childBaseValueLast != null)
                {
                    childBaseValueLast.setBaseContainer(childBaseSetApplied);
                    childBaseValueLast.setBatch(childBaseValueSaving.getBatch());
                    childBaseValueLast.setIndex(childBaseValueSaving.getIndex());
                    childBaseValueLast.setLast(false);

                    baseEntityManager.registerAsUpdated(childBaseValueLast);
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        childBaseValueSaving.getRepDate(),
                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        false,
                        true
                );
                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }

        if (childBaseSetLoaded != null)
        {
            for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
            {
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                {
                    continue;
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = childBaseValueLoaded.getRepDate();
                boolean reportDateEquals = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                if (reportDateEquals)
                {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);
                    boolean last = childBaseValueLoaded.isLast();

                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext != null && childBaseValueNext.isClosed())
                    {
                        baseEntityManager.registerAsDeleted(childBaseValueNext);

                        last = childBaseValueNext.isLast();
                    }

                    if (last) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                        if (childBaseValuePrevious != null)
                        {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setBatch(baseValueSaving.getBatch());
                            childBaseValuePrevious.setIndex(baseValueSaving.getIndex());
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                }
                else
                {
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext == null || !childBaseValueNext.isClosed())
                    {
                        IBaseValue childBaseValue = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                baseValueSaving.getRepDate(),
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                        childBaseValueLoaded.getValue(),
                                true,
                                childBaseValueLoaded.isLast()
                        );
                        childBaseValue.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsInserted(childBaseValue);

                        if (childBaseValueLoaded.isLast())
                        {
                            IBaseValue childBaseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getId(),
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    childBaseValueLoaded.getRepDate(),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date)childBaseValueLoaded.getValue()).getTime()) :
                                            childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    false
                            );
                            childBaseValueLast.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsUpdated(childBaseValueLast);
                        }
                    }
                    else
                    {
                        childBaseValueNext.setBaseContainer(childBaseSetApplied);
                        childBaseValueNext.setBatch(baseValueSaving.getBatch());
                        childBaseValueNext.setIndex(baseValueSaving.getIndex());
                        childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                    }
                }
            }
        }
    }

    protected void applyComplexSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                  IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        IMetaSet childMetaSet = (IMetaSet)metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();
        IMetaClass childMetaClass = (IMetaClass)childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet)baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;
        if (baseValueLoaded != null)
        {
            childBaseSetLoaded = (IBaseSet)baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0)
            {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                //set deletion
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;

                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            childBaseSetLoaded,
                            true,
                            baseValueLoaded.isLast());
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetLoaded,
                            true,
                            baseValueLoaded.isLast());
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                childBaseSetLoaded,
                                baseValueLoaded.isClosed(),
                                false);
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }
                }
            }
            else
            {
                //set update
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        childBaseSetApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
        }
        else
        {
            if (childBaseSetSaving == null)
            {
                return;
            }

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null)
            {
                childBaseSetLoaded = (IBaseSet)baseValueClosed.getValue();
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueClosed.getId(),
                        baseValueClosed.getBatch(),
                        baseValueClosed.getIndex(),
                        new Date(baseValueClosed.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        baseValueClosed.isLast());
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsUpdated(baseValueApplied);
            }
            else
            {
                IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                if (baseValuePrevious != null)
                {
                    childBaseSetLoaded = (IBaseSet)baseValuePrevious.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            baseValuePrevious.isLast());
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValuePrevious.isLast())
                    {
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
                else
                {
                    childBaseSetApplied = new BaseSet(childMetaType);
                    baseEntityManager.registerAsInserted(childBaseSetApplied);

                    // TODO: Check next value

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            true);
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }

        Set<UUID> processedUuids = new HashSet<UUID>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0)
        {
            //merge set items
            boolean baseValueFound;
            for (IBaseValue childBaseValueSaving: childBaseSetSaving.get())
            {
                IBaseEntity childBaseEntitySaving = (IBaseEntity)childBaseValueSaving.getValue();
                if (childBaseSetLoaded != null)
                {
                    baseValueFound = false;
                    for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
                    {
                        if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                        {
                            continue;
                        }

                        IBaseEntity childBaseEntityLoaded = (IBaseEntity)childBaseValueLoaded.getValue();

                        if (childBaseValueSaving.equals(childBaseValueLoaded))
                        {
                            // Mark as processed and found
                            processedUuids.add(childBaseValueLoaded.getUuid());
                            baseValueFound = true;

                            IBaseValue baseValueApplied = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getBatch(),
                                    childBaseValueLoaded.getIndex(),
                                    new Date(childBaseValueLoaded.getRepDate().getTime()),
                                    applyBaseEntityAdvanced(childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager),
                                    childBaseValueLoaded.isClosed(),
                                    childBaseValueLoaded.isLast());
                            childBaseSetApplied.put(baseValueApplied);
                            break;
                        }
                    }

                    if (baseValueFound)
                    {
                        continue;
                    }
                }

                if (childBaseEntitySaving.getId() > 0)
                {
                    IBaseSetValueDao setValueDao = persistableDaoPool
                            .getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);

                    // Check closed value
                    IBaseValue baseValueForSearch = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            childBaseValueSaving.getBatch(),
                            childBaseValueSaving.getIndex(),
                            new Date(childBaseValueSaving.getRepDate().getTime()),
                            childBaseValueSaving.getValue(),
                            childBaseValueSaving.isClosed(),
                            childBaseValueSaving.isLast());
                    baseValueForSearch.setBaseContainer(childBaseSetApplied);

                    IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);
                    if (childBaseValueClosed != null)
                    {
                        childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsDeleted(childBaseValueClosed);

                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                        if(childBaseValuePrevious != null && childBaseValuePrevious.getValue() != null) {
                            IBaseEntity childBaseEntityPrevious =  (IBaseEntity)childBaseValuePrevious.getValue();
                            childBaseValuePrevious.setValue(applyBaseEntityAdvanced(childBaseEntitySaving,
                                    childBaseEntityPrevious, baseEntityManager));
                            if (childBaseValueClosed.isLast())
                            {
                                childBaseValuePrevious.setIndex(childBaseValueSaving.getIndex());
                                childBaseValuePrevious.setBatch(childBaseValueSaving.getBatch());
                                childBaseValuePrevious.setLast(true);

                                childBaseSetApplied.put(childBaseValuePrevious);
                                baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                            }
                            else
                            {
                                childBaseSetApplied.put(childBaseValuePrevious);
                            }
                        }
                        continue;
                    }

                    // Check next value
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                    if (childBaseValueNext != null)
                    {
                        IBaseEntity childBaseEntityNext =  (IBaseEntity)childBaseValueNext.getValue();

                        childBaseValueNext.setBatch(childBaseValueSaving.getBatch());
                        childBaseValueNext.setIndex(childBaseValueSaving.getIndex());
                        childBaseValueNext.setValue(applyBaseEntityAdvanced(childBaseEntitySaving,
                                childBaseEntityNext, baseEntityManager));
                        childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                        childBaseSetApplied.put(childBaseValueNext);
                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                        continue;
                    }


                    IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                    if (childBaseValueLast != null)
                    {
                        childBaseValueLast.setBaseContainer(childBaseSetApplied);
                        childBaseValueLast.setBatch(childBaseValueSaving.getBatch());
                        childBaseValueLast.setIndex(childBaseValueSaving.getIndex());
                        childBaseValueLast.setLast(false);

                        baseEntityManager.registerAsUpdated(childBaseValueLast);
                    }
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        childBaseValueSaving.getRepDate(),
                        apply(childBaseEntitySaving, baseEntityManager),
                        false,
                        true
                );
                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }


        //process deletions
        if (childBaseSetLoaded != null)
        {
            for (IBaseValue childBaseValueLoaded: childBaseSetLoaded.get())
            {
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                {
                    continue;
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = childBaseValueLoaded.getRepDate();
                boolean reportDateEquals = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                if (reportDateEquals)
                {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);

                    IBaseEntity childBaseEntityLoaded = (IBaseEntity)childBaseValueLoaded.getValue();
                    if (childBaseEntityLoaded != null)
                    {
                        baseEntityManager.registerUnusedBaseEntity(childBaseEntityLoaded);
                    }
                    boolean last = childBaseValueLoaded.isLast();

                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext != null && childBaseValueNext.isClosed())
                    {
                        baseEntityManager.registerAsDeleted(childBaseValueNext);
                        IBaseEntity childBaseEntityNext = (IBaseEntity)childBaseValueNext.getValue();
                        if (childBaseEntityNext != null)
                        {
                            baseEntityManager.registerUnusedBaseEntity(childBaseEntityNext);
                        }

                        last = childBaseValueNext.isLast();
                    }

                    if (last) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                        if (childBaseValuePrevious != null)
                        {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setBatch(baseValueSaving.getBatch());
                            childBaseValuePrevious.setIndex(baseValueSaving.getIndex());
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                }
                else
                {
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext == null || !childBaseValueNext.isClosed())
                    {
                        IBaseValue childBaseValue = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                baseValueSaving.getRepDate(),
                                childBaseValueLoaded.getValue(),
                                true,
                                childBaseValueLoaded.isLast()
                        );
                        childBaseValue.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsInserted(childBaseValue);

                        if (childBaseValueLoaded.isLast())
                        {
                            IBaseValue childBaseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getId(),
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    childBaseValueLoaded.getRepDate(),
                                    childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    false
                            );
                            childBaseValueLast.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsUpdated(childBaseValueLast);
                        }
                    }
                    else
                    {
                        childBaseValueNext.setBaseContainer(childBaseSetApplied);
                        childBaseValueNext.setBatch(baseValueSaving.getBatch());
                        childBaseValueNext.setIndex(baseValueSaving.getIndex());
                        childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                    }
                }
            }
        }
    }

    protected void applySimpleValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                     IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaValue metaValue = (IMetaValue)metaType;
        if (baseValueLoaded != null)
        {
            if (baseValueSaving.getValue() == null)
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                    baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false
                        );
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                    baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);
                }
                return;
            }

            if (baseValueSaving.equalsByValue(baseValueLoaded))
            {
                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        metaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                baseValueLoaded.getValue(),
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast()
                );
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
            else
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
                else
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date)baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
            }
        }
        else
        {
            if (baseValueSaving.getValue() == null)
            {
                return;
            }

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);
            IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null)
            {
                baseValueClosed.setMetaAttribute(metaAttribute);
                if (baseValueClosed.equalsByValue(baseValueSaving))
                {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseEntityManager.registerAsDeleted(baseValueClosed);

                    IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);
                    if (baseValueClosed.isLast())
                    {
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setLast(true);

                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                    else
                    {
                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                    }
                }
                else
                {
                    baseValueClosed.setIndex(baseValueSaving.getIndex());
                    baseValueClosed.setBatch(baseValueSaving.getBatch());
                    baseValueClosed.setValue(metaValue.getTypeCode() == DataTypes.DATE ?
                            new Date(((Date) baseValueSaving.getValue()).getTime()) :
                            baseValueSaving.getValue());
                    baseValueClosed.setClosed(false);
                    baseEntity.put(metaAttribute.getName(), baseValueClosed);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
            }
            else
            {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                if (baseValueLast == null)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            true
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
                else
                {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();
                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);
                    boolean last = reportDateCompare == -1 ? false : true;

                    if (last)
                    {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date)baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            last
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }

    protected void applyComplexValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                      IBaseEntityManager baseEntityManager)
    {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaClass metaClass = (IMetaClass)metaType;
        if (baseValueLoaded != null)
        {
            if (baseValueSaving.getValue() == null)
            {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
                else
                {
                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false
                        );
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast()
                    );
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);
                }
                return;
            }

            IBaseEntity baseEntitySaving = (IBaseEntity)baseValueSaving.getValue();
            IBaseEntity baseEntityLoaded = (IBaseEntity)baseValueLoaded.getValue();

            if (baseValueSaving.equalsByValue(baseValueLoaded) || !metaClass.isSearchable())
            {
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        new Date(baseValueLoaded.getRepDate().getTime()),
                        baseEntityApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast()
                );
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
            }
            else
            {

                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                if (reportDateEquals)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseEntityApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
                else
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            baseValueLoaded.isLast()
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast())
                    {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                }
            }
        }
        else
        {
            IBaseEntity baseEntitySaving = (IBaseEntity)baseValueSaving.getValue();
            if (baseEntitySaving == null)
            {
                return;
            }

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);
            IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null)
            {
                baseValueClosed.setMetaAttribute(metaAttribute);
                IBaseEntity baseEntityClosed = (IBaseEntity)baseValueClosed.getValue();
                if (baseValueClosed.equalsByValue(baseValueSaving))
                {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseEntityManager.registerAsDeleted(baseValueClosed);

                    IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                    IBaseEntity baseEntityApplied;
                    if (metaAttribute.isImmutable())
                    {
                        if (baseEntitySaving.getId() < 1)
                        {
                            throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                    baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                        }
                        baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                                baseEntitySaving.getReportDate());
                    }
                    else
                    {
                        baseEntityApplied = metaClass.isSearchable() ?
                                apply(baseEntitySaving, baseEntityManager) :
                                applyBaseEntityAdvanced(baseEntitySaving, baseEntityClosed, baseEntityManager);
                    }
                    baseValuePrevious.setValue(baseEntityApplied);

                    if (baseValueClosed.isLast())
                    {
                        baseValuePrevious.setIndex(baseValueSaving.getIndex());
                        baseValuePrevious.setBatch(baseValueSaving.getBatch());
                        baseValuePrevious.setLast(true);

                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                    else
                    {
                        baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                    }
                }
                else
                {
                    IBaseEntity baseEntityApplied;
                    if (metaAttribute.isImmutable())
                    {
                        if (baseEntitySaving.getId() < 1)
                        {
                            throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                    baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                        }
                        baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                                baseEntitySaving.getReportDate());
                    }
                    else
                    {
                        baseEntityApplied = metaClass.isSearchable() ?
                                apply(baseEntitySaving, baseEntityManager) :
                                applyBaseEntityAdvanced(baseEntitySaving, baseEntityClosed, baseEntityManager);
                    }

                    baseValueClosed.setIndex(baseValueSaving.getIndex());
                    baseValueClosed.setBatch(baseValueSaving.getBatch());
                    baseValueClosed.setValue(baseEntityApplied);
                    baseValueClosed.setClosed(false);
                    baseEntity.put(metaAttribute.getName(), baseValueClosed);
                    baseEntityManager.registerAsUpdated(baseValueClosed);
                }
            }
            else
            {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable())
                {
                    if (baseEntitySaving.getId() < 1)
                    {
                        throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                }
                else
                {
                    baseEntityApplied = apply(baseEntitySaving, baseEntityManager);
                }

                if (baseValueLast == null)
                {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            true
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
                else
                {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();
                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);
                    boolean last = reportDateCompare == -1 ? false : true;

                    if (last)
                    {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            last
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }

    @Override
    @Transactional
    public IBaseEntity process(IBaseEntity baseEntity)
    {
        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity baseEntityPrepared = prepare(((BaseEntity) baseEntity).clone());
        IBaseEntity baseEntityApplied = apply(baseEntityPrepared, baseEntityManager);

        applyToDb(baseEntityManager);

        return baseEntityApplied;
    }

    public boolean checkReportDateExists(long baseEntityId, Date reportDate)
    {
        Select select = context
                .select(DSL.count().as("report_dates_count"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.equal(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return ((BigDecimal)rows.get(0).get("report_dates_count")).longValue() > 0;
    }



    private Set<BaseEntity> collectComplexSetValues(BaseSet baseSet)
    {
        Set<BaseEntity> entities = new HashSet<BaseEntity>();

        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isSetOfSets())
        {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.addAll(collectComplexSetValues((BaseSet)baseValue.getValue()));
            }
        } else {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext())
            {
                IBaseValue baseValue = it.next();
                entities.add((BaseEntity)baseValue.getValue());
            }
        }

        return entities;
    }

    public List<Long> getEntityIDsByMetaclass(long metaClassId) {
        ArrayList<Long> entityIds = new ArrayList<Long>();

        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> i = rows.iterator();
        while(i.hasNext())
        {
            Map<String, Object> row = i.next();

            entityIds.add(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return entityIds;
    }

//    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
//        ArrayList<RefListItem> entityIds = new ArrayList<RefListItem>();
//
//        Select select = context
//                .select(EAV_BE_ENTITIES.ID,
//                        EAV_BE_STRING_VALUES.as("name_value").VALUE.as("value"),
//                        EAV_BE_STRING_VALUES.as("code_value").VALUE.as("code"))
//                .from(EAV_BE_ENTITIES,
//                        EAV_BE_STRING_VALUES.as("name_value"),
//                        EAV_M_SIMPLE_ATTRIBUTES.as("name_attr"),
//                        EAV_BE_STRING_VALUES.as("code_value"),
//                        EAV_M_SIMPLE_ATTRIBUTES.as("code_attr"))
//                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId))
//
//                .and(EAV_BE_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.as("name_value").ENTITY_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("name_attr").ID.equal(EAV_BE_STRING_VALUES.as("name_value").ATTRIBUTE_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("name_attr").NAME.equal("name_ru"))
//
//                .and(EAV_BE_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.as("code_value").ENTITY_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("code_attr").ID.equal(EAV_BE_STRING_VALUES.as("code_value").ATTRIBUTE_ID))
//                .and(EAV_M_SIMPLE_ATTRIBUTES.as("code_attr").NAME.equal("code"));
//
//        logger.debug(select.toString());
//        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
//
//        Iterator<Map<String, Object>> i = rows.iterator();
//        while(i.hasNext())
//        {
//            Map<String, Object> row = i.next();
//
//            RefListItem rli = new RefListItem();
//
//            rli.setId(((BigDecimal)row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
//            rli.setTitle((String)row.get("value"));
//            rli.setCode((String)row.get("code"));
//
//            entityIds.add(rli);
//        }
//
//        return entityIds;
//    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        ArrayList<RefListItem> entityIds = new ArrayList<RefListItem>();

        Select select = context.select().from(
                context.select(
                        EAV_BE_ENTITIES.ID,
                        EAV_M_CLASSES.NAME.as("classes_name"),
                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                        EAV_BE_STRING_VALUES.VALUE,
                        DSL.val("string", String.class).as("type"))
                .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_STRING_VALUES)
                .where(EAV_M_CLASSES.ID.eq(metaClassId))
                .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                .and(EAV_BE_STRING_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                .union(context
                        .select(
                                EAV_BE_ENTITIES.ID,
                                EAV_M_CLASSES.NAME.as("classes_name"),
                                EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                DSL.field(
                                        "TO_CHAR({0})", String.class, EAV_BE_INTEGER_VALUES.VALUE),
                                DSL.val("integer", String.class).as("type"))
                        .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_INTEGER_VALUES)
                        .where(EAV_M_CLASSES.ID.eq(metaClassId))
                        .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                        .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                        .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                        .and(EAV_BE_INTEGER_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                        .and(EAV_BE_INTEGER_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                    .union(context
                            .select(
                                    EAV_BE_ENTITIES.ID,
                                    EAV_M_CLASSES.NAME.as("classes_name"),
                                    EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                    DSL.field(
                                            "TO_CHAR({0})", String.class, EAV_BE_DATE_VALUES.VALUE),
                                    DSL.val("date", String.class).as("type"))
                            .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_DATE_VALUES)
                            .where(EAV_M_CLASSES.ID.eq(metaClassId))
                            .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                            .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                            .and(EAV_BE_DATE_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                            .and(EAV_BE_DATE_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                        .union(context
                                .select(
                                        EAV_BE_ENTITIES.ID,
                                        EAV_M_CLASSES.NAME.as("classes_name"),
                                        EAV_M_SIMPLE_ATTRIBUTES.NAME,
                                        DSL.field(
                                                "TO_CHAR({0})", String.class, EAV_BE_BOOLEAN_VALUES.VALUE),
                                        DSL.val("boolean", String.class).as("type"))
                                .from(EAV_BE_ENTITIES, EAV_M_CLASSES, EAV_M_SIMPLE_ATTRIBUTES, EAV_BE_BOOLEAN_VALUES)
                                .where(EAV_M_CLASSES.ID.eq(metaClassId))
                                .and(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(EAV_M_CLASSES.ID))
                                .and(EAV_M_SIMPLE_ATTRIBUTES.CONTAINER_TYPE.eq(1))
                                .and(EAV_BE_BOOLEAN_VALUES.ATTRIBUTE_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.ID))
                                .and(EAV_BE_BOOLEAN_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID)))))).
                orderBy(DSL.field("ID"));

        logger.debug("LIST_BY_CLASS SQL: " + select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> i = rows.iterator();
        while(i.hasNext())
        {
            RefListItem rli = new RefListItem();

            Map<String, Object> row = i.next();
            long id = (Long)row.get("ID");
            long old_id = id;

            logger.debug("#####################");

            rli.setId(id);
            while (old_id == id) {
                if (((String)row.get("NAME")).equals("code")) {
                    rli.setCode((String)row.get("VALUE"));
                } else if (((String)row.get("NAME")).startsWith("name_")) {
                    rli.setTitle((String)row.get("VALUE"));
                }

                for (String key : row.keySet()) {
                    if (key.equals("NAME") || key.startsWith("name_")) {
                        continue;
                    }

                    rli.addValue(key, row.get(key));
                }

                row = i.next();
                old_id = id;
                id = (Long)row.get("ID");
            }

            entityIds.add(rli);
        }

        return entityIds;
    }

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta) {
        List<Long> ids = getEntityIDsByMetaclass(meta.getId());

        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        for (Long id : ids) {
            entities.add((BaseEntity)load(id));
        }

        return entities;
    }

    @Override
    public boolean isApproved(long id) {
        Select select = context
                .select(EAV_A_CREDITOR_STATE.ID)
                .from(EAV_A_CREDITOR_STATE)
                .where(EAV_A_CREDITOR_STATE.CREDITOR_ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            return true;
        }

        return false;
    }

    @Override
    public int batchCount(long id, String className) {
        throw new UnsupportedOperationException("Not yet implemented.");

        /*Select select = context
                .select(EAV_A_CREDITOR_USER.CREDITOR_ID, EAV_BE_ENTITIES.ID.count().as("cr_count"))
                .from(EAV_BE_ENTITIES)
                .leftOuterJoin(EAV_M_CLASSES).on(EAV_BE_ENTITIES.CLASS_ID.eq(EAV_M_CLASSES.ID))
                .leftOuterJoin(EAV_BE_COMPLEX_VALUES).on(EAV_BE_ENTITIES.ID.eq(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID))
                .leftOuterJoin(EAV_BATCHES).on(EAV_BE_COMPLEX_VALUES.BATCH_ID.eq(EAV_BATCHES.ID))
                .leftOuterJoin(EAV_A_CREDITOR_USER).on(EAV_BATCHES.USER_ID.eq(EAV_A_CREDITOR_USER.USER_ID))
                .where(EAV_M_CLASSES.NAME.eq("primary_contract"))
                .and(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(id))
                .groupBy(EAV_A_CREDITOR_USER.CREDITOR_ID);

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0)
        {
            return ((BigDecimal)rows.get(0).get("cr_count")).intValue();
        }

        return 0;*/
    }

    @Override
    @Transactional
    public boolean remove(long baseEntityId)
    {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.deleteRecursive(baseEntityId);
    }

    @Override
    public long getRandomBaseEntityId(IMetaClass metaClass)
    {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClass);
    }

    @Override
    public long getRandomBaseEntityId(long metaClassId)
    {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClassId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds)
    {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getChildBaseEntityIds(parentBaseEntityIds);
    }

}