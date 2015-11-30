package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.tool.optimizer.impl.BasicOptimizer;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IBaseEntityApplyDao baseEntityApplyDao;

    @Autowired
    IRefProcessorDao refProcessorDao;

    @Autowired
    IEavOptimizerDao eavOptimizerDao;

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

    @Autowired
    IRefRepository refRepositoryDao;

    @Value("${refs.cache.enabled}")
    private boolean isReferenceCacheEnabled;

    @Override
    public long search(IBaseEntity baseEntity, long creditorId) {
        IMetaClass metaClass = baseEntity.getMeta();

        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName()).
                findSingle((BaseEntity) baseEntity, creditorId);

        return baseEntityId == null ? 0 : baseEntityId;
    }

    @Override
    public IBaseEntity prepare(IBaseEntity baseEntity, long creditorId) {
        MetaClass metaClass = baseEntity.getMeta();

        if(isReferenceCacheEnabled && metaClass.isReference()) {
            IBaseEntity refBaseEntity = refRepositoryDao.getRef(baseEntity);

            if (refBaseEntity != null)
                return refBaseEntity;
        }

        creditorId = metaClass.isReference() ? 0 : creditorId;

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

                                if (childBaseEntity.getValueCount() != 0)
                                    prepare((IBaseEntity) childBaseValue.getValue(), creditorId);
                            }
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();

                        if (childBaseEntity.getValueCount() != 0)
                            baseValue.setValue(prepare((IBaseEntity) baseValue.getValue(), creditorId));
                    }
                }
            } else {
                IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                baseValue.setCreditorId(creditorId);
            }
        }

        if (metaClass.isSearchable()) {
            long baseEntityId;
            if (metaClass.getClassName().equals("subject")) {
                baseEntityId = eavOptimizerDao.find(new BasicOptimizer().getKeyString(baseEntity));
            } else {
                baseEntityId = search(baseEntity, creditorId);
            }

            if (baseEntityId > 0)
                baseEntity.setId(baseEntityId);
        }

        // fixme!
        if (metaClass.getClassName().equals("ref_doc_type")) {
            baseEntity = baseEntityLoadDao.load(baseEntity.getId());
        }

        return baseEntity;
    }

    @Override
    @Transactional
    public IBaseEntity process(final IBaseEntity baseEntity) {
        EntityHolder entityHolder = new EntityHolder();
        IBaseEntityManager baseEntityManager = new BaseEntityManager();

        IBaseEntity baseEntityPostPrepared;
        IBaseEntity baseEntityApplied;

        long creditorId = 0L;

        if (baseEntity.getMeta().getClassName().equals("credit")) {
            BaseEntity creditor = ((BaseEntity) baseEntity.getEl("creditor"));
            prepare(creditor, 0);
            creditorId = creditor.getId();

            if (creditorId < 1)
                throw new IllegalStateException("Кредитор не найден; \n" + creditor);
        }

        baseEntityPostPrepared = prepare(((BaseEntity) baseEntity).clone(), creditorId);

        if (baseEntityPostPrepared.getOperation() != null) {
            switch (baseEntityPostPrepared.getOperation()) {
                case DELETE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new KnownException("Сущность для удаления не найдена; \n" + baseEntityPostPrepared);

                    if (baseEntity.getMeta().isReference() && refProcessorDao.historyExists(
                            baseEntityPostPrepared.getMeta().getId(), baseEntityPostPrepared.getId()))
                        throw new KnownException("Справочник с историей не может быть удалена; \n " + baseEntity);

                    if (baseEntity.getMeta().isReference())
                        failIfHasUsages(baseEntityPostPrepared);

                    baseEntityManager.registerAsDeleted(baseEntityPostPrepared);
                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    entityHolder.setApplied(baseEntityApplied);
                    break;
                case CLOSE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new KnownException("Сущность для закрытия не найдена; \n" + baseEntityPostPrepared);

                    IBaseEntityReportDateDao baseEntityReportDateDao = persistableDaoPool.getPersistableDao(
                            BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

                    if (baseEntityReportDateDao.getMinReportDate(baseEntityPostPrepared.getId()).equals(
                            baseEntityPostPrepared.getReportDate()))
                        throw new IllegalStateException("Дата закрытия не может быть одинаковой с датой открытия; \n"
                            + baseEntityPostPrepared);


                    boolean reportDateExists = baseEntityReportDateDao.exists(baseEntityPostPrepared.getId(),
                            baseEntityPostPrepared.getReportDate());

                    IBaseEntityReportDate baseEntityReportDate;

                    if (reportDateExists) {
                        baseEntityReportDate = baseEntityReportDateDao.load(baseEntityPostPrepared.getId(),
                                baseEntityPostPrepared.getReportDate());

                        baseEntityReportDate.setBaseEntity(baseEntityPostPrepared);
                    } else {
                        baseEntityReportDate = baseEntityPostPrepared.getBaseEntityReportDate();
                        baseEntityPostPrepared.calculateValueCount(null);
                    }

                    baseEntityReportDate.setClosed(true);

                    if (reportDateExists) {
                        baseEntityManager.registerAsUpdated(baseEntityReportDate);
                    } else {
                        baseEntityManager.registerAsInserted(baseEntityReportDate);
                    }

                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    baseEntityApplyDao.applyToDb(baseEntityManager);

                    if(isReferenceCacheEnabled) {
                        if(baseEntityApplied.getMeta().isReference()){
                            if(refRepositoryDao.getRef(baseEntityApplied)!=null) {
                                refRepositoryDao.delRef(baseEntityApplied);
                            }
                        }
                    }
                    entityHolder.setApplied(baseEntityApplied);
                    break;
                case INSERT:
                    if (baseEntityPostPrepared.getId() > 0)
                        throw new KnownException("Запись была найдена в базе(" +
                                baseEntityPostPrepared.getId() + "). Вставка не произведена;");

                    baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null,
                            baseEntityManager, entityHolder);

                    baseEntityApplyDao.applyToDb(baseEntityManager);

                    if(isReferenceCacheEnabled) {
                        if(baseEntityApplied.getMeta().isReference()){
                            if(refRepositoryDao.getRef(baseEntityApplied)==null) {
                                refRepositoryDao.setRef(baseEntityApplied);
                            }
                        }
                    }
                    break;
                case UPDATE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new KnownException("Запись не была найдена в базе. " +
                                "Обновление не выполнено;");

                    baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null,
                            baseEntityManager, entityHolder);

                    baseEntityApplyDao.applyToDb(baseEntityManager);

                    if(isReferenceCacheEnabled) {
                        if(baseEntityApplied.getMeta().isReference()){
                            if(refRepositoryDao.getRef(baseEntityApplied)==null) {
                                refRepositoryDao.setRef(baseEntityApplied);
                            }
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Операция не поддерживается: "
                            + baseEntityPostPrepared.getOperation() + ";");
            }
        } else {
            baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null,
                    baseEntityManager, entityHolder);

            baseEntityApplyDao.applyToDb(baseEntityManager);

            if(isReferenceCacheEnabled) {
                if(baseEntityApplied.getMeta().isReference()){
                    if(refRepositoryDao.getRef(baseEntityApplied)==null){
                        refRepositoryDao.setRef(baseEntityApplied);
                    }
                }
            }
        }

        if (applyListener != null)
            applyListener.applyToDBEnded(entityHolder.getSaving(), entityHolder.getLoaded(),
                    entityHolder.getApplied(), baseEntityManager);

        return baseEntityApplied;
    }

    private void failIfHasUsages(IBaseEntity baseEntity) {
        MetaClass metaClassOfDeleting = metaClassRepository.getMetaClass(baseEntity.getMeta().getId());

        {
            Select select = context.selectDistinct(EAV_BE_ENTITIES.CLASS_ID).from(EAV_BE_COMPLEX_VALUES)
                    .join(EAV_BE_ENTITIES).on(EAV_BE_COMPLEX_VALUES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                    .where(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.eq(baseEntity.getId()));

            List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

            StringBuilder sbUsages = new StringBuilder();

            for (Map<String, Object> row : rows) {
                Long metaId = ((BigDecimal) row.get(EAV_BE_ENTITIES.CLASS_ID.getName())).longValue();

                MetaClass metaClass = metaClassRepository.getMetaClass(metaId);

                sbUsages.append(metaClass.getClassName() + ", ");
            }

            if (rows.size() > 0) {
                throw new IllegalStateException("Невозможно удалить сущность " + metaClassOfDeleting.getClassName()
                        + "(id: " + baseEntity.getId() + ") используется в классах: " + sbUsages.toString());
            }
        }

        {
            if ("ref_creditor".equals(metaClassOfDeleting.getClassName())) {
                Select select = context.select().from(EAV_A_CREDITOR_USER)
                        .where(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(baseEntity.getId())).limit(1);

                List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                        select.getBindValues().toArray());

                if (rows.size() > 0) {
                    throw new RuntimeException("Невозмозжно удалить кредитор у которго есть связки с пользователями "
                            + "(id: " + baseEntity.getId() + ")");
                }
            }
        }
    }

    public List<Long> getEntityIDsByMetaclass(long metaClassId) {
        ArrayList<Long> entityIds = new ArrayList<>();

        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId).
                        and(EAV_BE_ENTITIES.DELETED.eq(DataUtils.convert(false))));

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