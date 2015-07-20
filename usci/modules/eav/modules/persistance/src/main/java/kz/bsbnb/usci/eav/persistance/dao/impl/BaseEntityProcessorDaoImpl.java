package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.IRefProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityApplyDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

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

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IBaseEntityApplyDao baseEntityApplyDao;

    @Autowired
    IRefProcessorDao refProcessorDao;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    private IDaoListener applyListener;

    @Autowired
    public void setApplyListener(IDaoListener applyListener) {
        this.applyListener = applyListener;
    }

    @Override
    public long search(IBaseEntity baseEntity) {
        IMetaClass metaClass = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName()).findSingle((BaseEntity) baseEntity);
        return baseEntityId == null ? 0 : baseEntityId;
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
                            throw new UnsupportedOperationException("Не реализовано;");
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
                            throw new UnsupportedOperationException("Не реализовано;");
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


    @Override
    @Transactional
    public IBaseEntity process(final IBaseEntity baseEntity) {
        EntityHolder entityHolder = new EntityHolder();

        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity baseEntityPrepared = prepare(((BaseEntity) baseEntity).clone());
        IBaseEntity baseEntityPostPrepared = postPrepare(((BaseEntity) baseEntityPrepared).clone(), null);
        IBaseEntity baseEntityApplied;

        if (baseEntityPostPrepared.getOperation() != null) {
            switch (baseEntityPostPrepared.getOperation()) {
                case DELETE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new RuntimeException("Сущность для удаления не найдена;");

                    if (baseEntity.getMeta().isReference() && refProcessorDao.historyExists(
                            baseEntityPostPrepared.getMeta().getId(), baseEntityPostPrepared.getId()))
                        throw new RuntimeException("Справочник с историей не может быть удалена;");

                    baseEntityManager.registerAsDeleted(baseEntityPostPrepared);
                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    entityHolder.setApplied(baseEntityApplied);
                    entityHolder.setSaving(baseEntityPostPrepared);
                    break;
                case CLOSE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new RuntimeException("Сущность для закрытия не найдена;");

                    baseEntityApplied = baseEntityPostPrepared;

                    break;
                default:
                    throw new UnsupportedOperationException("Операция не поддерживается: "
                            + baseEntityPostPrepared.getOperation() + ";");
            }
        } else {
            baseEntityApplied = baseEntityApplyDao.apply(baseEntityPostPrepared, baseEntityManager, entityHolder);
            baseEntityApplyDao.applyToDb(baseEntityManager);
        }

         if (applyListener != null)
            applyListener.applyToDBEnded(entityHolder.getSaving(), entityHolder.getLoaded(),
                    entityHolder.getApplied(), baseEntityManager);

        return baseEntityApplied;
    }

    public List<Long> getEntityIDsByMetaclass(long metaClassId) {
        ArrayList<Long> entityIds = new ArrayList<>();

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

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta) {
        List<Long> ids = getEntityIDsByMetaclass(meta.getId());

        ArrayList<BaseEntity> entities = new ArrayList<>();

        for (Long id : ids)
            entities.add((BaseEntity) baseEntityLoadDao.load(id));

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

        return rows.size() > 1;
    }

    @Override
    @Transactional
    public boolean remove(long baseEntityId) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.deleteRecursive(baseEntityId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getChildBaseEntityIds(parentBaseEntityIds);
    }
}