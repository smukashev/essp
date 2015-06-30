package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.tool.Quote;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * @author a.motov
 */
@SuppressWarnings("unchecked")
@Repository
public class BaseEntityProcessorDaoImpl extends JDBCSupport implements IBaseEntityProcessorDao {
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

    private IDaoListener applyListener;

    public IDaoListener getApplyListener() {
        return applyListener;
    }

    @Autowired
    public void setApplyListener(IDaoListener applyListener) {
        this.applyListener = applyListener;
    }

    public IBaseEntity loadByMaxReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (maxReportDate == null)
            throw new RuntimeException("No data found on report date " + actualReportDate + ".");

        return load(id, maxReportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity loadByMaxReportDate(long id, Date reportDate) {
        return loadByMaxReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date reportDate) {
        return loadByMinReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByMinReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date minReportDate = baseEntityReportDateDao.getMinReportDate(id, actualReportDate);
        if (minReportDate == null)
            throw new RuntimeException("No data found on report date " + actualReportDate + ".");

        return load(id, minReportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity loadByReportDate(long id, Date reportDate) {
        return loadByReportDate(id, reportDate, false);
    }

    @Override
    public IBaseEntity loadByReportDate(long id, Date actualReportDate, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date reportDate = baseEntityReportDateDao.getMaxReportDate(id, actualReportDate);
        if (reportDate == null) {
            reportDate = baseEntityReportDateDao.getMinReportDate(id, actualReportDate);
            if (reportDate == null)
                throw new RuntimeException("No data found on report date " + actualReportDate + ".");
        }

        return load(id, reportDate, actualReportDate, caching);
    }

    @Override
    public IBaseEntity load(long id) {
        return load(id, false);
    }

    @Override
    public IBaseEntity load(long id, boolean caching) {
        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(id);
        if (maxReportDate == null)
            throw new UnsupportedOperationException("Not found appropriate report date.");

        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        if (baseEntityDao.isDeleted(id))
            return baseEntityDao.load(id);

        /*
        if (caching)
            return baseEntityCacheDao.getBaseEntity(id, maxReportDate);
        */

        return load(id, maxReportDate, maxReportDate);
    }

    @Override
    public IBaseEntity load(long id, Date maxReportDate, Date actualReportDate, boolean caching) {
        /*
        if (caching)
            return baseEntityCacheDao.getBaseEntity(id, actualReportDate);
        */

        return load(id, maxReportDate, actualReportDate);
    }

    public IBaseEntity load(long id, Date reportDate, Date actualReportDate) {
        IBaseEntityDao baseEntityDao =
                persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.load(id, reportDate, actualReportDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long search(IBaseEntity baseEntity) {
        IMetaClass metaClass = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName()).findSingle((BaseEntity) baseEntity);
        return baseEntityId == null ? 0 : baseEntityId;
    }

    public List<Long> search(long metaClassId) {
        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Long> baseEntityIds = new ArrayList<Long>();
        for (Map<String, Object> row : rows)
            baseEntityIds.add(((BigDecimal) row.get(EAV_BE_ENTITIES.ID.getName())).longValue());

        return baseEntityIds;
    }

    public List<Long> search(String className) {
        MetaClass metaClass = metaClassRepository.getMetaClass(className);
        if (metaClass != null)
            return search(metaClass.getId());

        return new ArrayList<>();
    }

    public IBaseEntity postPrepare(IBaseEntity baseEntity, IBaseEntity parentEntity) {
        MetaClass metaClass = baseEntity.getMeta();

        for (String attribute : baseEntity.getAttributes()) {
            IMetaType memberType = baseEntity.getMemberType(attribute);

            if (memberType.isComplex()) {
                IBaseValue memberValue = baseEntity.getBaseValue(attribute);

                if (memberValue.getValue() != null) {
                    if (memberType.isSet()) {
                        IMetaSet childMetaSet = (IMetaSet) memberType;
                        IMetaType childMetaType = childMetaSet.getMemberType();

                        if (childMetaType.isSet()) {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        } else {
                            IBaseSet childBaseSet = (IBaseSet) memberValue.getValue();

                            for (IBaseValue childBaseValue : childBaseSet.get()) {
                                IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                                if (childBaseEntity.getValueCount() != 0)
                                    postPrepare((IBaseEntity) childBaseValue.getValue(), baseEntity);
                            }
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) memberValue.getValue();

                        if (childBaseEntity.getValueCount() != 0)
                            postPrepare((IBaseEntity) memberValue.getValue(), baseEntity);
                    }
                }
            }
        }

        if (parentEntity != null && metaClass.isSearchable() && metaClass.isParentIsKey()) {
            Long baseEntityId = searcherPool.getImprovedBaseEntityLocalSearcher().
                    findSingleWithParent((BaseEntity) baseEntity, (BaseEntity) parentEntity);

            if (baseEntityId == null) baseEntity.setId(0);
            else baseEntity.setId(baseEntityId);
        }

        return baseEntity;
    }

    public IBaseEntity prepare(IBaseEntity baseEntity) {
        IMetaClass metaClass = baseEntity.getMeta();

        for (String attribute : baseEntity.getAttributes()) {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isComplex()) {
                IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                if (baseValue.getValue() != null) {
                    if (metaType.isSet()) {
                        IMetaSet childMetaSet = (IMetaSet) metaType;
                        IMetaType childMetaType = childMetaSet.getMemberType();
                        if (childMetaType.isSet()) {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        } else {
                            IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();
                            for (IBaseValue childBaseValue : childBaseSet.get()) {
                                IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();
                                if (childBaseEntity.getValueCount() != 0) {
                                    prepare((IBaseEntity) childBaseValue.getValue());
                                }
                            }
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
                        if (childBaseEntity.getValueCount() != 0) {
                            prepare((IBaseEntity) baseValue.getValue());
                        }
                    }
                }
            }
        }

        if (metaClass.isSearchable()) {
            long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
                baseEntity.setId(baseEntityId);
        }

        return baseEntity;
    }

    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager) {
        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable insertedObject : insertedObjects)
                    persistableDao.insert(insertedObject);
            }
        }

        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> updatedObjects = baseEntityManager.getUpdatedObjects(objectClass);
            if (updatedObjects != null && updatedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable updatedObject : updatedObjects)
                    persistableDao.update(updatedObject);
            }
        }

        for (int i = BaseEntityManager.CLASS_PRIORITY.size() - 1; i >= 0; i--) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> deletedObjects = baseEntityManager.getDeletedObjects(objectClass);
            if (deletedObjects != null && deletedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);
                for (IPersistable deletedObject : deletedObjects)
                    persistableDao.delete(deletedObject);
            }
        }

        for (IBaseEntity unusedBaseEntity : baseEntityManager.getUnusedBaseEntities()) {
            IBaseEntityDao baseEntityDao = persistableDaoPool
                    .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
            baseEntityDao.deleteRecursive(unusedBaseEntity.getId(), unusedBaseEntity.getMeta());
        }
    }

    private IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager) {
        return apply(baseEntityForSave, baseEntityManager, null);
    }

    private IBaseEntity apply(IBaseEntity baseEntityForSave, IBaseEntityManager baseEntityManager, EntityHolder entityHolder) {
        IBaseEntity baseEntityLoaded = null;
        IBaseEntity baseEntityApplied;

        if (baseEntityForSave.getId() < 1 || !baseEntityForSave.getMeta().isSearchable()) {
            baseEntityApplied = applyBaseEntityBasic(baseEntityForSave, baseEntityManager);
        } else {
            Date reportDate = baseEntityForSave.getReportDate();

            IBaseEntityReportDateDao baseEntityReportDateDao =
                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

            Date maxReportDate = baseEntityReportDateDao
                    .getMaxReportDate(baseEntityForSave.getId(), reportDate);

            if (maxReportDate == null) {
                Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntityForSave.getId(), reportDate);

                if (minReportDate == null)
                    throw new UnsupportedOperationException("No report date for this entity. Entity ID: "
                            + baseEntityForSave.getId());

                baseEntityLoaded = load(baseEntityForSave.getId(), minReportDate, reportDate);
                baseEntityApplied = applyBaseEntityAdvanced(baseEntityForSave, baseEntityLoaded, baseEntityManager);
            } else {
                baseEntityLoaded = load(baseEntityForSave.getId(), maxReportDate, reportDate);
                baseEntityApplied = applyBaseEntityAdvanced(baseEntityForSave, baseEntityLoaded, baseEntityManager);
            }
        }

        if (entityHolder != null) {
            entityHolder.saving = baseEntityForSave;
            entityHolder.loaded = baseEntityLoaded;
            entityHolder.applied = baseEntityApplied;
        }

        return baseEntityApplied;
    }

    private IBaseEntity applyBaseEntityBasic(IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager) {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMeta(), baseEntitySaving.getReportDate());

        for (String attribute : baseEntitySaving.getAttributes()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);
            applyBaseValueBasic(baseEntityApplied, baseValueSaving, baseEntityManager);

        }

        baseEntityApplied.calculateValueCount();
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);
        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    private IBaseEntity applyBaseEntityAdvanced(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                                                IBaseEntityManager baseEntityManager) {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        IMetaClass metaClass = baseEntitySaving.getMeta();

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded, baseEntitySaving.getReportDate());

        if (baseEntitySaving.getId() < 1 && baseEntityLoaded.getId() > 0)
            baseEntitySaving.setId(baseEntityLoaded.getId());

        for (String attribute : metaClass.getAttributeNames()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

            // Not present in saving instance of BaseEntity
            if (baseValueSaving == null)
                continue;

            IBaseContainer baseContainer = baseValueSaving.getBaseContainer();
            if (baseContainer != null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "contained not in the instance of BaseEntity is not possible.");

            IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
            if (metaAttribute == null)
                throw new RuntimeException("Advanced applying instance of BaseValue changes " +
                        "without meta data is not possible.");

            IMetaType metaType = metaAttribute.getMetaType();

            if (metaType.isComplex()) {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Not yet implemented.");

                if (metaType.isSet())
                    applyComplexSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applyComplexValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            } else {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Not yet implemented.");

                if (metaType.isSet())
                    applySimpleSet(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applySimpleValue(baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
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
        if (reportDateCompare == 0) {
            baseEntityManager.registerAsUpdated(baseEntityReportDate);
        } else {
            boolean reportDateExists = checkReportDateExists(baseEntityLoaded.getId(),
                    baseEntityReportDate.getReportDate());

            if (reportDateExists) {
                logger.error("REPORT_DATE_EXISTS ERROR:\n" + "DATA DUMP\n" + "baseEntitySaving: \n" +
                        baseEntitySaving + "baseEntityLoaded: \n" + baseEntityLoaded + "baseEntityApplied: \n" +
                        baseEntityApplied);

                throw new IllegalStateException("Report date " + baseEntityReportDate.getReportDate()
                        + " already exists");
            }

            baseEntityManager.registerAsInserted(baseEntityReportDate);
        }

        //TODO:Remove unused instances of BaseEntity

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    protected void applyBaseValueBasic(IBaseEntity baseEntityApplied, IBaseValue baseValue,
                                       IBaseEntityManager baseEntityManager) {
        if (baseValue.getValue() == null) {
            throw new RuntimeException("Basic applying instance of BaseValue changes with null value is not possible.");
        }

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer != null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY) {
            throw new RuntimeException("Basic applying instance of BaseValue changes contained not in the instance " +
                    "of BaseEntity is not possible.");
        }

        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null) {
            throw new RuntimeException("Basic applying instance of BaseValue changes without meta" +
                    " data is not possible.");
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Not yet implemented.");

                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();

                // TODO: Add implementation of immutable complex values in sets

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue childBaseValueApplied = BaseValueFactory.create(MetaContainerTypes.META_SET,
                            childMetaSet.getMemberType(), childBaseValue.getBatch(), childBaseValue.getIndex(),
                            new Date(baseValue.getRepDate().getTime()), childBaseEntityApplied, false, true);

                    childBaseSetApplied.put(childBaseValueApplied);
                    baseEntityManager.registerAsInserted(childBaseValueApplied);
                }

                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                        baseValue.getBatch(), baseValue.getIndex(), new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied, false, true);

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            } else {
                if (metaAttribute.isImmutable()) {
                    IMetaClass childMetaClass = (IMetaClass) metaType;
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
                    if (childBaseEntity.getValueCount() != 0) {
                        if (childBaseEntity.getId() < 1)
                            throw new UnsupportedOperationException("Attempt to write immutable instance " +
                                    "of BaseEntity with classname: " +
                                    childBaseEntity.getMeta().getClassName() + "\n" + childBaseEntity.toString());

                        IBaseEntity childBaseEntityImmutable = loadByMaxReportDate(childBaseEntity.getId(),
                                childBaseEntity.getReportDate(), childMetaClass.isReference());

                        if (childBaseEntityImmutable == null)
                            throw new RuntimeException("Instance of BaseEntity with id " + childBaseEntity.getId() +
                                    "not found in the DB.");

                        IBaseValue baseValueApplied = BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                                baseValue.getBatch(), baseValue.getIndex(), new Date(baseValue.getRepDate().getTime()),
                                childBaseEntityImmutable, false, true);

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                } else {
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(childBaseEntity, baseEntityManager);

                    IBaseValue baseValueApplied = BaseValueFactory.create(MetaContainerTypes.META_CLASS, metaType,
                            baseValue.getBatch(), baseValue.getIndex(), new Date(baseValue.getRepDate().getTime()),
                            childBaseEntityApplied, false, true);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        } else {
            if (metaType.isSet()) {
                if (metaType.isSetOfSets()) {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }

                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();
                IMetaValue childMetaValue = (IMetaValue) childMetaSet.getMemberType();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseValue childBaseValueApplied =
                            BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaSet.getMemberType(),
                                    childBaseValue.getBatch(),
                                    childBaseValue.getIndex(),
                                    new Date(baseValue.getRepDate().getTime()),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date) childBaseValue.getValue()).getTime()) :
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
            } else {
                IMetaValue metaValue = (IMetaValue) metaType;
                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValue.getBatch(),
                        baseValue.getIndex(),
                        new Date(baseValue.getRepDate().getTime()),
                        metaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date) baseValue.getValue()).getTime()) :
                                baseValue.getValue(),
                        false,
                        true
                );

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
        }
    }

    protected void applySimpleSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                  IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        if (metaAttribute.isFinal()) {
            throw new UnsupportedOperationException("Not yet implemented.");
        }

        IMetaSet childMetaSet = (IMetaSet) metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();
        IMetaValue childMetaValue = (IMetaValue) childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet) baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;

        Date reportDateSaving = null;
        Date reportDateLoaded = null;
        if (baseValueLoaded != null) {
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();
            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                reportDateSaving = baseValueSaving.getRepDate();
                reportDateLoaded = baseValueLoaded.getRepDate();
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
                if (compare == 0) {
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
                } else if (compare == 1) {
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

                    if (baseValueLoaded.isLast()) {
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
            } else {
                reportDateSaving = baseValueSaving.getRepDate();
                reportDateLoaded = baseValueLoaded.getRepDate();
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (metaAttribute.isFinal() && !(compare == 0)) {
                    throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                            "mistakenly loaded from the database.");
                } else if (compare >= 0) {
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
                } else if (compare == -1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getBatch(),
                            baseValueLoaded.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            }
        } else {
            if (childBaseSetSaving == null) {
                return;
            }

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);
            if (baseValueClosed != null) {
                childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
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
            } else {
                IBaseEntityReportDateDao baseEntityReportDateDao =
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                reportDateLoaded = baseEntityReportDateDao.getMinReportDate(baseEntity.getId());
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                boolean isNew = true;
                if (compare == 1) {
                    IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                    if (baseValuePrevious != null) {
                        reportDateLoaded = baseValuePrevious.getRepDate();

                        childBaseSetLoaded = (IBaseSet) baseValuePrevious.getValue();
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

                        if (baseValuePrevious.isLast()) {
                            baseValuePrevious.setBaseContainer(baseEntity);
                            baseValuePrevious.setMetaAttribute(metaAttribute);
                            baseValuePrevious.setBatch(baseValueSaving.getBatch());
                            baseValuePrevious.setIndex(baseValueSaving.getIndex());
                            baseValuePrevious.setLast(false);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        }
                        isNew = false;
                    }

                } else if (compare == -1) {
                    IBaseValue baseValueNext = baseValueDao.getNextBaseValue(baseValueSaving);
                    if (baseValueNext != null) {
                        boolean nextByRepDateIsNextByBaseValue =
                                DataUtils.compareBeginningOfTheDay(reportDateLoaded, baseValueNext.getRepDate()) == 0;
                        if (nextByRepDateIsNextByBaseValue) {
                            // on reportDateLoaded (minReportDate) baseEntity existed, but baseValue did not.
                            reportDateLoaded = baseValueNext.getRepDate();
                        }

                        childBaseSetLoaded = (IBaseSet) baseValueNext.getValue();
                        childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                false); //baseValueNext.isLast());
                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);

                        /*
                        if (baseValueNext.isLast())
                        {
                            baseValueNext.setBaseContainer(baseEntity);
                            baseValueNext.setMetaAttribute(metaAttribute);
                            baseValueNext.setBatch(baseValueSaving.getBatch());
                            baseValueNext.setIndex(baseValueSaving.getIndex());
                            baseValueNext.setLast(false);
                            baseEntityManager.registerAsUpdated(baseValueNext);
                        }
                        */
                        isNew = false;

                        // TODO: Close @reportDateLoaded
                        if (!nextByRepDateIsNextByBaseValue) {
                            IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    metaType,
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    reportDateLoaded,
                                    childBaseSetApplied,
                                    true,
                                    false); //baseValueNext.isLast());
                            baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed);
                            baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                        }

                    }
                } else { // compare == 0
                    // TODO: cannot be equal, since baseValueLoaded == null
                    // TODO: do nothing
                }

                if (isNew) {
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
                            compare != -1); // if compare = -1, closing value will be inserted, it will become last
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (compare == -1) {
                        // Close inserted value at reportDateLoaded
                        //childBaseSetApplied = new BaseSet(childMetaType);
                        //baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                reportDateLoaded,
                                childBaseSetApplied,
                                true,
                                true);
                        baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed); // TODO: ?
                        baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                    }
                }
            }
        }

        Set<UUID> processedUuids = new HashSet<UUID>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            boolean baseValueFound;
            int compare;
            if (reportDateSaving == null || reportDateLoaded == null) {
                compare = -2; // Why?
            } else {
                compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
            }

            IBaseSetValue.HistoryType historyType = null;
            Date nextReportDate = null;
            Date previousReportDate = null;
            Date nextReportDateByEntity = null;
            Date previousReportDateByEntity = null;
            Date nextReportDateBySet = null;
            Date previousReportDateBySet = null;
            boolean reportDatesByEntityNotLoaded = true;
            boolean reportDatesBySetNotLoaded = true;
            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                boolean historyTypeChanged = false;
                if (historyType == null) {
                    historyTypeChanged = true;
                    IBaseSetValue baseSetValue = (IBaseSetValue) childBaseValueSaving;
                    historyType = baseSetValue.getHistoryType();
                } else {
                    IBaseSetValue baseSetValue = (IBaseSetValue) childBaseValueSaving;
                    IBaseSetValue.HistoryType newHistoryType = baseSetValue.getHistoryType();
                    if (!newHistoryType.equals(historyType)) {
                        historyTypeChanged = true;
                        historyType = newHistoryType;
                    }
                }

                if (historyTypeChanged) {
                    if (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_ENTITY || childBaseSetApplied.getId() < 1) {
                        if (reportDatesByEntityNotLoaded) {
                            reportDatesByEntityNotLoaded = false;
                            IBaseEntityReportDateDao baseEntityReportDateDao =
                                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                            nextReportDateByEntity = baseEntityReportDateDao.getNextReportDate(baseEntity.getId(), baseEntity.getReportDate());
                            previousReportDateByEntity = baseEntityReportDateDao.getPreviousReportDate(baseEntity.getId(), baseEntity.getReportDate());
                        }
                        nextReportDate = nextReportDateByEntity;
                        previousReportDate = previousReportDateByEntity;
                    } else {
                        if (reportDatesBySetNotLoaded) {
                            reportDatesByEntityNotLoaded = false;
                            IBaseSetValueDao baseSetValueDao =
                                    persistableDaoPool.getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);
                            nextReportDateBySet = baseSetValueDao.getNextReportDate(childBaseSetApplied.getId(), baseEntity.getReportDate());
                            previousReportDateBySet = baseSetValueDao.getPreviousReportDate(childBaseSetApplied.getId(), baseEntity.getReportDate());
                        }
                        nextReportDate = nextReportDateBySet;
                        previousReportDate = previousReportDateBySet;
                    }
                }

                if (childBaseSetLoaded != null) {
                    baseValueFound = false;
                    for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                        if (processedUuids.contains(childBaseValueLoaded.getUuid())) {
                            continue;
                        }

                        if (childBaseValueSaving.equalsByValue(childMetaValue, childBaseValueLoaded)) {
                            // Mark as processed and found
                            processedUuids.add(childBaseValueLoaded.getUuid());
                            baseValueFound = true;

                            if (compare == -1) {
                                // Если следующая дата совпадает с датой загруженного значения, то
                                // переносим запись на дату сохранения
                                if (DataTypeUtil.compareBeginningOfTheDay(nextReportDate, childBaseValueLoaded.getRepDate()) == 0) {
                                    IBaseValue baseValueApplied = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueLoaded.getId(), // update existing
                                            childBaseValueLoaded.getBatch(),
                                            childBaseValueLoaded.getIndex(),
                                            new Date(childBaseValueSaving.getRepDate().getTime()),
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                                    childBaseValueLoaded.getValue(),
                                            childBaseValueLoaded.isClosed(),
                                            childBaseValueLoaded.isLast());
                                    childBaseSetApplied.put(baseValueApplied);
                                    baseEntityManager.registerAsUpdated(baseValueApplied);
                                } else {
                                    // Добавляем запись на дату сохранения (isClosed = false, isLast = false)
                                    IBaseValue baseValueApplied = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueSaving.getBatch(),
                                            childBaseValueSaving.getIndex(),
                                            new Date(childBaseValueSaving.getRepDate().getTime()),
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                                    childBaseValueSaving.getValue(),
                                            false,
                                            false);
                                    childBaseSetApplied.put(baseValueApplied);
                                    baseEntityManager.registerAsInserted(baseValueApplied);

                                    // Записываем запись закрытия на следующую дату (isClosed = true, isLast = false)
                                    IBaseValue baseValueClosed = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueSaving.getBatch(),
                                            childBaseValueSaving.getIndex(),
                                            nextReportDate,
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                                    childBaseValueSaving.getValue(),
                                            true,
                                            false);
                                    baseValueClosed.setBaseContainer(childBaseSetApplied);
                                    baseEntityManager.registerAsInserted(baseValueApplied);
                                }
                            } else {
                                IBaseValue baseValueApplied = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        childBaseValueLoaded.getId(), // update existing
                                        childBaseValueSaving.getBatch(),
                                        childBaseValueSaving.getIndex(),
                                        new Date(childBaseValueLoaded.getRepDate().getTime()),
                                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                                childBaseValueLoaded.getValue(),
                                        childBaseValueLoaded.isClosed(),
                                        childBaseValueLoaded.isLast());
                                childBaseSetApplied.put(baseValueApplied);
                            }

                            break;
                        }
                    }

                    if (baseValueFound) {
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
                                new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        childBaseValueSaving.isClosed(),
                        childBaseValueSaving.isLast());
                baseValueForSearch.setBaseContainer(childBaseSetApplied);

                IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);
                if (childBaseValueClosed != null) {
                    childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsDeleted(childBaseValueClosed);

                    IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                    if (childBaseValueClosed.isLast()) {
                        childBaseValuePrevious.setIndex(childBaseValueSaving.getIndex());
                        childBaseValuePrevious.setBatch(childBaseValueSaving.getBatch());
                        childBaseValuePrevious.setLast(true);

                        childBaseSetApplied.put(childBaseValuePrevious);
                        baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                    } else {
                        childBaseSetApplied.put(childBaseValuePrevious);
                    }

                    continue;
                }

                // Check next value
                IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                if (childBaseValueNext != null) {
                    // Если следующая дата совпадает с датой следующей записи, то
                    // переносим запись на дату сохранения
                    if (DataTypeUtil.compareBeginningOfTheDay(nextReportDate, childBaseValueNext.getRepDate()) == 0) {
                        childBaseValueNext.setBatch(childBaseValueSaving.getBatch());
                        childBaseValueNext.setIndex(childBaseValueSaving.getIndex());
                        childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));
                        childBaseSetApplied.put(childBaseValueNext);
                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                    } else {
                        // Добавляем запись на дату сохранения (isClosed = false, isLast = false)
                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                childBaseValueSaving.getBatch(),
                                childBaseValueSaving.getIndex(),
                                new Date(childBaseValueSaving.getRepDate().getTime()),
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                        childBaseValueSaving.getValue(),
                                false,
                                false);
                        childBaseSetApplied.put(baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);

                        // Записываем запись закрытия на следующую дату (isClosed = true, isLast = false)
                        IBaseValue baseValueClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                childBaseValueSaving.getBatch(),
                                childBaseValueSaving.getIndex(),
                                nextReportDate,
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                        childBaseValueSaving.getValue(),
                                true,
                                false);
                        baseValueClosed.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                    continue;
                }


                IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                if (childBaseValueLast != null) {
                    childBaseValueLast.setBaseContainer(childBaseSetApplied);
                    childBaseValueLast.setBatch(childBaseValueSaving.getBatch());
                    childBaseValueLast.setIndex(childBaseValueSaving.getIndex());
                    childBaseValueLast.setLast(false);

                    baseEntityManager.registerAsUpdated(childBaseValueLast);
                }

                boolean lastBaseValue = true;
                if (previousReportDate != null && nextReportDate != null &&
                        (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_ENTITY ||
                                historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_SET)) {
                    lastBaseValue = false;

                    IBaseValue childBaseValueNextClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            childBaseValueSaving.getBatch(),
                            childBaseValueSaving.getIndex(),
                            nextReportDate,
                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                    childBaseValueSaving.getValue(),
                            true,
                            true
                    );
                    childBaseValueNextClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsInserted(childBaseValueNextClosed);
                } else if (previousReportDate == null && nextReportDate != null &&
                        (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_ENTITY ||
                                historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_SET)) {
                    lastBaseValue = false;

                    IBaseValue childBaseValueNextClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            childBaseValueSaving.getBatch(),
                            childBaseValueSaving.getIndex(),
                            nextReportDate,
                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                    childBaseValueSaving.getValue(),
                            true,
                            true
                    );
                    childBaseValueNextClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsInserted(childBaseValueNextClosed);
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        childBaseValueSaving.getRepDate(),
                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                new Date(((Date) childBaseValueSaving.getValue()).getTime()) :
                                childBaseValueSaving.getValue(),
                        false,
                        lastBaseValue


                );
                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }

        if (childBaseSetLoaded != null) {
            Date nextReportDate = null;
            Date nextReportDateByEntity = null;
            Date nextReportDateBySet = null;
            boolean reportDatesByEntityNotLoaded = true;
            boolean reportDatesBySetNotLoaded = true;
            IBaseSetValue.HistoryType historyType = null;

            // Цикл по загруженным значениям массива
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                boolean historyTypeChanged = false;
                if (historyType == null) {
                    historyTypeChanged = true;
                    IBaseSetValue baseSetValue = (IBaseSetValue) childBaseValueLoaded;
                    historyType = baseSetValue.getHistoryType();
                } else {
                    IBaseSetValue baseSetValue = (IBaseSetValue) childBaseValueLoaded;
                    IBaseSetValue.HistoryType newHistoryType = baseSetValue.getHistoryType();
                    if (!newHistoryType.equals(historyType)) {
                        historyTypeChanged = true;
                        historyType = newHistoryType;
                    }
                }

                if (historyTypeChanged) {
                    if (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_ENTITY || childBaseSetApplied.getId() < 1) {
                        if (reportDatesByEntityNotLoaded) {
                            reportDatesByEntityNotLoaded = false;
                            IBaseEntityReportDateDao baseEntityReportDateDao =
                                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                            nextReportDateByEntity = baseEntityReportDateDao.getNextReportDate(baseEntity.getId(), baseEntity.getReportDate());
                        }
                        nextReportDate = nextReportDateByEntity;
                    } else {
                        if (reportDatesBySetNotLoaded) {
                            reportDatesByEntityNotLoaded = false;
                            IBaseSetValueDao baseSetValueDao =
                                    persistableDaoPool.getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);
                            nextReportDateBySet = baseSetValueDao.getNextReportDate(childBaseSetApplied.getId(), baseEntity.getReportDate());
                        }
                        nextReportDate = nextReportDateBySet;
                    }

                }

                // Выходим если уже обрабатывали данное значение
                if (processedUuids.contains(childBaseValueLoaded.getUuid())) {
                    continue;
                }

                // Получаем даты загрузки и сохранения
                reportDateSaving = baseValueSaving.getRepDate();
                reportDateLoaded = childBaseValueLoaded.getRepDate();
                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                // Если дата загруженной записи совпадает с датой сохранения
                if (compare == 0) {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);
                    boolean last = childBaseValueLoaded.isLast();

                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext != null && childBaseValueNext.isClosed()) {
                        baseEntityManager.registerAsDeleted(childBaseValueNext);
                        last = childBaseValueNext.isLast();
                    }

                    if (last) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                        if (childBaseValuePrevious != null) {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setBatch(baseValueSaving.getBatch());
                            childBaseValuePrevious.setIndex(baseValueSaving.getIndex());
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                }
                // Если дата загруженной записи ранее даты сохранения
                else if (compare == 1) {
                    // Находим следующую запись по данному значению
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                    if (childBaseValueNext == null || !childBaseValueNext.isClosed()) // What we are checking?!
                    {
                        // Записываем запись закрытия на дату сохранения (isClosed = true, isLast = ?)
                        IBaseValue childBaseValue = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                baseValueSaving.getRepDate(),
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                        childBaseValueLoaded.getValue(),
                                true,
                                childBaseValueLoaded.isLast()
                        );
                        childBaseValue.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsInserted(childBaseValue);

                        // В случае, если запись была последней, то снимаем признак последней записи
                        if (childBaseValueLoaded.isLast()) {
                            IBaseValue childBaseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getId(),
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    childBaseValueLoaded.getRepDate(),
                                    childMetaValue.getTypeCode() == DataTypes.DATE ?
                                            new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                            childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    false
                            );
                            childBaseValueLast.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsUpdated(childBaseValueLast);
                        }
                    } else {
                        if (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_SET) {
                            // Переносим следущую запись на дату сохранения
                            childBaseValueNext.setBaseContainer(childBaseSetApplied);
                            childBaseValueNext.setBatch(baseValueSaving.getBatch());
                            childBaseValueNext.setIndex(baseValueSaving.getIndex());
                            childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                            baseEntityManager.registerAsUpdated(childBaseValueNext);
                        } else if (historyType == IBaseSetValue.HistoryType.RESTRICTED_BY_ENTITY) {
                            if (DataTypeUtil.compareBeginningOfTheDay(nextReportDate, childBaseValueNext.getRepDate()) == 0) {
                                // Переносим следущую запись на дату сохранения
                                childBaseValueNext.setBaseContainer(childBaseSetApplied);
                                childBaseValueNext.setBatch(baseValueSaving.getBatch());
                                childBaseValueNext.setIndex(baseValueSaving.getIndex());
                                childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                                baseEntityManager.registerAsUpdated(childBaseValueNext);
                            } else {
                                // Записываем запись закрытия на дату сохранения (isClosed = true, isLast = false)
                                IBaseValue childBaseValueClosed = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        baseValueSaving.getBatch(),
                                        baseValueSaving.getIndex(),
                                        baseValueSaving.getRepDate(),
                                        childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                                childBaseValueLoaded.getValue(),
                                        true,
                                        false
                                );
                                childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                                baseEntityManager.registerAsInserted(childBaseValueClosed);

                                // В случае, если имеется следующая дата, то добавляем запись открытия
                                // на следующую дату по данному значению (isClosed = false, isLast = false)
                                // Можно не проверять, т.к. если следующая запись по данному значению
                                if (nextReportDate != null) {
                                    IBaseValue childBaseValueOpened = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            baseValueSaving.getBatch(),
                                            baseValueSaving.getIndex(),
                                            nextReportDate,
                                            childMetaValue.getTypeCode() == DataTypes.DATE ?
                                                    new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                                    childBaseValueLoaded.getValue(),
                                            false,
                                            false
                                    );
                                    childBaseValueOpened.setBaseContainer(childBaseSetApplied);
                                    baseEntityManager.registerAsInserted(childBaseValueOpened);
                                }
                            }
                        } else {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }
                    }
                }
                // Если дата загруженной записи позднее даты сохранения
                else if (compare == -1) {
                    // Nothing
                }
            }
        }
    }

    protected void applyComplexSet(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                   IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        IMetaSet childMetaSet = (IMetaSet) metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();
        IMetaClass childMetaClass = (IMetaClass) childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet) baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;

        Date reportDateSaving = null;
        Date reportDateLoaded = null;

        if (baseValueLoaded != null) {
            reportDateLoaded = baseValueLoaded.getRepDate();
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                //set deletion
                reportDateSaving = baseValueSaving.getRepDate();
                //boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
                if (compare == 0) {
                    if (metaAttribute.isFinal()) // ? Should be not null for existing BaseSet ???
                    {
                        if (!childMetaClass.isSearchable() && childMetaClass.hasNotFinalAttributes()) {
                            throw new RuntimeException("Detected situation where one or more attributes " +
                                    "without final flag contains in attribute with final flag. Class name: " +
                                    baseEntity.getMeta().getClassName() + ", attribute: " + metaAttribute.getName());
                        }

                        // TODO: Clone child instance of BaseEntity or maybe use variable baseEntityLoaded to registration as deleted
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueLoaded.getBatch(),
                                baseValueLoaded.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast()
                        );
                        baseValueDeleted.setBaseContainer(baseEntity);
                        baseValueDeleted.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);
                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);
                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntity);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(baseValuePrevious);
                            }
                        }

                        if (!metaAttribute.isImmutable() && !childMetaClass.isSearchable()) {
                            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                                IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();
                                IBaseEntity childBaseEntitySaving = new BaseEntity(childBaseEntityLoaded, baseValueSaving.getRepDate());
                                for (String attributeName : childMetaClass.getAttributeNames()) {
                                    childBaseEntitySaving.put(attributeName,
                                            BaseValueFactory.create(
                                                    MetaContainerTypes.META_CLASS,
                                                    childMetaType,
                                                    baseValueSaving.getBatch(),
                                                    baseValueSaving.getIndex(),
                                                    new Date(baseValueSaving.getRepDate().getTime()),
                                                    null));
                                }
                                applyBaseEntityAdvanced(childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager);

                                IBaseSetComplexValueDao baseSetComplexValueDao = persistableDaoPool
                                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetComplexValueDao.class);
                                boolean singleBaseValue = baseSetComplexValueDao.isSingleBaseValue(childBaseValueLoaded);
                                if (singleBaseValue) {
                                    baseEntityManager.registerAsDeleted(childBaseEntityLoaded);
                                }
                            }
                        }
                        return;
                    } else {
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
                } else if (compare == 1) {
                    if (metaAttribute.isFinal()) {
                        throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                                "mistakenly loaded from the database.");
                    }

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

                    if (baseValueLoaded.isLast()) {
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
            } else {
                reportDateSaving = baseValueSaving.getRepDate();
                //boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (metaAttribute.isFinal() && !(compare == 0)) {
                    throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                            "mistakenly loaded from the database.");
                } else if (compare >= 0) {
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
                } else if (compare == -1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getBatch(),
                            baseValueLoaded.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            }
        } else {
            if (childBaseSetSaving == null) {
                return;
            }

            reportDateSaving = baseValueSaving.getRepDate();

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = null;
            if (!metaAttribute.isFinal()) {
                baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving); // searches for closed BV at reportDateSaving

                if (baseValueClosed != null) {
                    reportDateLoaded = baseValueClosed.getRepDate();

                    childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
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
            }

            if (baseValueClosed == null) {
                IBaseEntityReportDateDao baseEntityReportDateDao =
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                reportDateLoaded = baseEntityReportDateDao.getMinReportDate(baseEntity.getId());
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                boolean isNew = true;
                if (compare == 1) {
                    IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                    if (baseValuePrevious != null) {
                        reportDateLoaded = baseValuePrevious.getRepDate();

                        childBaseSetLoaded = (IBaseSet) baseValuePrevious.getValue();
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

                        if (baseValuePrevious.isLast()) {
                            baseValuePrevious.setBaseContainer(baseEntity);
                            baseValuePrevious.setMetaAttribute(metaAttribute);
                            baseValuePrevious.setBatch(baseValueSaving.getBatch());
                            baseValuePrevious.setIndex(baseValueSaving.getIndex());
                            baseValuePrevious.setLast(false);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        }
                        isNew = false;
                    }

                } else if (compare == -1) {
                    IBaseValue baseValueNext = baseValueDao.getNextBaseValue(baseValueSaving);
                    if (baseValueNext != null) {
                        boolean nextByRepDateIsNextByBaseValue =
                                DataUtils.compareBeginningOfTheDay(reportDateLoaded, baseValueNext.getRepDate()) == 0;
                        if (nextByRepDateIsNextByBaseValue) {
                            // on reportDateLoaded (minReportDate) baseEntity existed, but baseValue did not.
                            reportDateLoaded = baseValueNext.getRepDate();
                        }

                        childBaseSetLoaded = (IBaseSet) baseValueNext.getValue();
                        childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                false); //baseValueNext.isLast());
                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);

//                        if (baseValueNext.isLast())
//                        {
//                            baseValueNext.setBaseContainer(baseEntity);
//                            baseValueNext.setMetaAttribute(metaAttribute);
//                            baseValueNext.setBatch(baseValueSaving.getBatch());
//                            baseValueNext.setIndex(baseValueSaving.getIndex());
//                            baseValueNext.setLast(false);
//                            baseEntityManager.registerAsUpdated(baseValueNext);
//                        }
                        isNew = false;

                        // TODO: Close @reportDateLoaded
                        if (!nextByRepDateIsNextByBaseValue) {
                            IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    metaType,
                                    baseValueSaving.getBatch(),
                                    baseValueSaving.getIndex(),
                                    reportDateLoaded,
                                    childBaseSetApplied,
                                    true,
                                    false); //baseValueNext.isLast());
                            baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed); // TODO: ?
                            baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                        }

                    }
                } else { // compare == 0
                    // TODO: cannot be equal, since baseValueLoaded == null
                    // TODO: do nothing
                }

                if (isNew) {
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
                            compare != -1); // if compare = -1, closing value will be inserted, it will become last
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (compare == -1) {
                        // Close inserted value at reportDateLoaded
                        //childBaseSetApplied = new BaseSet(childMetaType);
                        //baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                reportDateLoaded,
                                childBaseSetApplied,
                                true,
                                true);
                        baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed);
                        baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                    }
                }
            }
        }

        Set<UUID> processedUuids = new HashSet<UUID>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            //merge set items
            boolean baseValueFound;
            int compare;
            if (reportDateSaving == null || reportDateLoaded == null) {
                compare = -2;
            } else {
                compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
            }

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                IBaseEntity childBaseEntitySaving = (IBaseEntity) childBaseValueSaving.getValue();
                if (childBaseSetLoaded != null) {
                    //boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                    if (!metaAttribute.isFinal() || (metaAttribute.isFinal() && compare == 0)) {
                        baseValueFound = false;
                        for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                            if (processedUuids.contains(childBaseValueLoaded.getUuid())) {
                                continue;
                            }

                            IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();
                            //if (childBaseValueSaving.equalsByValue(childBaseValueLoaded))
                            if (childBaseValueSaving.equals(childBaseValueLoaded)) {
                                // Mark as processed and found
                                processedUuids.add(childBaseValueLoaded.getUuid());
                                baseValueFound = true;

                                boolean nextByRepDateIsNextByBaseValue =
                                        DataUtils.compareBeginningOfTheDay(reportDateLoaded, childBaseValueLoaded.getRepDate()) == 0;

                                if (nextByRepDateIsNextByBaseValue) {
                                    Date reportDateApplied = compare == -1 ? new Date(childBaseValueSaving.getRepDate().getTime()) :
                                            new Date(childBaseValueLoaded.getRepDate().getTime());


                                    IBaseValue baseValueApplied = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueLoaded.getId(),
                                            childBaseValueLoaded.getBatch(),
                                            childBaseValueLoaded.getIndex(),
                                            reportDateApplied,
                                            //new Date(childBaseValueLoaded.getRepDate().getTime()),
                                            applyBaseEntityAdvanced(childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager),
                                            childBaseValueLoaded.isClosed(),
                                            childBaseValueLoaded.isLast());
                                    childBaseSetApplied.put(baseValueApplied);

                                    // Need to update existing record since reportDate changed to reportDateSaving and reportDateSaving < reportDateLoaded
                                    // When reportDateLoaded < reportDateSaving (compare != 1) then no need to update
                                    if (compare == -1) {
                                        baseEntityManager.registerAsUpdated(baseValueApplied);
                                    }
                                } else {
                                    // insert open @ reportDateSaving

                                    IBaseEntity baseEntityApplied =
                                            applyBaseEntityAdvanced(childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager);

                                    IBaseValue baseValueApplied = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueLoaded.getId(),
                                            childBaseValueLoaded.getBatch(),
                                            childBaseValueLoaded.getIndex(),
                                            reportDateSaving,
                                            baseEntityApplied,
                                            false,
                                            compare == 1 && childBaseValueLoaded.isLast());
                                    childBaseSetApplied.put(baseValueApplied);
                                    //baseEntity.put(metaAttribute.getName(), baseValueApplied);
                                    baseEntityManager.registerAsInserted(baseValueApplied);

                                    if (compare == -1) {
                                        // insert closed @reportDateLoaded
                                        IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                                MetaContainerTypes.META_SET,
                                                childMetaType,
                                                childBaseValueLoaded.getId(),
                                                childBaseValueLoaded.getBatch(),
                                                childBaseValueLoaded.getIndex(),
                                                reportDateLoaded, //reportDateSaving,
                                                baseEntityApplied,
                                                true,
                                                false);

                                        childBaseSetApplied.put(baseValueAppliedClosed);
                                        //baseEntity.put(metaAttribute.getName(), baseValueApplied);
                                        baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                                    }
                                }

                                break;
                            }
                        }

                        if (baseValueFound) {
                            continue;
                        }
                    }
                }

                if (childBaseEntitySaving.getId() > 0 && compare != -1) {
                    IBaseSetValueDao setValueDao = persistableDaoPool
                            .getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);

                    if (!metaAttribute.isFinal()) {
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
                        if (childBaseValueClosed != null) {
                            childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsDeleted(childBaseValueClosed);

                            IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                            if (childBaseValuePrevious != null && childBaseValuePrevious.getValue() != null) {
                                IBaseEntity childBaseEntityPrevious = (IBaseEntity) childBaseValuePrevious.getValue();
                                childBaseValuePrevious.setValue(applyBaseEntityAdvanced(childBaseEntitySaving,
                                        childBaseEntityPrevious, baseEntityManager));
                                if (childBaseValueClosed.isLast()) {
                                    childBaseValuePrevious.setIndex(childBaseValueSaving.getIndex());
                                    childBaseValuePrevious.setBatch(childBaseValueSaving.getBatch());
                                    childBaseValuePrevious.setLast(true);

                                    childBaseSetApplied.put(childBaseValuePrevious);
                                    baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                                } else {
                                    childBaseSetApplied.put(childBaseValuePrevious);
                                }
                            }
                            continue;
                        }

                        // Check next value
                        IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                        if (childBaseValueNext != null) {
                            IBaseEntity childBaseEntityNext = (IBaseEntity) childBaseValueNext.getValue();

                            childBaseValueNext.setBatch(childBaseValueSaving.getBatch());
                            childBaseValueNext.setIndex(childBaseValueSaving.getIndex());
                            childBaseValueNext.setValue(applyBaseEntityAdvanced(childBaseEntitySaving,
                                    childBaseEntityNext, baseEntityManager));
                            childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                            childBaseSetApplied.put(childBaseValueNext);
                            baseEntityManager.registerAsUpdated(childBaseValueNext);
                            continue;
                        }
                    }

                    IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                    if (childBaseValueLast != null) {
                        childBaseValueLast.setBaseContainer(childBaseSetApplied);
                        childBaseValueLast.setBatch(childBaseValueSaving.getBatch());
                        childBaseValueLast.setIndex(childBaseValueSaving.getIndex());
                        childBaseValueLast.setLast(false);

                        baseEntityManager.registerAsUpdated(childBaseValueLast);
                    }
                }

                IBaseEntity baseEntitySavingTmp = apply(childBaseEntitySaving, baseEntityManager);
                IBaseEntityReportDateDao baseEntityReportDateDao =
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntity.getId());

                boolean isClosing = compare == -1 || (compare == -2 && reportDateSaving != null &&
                        DataUtils.compareBeginningOfTheDay(reportDateSaving, minReportDate) == -1);

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        childBaseValueSaving.getBatch(),
                        childBaseValueSaving.getIndex(),
                        childBaseValueSaving.getRepDate(),
                        //apply(childBaseEntitySaving, baseEntityManager),
                        baseEntitySavingTmp,
                        false,
                        !isClosing
                );
                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);


                if (isClosing) { // case when compare=-2 && reportDateLoaded = null

                    //IBaseEntity baseEntityAAA = apply(childBaseEntitySaving, baseEntityManager);
                    IBaseValue childBaseValueAppliedClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            childBaseValueSaving.getBatch(),
                            childBaseValueSaving.getIndex(),
                            reportDateLoaded != null ? reportDateLoaded : minReportDate,
                            //baseEntityAAA,
                            baseEntitySavingTmp,
                            true,
                            isClosing
                    );
                    //childBaseSetApplied.put(childBaseValueAppliedClosed);
                    childBaseValueAppliedClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsInserted(childBaseValueAppliedClosed);
                }
            }
        }


        //process deletions
        if (childBaseSetLoaded != null) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUuids.contains(childBaseValueLoaded.getUuid())) {
                    continue;
                }

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);
                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                //if (reportDateLoaded == null || DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0)
                if (compare != -1) {

                    if (reportDateLoaded == null || compare == 0) {
                        baseEntityManager.registerAsDeleted(childBaseValueLoaded);

                        IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();
                        if (childBaseEntityLoaded != null) {
                            baseEntityManager.registerUnusedBaseEntity(childBaseEntityLoaded);
                        }


                        boolean last = childBaseValueLoaded.isLast();

                        if (!metaAttribute.isFinal()) {
                            IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                            if (childBaseValueNext != null && childBaseValueNext.isClosed()) {
                                baseEntityManager.registerAsDeleted(childBaseValueNext);
                                IBaseEntity childBaseEntityNext = (IBaseEntity) childBaseValueNext.getValue();
                                if (childBaseEntityNext != null) {
                                    baseEntityManager.registerUnusedBaseEntity(childBaseEntityNext);
                                }

                                last = childBaseValueNext.isLast();
                            }
                        }

                        if (last && !(metaAttribute.isFinal() && !childMetaClass.isSearchable())) {
                            IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                            if (childBaseValuePrevious != null) {
                                childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                                childBaseValuePrevious.setBatch(baseValueSaving.getBatch());
                                childBaseValuePrevious.setIndex(baseValueSaving.getIndex());
                                childBaseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                            }
                        }
                    } else {


                        IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);
                        if (childBaseValueNext == null || !childBaseValueNext.isClosed()) {
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

                            if (childBaseValueLoaded.isLast()) {
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
                        } else {
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
    }

    protected void applySimpleValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                    IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaValue metaValue = (IMetaValue) metaType;

        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    if (metaAttribute.isFinal()) {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueLoaded.getBatch(),
                                baseValueLoaded.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast()
                        );

                        baseValueDeleted.setBaseContainer(baseEntity);
                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);

                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntity);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(baseValuePrevious);
                            }
                        }
                    } else {
                        IBaseValue baseValueClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                true,
                                baseValueLoaded.isLast()
                        );
                        baseValueClosed.setBaseContainer(baseEntity);
                        baseValueClosed.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                } else if (compare == 1) {
                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);

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
                                    new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                                    baseValueLoaded.getValue(),
                            true,
                            baseValueLoaded.isLast());

                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(baseValueClosed);
                }

                return;
            }

            if (baseValueSaving.equalsByValue(baseValueLoaded)) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // changing key values
                Object baseV;
                if (baseValueSaving.getNewBaseValue() != null) {
                    baseV = baseValueSaving.getNewBaseValue().getValue();
                } else {
                    baseV = metaValue.getTypeCode() == DataTypes.DATE ?
                            new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                            baseValueLoaded.getValue();
                }

                if (compare == 0 || compare == 1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getBatch(),
                            baseValueLoaded.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseV,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                } else if (compare == -1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getBatch(),
                            baseValueLoaded.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseV,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                } else if (compare == 1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            baseValueLoaded.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                metaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) baseValueLoaded.getValue()).getTime()) :
                                        baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);

                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                } else if (compare == -1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            baseValueLoaded.isLast()
                    );

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    // TODO: bug
                    /*IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                            baseValueSaving.getValue(),
                            true,
                            false);

                    baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed);
                    baseEntityManager.registerAsInserted(baseValueAppliedClosed);*/
                }
            }
        } else {
            if (baseValueSaving.getValue() == null)
                return;

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = null;

            // Проверку закрытого значения производим только если атрибут не имеет флага IS_FINAL.
            if (!metaAttribute.isFinal()) {
                baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
                if (baseValueClosed != null) {
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseValueClosed.setBaseContainer(baseEntity);
                        baseEntityManager.registerAsDeleted(baseValueClosed);

                        IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);
                        if (baseValueClosed.isLast()) {
                            baseValuePrevious.setIndex(baseValueSaving.getIndex());
                            baseValuePrevious.setBatch(baseValueSaving.getBatch());
                            baseValuePrevious.setLast(true);

                            baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        } else {
                            baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        }
                    } else {
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
            }

            if (metaAttribute.isFinal() || baseValueClosed == null) {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);

                if (baseValueLast == null) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            metaValue.getTypeCode() == DataTypes.DATE ?
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            true);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();

                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);

                    boolean last = reportDateCompare == -1 ? false : true;

                    if (last) {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
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
                                    new Date(((Date) baseValueSaving.getValue()).getTime()) :
                                    baseValueSaving.getValue(),
                            false,
                            last);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }

    protected void applyComplexValue(IBaseEntity baseEntity, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                                     IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaClass metaClass = (IMetaClass) metaType;
        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
                if (compare == 0) {
                    if (metaAttribute.isFinal()) {
                        IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
                        IMetaClass childMetaClass = (IMetaClass) metaType;

                        if (childMetaClass.hasNotFinalAttributes() && !childMetaClass.isSearchable()) {
                            throw new RuntimeException("Detected situation where one or more attributes " +
                                    "without final flag contains in attribute with final flag. Class name: " +
                                    baseEntity.getMeta().getClassName() + ", attribute: " + metaAttribute.getName());
                        }

                        // TODO: Clone child instance of BaseEntity or maybe use variable baseEntityLoaded to registration as deleted
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueLoaded.getBatch(),
                                baseValueLoaded.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast()
                        );
                        baseValueDeleted.setBaseContainer(baseEntity);
                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);
                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);
                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntity);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(baseValuePrevious);
                            }
                        }

                        if (!childMetaClass.isSearchable() && !metaAttribute.isImmutable()) {
                            IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded, baseValueSaving.getRepDate());
                            for (String attributeName : childMetaClass.getAttributeNames()) {
                                IMetaAttribute childMetaAttribute = childMetaClass.getMetaAttribute(attributeName);
                                IMetaType childMetaType = childMetaAttribute.getMetaType();
                                baseEntitySaving.put(attributeName,
                                        BaseValueFactory.create(
                                                MetaContainerTypes.META_CLASS,
                                                childMetaType,
                                                baseValueSaving.getBatch(),
                                                baseValueSaving.getIndex(),
                                                new Date(baseValueSaving.getRepDate().getTime()),
                                                null));
                            }
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);

                            IBaseEntityComplexValueDao baseEntityComplexValueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseEntityComplexValueDao.class);
                            boolean singleBaseValue = baseEntityComplexValueDao.isSingleBaseValue(baseValueLoaded);
                            if (singleBaseValue) {
                                baseEntityManager.registerAsDeleted(baseEntityLoaded);
                            }
                        }
                        return;
                    } else {
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

                } else if (compare == 1) {
                    if (metaAttribute.isFinal()) {
                        throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                                "mistakenly loaded from the database.");
                    }

                    if (baseValueLoaded.isLast()) {
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
                /*else if(compare == -1)
                {
                    if (metaAttribute.isFinal())
                    {
                        throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                                "mistakenly loaded from the database.");
                    }

                }*/
                return;
            }

            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

            if (baseValueSaving.equalsByValue(baseValueLoaded) || !metaClass.isSearchable()) {
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1) {
                        throw new RuntimeException("Attempt to write immutable instance of " +
                                "BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                    }
                    //baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                    baseEntityApplied = loadByReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                int compare = DataTypeUtil.compareBeginningOfTheDay(baseValueSaving.getRepDate(), baseValueLoaded.getRepDate());
                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        baseValueLoaded.getBatch(),
                        baseValueLoaded.getIndex(),
                        compare == -1 ? new Date(baseValueSaving.getRepDate().getTime()) :
                                new Date(baseValueLoaded.getRepDate().getTime()),
                        baseEntityApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast()
                );
                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                if (compare == -1) {
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            } else {
                IBaseEntity baseEntityApplied;
                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new RuntimeException("Attempt to write immutable instance of " +
                                "BaseEntity with classname: " +
                                baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());

                    //baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                    baseEntityApplied = loadByReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(baseEntitySaving, baseEntityManager) :
                            applyBaseEntityAdvanced(baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();
/*                boolean reportDateEquals =
                        DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;*/
                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);
                if (compare == 0) {
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
                } else if (compare == 1) {
                    if (metaAttribute.isFinal()) {
                        throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                                "mistakenly loaded from the database.");
                    }

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

                    if (baseValueLoaded.isLast()) {
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
                } else if (compare == -1) {
                    if (metaAttribute.isFinal()) {
                        throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                                "mistakenly loaded from the database.");
                    }

                    if (baseValueLoaded.isClosed()) {

                        IBaseValue newBaseValueLoaded = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueSaving.getValue(),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast());

                        newBaseValueLoaded.setBaseContainer(baseEntity);
                        newBaseValueLoaded.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(newBaseValueLoaded);
                    }

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

                }
            }
        } else {
            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            if (baseEntitySaving == null) {
                return;
            }

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);
            IBaseValue baseValueClosed = null;
            if (!metaAttribute.isFinal()) {
                baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);
                if (baseValueClosed != null) {
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    IBaseEntity baseEntityClosed = (IBaseEntity) baseValueClosed.getValue();
                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseValueClosed.setBaseContainer(baseEntity);
                        baseEntityManager.registerAsDeleted(baseValueClosed);

                        IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                        IBaseEntity baseEntityApplied;
                        if (metaAttribute.isImmutable()) {
                            if (baseEntitySaving.getId() < 1) {
                                throw new RuntimeException("Attempt to write immutable instance of BaseEntity with classname: " +
                                        baseEntitySaving.getMeta().getClassName() + "\n" + baseEntitySaving.toString());
                            }
                            //baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                            //        baseEntitySaving.getReportDate());
                            baseEntityApplied = loadByReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(baseEntitySaving, baseEntityManager) :
                                    applyBaseEntityAdvanced(baseEntitySaving, baseEntityClosed, baseEntityManager);
                        }
                        baseValuePrevious.setValue(baseEntityApplied);

                        if (baseValueClosed.isLast()) {
                            baseValuePrevious.setIndex(baseValueSaving.getIndex());
                            baseValuePrevious.setBatch(baseValueSaving.getBatch());
                            baseValuePrevious.setLast(true);

                            baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        } else {
                            baseEntity.put(metaAttribute.getName(), baseValuePrevious);
                        }
                    } else {
                        IBaseEntity baseEntityApplied;
                        if (metaAttribute.isImmutable()) {
                            if (baseEntitySaving.getId() < 1)
                                throw new RuntimeException("Attempt to write immutable instance of " +
                                        "BaseEntity with classname: " + baseEntitySaving.getMeta().getClassName() +
                                        "\n" + baseEntitySaving.toString());

                            //baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                            //        baseEntitySaving.getReportDate());
                            baseEntityApplied = loadByReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
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
            }

            if (metaAttribute.isFinal() || baseValueClosed == null) {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                IBaseEntity baseEntityApplied = null;
                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new RuntimeException("Attempt to write immutable instance of " +
                                "BaseEntity with classname: " + baseEntitySaving.getMeta().getClassName() +
                                "\n" + baseEntitySaving.toString());

                    //baseEntityApplied = loadByMaxReportDate(baseEntitySaving.getId(),
                    //        baseEntitySaving.getReportDate());
                    baseEntityApplied = loadByReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    if (metaAttribute.isFinal()) {
                        IBaseValue previousBaseValue = valueDao.getPreviousBaseValue(baseValueSaving);
                        if (previousBaseValue != null) {
                            baseEntityApplied = applyBaseEntityAdvanced(baseEntitySaving,
                                    (IBaseEntity) previousBaseValue.getValue(), baseEntityManager);
                        } else {
                            IBaseValue nextBaseValue = valueDao.getNextBaseValue(baseValueSaving);
                            if (nextBaseValue != null) {
                                baseEntityApplied = applyBaseEntityAdvanced(baseEntitySaving,
                                        (IBaseEntity) nextBaseValue.getValue(), baseEntityManager);
                            } else {
                                baseEntityApplied = apply(baseEntitySaving, baseEntityManager);
                            }
                        }
                    } else {
                        baseEntityApplied = apply(baseEntitySaving, baseEntityManager);
                    }
                }

                if (baseValueLast == null) {
                    boolean isClosing = false;
                    if (baseEntitySaving.getId() > 0) {
                        Date reportDateSaving = new Date(baseValueSaving.getRepDate().getTime());
                        IBaseEntityReportDateDao baseEntityReportDateDao =
                                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                        Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntitySaving.getId());

                        // TODO: Inserting on earlier(nonexisting) date: consider case when only root entity existed before,
                        // TODO: but all the child entities are new; entity Id could be null?

                        int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, minReportDate);
                        if (compare == -1) {
                            isClosing = true;
                        }
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueSaving.getBatch(),
                            baseValueSaving.getIndex(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            !isClosing
                    );
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (isClosing) {
                        IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueSaving.getBatch(),
                                baseValueSaving.getIndex(),
                                new Date(baseValueSaving.getRepDate().getTime()),
                                baseEntityApplied,
                                true,
                                true
                        );
                        baseEntity.put(metaAttribute.getName(), baseValueAppliedClosed);
                        baseEntityManager.registerAsInserted(baseValueAppliedClosed);
                    }
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();
                    int reportDateCompare =
                            DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);
                    boolean last = reportDateCompare != -1;

                    if (last) {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
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
    public IBaseEntity process(IBaseEntity baseEntity) {
        EntityHolder entityHolder = new EntityHolder();

        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity baseEntityPrepared = prepare(((BaseEntity) baseEntity).clone());
        IBaseEntity baseEntityPostPrepared = postPrepare(((BaseEntity) baseEntityPrepared).clone(), null);
        IBaseEntity baseEntityApplied;

        baseEntityApplied = apply(baseEntityPostPrepared, baseEntityManager, entityHolder);

        applyToDb(baseEntityManager);

        // TODO: Uncomment on finish
        /* if (baseEntityPostPrepared.getOperation() != null) {
            switch (baseEntityPostPrepared.getOperation()) {
                case DELETE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new RuntimeException("deleting entity must be found");

                    if (baseEntity.getMeta().isReference() && historyExists(
                            baseEntityPostPrepared.getMeta().getId(), baseEntityPostPrepared.getId()))
                        throw new RuntimeException("Reference with history cannot be deleted!");

                    baseEntityManager.registerAsDeleted(baseEntityPostPrepared);
                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    entityHolder.applied = baseEntityApplied;
                    entityHolder.saving = baseEntityPostPrepared;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported operation: "
                            + baseEntityPostPrepared.getOperation());
            }
        } else {
            baseEntityApplied = apply(baseEntityPostPrepared, baseEntityManager, entityHolder);
        } */

        /* if (applyListener != null)
            applyListener.applyToDBEnded(entityHolder.saving, entityHolder.loaded,
                    entityHolder.applied, baseEntityManager); */

        return baseEntityApplied;
    }

    private boolean historyExists(long metaId, long entityId) {
        List<Map<String, Object>> rows = getRefListResponseWithHis(metaId, entityId);
        return rows.size() > 1;
    }

    public boolean checkReportDateExists(long baseEntityId, Date reportDate) {
        Select select = context
                .select(DSL.count().as("report_dates_count"))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(baseEntityId))
                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.equal(DataUtils.convert(reportDate)));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        return ((BigDecimal) rows.get(0).get("report_dates_count")).longValue() > 0;
    }

    private Set<BaseEntity> collectComplexSetValues(BaseSet baseSet) {
        Set<BaseEntity> entities = new HashSet<BaseEntity>();

        IMetaType metaType = baseSet.getMemberType();
        if (metaType.isSetOfSets()) {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext()) {
                IBaseValue baseValue = it.next();
                entities.addAll(collectComplexSetValues((BaseSet) baseValue.getValue()));
            }
        } else {
            Collection<IBaseValue> baseValues = baseSet.get();
            Iterator<IBaseValue> it = baseValues.iterator();
            while (it.hasNext()) {
                IBaseValue baseValue = it.next();
                entities.add((BaseEntity) baseValue.getValue());
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
        while (i.hasNext()) {
            Map<String, Object> row = i.next();

            entityIds.add(((BigDecimal) row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return entityIds;
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select complexAttrsSelect = context.select().from(EAV_M_COMPLEX_ATTRIBUTES).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select simpleSetsSelect = context.select().from(EAV_M_SIMPLE_SET).where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(metaClassId));
        Select complexSetsSelect = context.select().from(EAV_M_COMPLEX_SET).where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexAttrs = queryForListWithStats(complexAttrsSelect.getSQL(), complexAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> simpleSets = queryForListWithStats(simpleSetsSelect.getSQL(), simpleSetsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexSets = queryForListWithStats(complexSetsSelect.getSQL(), complexSetsSelect.getBindValues().toArray());

        List<String> names = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : complexAttrs) {
            String attrName = (String) attr.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_COMPLEX_ATTRIBUTES.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : simpleSets) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_SET.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_SIMPLE_SET.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : complexSets) {
            String attrName = (String) attr.get(EAV_M_COMPLEX_SET.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_COMPLEX_SET.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        return new RefColumnsResponse(names, titles);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        List<Map<String, Object>> rows = getRefListResponseWithHis(metaClassId, null);

        addOpenCloseDates(rows);

        if (date != null) {
            rows = filter(rows, date);
        }

        // check: rows must be sorted at this stage

        if (!withHis) {
            rows = removeHistory(rows);
        }

        return new RefListResponse(rows);
    }

    private List<Map<String, Object>> removeHistory(List<Map<String, Object>> rows) {
        Map<Object, Map<String, Object>> groupedRows = new TreeMap<Object, Map<String, Object>>();

        for (Map<String, Object> row : rows) {
            String groupProperty = "ID";
            Object groupPropertyValue = row.get(groupProperty);
            groupedRows.put(groupPropertyValue, row);
        }

        return new ArrayList<Map<String, Object>>(groupedRows.values());
    }

    private List<Map<String, Object>> filter(List<Map<String, Object>> rows, Date date) {
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();

        for (Map<String, Object> row : rows) {
            Date repDate = (Date) row.get("report_date");

            if (!repDate.after(date)) {
                filtered.add(row);
            }
        }

        return filtered;
    }

    private void addOpenCloseDates(List<Map<String, Object>> rows) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        Map<String, Object> prev = null;

        for (Map<String, Object> row : rows) {
            Date repDate = (Date) row.get("report_date");
            String sRepDate = df.format(repDate);

            row.put("open_date", sRepDate);

            if (prev != null) {
                Object id = row.get("ID");
                Object prevId = prev.get("ID");

                if (id.equals(prevId)) {
                    prev.put("close_date", sRepDate);
                }
            }
            prev = row;
        }
    }

    private List<Map<String, Object>> getRefListResponseWithHis(long metaClassId, Long entityId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(DSL.field("id"));
        fields.add(DSL.min(DSL.field("report_date")).as("report_date"));

        Collection<Field> groupByFields = new ArrayList<Field>();
        groupByFields.add(DSL.field("id"));

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            attrName = "\"" + attrName + "\"";
            fields.add(DSL.field(attrName));
            groupByFields.add(DSL.field(attrName));
        }

        Collection<Field> fieldsInner = new ArrayList<Field>();

        fieldsInner.add(DSL.field("\"dat\".id"));
        fieldsInner.add(DSL.field("\"dat\".report_date"));

        for (Map<String, Object> attr : simpleAttrs) {
            BigDecimal attrId = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.ID.getName());
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrType = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE.getName());
            BigDecimal attrFinal = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL.getName());
            boolean isFinal = attrFinal != null && attrFinal.byteValue() != 0;
            Table valuesTable = getValuesTable(attrType);

            SelectConditionStep<Record1<Object>> selectMaxRepDate = context.select(DSL.max(DSL.field("report_date"))).from(valuesTable)
                    .where(DSL.field("attribute_id").eq(attrId))
                    .and(DSL.field("entity_id").eq(DSL.field("\"dat\".id")));

            if (isFinal) {
                selectMaxRepDate.and(DSL.field("report_date").eq(DSL.field("\"dat\".report_date")));
            } else {
                selectMaxRepDate.and(DSL.field("report_date").le(DSL.field("\"dat\".report_date")));
            }


            Field fieldInner = context.select(DSL.field("value")).from(valuesTable)
                    .where(DSL.field("attribute_id").eq(attrId)).and(DSL.field("report_date").eq(
                            selectMaxRepDate
                    ).and(DSL.field("entity_id").eq(DSL.field("\"dat\".id"))))
                    .asField(attrName);

            fieldsInner.add(fieldInner);
        }

        SelectLimitStep select = context.select(fields.toArray(new Field[]{})).from(
                context.select(fieldsInner.toArray(new Field[]{})).from(
                        context.select(EAV_BE_ENTITIES.ID, EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                                .from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES)
                                .on(EAV_BE_ENTITIES.ID.eq(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(entityId != null ? EAV_BE_ENTITIES.ID.eq(entityId) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
                                .asTable("dat")
                )
        ).groupBy(groupByFields).orderBy(DSL.field("id"), DSL.min(DSL.field("report_date")));

        return queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
    }

    private List<Map<String, Object>> getRefListResponseWithoutHis(long metaClassId, Date date) {
        java.sql.Date dt = null;

        if (date != null) {
            dt = new java.sql.Date(date.getTime());
        }

        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());

        Collection<Field> fields = new ArrayList<Field>();

        fields.add(DSL.field("\"enr\".id"));
        fields.add(DSL.field("\"enr\".report_date"));

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            fields.add(DSL.field("\"s_" + attrName + "\".value").as(attrName));
        }

        SelectJoinStep select = context.select(fields.toArray(new Field[]{})).from(
                context.select().from(
                        context.select(
                                EAV_BE_ENTITIES.ID,
                                EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE,
                                DSL.rowNumber().over().partitionBy(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID).orderBy(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.desc()).as("p")
                        ).from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES).on(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(dt != null ? EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.le(dt) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
                                .asTable("sub")
                ).where(DSL.field("\"sub\".\"p\"").eq(1)).asTable("enr")
        );

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrType = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE.getName());
            String attrSubTable = "s_" + attrName;

            Table valuesTable = getValuesTable(attrType);

            select.leftOuterJoin(
                    context.select().from(
                            context.select(
                                    valuesTable.field("ENTITY_ID"),
                                    valuesTable.field("VALUE"),
                                    valuesTable.field("REPORT_DATE"),
                                    DSL.rowNumber().over().partitionBy(valuesTable.field("ENTITY_ID")).orderBy(valuesTable.field("REPORT_DATE").desc()).as("p")
                            ).from(valuesTable)
                                    .where(valuesTable.field("ATTRIBUTE_ID").eq(attr.get("ID")))
                                    .and(dt != null ? valuesTable.field("REPORT_DATE").le(dt) : DSL.trueCondition())
                                    .asTable("sub")
                    ).where(DSL.field("\"sub\".\"p\"").eq(1))
                            .asTable(attrSubTable)
            ).on("\"" + attrSubTable + "\"" + "." + "ENTITY_ID = \"enr\".id");
        }

        return queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
    }

    private Table getValuesTable(String attrType) {
        switch (attrType) {
            case "STRING":
                return EAV_BE_STRING_VALUES;
            case "DOUBLE":
                return EAV_BE_DOUBLE_VALUES;
            case "INTEGER":
                return EAV_BE_INTEGER_VALUES;
            case "DATE":
                return EAV_BE_DATE_VALUES;
            case "BOOLEAN":
                return EAV_BE_BOOLEAN_VALUES;
        }

        return null;
    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        List<RefListItem> items = getRefsByMetaclassInner(metaClassId, false);
        return items;
    }

    /**
     * Analog of getRefsByMetaClass method, not escapes content with quotes
     */
    public List<RefListItem> getRefsByMetaclassRaw(long metaClassId) {
        List<RefListItem> items = getRefsByMetaclassInner(metaClassId, true);
        return items;
    }

    private List<RefListItem> getRefsByMetaclassInner(long metaClassId, boolean raw) {
        List<Map<String, Object>> rows = getRefListResponseWithoutHis(metaClassId, null);

        List<RefListItem> items = new ArrayList<RefListItem>();

        String titleKey = null;

        if (!rows.isEmpty()) {
            Set<String> keys = rows.get(0).keySet();

            for (String key : keys) {
                if (key.startsWith("name")) {
                    titleKey = key;
                    break;
                }
            }
        }

        for (Map<String, Object> row : rows) {
            Object id = row.get("ID");
            String title = titleKey != null ? (String) row.get(titleKey) : "------------------------";

            RefListItem item = new RefListItem();
            item.setId(((BigDecimal) id).longValue());
            item.setTitle(raw ? title : Quote.addSlashes(title));
            items.add(item);
        }

        return items;
    }

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta) {
        List<Long> ids = getEntityIDsByMetaclass(meta.getId());

        ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();

        for (Long id : ids) {
            entities.add((BaseEntity) load(id));
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

        if (rows.size() > 1) {
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

    /**
     * @param baseEntityLeft    - first base entity
     * @param baseEntityRight   -  second base entity
     * @param mergeManager      -  - merge manager containing information about how the two entities
     *                          are to be merged
     * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
     *                          from the merge operation
     * @param choice            - MergeResultChoice object - determines the resulting entity
     * @param deleteUnused
     * @return IBaseEntity containing the result of the merge operation. Depending on
     * choice it is either left or right entity
     * @author dakkuliyev
     * Given right and left entities, merge manager object, base entity manager object,
     * and merge result choice perform merge operation. Does not perform any operations in
     * the database.
     */
    private IBaseEntity mergeBaseEntity(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight, IBaseEntityMergeManager mergeManager,
                                        IBaseEntityManager baseEntityManager, MergeResultChoice choice, boolean deleteUnused) {

        // although it is safe to assume that both entities exist in DB, it is still worth checking
        if (baseEntityLeft.getId() < 1 && baseEntityRight.getId() < 1) {
            throw new RuntimeException("Merging two BaseEntity objects requires " +
                    "for both objects to exits in DB.");
        }

        IMetaClass metaClass = baseEntityLeft.getMeta();
        IBaseEntity baseEntityApplied;


        if (choice == MergeResultChoice.RIGHT) {
            baseEntityApplied = new BaseEntity(baseEntityRight, baseEntityRight.getReportDate());
        } else {
            baseEntityApplied = new BaseEntity(baseEntityLeft, baseEntityLeft.getReportDate());
        }

        for (String attribute : metaClass.getAttributeNames()) {
            IBaseValue baseValueLeft = baseEntityLeft.getBaseValue(attribute);
            IBaseValue baseValueRight = baseEntityRight.getBaseValue(attribute);
            MergeManagerKey attrKey = new MergeManagerKey(attribute);

            if (baseValueLeft != null && baseValueRight != null) {
                IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
                IMetaType metaType = metaAttribute.getMetaType();
                // since there is no child map - there is need to look for child merge manager
                if (mergeManager.getChildMap() == null) {
                    if (metaType.isSetOfSets()) {
                        throw new UnsupportedOperationException("Not yet implemented.");
                    }

                    if (metaType.isSet()) {
                        // merge set
                        mergeSet(baseEntityApplied, baseValueLeft, baseValueRight,
                                mergeManager, baseEntityManager, choice, deleteUnused);
                    } else {
                        // merge value
                        mergeValue(baseEntityApplied, baseValueLeft, baseValueRight,
                                mergeManager, baseEntityManager, choice, deleteUnused);
                    }
                } else {
                    // get child manager for this attribute
                    if (mergeManager.containsKey(attrKey)) {

                        if (metaType.isSetOfSets()) {
                            throw new UnsupportedOperationException("Not yet implemented.");
                        }

                        if (metaType.isSet()) {
                            // merge set
                            mergeSet(baseEntityApplied, baseValueLeft, baseValueRight,
                                    mergeManager.getChildManager(attrKey), baseEntityManager, choice, deleteUnused);
                        } else {
                            // merge value
                            mergeValue(baseEntityApplied, baseValueLeft, baseValueRight,
                                    mergeManager.getChildManager(attrKey), baseEntityManager, choice, deleteUnused);
                        }

                    } else {
                        // not present in merge manager - hence just copy to baseEntityApplied
                        if (choice == MergeResultChoice.RIGHT) {
                            IBaseValue baseValueRightApplied = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    metaType,
                                    baseValueRight.getId(),
                                    baseValueRight.getBatch(),
                                    baseValueRight.getIndex(),
                                    new Date(baseValueRight.getRepDate().getTime()),
                                    baseValueRight.getValue(),
                                    baseValueRight.isClosed(),
                                    baseValueRight.isLast());
                            baseEntityApplied.put(metaAttribute.getName(), baseValueRightApplied);
                        } else {
                            IBaseValue baseValueLeftApplied = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    metaType,
                                    baseValueLeft.getId(),
                                    baseValueLeft.getBatch(),
                                    baseValueLeft.getIndex(),
                                    new Date(baseValueLeft.getRepDate().getTime()),
                                    baseValueLeft.getValue(),
                                    baseValueLeft.isClosed(),
                                    baseValueLeft.isLast());
                            baseEntityApplied.put(metaAttribute.getName(), baseValueLeftApplied);
                        }

                    }

                }

            } else {
                // if both of the values are null - do nothing
                if (baseValueLeft == null && baseValueRight == null) {
                    continue;
                } else {
                    if (mergeManager.containsKey(attrKey)) {
                        // one of the basevalues is null and there is a child manager
                        mergeNullValue(baseEntityApplied, baseEntityLeft, baseEntityRight, mergeManager.getChildManager(attrKey),
                                baseEntityManager, choice, attribute);
                    } else {
                        // one of the basevalues is null and there is no child manager
                        if (choice == MergeResultChoice.RIGHT) {
                            if (baseValueRight != null)
                                baseEntityApplied.put(baseEntityApplied.isSet() ? null : attribute, baseValueRight);
                        } else {
                            if (baseValueLeft != null)
                                baseEntityApplied.put(baseEntityApplied.isSet() ? null : attribute, baseValueLeft);
                        }
                    }
                }
            }
        }

        return baseEntityApplied;
    }

    /**
     * @param baseEntity        - baseEntity resulting from the merge operation
     * @param baseEntityLeft    - first base entity
     * @param baseEntityRight   -  second base entity
     * @param mergeManager      -  - merge manager containing information about how the two entities
     *                          are to be merged
     * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
     *                          from the merge operation
     * @param choice            - MergeResultChoice object - determines the resulting entity
     * @param attribute         - attribute of the null base value
     * @author dakkuliyev
     * Given right and left entities, merge manager object, base entity manager object,
     * and merge result choice perform merge operation. Does not perform any operations in
     * the database. The result of the opration is reflected in baseEntity. Method does not
     * return anything.
     * This method is used when one of entities has null base value
     */
    private void mergeNullValue(IBaseEntity baseEntity, IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
                                IBaseEntityMergeManager mergeManager,
                                IBaseEntityManager baseEntityManager, MergeResultChoice choice, String attribute) {
        IBaseValue baseValueLeft = baseEntityLeft.getBaseValue(attribute);
        IBaseValue baseValueRight = baseEntityRight.getBaseValue(attribute);
        if (baseValueLeft == null) {
            IMetaAttribute metaAttribute = baseValueRight.getMetaAttribute();
            IMetaType metaType = metaAttribute.getMetaType();
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
                IBaseValue oldBaseValueRight = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueRight.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        baseValueRight.getValue(),
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());

                if (choice == MergeResultChoice.RIGHT) {
                    oldBaseValueRight.setBaseContainer(baseEntity);
                } else {
                    oldBaseValueRight.setBaseContainer(baseEntityRight);
                }
                // delete old baseValueRight
                oldBaseValueRight.setMetaAttribute(metaAttribute);
                baseEntityManager.registerAsDeleted(oldBaseValueRight);
            }
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT ||
                    mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
                // copy from right to left
                IBaseValue newBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueRight.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        baseValueRight.getValue(),
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());

                if (choice == MergeResultChoice.RIGHT) {
                    baseEntity.put(attribute, baseValueRight);

                    newBaseValueLeft.setBaseContainer(baseEntityLeft);
                    newBaseValueLeft.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(newBaseValueLeft);
                } else {
                    // insert newBaseValueLeft
                    baseEntity.put(attribute, newBaseValueLeft);
                    baseEntityManager.registerAsInserted(newBaseValueLeft);
                }
            }
        }

        if (baseValueRight == null) {
            IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
            IMetaType metaType = metaAttribute.getMetaType();
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
                IBaseValue oldBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueLeft.getBatch(),
                        baseValueLeft.getIndex(),
                        new Date(baseValueLeft.getRepDate().getTime()),
                        baseValueLeft.getValue(),
                        baseValueLeft.isClosed(),
                        baseValueLeft.isLast());

                if (choice == MergeResultChoice.RIGHT) {
                    oldBaseValueLeft.setBaseContainer(baseEntityLeft);
                } else {
                    oldBaseValueLeft.setBaseContainer(baseEntity);
                }
                oldBaseValueLeft.setMetaAttribute(metaAttribute);
                baseEntityManager.registerAsDeleted(oldBaseValueLeft);
            }
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT ||
                    mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
                // copy from left to right
                IBaseValue newBaseValueRight = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueLeft.getBatch(),
                        baseValueLeft.getIndex(),
                        new Date(baseValueLeft.getRepDate().getTime()),
                        baseValueLeft.getValue(),
                        baseValueLeft.isClosed(),
                        baseValueLeft.isLast());
                if (choice == MergeResultChoice.LEFT) {
                    baseEntity.put(attribute, baseValueLeft);

                    newBaseValueRight.setBaseContainer(baseEntityRight);
                    newBaseValueRight.setMetaAttribute(metaAttribute);
                    baseEntityManager.registerAsInserted(newBaseValueRight);
                } else {
                    // insert newBasevalueRight
                    baseEntity.put(attribute, newBaseValueRight);
                    baseEntityManager.registerAsInserted(newBaseValueRight);
                }
            }
        }
    }

    /**
     * @param baseEntity        - baseEntity resulting from the merge operation
     * @param baseValueLeft     - first base entity
     * @param baseValueRight    -  second base entity
     * @param mergeManager      -  - merge manager containing information about how the two entities
     *                          are to be merged
     * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
     *                          from the merge operation
     * @param choice            - MergeResultChoice object - determines the resulting entity
     * @param deleteUnused
     * @author dakkuliyev
     * Given right and left base values, merge manager object, base entity manager object,
     * and merge result choice perform merge operation. Does not perform any operations in
     * the database. The result of the opration is reflected in baseEntity. Method does not
     * return anything.
     * This method is used when merging SETs
     */
    private void mergeSet(IBaseEntity baseEntity, IBaseValue baseValueLeft, IBaseValue baseValueRight,
                          IBaseEntityMergeManager mergeManager,
                          IBaseEntityManager baseEntityManager, MergeResultChoice choice, boolean deleteUnused) {
        IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        IMetaSet childMetaSet = (IMetaSet) metaType;
        IMetaType childMetaType = childMetaSet.getMemberType();

        IBaseSet childBaseSetLeft = (IBaseSet) baseValueLeft.getValue();
        IBaseSet childBaseSetRight = (IBaseSet) baseValueRight.getValue();
        IBaseSet childBaseSetApplied = null;
        IBaseSet childBaseSetAppliedRight = null;
        IBaseSet childBaseSetAppliedLeft = null;

        if (mergeManager.getAction() != IBaseEntityMergeManager.Action.KEEP_BOTH) {
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
                IBaseValue newBaseValueRight = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueRight.getId(),
                        baseValueLeft.getBatch(),
                        baseValueLeft.getIndex(),
                        new Date(baseValueLeft.getRepDate().getTime()),
                        baseValueLeft.getValue(),
                        baseValueLeft.isClosed(),
                        baseValueLeft.isLast());

                newBaseValueRight.setBaseContainer(baseValueRight.getBaseContainer());
                newBaseValueRight.setMetaAttribute(metaAttribute);

                baseEntityManager.registerAsUpdated(newBaseValueRight);
                //delete
                if (metaType.isComplex()) {
                    baseEntityManager.registerAsDeleted((IPersistable) baseValueRight.getValue());
                }

                if (mergeManager.getChildMap() == null) {
                    if (choice == MergeResultChoice.LEFT) {
                        baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueLeft);
                    } else {
                        baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
                    }
                }

            } else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
                IBaseValue newBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        baseValueRight.getValue(),
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());

                newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
                newBaseValueLeft.setMetaAttribute(metaAttribute);

                baseEntityManager.registerAsUpdated(newBaseValueLeft);
                // delete
                if (metaType.isComplex()) {
                    baseEntityManager.registerAsDeleted((IPersistable) baseValueLeft.getValue());
                }
                if (mergeManager.getChildMap() == null) {
                    if (choice == MergeResultChoice.LEFT) {
                        baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
                    } else {
                        baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
                    }
                }

            } else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
                IBaseValue newBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        baseValueRight.getValue(),
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());

                newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
                newBaseValueLeft.setMetaAttribute(metaAttribute);

                baseEntityManager.registerAsUpdated(newBaseValueLeft);

                if (metaType.isComplex()) {
                    baseEntityManager.registerAsDeleted((IPersistable) baseValueLeft.getValue());
                }

                childBaseSetApplied = (BaseSet) baseValueRight.getValue();

                // merge two sets
                for (IBaseValue childBaseValueLeft : childBaseSetLeft.get()) {
                    boolean contains = false;
                    for (IBaseValue appliedSetValue : childBaseSetApplied.get()) {
                        if (childBaseValueLeft.equals(appliedSetValue)) {
                            contains = true;
                        }
                    }
                    // if value is not a duplicate - then put it to the resulting set
                    if (!contains) {
                        IBaseValue newChildBaseValueLeft = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                childBaseValueLeft.getId(),
                                childBaseValueLeft.getBatch(),
                                childBaseValueLeft.getIndex(),
                                new Date(childBaseValueLeft.getRepDate().getTime()),
                                childBaseValueLeft.getValue(),
                                childBaseValueLeft.isClosed(),
                                childBaseValueLeft.isLast());

                        childBaseSetApplied.put(childBaseValueLeft);
                        newChildBaseValueLeft.setBaseContainer(childBaseSetApplied);

                        baseEntityManager.registerAsUpdated(newChildBaseValueLeft);
                    }
                }

                baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
            }
        }

        // we haven't reached the base case
        if (mergeManager.getChildMap() != null) {
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
                throw new UnsupportedOperationException("Can't process sets after MERGE operation.");
            }

            Set<UUID> processedUuidsLeft = new HashSet<UUID>();
            Set<UUID> processedUuidsRight = new HashSet<UUID>();

            childBaseSetAppliedLeft = new BaseSet(childBaseSetLeft.getId(), childMetaType);
            childBaseSetAppliedRight = new BaseSet(childBaseSetRight.getId(), childMetaType);
            // we haven't reached the base case - do recursion
            for (IBaseValue childBaseValueLeft : childBaseSetLeft.get()) {
                IBaseEntity childBaseEntityLeft = (IBaseEntity) childBaseValueLeft.getValue();

                for (IBaseValue childBaseValueRight : childBaseSetRight.get()) {
                    IBaseEntity childBaseEntityRight = (IBaseEntity) childBaseValueRight.getValue();

                    MergeManagerKey idKey = new MergeManagerKey(childBaseEntityLeft.getId(),
                            childBaseEntityRight.getId());
                    if (mergeManager.containsKey(idKey) && (mergeManager.getChildManager(idKey) != null)) {
                        if (processedUuidsLeft.contains(childBaseValueLeft.getUuid()) ||
                                processedUuidsRight.contains(childBaseValueRight.getUuid())) {
                            throw new RuntimeException("Two BaseValue objects can be paired only once");
                        } else {
                            processedUuidsLeft.add(childBaseValueLeft.getUuid());
                            processedUuidsRight.add(childBaseValueRight.getUuid());
                        }

                        IBaseEntity currentEntity = mergeBaseEntity(childBaseEntityLeft, childBaseEntityRight, mergeManager.getChildManager(idKey),
                                baseEntityManager, choice, deleteUnused);

                        //if(mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH){
                        if (choice == MergeResultChoice.LEFT) {

                            IBaseValue newChildBaseValueLeft = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    childMetaType,
                                    childBaseValueLeft.getId(),
                                    childBaseValueLeft.getBatch(),
                                    childBaseValueLeft.getIndex(),
                                    new Date(childBaseValueLeft.getRepDate().getTime()),
                                    currentEntity,
                                    childBaseValueLeft.isClosed(),
                                    childBaseValueLeft.isLast());
                            childBaseSetAppliedLeft.put(newChildBaseValueLeft);

                        } else {

                            IBaseValue newChildBaseValueRight = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    childMetaType,
                                    childBaseValueRight.getId(),
                                    childBaseValueRight.getBatch(),
                                    childBaseValueRight.getIndex(),
                                    new Date(childBaseValueRight.getRepDate().getTime()),
                                    currentEntity,
                                    childBaseValueRight.isClosed(),
                                    childBaseValueRight.isLast());
                            childBaseSetAppliedRight.put(newChildBaseValueRight);
                        }
                        //}
                    }

                }

            }
            // if(mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH){
            if (choice == MergeResultChoice.LEFT) {
                IBaseValue newBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        childBaseSetAppliedLeft,
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());
                baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
            } else {
                IBaseValue newBaseValueRight = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueRight.getId(),
                        baseValueLeft.getBatch(),
                        baseValueLeft.getIndex(),
                        new Date(baseValueLeft.getRepDate().getTime()),
                        childBaseSetAppliedRight,
                        baseValueLeft.isClosed(),
                        baseValueLeft.isLast());
                baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
            }
            //}
        }
    }

    /**
     * @param baseEntity        - baseEntity resulting from the merge operation
     * @param baseValueLeft     - first base entity
     * @param baseValueRight    -  second base entity
     * @param mergeManager      -  - merge manager containing information about how the two entities
     *                          are to be merged
     * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
     *                          from the merge operation
     * @param choice            - MergeResultChoice object - determines the resulting entity
     * @param deleteUnused
     * @author dakkuliyev
     * Given right and left base values, merge manager object, base entity manager object,
     * and merge result choice perform merge operation. Does not perform any operations in
     * the database. The result of the opration is reflected in baseEntity. Method does not
     * return anything.
     * This method is used when merging Complex/Simple values
     */
    private void mergeValue(IBaseContainer baseEntity, IBaseValue baseValueLeft, IBaseValue baseValueRight,
                            IBaseEntityMergeManager mergeManager, IBaseEntityManager baseEntityManager,
                            MergeResultChoice choice, boolean deleteUnused) {
        IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        if (mergeManager.getAction() != IBaseEntityMergeManager.Action.KEEP_BOTH) {
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
                IBaseValue newBaseValueRight = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueRight.getId(),
                        baseValueLeft.getBatch(),
                        baseValueLeft.getIndex(),
                        new Date(baseValueLeft.getRepDate().getTime()),
                        baseValueLeft.getValue(),
                        baseValueLeft.isClosed(),
                        baseValueLeft.isLast());

                newBaseValueRight.setBaseContainer(baseValueRight.getBaseContainer());
                newBaseValueRight.setMetaAttribute(metaAttribute);

                if (choice == MergeResultChoice.RIGHT) {
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
                } else {
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueLeft);
                }

                baseEntityManager.registerAsUpdated(newBaseValueRight);

                if (metaType.isComplex() && !metaType.isReference()) {
                    //baseEntityManager.registerAsDeleted((IPersistable)baseValueRight.getValue());
//                    baseEntityManager.registerUnusedBaseEntity((IBaseEntity) baseValueRight.getValue());
                    if (deleteUnused) {
                        BaseEntity be = (BaseEntity) baseValueRight.getValue();

                        if (!isEntityUsedElse(be.getId(), baseValueRight.getBaseContainer().getId())) {
                            be.setOperation(OperationType.DELETE);
                            baseEntityManager.registerAsDeleted(be);
                        }
                    }
                }

            } else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
                IBaseValue newBaseValueLeft = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLeft.getId(),
                        baseValueRight.getBatch(),
                        baseValueRight.getIndex(),
                        new Date(baseValueRight.getRepDate().getTime()),
                        baseValueRight.getValue(),
                        baseValueRight.isClosed(),
                        baseValueRight.isLast());

                newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
                newBaseValueLeft.setMetaAttribute(metaAttribute);

                if (choice == MergeResultChoice.LEFT) {
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
                } else {
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
                }

                baseEntityManager.registerAsUpdated(newBaseValueLeft);
                if (metaType.isComplex() && !metaType.isReference()) {
                    //baseEntityManager.registerAsDeleted((IPersistable) baseValueLeft.getValue());
//                    baseEntityManager.registerUnusedBaseEntity((IBaseEntity) baseValueLeft.getValue());
                    if (deleteUnused) {
                        BaseEntity be = (BaseEntity) baseValueLeft.getValue();

                        if (!isEntityUsedElse(be.getId(), baseValueLeft.getBaseContainer().getId())) {
                            be.setOperation(OperationType.DELETE);
                            baseEntityManager.registerAsDeleted(be);
                        }
                    }
                }

            }
            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
                throw new RuntimeException("Invalid structure of MergeManager");
            }
        }

        if (mergeManager.getChildMap() != null) {

            // we haven't reached the base case - do recursion
            IBaseEntity baseEntityLeft = (IBaseEntity) baseValueLeft.getValue();
            IBaseEntity baseEntityRight = (IBaseEntity) baseValueRight.getValue();


            MergeManagerKey<Long> idKey = new MergeManagerKey<Long>(new Long(baseEntityLeft.getId()), new Long(baseEntityRight.getId()));
            IBaseEntityMergeManager childMergeManager = mergeManager.getChildManager(idKey);
            IBaseEntity currentApplied = null;
            if (childMergeManager == null) {
                currentApplied = mergeBaseEntity(baseEntityLeft, baseEntityRight, mergeManager, baseEntityManager, choice, deleteUnused);
            } else {
                currentApplied = mergeBaseEntity(baseEntityLeft, baseEntityRight, childMergeManager, baseEntityManager, choice, deleteUnused);
            }

            if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH) {
                if (choice == MergeResultChoice.LEFT) {
                    IBaseValue newBaseValueLeft = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLeft.getId(),
                            baseValueRight.getBatch(),
                            baseValueRight.getIndex(),
                            new Date(baseValueRight.getRepDate().getTime()),
                            currentApplied,
                            baseValueRight.isClosed(),
                            baseValueRight.isLast());
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);

                } else {
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
                    IBaseValue newBaseValueRight = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueRight.getId(),
                            baseValueLeft.getBatch(),
                            baseValueLeft.getIndex(),
                            new Date(baseValueLeft.getRepDate().getTime()),
                            currentApplied,
                            baseValueLeft.isClosed(),
                            baseValueLeft.isLast());
                    baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
                }
            }
        }
    }

    private boolean isEntityUsedElse(long entityIdToCheck, long entityIdContaining) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        boolean used = baseEntityDao.isUsed(entityIdToCheck, entityIdContaining);
        return used;
    }

    /**
     * Given two entities, merge manager. and merge result choice, perform merge
     * operation and return resulting base entity. The choice of the resulting entity
     * (right or left) depends on merge result choice. Changes are reflected in the database
     *
     * @param baseEntityLeft  - left entity
     * @param baseEntityRight - right entity
     * @param mergeManager    - merge manager containing information about how the two entities
     *                        are to be merged
     * @param choice          - MergeResultChoice object - determines the resulting entity
     * @return IBaseEntity containing the result of the merge operation. Depending on
     * choice it is either left or right entity
     */
    public IBaseEntity merge(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
                             IBaseEntityMergeManager mergeManager, MergeResultChoice choice, boolean deleteUnused) {
        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity resultingBaseEntity = mergeBaseEntity(baseEntityLeft, baseEntityRight,
                mergeManager, baseEntityManager, choice, deleteUnused);

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>RESULT ENTITY<<<<<<<<<<<<<<<<<<<<<<<<<<");
        System.out.println(resultingBaseEntity);
        applyToDb(baseEntityManager);

        return resultingBaseEntity;
    }

    @Override
    @Transactional
    public boolean remove(long baseEntityId) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.deleteRecursive(baseEntityId);
    }

    @Override
    public long getRandomBaseEntityId(IMetaClass metaClass) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClass);
    }

    @Override
    public long getRandomBaseEntityId(long metaClassId) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClassId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getChildBaseEntityIds(parentBaseEntityIds);
    }

    @Override
    public void populate(String metaName, Long scId, Date reportDate) {
        Long id = metaClassRepository.getMetaClass(metaName).getId();
        Insert ins = context.insertInto(SC_ID_BAG, SC_ID_BAG.ENTITY_ID, SC_ID_BAG.SHOWCASE_ID, SC_ID_BAG.REPORT_DATE)
                .select(context.select(EAV_BE_ENTITIES.ID, DSL.val(scId).as("SHOWCASE_ID"), EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                        .from(EAV_BE_ENTITIES.join(EAV_BE_ENTITY_REPORT_DATES).on(EAV_BE_ENTITIES.ID.eq(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID)))
                        .where(EAV_BE_ENTITIES.CLASS_ID.equal(id)
                                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.eq(new java.sql.Date(reportDate.getTime())))));
        jdbcTemplate.update(ins.getSQL(), ins.getBindValues().toArray());
    }

    @Override
    public void populateSC() {
        Long id = metaClassRepository.getMetaClass("credit").getId();
        Insert insert = context.insertInto(SC_ENTITIES, SC_ENTITIES.ENTITY_ID).select(
                context.select(EAV_BE_ENTITIES.ID)
                        .from(EAV_BE_ENTITIES)
                        .where(EAV_BE_ENTITIES.CLASS_ID.eq(id))
        );
        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void populateSC(Long creditorId) {
        MetaClass metaClass = metaClassRepository.getMetaClass("credit");
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute("creditor");

        Insert insert = context.insertInto(SC_ENTITIES, SC_ENTITIES.ENTITY_ID).select(
                context.selectDistinct(EAV_BE_COMPLEX_VALUES.ENTITY_ID)
                        .from(EAV_BE_COMPLEX_VALUES)
                        .where(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.eq(creditorId))
        );
        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public List<Long> getNewTableIds(Long id) {
        List<Long> list;
        Select select = context.select(SC_ID_BAG.ID).from(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id)).limit(10);
        Select select2 = context.select(select.field(0)).from(select);
        list = jdbcTemplate.queryForList(select2.getSQL(), Long.class, select2.getBindValues().toArray());

        return list;
    }

    @Override
    public void removeNewTableIds(List<Long> list, Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.ID.in(list).and(SC_ID_BAG.SHOWCASE_ID.eq(id)));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<Long> getSCEntityIds(Long id) {
        List<Long> list;
        Select select = context.select(SC_ID_BAG.ID).from(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id)).limit(100);
        Select select2 = context.select(select.field(0)).from(select);
        list = jdbcTemplate.queryForList(select2.getSQL(), Long.class, select2.getBindValues().toArray());

        return list;
    }

    @Override
    public List<Long[]> getSCEntityIds(int limit, Long prevMaxId) {
        Select select = context.select(SC_ENTITIES.ID, SC_ENTITIES.ENTITY_ID).from(SC_ENTITIES)
                .where(SC_ENTITIES.ID.gt(prevMaxId))
                .orderBy(SC_ENTITIES.ID).limit(limit);
        Select select2 = context.select(select.field(0), select.field(1)).from(select);
        List<Long[]> result = jdbcTemplate.query(select2.getSQL(), select2.getBindValues().toArray(), new RowMapper<Long[]>() {
            @Override
            public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long[] row = new Long[2];
                row[0] = rs.getLong(SC_ENTITIES.ID.getName());
                row[1] = rs.getLong(SC_ENTITIES.ENTITY_ID.getName());
                return row;
            }
        });
        return result;
    }

    @Override
    public List<Date> getEntityReportDates(Long entityId) {
        Select select = context.select(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(entityId))
                .orderBy(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE);
        List<Date> reportDates = jdbcTemplate.queryForList(select.getSQL(), Date.class, select.getBindValues().toArray());
        return reportDates;
    }

    @Override
    public void removeSCEntityIds(List<Long> list, Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.ID.in(list).and(SC_ID_BAG.SHOWCASE_ID.eq(id)));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public void removeSCEntityIds(List<Long> entityIds) {
        Delete delete = context.delete(SC_ENTITIES).where(SC_ENTITIES.ENTITY_ID.in(entityIds));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public void removeShowcaseId(Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<Long> getShowcaseIdsToLoad() {
        Select select = context.selectDistinct(SC_ID_BAG.SHOWCASE_ID).from(SC_ID_BAG);
        return jdbcTemplate.queryForList(select.getSQL(), Long.class);
    }

    static class EntityHolder {
        IBaseEntity saving;
        IBaseEntity loaded;
        IBaseEntity applied;
    }
}