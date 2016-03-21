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
import kz.bsbnb.usci.eav.model.exceptions.KnownIterativeException;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.logic.IRuleServicePool;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.tool.optimizer.impl.BasicOptimizer;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
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

    private static final String LOGIC_RULE_SETTING = "LOGIC_RULE_SETTING";
    private static final String LOGIC_RULE_META = "LOGIC_RULE_META";

    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    private IBaseEntityApplyDao baseEntityApplyDao;

    @Autowired
    private IRefProcessorDao refProcessorDao;

    @Autowired
    private IEavOptimizerDao eavOptimizerDao;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @Autowired
    private IRuleServicePool ruleServicePool;

    private IDaoListener applyListener;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private Set<String> metaRules;

    @Autowired
    public void setApplyListener(IDaoListener applyListener) {
        this.applyListener = applyListener;
    }

    @Value("${rules.enabled}")
    private boolean rulesEnabled;

    @Autowired
    private IEavGlobalDao globalDao;

    @Autowired
    private IRefRepository refRepository;

    @Override
    public long search(IBaseEntity baseEntity, long creditorId) {
        Long baseEntityId = searcherPool.getSearcher(baseEntity.getMeta().getClassName()).findSingle((BaseEntity) baseEntity, creditorId);
        return baseEntityId == null ? 0 : baseEntityId;
    }

    @Override
    public IBaseEntity prepare(final IBaseEntity baseEntity, long creditorId) {
        MetaClass metaClass = baseEntity.getMeta();

        final boolean isReference = metaClass.isReference();
        creditorId = isReference ? 0 : creditorId;

        if (isReference) {
            IBaseEntity referenceEntity = refRepository.findRef(baseEntity);

            if (referenceEntity != null)
                return referenceEntity;
        }

        for (String attribute : baseEntity.getAttributes()) {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            IBaseValue baseValue = baseEntity.getBaseValue(attribute);

            if (metaType.isComplex()) {
                if (baseValue.getValue() != null) {
                    if (metaType.isSet()) {
                        IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();
                        for (IBaseValue childBaseValue : childBaseSet.get()) {
                            IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                            if (childBaseEntity.getValueCount() != 0)
                                prepare((IBaseEntity) childBaseValue.getValue(), creditorId);
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();

                        if (childBaseEntity.getValueCount() != 0)
                            prepare(childBaseEntity, creditorId);
                    }
                }
            }

            if (isReference) baseValue.setCreditorId(creditorId);
        }

        if (metaClass.isSearchable()) {
            long baseEntityId;

            if (BasicOptimizer.metaList.contains(metaClass.getClassName())) {
                baseEntityId = eavOptimizerDao.find(creditorId, baseEntity.getMeta().getId(), BasicOptimizer.getKeyString(baseEntity));
            } else {
                baseEntityId = search(baseEntity, creditorId);
            }

            if (baseEntityId > 0)
                baseEntity.setId(baseEntityId);
        }

        if (isReference)
            baseEntity.getBaseEntityReportDate().setCreditorId(creditorId);

        return baseEntity;
    }

    @Override
    @Transactional
    public IBaseEntity process(final IBaseEntity baseEntity) {
        EntityHolder entityHolder = new EntityHolder();
        IBaseEntityManager baseEntityManager = new BaseEntityManager();

        IBaseEntity baseEntityPostPrepared;
        IBaseEntity baseEntityApplied;

        /* Все данные кроме справочников должны иметь кредитора */
        if (!baseEntity.getMeta().isReference() && baseEntity.getBaseEntityReportDate().getCreditorId() == 0)
            throw new IllegalStateException(Errors.getMessage(Errors.E197));

        long creditorId = baseEntity.getBaseEntityReportDate().getCreditorId();
        baseEntityManager.registerCreditorId(creditorId);

        baseEntityPostPrepared = prepare(((BaseEntity) baseEntity).clone(), creditorId);

        if (baseEntityPostPrepared.getOperation() != null) {
            switch (baseEntityPostPrepared.getOperation()) {
                case DELETE:
                    if (baseEntityPostPrepared.getId() <= 0) {
                        logger.error("Сущность для удаления не найдена; \n" + baseEntityPostPrepared);
                        throw new KnownException(Errors.getMessage(Errors.E112));
                    }


                    if (baseEntity.getMeta().isReference() && refProcessorDao.historyExists(
                            baseEntityPostPrepared.getMeta().getId(), baseEntityPostPrepared.getId())) {
                        logger.error("Справочник с историей не может быть удалена; \n " + baseEntity);
                        throw new KnownException(Errors.getMessage(Errors.E113));
                    }

                    if (baseEntity.getMeta().isReference())
                        failIfHasUsages(baseEntityPostPrepared);

                    baseEntityManager.registerAsDeleted(baseEntityPostPrepared);
                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    entityHolder.setApplied(baseEntityApplied);

                    baseEntityApplyDao.applyToDb(baseEntityManager);
                    break;
                case CLOSE:
                    if (baseEntityPostPrepared.getId() <= 0) {
                        logger.error("Сущность для закрытия не найдена; \n" + baseEntityPostPrepared);
                        throw new KnownException(Errors.getMessage(Errors.E114));
                    }


                    IBaseEntityReportDateDao baseEntityReportDateDao = persistableDaoPool.getPersistableDao(
                            BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

                    if (baseEntityReportDateDao.getMinReportDate(baseEntityPostPrepared.getId()).equals(
                            baseEntityPostPrepared.getReportDate())) {
                        logger.error("Дата закрытия не может быть одинаковой с датой открытия; \n"
                                + baseEntityPostPrepared);
                        throw new IllegalStateException(Errors.getMessage(Errors.E115));
                    }


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

                    entityHolder.setApplied(baseEntityApplied);
                    break;
                case INSERT:
                    if (baseEntityPostPrepared.getId() > 0)
                        throw new KnownException(Errors.getMessage(Errors.E196, baseEntityPostPrepared.getId()));

                    baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null,
                            baseEntityManager, entityHolder);

                    if (rulesEnabled)
                        processLogicControl(baseEntityApplied);

                    baseEntityApplyDao.applyToDb(baseEntityManager);
                    break;
                case UPDATE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new KnownException(Errors.getMessage(Errors.E198));

                    baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null,
                            baseEntityManager, entityHolder);

                    if (rulesEnabled)
                        processLogicControl(baseEntityApplied);

                    baseEntityApplyDao.applyToDb(baseEntityManager);
                    break;
                default:
                    throw new UnsupportedOperationException(Errors.getMessage(Errors.E118, baseEntityPostPrepared.getOperation()));
            }
        } else {
            baseEntityApplied = baseEntityApplyDao.apply(creditorId, baseEntityPostPrepared, null, baseEntityManager, entityHolder);

            baseEntityApplyDao.applyToDb(baseEntityManager);
        }

        if (applyListener != null)
            applyListener.applyToDBEnded(entityHolder.getSaving(), entityHolder.getLoaded(), entityHolder.getApplied(), baseEntityManager);

        return baseEntityApplied;
    }

    private void processLogicControl(IBaseEntity baseEntityApplied) {
        if (metaRules == null) {
            String[] metaArray = globalDao.getValue(LOGIC_RULE_SETTING, LOGIC_RULE_META).split(",");
            metaRules = new HashSet<>(Arrays.asList(metaArray));
        }

        if (metaRules.contains(baseEntityApplied.getMeta().getClassName())) {
            List<String> errors = ruleServicePool.getRuleService().runRules((BaseEntity) baseEntityApplied,
                    baseEntityApplied.getMeta().getClassName() + "_process", baseEntityApplied.getReportDate());

            if (errors.size() > 0) {
                throw new KnownIterativeException(errors);
            }
        }
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

                sbUsages.append(metaClass.getClassName()).append(", ");
            }

            if (rows.size() > 0) {
                throw new IllegalStateException(Errors.getMessage(Errors.E109, baseEntity.getId(), sbUsages.toString()));
            }
        }

        {
            if ("ref_creditor".equals(metaClassOfDeleting.getClassName())) {
                Select select = context.select().from(EAV_A_CREDITOR_USER)
                        .where(EAV_A_CREDITOR_USER.CREDITOR_ID.eq(baseEntity.getId())).limit(1);

                List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                        select.getBindValues().toArray());

                if (rows.size() > 0) {
                    throw new RuntimeException(Errors.getMessage(Errors.E110, baseEntity.getId()));
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

        for (Map<String, Object> row : rows) {
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
        IBaseEntityDao baseEntityDao = persistableDaoPool.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.deleteRecursive(baseEntityId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getChildBaseEntityIds(parentBaseEntityIds);
    }
}