package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory.EachAttributeBinding;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory.IEachAttribute;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory.IGetFunction;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory.IGetFunctionBinding;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.CompareFactory;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.CompareFactory.DATE;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.tool.optimizer.EavOptimizerData;
import kz.bsbnb.usci.eav.tool.optimizer.impl.BasicOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.usci.eav.persistance.dao.impl.apply.CompareFactory.DATE.DATE_LESS;

@Repository
public class BaseEntityApplyDaoImpl extends JDBCSupport implements IBaseEntityApplyDao {

    @Autowired
    IRefRepository refRepository;
    @Value("${testMode}")
    private Boolean testMode = false;
    @Autowired
    private IPersistableDaoPool persistableDaoPool;
    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;
    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;
    @Autowired
    private IEavOptimizerDao eavOptimizerDao;

    @Override
    public IBaseEntity apply(long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApply();

        block:
        {

            /* Новые сущности или сущности не имеющие ключевые атрибуты */
            if (IS.NEW(baseEntitySaving) || IS.NOT_SEARCHABLE(baseEntitySaving)) {
                IBaseEntity baseEntity = applyBaseEntityBasic(creditorId, baseEntitySaving, baseEntityManager);
                history.end();
                return baseEntity;
            }

            if (IS.NOT_EMPTY(baseEntityLoaded)) break block;

            Date reportDateSaving = baseEntitySaving.getReportDate();

            /* Получение максимальной отчетной даты из прошедших периодов */
            Date reportDate = baseEntityReportDateDao.getMaxReportDate(baseEntitySaving.getId(), reportDateSaving);

            /* Получение минимальной отчетной даты из будущих периодов */
            if (IS.EMPTY(reportDate))
                reportDate = baseEntityReportDateDao.getMinReportDate(baseEntitySaving.getId(), reportDateSaving);

            if (IS.EMPTY(reportDate))
                throw history.Error56();

            baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), reportDate, reportDateSaving);

            if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                throw history.Error57();

        }

        IBaseEntity baseEntity = applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
        history.end();
        return baseEntity;

    }

    @Override
    public IBaseEntity applyBaseEntityBasic(long creditorId, IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntitySaving, null, baseEntityManager, persistableDaoPool);

        history.beginApplyBaseEntityBasic();

        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null) {
            history.end();
            return foundProcessedBaseEntity;
        }

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMeta(), baseEntitySaving.getReportDate(), creditorId);
        if (baseEntitySaving.getAddInfo() != null)
            baseEntityApplied.setAddInfo(baseEntitySaving.getAddInfo().parentEntity, baseEntitySaving.getAddInfo().isSet,
                    baseEntitySaving.getAddInfo().attributeId);

        for (String attribute : baseEntitySaving.getAttributes()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);

            /* Пропускает закрытые теги на новые сущности <tag/> */
            if (baseValueSaving.getValue() == null)
                continue;

            applyBaseValueBasic(creditorId, baseEntityApplied, baseValueSaving, baseEntityManager);
        }

        baseEntityApplied.calculateValueCount(null);
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);
        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        history.end();
        return baseEntityApplied;

    }

    @Override
    public void applyBaseValueBasic(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntityApplied, baseValueSaving,
                null, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApplyBaseValueBasic();

        if (IS.META_ATTRIBUTE_EMPTY())
            throw history.Error60();

        if (IS.BASE_CONTAINER_NOT_EMPTY() && IS.BASE_CONTAINER_SET())
            throw history.Error59();

        IMetaSet childMetaSet = null;
        try {
            childMetaSet = (IMetaSet) baseValueSaving.getMetaAttribute().getMetaType();
        } catch (Exception e) {
        }

        if (IS.COMPLEX()) {
            if (IS.SET()) {
                IBaseSet childBaseSet = history.childFrom(baseValueSaving);
                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType(), creditorId);
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                    if (IS.IMMUTABLE() && IS.VALUE_COUNT_NOT_EMPTY(childBaseEntity) && IS.NEW(childBaseEntity))
                        throw history.ErrorIE(childBaseEntity);

                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null, baseEntityManager);

                    history
                            .applied(baseValueSaving)
                            .containerType(MetaContainerTypes.META_SET)
                            .metaType(childMetaSet.getMemberType())
                            .id(0L)
                            .value(childBaseEntityApplied)
                            .closed(false)
                            .last(true)

                            .parent(false)
                            .attribute(childBaseSetApplied)

                            .inserted().result();

                }

                history.base(childBaseSetApplied).inserted().execute();

                history.applied(baseValueSaving)
                        .id(0L)
                        .value(childBaseSetApplied)
                        .closed(false)
                        .last(true)

                        .parent(false)

                        .inserted().execute();

            } else {
                if (IS.IMMUTABLE()) {
                    IBaseEntity childBaseEntity = history.childEntityFrom(baseValueSaving);

                    if (IS.VALUE_COUNT_NOT_EMPTY(childBaseEntity)) {
                        if (IS.NEW(childBaseEntity))
                            throw history.ErrorIE(childBaseEntity);

                        IBaseEntity childBaseEntityImmutable;

                        if (IS.REFERENCE()) {
                            childBaseEntityImmutable = refRepository.get(childBaseEntity);
                        } else {
                            childBaseEntityImmutable = baseEntityLoadDao.loadByMaxReportDate(
                                    childBaseEntity.getId(), childBaseEntity.getReportDate());
                        }

                        if (IS.EMPTY(childBaseEntityImmutable))
                            throw history.Error63(childBaseEntity);

                        if (childBaseEntityImmutable.getBaseEntityReportDate().isClosed())
                            throw history.ErrorCEFI(childBaseEntityImmutable);

                        history.applied(baseValueSaving)
                                .id(0L)
                                .value(childBaseEntityImmutable)
                                .closed(false)
                                .last(true)

                                .parent(false)

                                .inserted().execute();

                    } else {
                        throw history.Error64(childBaseEntity);
                    }
                } else {
                    IBaseEntity childBaseEntity = history.childEntityFrom(baseValueSaving);
                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null, baseEntityManager);

                    history.applied(baseValueSaving)
                            .id(0L)
                            .value(childBaseEntityApplied)
                            .closed(false)
                            .last(true)

                            .parent(false)

                            .inserted().execute();

                }
            }
        } else {
            if (IS.SET()) {
                IBaseSet childBaseSet = history.childFrom(baseValueSaving);
                IMetaValue metaValue = (IMetaValue) childMetaSet.getMemberType();
                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType(), creditorId);
                for (IBaseValue childBaseValue : childBaseSet.get()) {

                    history.applied(baseValueSaving)
                            .containerType(MetaContainerTypes.META_SET)
                            .metaType(childMetaSet.getMemberType())
                            .castedValue(metaValue, childBaseValue)
                            .id(0L)
                            .closed(false)
                            .last(true)

                            .parent(false)
                            .attribute(childBaseSetApplied)

                            .inserted().execute();

                }

                history.base(childBaseSetApplied).inserted().execute();

                history.applied(baseValueSaving).id(0L).value(childBaseSetApplied).closed(false).last(true).parent(false).inserted().execute();
            } else {
                history.applied(baseValueSaving).id(0L).closed(false).last(true).parent(false).inserted().execute();
            }
        }

        history.end();

    }

    @Override
    public IBaseEntity applyBaseEntityAdvanced(long creditorId, IBaseEntity baseEntitySaving,
                                               IBaseEntity baseEntityLoaded, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager, persistableDaoPool);

        CompareFactory IS = history.getCompareFactory();

        history.beginApplyBaseEntityAdvanced();

        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (IS.NOT_EMPTY(foundProcessedBaseEntity)) {
            history.end();
            return foundProcessedBaseEntity;
        }

        IMetaClass metaClass = baseEntitySaving.getMeta();

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded, baseEntitySaving.getReportDate());
        baseEntityApplied.setUserId(baseEntitySaving.getUserId());
        baseEntityApplied.setBatchId(baseEntitySaving.getBatchId());

        // Устанавливает ID для !metaClass.isSearchable()
        if (IS.NEW(baseEntitySaving) && IS.NOT_NEW(baseEntityLoaded))
            baseEntitySaving.setId(baseEntityLoaded.getId());


        if (baseEntityReportDateDao.exists(baseEntitySaving.getId(), baseEntitySaving.getReportDate())) {
            baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), baseEntitySaving.getReportDate(), baseEntitySaving.getReportDate());
            baseEntityLoaded.setUserId(baseEntitySaving.getUserId());
            baseEntityLoaded.setBatchId(baseEntitySaving.getBatchId());
        }

        for (String attrName : metaClass.getAttributeNames()) {

            history.initFromChild(attrName);

            IS = history.createCompareFactory(attrName);

            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attrName);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attrName);

            if (IS.EMPTY(baseValueSaving) && IS.NOT_EMPTY(baseValueLoaded) && IS.NOT_NULLABLE())
                baseEntityApplied.put(attrName, baseValueLoaded);

            if (IS.EMPTY(baseValueSaving) && IS.NULLABLE()) {
                baseValueSaving = history.applied(null)
                        .containerType(baseEntitySaving.getBaseContainerType().getValue()).id(0L).dateFrom(baseEntitySaving).value(null).closed(false).last(true).parentFrom(baseEntitySaving).attribute(false).result();
            }

            if (IS.EMPTY(baseValueSaving))
                continue;

            if (IS.COMPLEX()) {
                if (IS.SET())
                    applyComplexSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applyComplexValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            } else {
                if (IS.SET())
                    applySimpleSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applySimpleValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            }
        }

        switch (IS.COMPARE_DATE(baseEntitySaving, baseEntityLoaded)) {
            case DATE_MORE:
            case DATE_EQUAL:
                baseEntityApplied.calculateValueCount(baseEntityLoaded);
                break;
            case DATE_LESS:
                baseEntityApplied.calculateValueCount(null);
        }
        if (baseEntitySaving.getAddInfo() != null)
            baseEntityApplied.setAddInfo(baseEntitySaving.getAddInfo().parentEntity, baseEntitySaving.getAddInfo().isSet,
                    baseEntitySaving.getAddInfo().attributeId);

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();

        if (baseEntityReportDateDao.exists(baseEntityApplied.getId(), baseEntityApplied.getReportDate())) {
            baseEntityManager.registerAsUpdated(baseEntityReportDate);
        } else {
            baseEntityManager.registerAsInserted(baseEntityReportDate);
        }

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        history.end();
        return baseEntityApplied;

    }

    @Override
    public void applySimpleValue(final long creditorId, final IBaseEntity baseEntityApplied, final IBaseValue baseValueSaving,
                                 final IBaseValue baseValueLoaded, final IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntityApplied, baseValueSaving,
                baseValueLoaded, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApplySimpleValue();

        history.persistable("baseValueSaving", baseValueSaving);
        history.persistable("baseValueLoaded", baseValueLoaded);
        history.persistable("baseEntityApplied", baseEntityApplied);

        if (IS.NOT_EMPTY(baseValueLoaded)) {
            if (IS.VALUE_EMPTY(baseValueSaving)) {
                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                        if (IS.FINAL()) {
                            history.applied(baseValueLoaded).deleted().attribute(false).execute();
                            if (IS.LAST(baseValueLoaded)) {
                                history.previous(baseValueLoaded).last(true).updated().execute();
                            }
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                            if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                                history.next().deleted().execute();
                            }
                        } else {
                            history.applied(baseValueLoaded).closed(true).attribute(false).deleted().execute();
                            if (IS.LAST(baseValueLoaded)) {
                                history.previous(baseValueLoaded).last(true).updated().execute();
                            }
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                            if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                                history.next().deleted().execute();
                            }
                        }
                        break;
                    case DATE_MORE:
                        if (IS.FINAL())
                            throw history.Error66();
                        if (IS.LAST(baseValueLoaded)) {
                            history.applied(baseValueLoaded).last(false).attribute(false).updated().execute();
                        }
                        IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                        if (IS.NOT_EMPTY(baseValueNext))
                            if (IS.CLOSED(baseValueNext)) {
                                history.next().dateFrom(baseValueSaving).updated().execute();
                            } else {
                                if (StaticRouter.exceptionOnForbiddenCloseE299())
                                    throw history.Error299(baseValueNext);
                            }
                        else {
                            history.applied(baseValueLoaded, true, true).id(0L).attribute(false).inserted().execute();
                        }
                        break;
                    case DATE_LESS:
                        // throw history.Error75();
                        break;
                }
                history.end();
                return;
            }

            if (IS.VALUE_EQUALS(baseValueSaving, baseValueLoaded)) {

                // Именение ключевых полей
                // <tag operation="new" data="new_value">old_value</tag>
                Object baseValue;
                if (IS.NEW_VALUE_NOT_EMPTY(baseValueSaving)) {
                    baseValue = history.newValue(baseValueSaving);
                    history.base().optimizer().execute();
                } else {
                    baseValue = history.castedValue(baseValueLoaded);
                }

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                    case DATE_MORE:
                        history.applied(baseValueLoaded).value(baseValue).execute();
                        // Запуск на изменение ключевого поля
                        if (IS.NEW_VALUE_NOT_EMPTY(baseValueSaving))
                            history.from().updated().execute();
                        break;
                    case DATE_LESS:
                        history.applied(baseValueLoaded).value(baseValue).dateFrom(baseValueSaving).updated().execute();
                }

            } else {
                history.rep_dates("baseValueSaving", baseValueSaving, "baseValueLoaded", baseValueLoaded);
                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                        history.event("RepDate of baseValueSaving EQUAL to baseValueLoaded");
                        history.applied(baseValueLoaded).castedValue(baseValueSaving).updated().execute();
                        break;
                    case DATE_MORE:
                        history.event("RepDate of baseValueSaving MORE when baseValueLoaded");
                        if (IS.FINAL())
                            throw history.Error69();

                        history.applied(baseValueSaving).id(0L).closedFrom(baseValueLoaded).lastFrom(baseValueLoaded).inserted().execute();

                        if (IS.LAST(baseValueLoaded)) {
                            history.applied(baseValueLoaded).last(false).updated().execute();
                        }
                        break;
                    case DATE_LESS:
                        history.event("RepDate of baseValueSaving LESS when baseValueLoaded");
                        history.applied(baseValueSaving).id(0L).closed(false).last(false).inserted().execute();
                }
            }
        } else {
            if (IS.VALUE_EMPTY(baseValueSaving)) {
                history.end();
                return;
                // throw history.ErrorUO();
            }

            if (IS.NOT_FINAL()) {
                IBaseValue baseValueClosed = history.closed(baseValueSaving).parent(true).result();
                if (IS.NOT_EMPTY(baseValueClosed)) {
                    if (IS.VALUE_EQUALS(baseValueClosed, baseValueSaving)) {
                        baseValueClosed = history.closed().deleted().result();

                        IBaseValue baseValuePrevious = history.previous(baseValueClosed).result();

                        if (IS.EMPTY(baseValuePrevious))
                            throw history.Error70(baseValueClosed);

                        history.previous().last(true).parent(true).updated().execute();
                    } else {
                        history.closed().castedValue(baseValueSaving).closed(false).updated().execute();
                    }
                } else {
                    IBaseValue baseValueNext = history.closed(baseValueSaving).result();

                    if (IS.NOT_EMPTY(baseValueNext)) {
                        history.applied(baseValueSaving).idFrom(baseValueNext).closedFrom(baseValueNext).lastFrom(baseValueNext).parent(false).updated().execute();
                    } else {
                        history.applied(baseValueSaving).id(0L).closed(false).last(true).parent(false).inserted().execute();
                    }
                }
            } else {
                IBaseValue baseValueLast = history.closed(baseValueSaving).result();

                if (IS.EMPTY(baseValueLast)) {
                    history.applied(baseValueSaving).id(0L).closed(false).last(true).parent(false).inserted().execute();
                } else {
                    boolean last = false;
                    switch (IS.COMPARE_DATE(baseValueSaving, baseValueLast)) {
                        case DATE_EQUAL:
                        case DATE_MORE:
                            history.closed().last(true).parent(true).updated().execute();
                            last = true;
                    }

                    history.applied(baseValueSaving).id(0L).closed(false).last(last).parent(false).inserted().execute();
                }
            }
        }
        history.end();
    }

    @Override
    public void applyComplexValue(final long creditorId, final IBaseEntity baseEntity,
                                  final IBaseValue baseValueSaving,
                                  final IBaseValue baseValueLoaded, final IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntity, baseValueSaving,
                baseValueLoaded, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApplyComplexValue();

        IGetFunction getEntityApplied = new IGetFunction() {
            @Override
            public IBaseEntity execute(IGetFunctionBinding binding, ApplyHistoryFactory history, CompareFactory IS) {
                IBaseEntity baseEntityApplied = null;
                if (IS.IMMUTABLE()) {
                    if (IS.REFERENCE()) {
                        baseEntityApplied = refRepository.get(binding.baseEntitySaving);
                    } else {
                        baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(binding.baseEntitySaving.getId(), binding.baseEntitySaving.getReportDate());
                    }
                    if (baseEntityApplied.getBaseEntityReportDate().isClosed())
                        throw history.ErrorCEFI(baseEntityApplied);
                }
                return baseEntityApplied;
            }
        };

        IGetFunction getEntityAppliedAdvanced = new IGetFunction() {
            @Override
            public IBaseEntity execute(IGetFunctionBinding binding, ApplyHistoryFactory history, CompareFactory IS) {
                return applyBaseEntityAdvanced(creditorId, binding.baseEntitySaving, binding.baseEntityLoaded, baseEntityManager);
            }
        };

        if (IS.NOT_EMPTY(baseValueLoaded)) {
            if (IS.VALUE_EMPTY(baseValueSaving)) {

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                        if (IS.FINAL()) {

                            history.applied(baseValueLoaded).attribute(false).deleted().execute();

                            if (IS.LAST(baseValueLoaded)) {
                                IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();

                                if (IS.NOT_EMPTY(baseValuePrevious)) {
                                    history.previous().last(true).parent(true).updated().execute();
                                }
                            }

                            if (IS.NOT_SEARCHABLE() && IS.NOT_IMMUTABLE()) {

                                history.eachAttribute(new IEachAttribute() {
                                    public void execute(EachAttributeBinding binding, ApplyHistoryFactory history, CompareFactory IS) {
                                        binding.baseEntitySaving.put(
                                                binding.attributeName,
                                                history.applied(baseValueSaving).metaType(binding.childMetaType).id(0L).value(null).closed(false).last(true).result()
                                        );
                                    }
                                });

                                history.get(getEntityAppliedAdvanced);

                                //TODO: E92 refactor
                                //if (!baseEntityLoaded.getMeta().isSearchable())
                                //    baseEntityManager.registerAsDeleted(baseEntityLoaded);
                            }

                            // delete next closed value
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                            if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                                history.next().parent(true).deleted().execute();
                            }

                            history.end();
                            return;
                        } else {

                            history.applied(baseValueLoaded).deleted().execute();

                            if (IS.LAST(baseValueLoaded)) {
                                IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();
                                if (IS.NOT_EMPTY(baseValuePrevious)) {
                                    history.previous().last(true).parent(true).updated().execute();
                                }
                            }

                            if (IS.NOT_SEARCHABLE() && IS.NOT_IMMUTABLE()) {

                                history.eachAttribute(new IEachAttribute() {
                                    public void execute(EachAttributeBinding binding, ApplyHistoryFactory history, CompareFactory IS) {
                                        binding.baseEntitySaving.put(
                                                binding.attributeName,
                                                history.applied(baseValueSaving).metaType(binding.childMetaType).id(0L).value(null).closed(false).last(true).result()
                                        );
                                    }
                                });

                                history.get(getEntityAppliedAdvanced);

                                //TODO: E92 refactor
                                //if (!baseEntityLoaded.getMeta().isSearchable())
                                //    baseEntityManager.registerAsDeleted(baseEntityLoaded);
                            }

                            // delete next closed value
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                            if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                                history.previous().parent(true).deleted().execute();
                            }
                        }
                        break;
                    case DATE_MORE:
                        if (IS.FINAL())
                            throw history.Error66();

                        if (IS.LAST(baseValueLoaded)) {
                            history.last(baseValueLoaded).last(false).updated().execute();
                        }

                        IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                        // check for next closed value
                        if (IS.NOT_EMPTY(baseValueNext))
                            if (IS.CLOSED(baseValueNext)) {
                                history.next().dateFrom(baseValueSaving).parent(true).updated().execute();
                            } else {
                                if (StaticRouter.exceptionOnForbiddenCloseE299())
                                    throw history.Error299(baseValueNext);
                            }
                        else {
                            history.applied(baseValueLoaded).id(0L).dateFrom(baseValueSaving).closed(true).last(true).inserted().execute();
                        }

                        break;
                    case DATE_LESS:
                        // throw history.ErrorUO();
                }

                history.end();
                return;
            }

            IBaseEntity baseEntitySaving = history.childEntityFrom(baseValueSaving);
            IBaseEntity baseEntityLoaded = history.childEntityFrom(baseValueLoaded);

            if (IS.IMMUTABLE() && IS.NEW(baseEntitySaving))
                throw history.ErrorIE(baseEntitySaving);

            if (IS.ID_EQUALS(baseEntitySaving, baseEntityLoaded) || IS.NOT_SEARCHABLE()) {

                IBaseEntity baseEntityApplied;
                if (IS.IMMUTABLE()) {
                    baseEntityApplied = history.get(getEntityApplied, baseEntityLoaded, baseEntitySaving);
                } else {
                    baseEntityApplied = IS.SEARCHABLE() ?
                            apply(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager) :
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                history.applied(baseValueLoaded).value(baseEntityApplied);
                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_LESS:
                        history.applied().dateFrom(baseValueSaving).updated();
                }
                history.applied().execute();

            } else {

                IBaseEntity baseEntityApplied;
                if (IS.IMMUTABLE()) {
                    baseEntityApplied = history.get(getEntityApplied, baseEntityLoaded, baseEntitySaving);
                } else {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);
                }

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {

                    case DATE_EQUAL:
                        history.applied(baseValueLoaded).value(baseEntityApplied).updated().execute();
                        break;

                    case DATE_MORE:
                        if (IS.FINAL())
                            throw history.Error69();

                        history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).inserted().execute();

                        if (IS.LAST(baseValueLoaded)) {
                            history.previous(baseValueLoaded).last(false).parent(true).updated().execute();
                        }
                        break;

                    case DATE_LESS:

                        if (IS.FINAL())
                            throw history.Error69();

                        history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(false).parent(false).inserted().execute();
                }

            }
        } else {
            IBaseEntity baseEntitySaving = history.childEntityFrom(baseValueSaving);
            if (IS.EMPTY(baseEntitySaving)) {
                history.end();
                return;
                // throw history.ErrorUO();
            }

            if (IS.IMMUTABLE() && IS.NEW(baseEntitySaving))
                throw history.Error71();

            if (IS.NOT_FINAL()) {
                IBaseValue baseValueClosed = history.closed(baseValueSaving).parent(true).result();

                if (IS.NOT_EMPTY(baseValueClosed)) {

                    IBaseEntity baseEntityClosed = history.childEntityFrom(baseValueClosed);

                    if (IS.VALUE_EQUALS(baseValueClosed, baseValueSaving)) {
                        history.closed().deleted().execute();

                        IBaseValue baseValuePrevious = history.previous(baseValueClosed).parent(true).result();

                        if (IS.EMPTY(baseValuePrevious))
                            throw history.Error70(baseValueClosed);

                        IBaseEntity baseEntityApplied;
                        if (IS.IMMUTABLE()) {
                            baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        } else {
                            baseEntityApplied = IS.SEARCHABLE() ?
                                    apply(creditorId, baseEntitySaving, null, baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager);
                        }

                        history.previous().value(baseEntityApplied);

                        if (IS.LAST(baseValueClosed)) {
                            history.previous().last(true).attribute(true).updated().execute();
                        } else {
                            history.previous().attribute(true).execute();
                        }
                    } else {
                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (IS.NOT_IMMUTABLE()) {
                            baseEntityApplied = IS.SEARCHABLE() ?
                                    apply(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager);
                        }

                        history.closed().value(baseEntityApplied).closed(false).attribute(true).updated().execute();
                    }
                } else {
                    IBaseValue<IBaseEntity> baseValueNext = history.next(baseValueSaving).parent(true).result();

                    if (IS.NOT_EMPTY(baseValueNext)) {
                        IBaseEntity baseEntityApplied;
                        if (IS.IMMUTABLE()) {
                            baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        } else {
                            baseEntityApplied = IS.SEARCHABLE() ?
                                    apply(creditorId, baseEntitySaving, baseValueNext.getValue(), baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseValueNext.getValue(), baseEntityManager);
                        }

                        history.next().dateFrom(baseValueSaving).value(baseEntityApplied).attribute(true).updated().execute();

                    } else {
                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (IS.NOT_IMMUTABLE()) {
                            baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);
                        }

                        history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).inserted().execute();
                    }
                }
            } else {
                IBaseValue baseValueLast = history.last(baseValueSaving).result();

                IBaseEntity baseEntityApplied;
                if (IS.IMMUTABLE()) {
                    baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                } else {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);

                    IBaseValue previousBaseValue = history.previous(baseValueSaving).result();

                    if (IS.NOT_EMPTY(previousBaseValue) && IS.LAST(previousBaseValue)) {
                        history.applied(previousBaseValue).last(false).attribute(true).updated().execute();
                    }
                }

                if (IS.EMPTY(baseValueLast)) {
                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).attribute(true).inserted().execute();
                } else {
                    boolean last = false;
                    switch (IS.COMPARE_DATE(baseValueSaving, baseValueLast)) {
                        case DATE_EQUAL:
                        case DATE_MORE:
                            history.last().last(false).parent(true).updated().execute();
                            last = true;
                    }

                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(last).attribute(true).inserted().execute();
                }
            }
        }
        history.end();
    }

    @Override
    public void applySimpleSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                               IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntity, baseValueSaving,
                baseValueLoaded, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApplySimpleSet();

        IBaseSet childBaseSetSaving = history.childFrom(baseValueSaving);
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;

        Boolean isBaseSetDeleted = false;

        if (IS.FINAL())
            throw history.Error02();

        if (IS.NOT_EMPTY(baseValueLoaded)) {
            childBaseSetLoaded = history.childFrom(baseValueLoaded);

            if (IS.EMPTY(childBaseSetSaving) || IS.VALUE_COUNT_EMPTY(childBaseSetSaving)) {
                childBaseSetApplied = history.childNew(childBaseSetLoaded);

                IBaseValue baseValueNext;

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {

                    case DATE_EQUAL: /* case#1 */
                        history.applied(baseValueLoaded).value(childBaseSetLoaded).deleted().execute();

                        if (IS.LAST(baseValueLoaded)) {
                            history.previous(baseValueLoaded).last(true).parent(true).updated().execute();
                        }

                        // delete next closed value
                        baseValueNext = history.next(baseValueLoaded).parent(true).result();
                        if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                            history.next().deleted().execute();
                        }

                        isBaseSetDeleted = true;
                        break;

                    case DATE_MORE: /* case#2 */
                        baseValueNext = history.next(baseValueLoaded).parent(true).result();

                        // check for next closed value
                        if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                            history.next().dateFrom(baseValueSaving).updated().execute();
                        } else {
                            history.applied(baseValueSaving).id(0L).value(childBaseSetLoaded).closed(true).last(true).attribute(false).inserted().execute();

                            if (IS.LAST(baseValueLoaded)) {
                                history.applied(baseValueLoaded).value(childBaseSetLoaded).last(false).attribute(false).updated().execute();
                            }
                        }

                        isBaseSetDeleted = true; // todo: check for cumulative arrays
                        break;

                    case DATE_LESS:
                        throw history.Error72();

                }

            } else {

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                    case DATE_MORE:
                        /* case#3 */
                        childBaseSetApplied = history.childNew(childBaseSetLoaded);

                        history.applied(baseValueLoaded).value(childBaseSetApplied).parent(false).execute();
                        break;
                    case DATE_LESS: /* case#4 */
                        childBaseSetApplied = history.childNew(childBaseSetLoaded);
                        history.applied(baseValueLoaded).dateFrom(baseValueSaving).value(childBaseSetApplied).parent(false).updated().execute();
                }

            }
        } else {
            if (IS.EMPTY(childBaseSetSaving)) {
                history.end();
                return;
                // throw history.ErrorUO();
            }

            IBaseValue baseValueClosed = history.closed(baseValueSaving).result();

            // case#5
            if (IS.NOT_EMPTY(baseValueClosed)) {
                history.closed().parentFrom(baseValueSaving).execute();

                history.applied(baseValueClosed).value(null).parent(false).attribute(false).deleted().execute();

                IBaseValue baseValuePrevious = history.previous(baseValueClosed).result();

                if (IS.EMPTY(baseValuePrevious))
                    throw history.Error70(baseValueClosed);

                baseValuePrevious = history.previous().parentFrom(baseValueSaving).result();

                childBaseSetLoaded = history.childFrom(baseValueClosed);
                childBaseSetApplied = history.childNew(childBaseSetLoaded);

                history.applied(baseValuePrevious).value(childBaseSetApplied).closed(false).last(true).attribute(true).updated().execute();
                // case#6
            } else {
                IBaseValue baseValueNext = history.next(baseValueSaving).result();

                if (IS.NOT_EMPTY(baseValueNext)) {
                    childBaseSetLoaded = history.childFrom(baseValueNext);
                    childBaseSetApplied = history.childNew(childBaseSetLoaded);

                    history.applied(baseValueNext).dateFrom(baseValueSaving).value(childBaseSetApplied).closed(false).parent(false).updated().execute();
                } else {
                    IBaseValue baseValueExisting = history.existing(baseValueSaving).result();

                    if (IS.NOT_EMPTY(baseValueExisting)) {
                        childBaseSetLoaded = history.childFrom(baseValueExisting);
                        childBaseSetApplied = history.childNew(childBaseSetLoaded);

                        history.applied(baseValueExisting).dateFrom(baseValueSaving).value(childBaseSetApplied).closed(false).lastFrom(baseValueNext).parent(false).updated().execute();

                    } else {
                        childBaseSetApplied = history.childNew();
                        history.base(childBaseSetApplied).inserted().execute();

                        history.applied(baseValueSaving).id(0L).value(childBaseSetApplied).closed(false).last(true).parent(false).inserted().execute();
                    }
                }
            }
        }

        if (IS.NOT_EMPTY(childBaseSetSaving) && IS.VALUE_COUNT_NOT_EMPTY(childBaseSetSaving)) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {

                history.withValueDao(childBaseValueSaving, IBaseSetValueDao.class);

                if (IS.NOT_EMPTY(childBaseSetLoaded)) {
                    baseValueFound = false;

                    for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                        if (history.contains(childBaseValueLoaded))
                            continue;

                        if (IS.VALUE_EQUALS_BY_CHILD_META(childBaseValueSaving, childBaseValueLoaded)) {
                            history.processed(childBaseValueLoaded);
                            baseValueFound = true;

                            switch (IS.COMPARE_DATE(childBaseValueSaving, childBaseValueLoaded)) {
                                case DATE_LESS:
                                    history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).dateFrom(childBaseValueSaving).parent(false).attribute(false).updated();
                                    break;
                                default:
                                    history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).parent(false).attribute(false);
                            }
                            history.applied().attribute(childBaseSetApplied).execute();

                            break;
                        }
                    }

                    if (baseValueFound)
                        continue;
                }

                // Check closed value
                IBaseValue baseValueForSearch = history.applied(childBaseValueSaving).containerType(MetaContainerTypes.META_SET).child(true).id(0L).containerValue(childBaseSetApplied).parent(false).attribute(false).result();

                IBaseValue childBaseValueClosed = history.closed(baseValueForSearch).result();

                if (IS.NOT_EMPTY(childBaseValueClosed)) {
                    history.closed().containerValue(childBaseSetApplied).deleted().execute();

                    IBaseValue childBaseValuePrevious = history.previous(childBaseValueClosed).result();
                    if (IS.NOT_EMPTY(childBaseValuePrevious)) {
                        if (childBaseValueClosed.isLast()) {
                            history.previous().attribute(childBaseSetApplied).last(true).updated().execute();
                        } else {
                            history.previous().attribute(childBaseSetApplied).execute();
                        }
                    } else {
                        throw history.Error73();
                    }

                    continue;
                }

                // Check next value
                IBaseValue childBaseValueNext = history.next(childBaseValueSaving).result();
                if (IS.NOT_EMPTY(childBaseValueNext)) {
                    history.next().dateFrom(childBaseValueSaving).attribute(childBaseSetApplied).updated().execute();
                    continue;
                }

                IBaseValue childBaseValueLast = history.last(childBaseValueSaving).result();
                if (IS.NOT_EMPTY(childBaseValueLast)) {
                    switch (IS.COMPARE_DATE(childBaseValueSaving, childBaseValueLast)) {
                        case DATE_LESS:
                            history.applied(childBaseValueSaving).containerType(MetaContainerTypes.META_SET).child(true).id(0L).closed(false).last(false).attribute(childBaseSetApplied).parent(false).attribute(false).inserted().execute();
                            break;
                        default:
                            throw history.Error74();
                    }

                    continue;
                }

                history.applied(childBaseValueSaving).containerType(MetaContainerTypes.META_SET).child(true).id(0L).closed(false).last(true).attribute(childBaseSetApplied).parent(false).attribute(false).inserted().execute();
            }
            history.noWith();
        }

        /* Удаляет элементы массива, если массив не накопительный или массив накопительный и родитель был удалён */
        if (IS.NOT_EMPTY(childBaseSetLoaded) &&
                ((IS.CUMULATIVE() && isBaseSetDeleted) || IS.NOT_CUMULATIVE())) {
            LOOP:
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (history.contains(childBaseValueLoaded))
                    continue;

                history.withValueDao(childBaseValueLoaded, IBaseSetValueDao.class);

                switch (IS.COMPARE_DATE(baseValueSaving, childBaseValueLoaded)) {

                    case DATE_LESS:
                        continue LOOP;

                    case DATE_EQUAL:
                        history.base(childBaseValueLoaded).deleted().execute();

                        if (IS.LAST(childBaseValueLoaded)) {
                            IBaseValue childBaseValuePrevious = history.previous(childBaseValueLoaded).result();

                            if (IS.NOT_EMPTY(childBaseValuePrevious)) {
                                history.previous().containerValue(childBaseSetApplied).last(true).updated().execute();
                            }
                        }
                        break;

                    case DATE_MORE:
                        history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).id(0L).dateFrom(baseValueSaving).closed(true).containerValue(childBaseSetApplied).parent(false).attribute(false).inserted().execute();
                        if (IS.LAST(childBaseValueLoaded)) {
                            history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).last(false).containerValue(childBaseSetApplied).parent(false).attribute(false).updated().execute();
                        }
                        break;

                }
            }
            history.noWith();
        }
        history.end();
    }

    @Override
    public void applyComplexSet(final long creditorId, final IBaseEntity baseEntity,
                                final IBaseValue baseValueSaving,
                                final IBaseValue baseValueLoaded, final IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, creditorId, baseEntity, baseValueSaving,
                baseValueLoaded, baseEntityManager, persistableDaoPool);

        final CompareFactory IS = history.getCompareFactory();

        history.beginApplyComplexSet();

        IBaseSet childBaseSetSaving = history.childFrom(baseValueSaving);
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;

        boolean isBaseSetDeleted = false;

        if (IS.NOT_EMPTY(baseValueLoaded)) {
            childBaseSetLoaded = history.childFrom(baseValueLoaded);

            if (IS.EMPTY(childBaseSetSaving) || IS.VALUE_COUNT_EMPTY(childBaseSetSaving)) {
                childBaseSetApplied = history.childNew(childBaseSetLoaded);

                switch (IS.COMPARE_DATE(baseValueSaving, baseValueLoaded)) {
                    case DATE_EQUAL:
                        /* case#1 */
                        if (IS.FINAL()) {
                            history.applied(baseValueLoaded).creditorIdFrom(baseValueLoaded).attribute(false).deleted().execute();

                            if (IS.LAST(baseValueLoaded)) {

                                IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();

                                if (IS.NOT_EMPTY(baseValuePrevious)) {
                                    history.previous().last(true).parent(true).updated().execute();
                                }

                            }

                            // delete next closed value
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                            if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                                history.next().parent(true).deleted().execute();
                            }

                            isBaseSetDeleted = true;

                        }
                        /* case#2 */
                        if (IS.NOT_FINAL()) {
                            history.applied(baseValueLoaded).creditorIdFrom(baseValueLoaded).value(childBaseSetLoaded).closed(true).attribute(false).deleted().execute();

                            if (IS.LAST(baseValueLoaded)) {

                                IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();

                                if (IS.NOT_EMPTY(baseValuePrevious)) {
                                    history.previous().parent(true).last(true).updated();
                                }
                            }

                            // delete next closed value
                            IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                            if (IS.NOT_EMPTY(baseValueNext) && IS.LAST(baseValueNext)) {
                                history.next().parent(true).deleted().execute();
                            }

                            isBaseSetDeleted = true;
                        }
                        break;
                    case DATE_MORE:
                        /* case#3 */
                        if (IS.FINAL())
                            throw history.Error66();

                        IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                        // check for next closed value
                        if (IS.NOT_EMPTY(baseValueNext) && IS.CLOSED(baseValueNext)) {
                            history.next().parent(true).dateFrom(baseValueSaving).updated().execute();
                        } else {
                            history.applied(baseValueSaving).creditorIdFrom(baseValueSaving).id(0L).value(childBaseSetLoaded).closed(true).attribute(false).inserted().execute();

                            if (IS.LAST(baseValueLoaded)) {
                                history.applied(baseValueLoaded).creditorIdFrom(baseValueLoaded).value(childBaseSetLoaded).last(false).attribute(false).updated().execute();
                            }
                        }

                        isBaseSetDeleted = true;
                        break;
                    case DATE_LESS:
                        // throw history.ErrorUO();

                }

                // case#4
            } else {
                history.rep_dates("baseValueSaving", baseValueSaving, "baseValueLoaded", baseValueLoaded);
                DATE compare = IS.COMPARE_DATE(baseValueSaving, baseValueLoaded);

                if (IS.NOT_EQUAL(compare) && IS.FINAL()) {
                    history.event("RepDate of baseValueSaving EQUAL to baseValueLoaded");
                    throw history.Error67();
                }

                if (IS.EQUAL_MORE(compare)) {
                    history.event("RepDate of baseValueSaving MORE when baseValueLoaded");
                    childBaseSetApplied = history.childNew(childBaseSetLoaded);

                    history.applied(baseValueLoaded).creditorIdFrom(baseValueLoaded).value(childBaseSetApplied).execute();
                }
                if (IS.LESS(compare)) {
                    history.event("RepDate of baseValueSaving LESS when baseValueLoaded");
                    childBaseSetApplied = history.childNew(childBaseSetLoaded);

                    history.applied(baseValueLoaded).creditorIdFrom(baseValueLoaded).dateFrom(baseValueSaving).value(childBaseSetApplied).updated().execute();

                }
            }
        } else {
            if (IS.EMPTY(childBaseSetSaving)) {
                history.end();
                return;
                // throw history.ErrorUO();
            }

            IBaseValue baseValueClosed;
            // case#5
            if (IS.NOT_FINAL()) {
                baseValueClosed = history.closed(baseValueSaving).result();

                if (IS.NOT_EMPTY(baseValueClosed)) {
                    history.closed().parent(true).deleted().execute();

                    IBaseValue baseValuePrevious = history.previous(baseValueClosed).result();

                    if (IS.EMPTY(baseValuePrevious))
                        throw history.Error68();

                    baseValuePrevious = history.previous().parent(true).result();

                    childBaseSetLoaded = history.childFrom(baseValueClosed);
                    childBaseSetApplied = history.childNew(baseValuePrevious);

                    history.applied(baseValuePrevious).creditorIdFrom(baseValuePrevious).value(childBaseSetApplied).closed(false).last(true).updated().execute();
                }

                // case#6
                if (IS.EMPTY(baseValueClosed)) {
                    IBaseValue baseValueNext = history.next(baseValueSaving).result();

                    if (IS.NOT_EMPTY(baseValueNext)) {
                        childBaseSetLoaded = history.childFrom(baseValueNext);
                        childBaseSetApplied = history.childNew(baseValueNext);

                        history.applied(baseValueNext).dateFrom(baseValueSaving).value(childBaseSetApplied).attribute(false).updated().execute();
                    } else {
                        childBaseSetApplied = history.childNew();
                        history.base(childBaseSetApplied).inserted().execute();

                        history.applied(baseValueSaving).id(0L).value(childBaseSetApplied).closed(false).last(true).inserted().execute();
                    }
                }
            } else {
                childBaseSetApplied = history.childNew();
                history.base(childBaseSetApplied).inserted().execute();

                history.applied(baseValueSaving).id(0L).value(childBaseSetApplied).closed(false).last(true).inserted().execute();

            }
        }

        if (IS.NOT_EMPTY(childBaseSetSaving) && IS.VALUE_COUNT_NOT_EMPTY(childBaseSetSaving)) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                IBaseEntity childBaseEntitySaving = history.childEntityFrom(childBaseValueSaving);

                if (IS.NOT_EMPTY(childBaseSetLoaded)) {
                    DATE compare = IS.COMPARE_DATE(baseValueSaving, baseValueLoaded);

                    if (IS.NOT_FINAL() || (IS.FINAL() && IS.EQUAL(compare))) {
                        baseValueFound = false;

                        for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                            if (history.contains(childBaseValueLoaded))
                                continue;

                            IBaseEntity childBaseEntityLoaded = history.childEntityFrom(childBaseValueLoaded);

                            history.event("Befor compare childBaseValueSaving to childBaseValueLoaded");
                            history.persistable("childBaseValueSaving", childBaseValueSaving);
                            history.persistable("childBaseValueLoaded", childBaseValueLoaded);

                            if (IS.EQUALS(childBaseValueSaving, childBaseValueLoaded) || IS.ID_EQUALS(childBaseEntitySaving, childBaseEntityLoaded)) {
                                history.event("childBaseValueSaving EQUALS to childBaseValueLoaded");
                                history.processed(childBaseValueLoaded);
                                baseValueFound = true;

                                IBaseEntity baseEntityApplied = applyBaseEntityAdvanced(
                                        creditorId,
                                        childBaseEntitySaving,
                                        childBaseEntityLoaded,
                                        baseEntityManager);

                                history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).creditorIdFrom(childBaseValueLoaded).value(baseEntityApplied).attribute(childBaseSetApplied).parent(false);

                                history.rep_dates("childBaseValueSaving", childBaseValueSaving, "childBaseValueLoaded", childBaseValueLoaded);
                                if (IS.COMPARE_DATE(childBaseValueSaving, childBaseValueLoaded) == DATE_LESS) {
                                    history.event("RepDate of childBaseValueSaving LESS to childBaseValueLoaded");
                                    history.applied().dateFrom(childBaseValueSaving).parent(false).updated().execute();
                                }

                                break;
                            }
                        }

                        if (baseValueFound)
                            continue;
                    }
                }

                // Если значение было закрыто и оно не ключевое, элемент массива не будет идентифицирован.
                if (IS.NOT_NEW(childBaseEntitySaving)) {
                    history.withValueDao(childBaseValueSaving, IBaseSetValueDao.class);

                    if (IS.NOT_FINAL()) {
                        IBaseValue baseValueForSearch = history.applied(childBaseValueSaving).containerType(MetaContainerTypes.META_SET).child(true).creditorIdFrom(childBaseValueSaving).id(0L).attribute(false).parentFrom(childBaseSetApplied).result();

                        IBaseValue childBaseValueClosed = history.closed(baseValueForSearch).result();

                        if (IS.NOT_EMPTY(childBaseValueClosed)) {
                            history.closed().containerValue(childBaseSetApplied).deleted().execute();

                            // todo: UNQ_CONST
                            IBaseValue childBaseValuePrevious = history.base(childBaseValueClosed).result(); //setValueDao.getPreviousBaseValue(childBaseValueClosed);

                            IBaseEntity childBaseEntityPrevious = history.childEntityFrom(childBaseValuePrevious);

                            if (IS.NOT_EMPTY(childBaseValuePrevious) && IS.NOT_EMPTY(childBaseEntityPrevious)) {
                                history.base().parentFrom(childBaseSetApplied);

                                history.base().value(applyBaseEntityAdvanced(creditorId,
                                        childBaseEntitySaving, childBaseEntityPrevious, baseEntityManager));

                                if (IS.LAST(childBaseValueClosed)) {
                                    history.base().last(true).attribute(childBaseSetApplied).updated().execute();
                                } else {
                                    history.base().attribute(childBaseSetApplied).execute();
                                }
                            }

                            continue;
                        }

                        // Check next value
                        IBaseValue childBaseValueNext = history.next(childBaseValueSaving).result();
                        if (IS.NOT_EMPTY(childBaseValueNext)) {
                            IBaseEntity childBaseEntityNext = history.childEntityFrom(childBaseValueNext);

                            history.next().dateFrom(childBaseValueSaving).value(applyBaseEntityAdvanced(creditorId, childBaseEntitySaving,
                                    childBaseEntityNext, baseEntityManager)).attribute(childBaseSetApplied).updated().execute();
                            continue;
                        }
                    }
                }
                history.noWith();


                history.applied(childBaseValueSaving).containerType(MetaContainerTypes.META_SET).child(true).id(0L).value(apply(creditorId, childBaseEntitySaving, null, baseEntityManager))
                        .closed(false).last(true).parent(false).attribute(childBaseSetApplied).inserted().execute();
            }
        }

        /* Удаляет элементы массива, если массив не накопительный или массив накопительный и родитель был удалён */
        if (IS.NOT_EMPTY(childBaseSetLoaded) &&
                ((IS.CUMULATIVE() && isBaseSetDeleted) || IS.NOT_CUMULATIVE())) {
            //одно закрытие на несколько одинаковых записей

            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (history.contains(childBaseValueLoaded))
                    continue;

                history.withValueDao(childBaseValueLoaded, IBaseSetValueDao.class);

                IBaseEntity childBaseEntityLoaded = history.childEntityFrom(childBaseValueLoaded);

                history.rep_dates("baseValueSaving", baseValueSaving, "childBaseValueLoaded", childBaseValueLoaded);
                switch (IS.COMPARE_DATE(baseValueSaving, childBaseValueLoaded)) {

                    case DATE_LESS:
                        history.event("RepDate of baseValueSaving LESS when childBaseValueLoaded");
                        history.persistable("childBaseValueLoaded", childBaseValueLoaded);
                        continue;

                    case DATE_EQUAL:
                        history.event("RepDate of baseValueSaving EQUAL to childBaseValueLoaded");
                        history.persistable("childBaseValueLoaded", childBaseValueLoaded);
                        history.base(childBaseValueLoaded).deleted().execute();

                        if (IS.NOT_EMPTY(childBaseEntityLoaded) && IS.CHILD_NOT_SEARCHABLE())
                            history.base(childBaseEntityLoaded).deleted().execute();

                        boolean last = IS.LAST(childBaseValueLoaded);

                        if (IS.NOT_FINAL()) {
                            IBaseValue childBaseValueNext = history.next(childBaseValueLoaded).result();

                            if (childBaseValueNext != null && childBaseValueNext.isClosed()) {
                                last = history.next().deleted().result().isLast();
                            }
                        }

                        if (last && !(IS.FINAL() && IS.CHILD_NOT_SEARCHABLE())) {
                            IBaseValue childBaseValuePrevious = history.previous(childBaseValueLoaded).result();
                            if (IS.NOT_EMPTY(childBaseValuePrevious)) {
                                history.previous().last(true).containerValue(childBaseSetApplied).updated().execute();
                            }
                        }
                        break;
                    case DATE_MORE:
                        history.event("RepDate of baseValueSaving MORE when childBaseValueLoaded");
                        history.persistable("childBaseValueLoaded", childBaseValueLoaded);
                        IBaseValue childBaseValueNext = history.next(childBaseValueLoaded).result();

                        if (IS.EMPTY(childBaseValueNext) || IS.NOT_CLOSED(childBaseValueNext)) {

                        /* Не идентифицируемый элемент массива не может быт закрыт если не указан как закрываемый*/
                            if (IS.CHILD_NOT_SEARCHABLE() && IS.CHILD_NOT_CLOSABLE()) {
                                history.base(childBaseValueLoaded).attribute(childBaseSetApplied).execute();
                                continue;
                            }

                            IBaseEntity closedChildBaseEntity = history.childEntityFrom(childBaseValueLoaded);
                            if (!history.contains(closedChildBaseEntity)) {
                                history.processed(closedChildBaseEntity);

                                history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).creditorIdFrom(baseValueSaving).id(0L).dateFrom(baseValueSaving).value(childBaseEntityLoaded).closed(true)
                                        .parent(false).attribute(false).containerValue(childBaseSetApplied).inserted().execute();
                            }

                            if (childBaseValueLoaded.isLast()) {
                                history.applied(childBaseValueLoaded).containerType(MetaContainerTypes.META_SET).child(true).creditorIdFrom(childBaseValueLoaded).value(childBaseEntityLoaded).last(false)
                                        .parent(false).attribute(false).containerValue(childBaseSetApplied).updated().execute();
                            }
                        } else {
                            history.next().dateFrom(baseValueSaving).containerValue(childBaseSetApplied).updated().execute();
                        }
                }
            }
            history.noWith();
        }

        /* Обработка накопительных массивов для витрин */
        if (IS.CUMULATIVE() && !isBaseSetDeleted && IS.NOT_EMPTY(childBaseSetLoaded)) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (history.contains(childBaseValueLoaded))
                    continue;

                if (IS.NOT_EMPTY(childBaseSetApplied)) childBaseSetApplied.put(childBaseValueLoaded);
            }
        }

        history.end();

    }

    @Override
    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(testMode, baseEntityManager);

        history.beginApplyToDb();

        long applyToDbTime = System.currentTimeMillis();


        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class<? extends IPersistable> objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable insertedObject : insertedObjects) {
                    try {
                        persistableDao.insert(insertedObject);

                        if (insertedObject instanceof BaseEntity) {
                            BaseEntity be = (BaseEntity) insertedObject;
                            if (BasicOptimizer.metaList.contains(be.getMeta().getClassName())) {
                                EavOptimizerData eod = new EavOptimizerData(baseEntityManager.getCreditorId(),
                                        be.getMeta().getId(), be.getId(), BasicOptimizer.getKeyString(be));
                                eavOptimizerDao.insert(eod);
                            }
                        }

                        if (insertedObject instanceof BaseEntityReportDate) {
                            IBaseEntity baseEntity = ((BaseEntityReportDate) insertedObject).getBaseEntity();
                            if (baseEntity.getMeta().isReference())
                                refRepository.invalidate(baseEntity);
                        }

                    } catch (Exception insertException) {
                        throw history.Error76(insertedObject, insertException);
                    }
                }
            }
        }

        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class<? extends IPersistable> objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> updatedObjects = baseEntityManager.getUpdatedObjects(objectClass);
            if (updatedObjects != null && updatedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable updatedObject : updatedObjects) {
                    try {
                        persistableDao.update(updatedObject);

                        if (updatedObject instanceof BaseEntityReportDate) {
                            IBaseEntity baseEntity = ((BaseEntityReportDate) updatedObject).getBaseEntity();
                            if (baseEntity.getMeta().isReference())
                                refRepository.invalidate(baseEntity);
                        }

                    } catch (Exception updateException) {
                        throw history.Error77(updatedObject, updateException);
                    }
                }
            }
        }

        for (int i = BaseEntityManager.CLASS_PRIORITY.size() - 1; i >= 0; i--) {
            Class<? extends IPersistable> objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> deletedObjects = baseEntityManager.getDeletedObjects(objectClass);
            if (deletedObjects != null && deletedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable deletedObject : deletedObjects) {
                    try {
                        persistableDao.delete(deletedObject);

                        if (deletedObject instanceof IBaseEntity) {
                            if (((IBaseEntity) deletedObject).getMeta().isReference())
                                refRepository.invalidate(((IBaseEntity) deletedObject));
                        }
                    } catch (Exception deleteException) {
                        throw history.Error78(deletedObject, deleteException);
                    }
                }
            }
        }

        /* Изменение ключевых полей в оптимизаторе */
        for (Map.Entry<Long, IBaseEntity> entry : baseEntityManager.getOptimizerEntities().entrySet()) {
            EavOptimizerData eod = new EavOptimizerData(
                    baseEntityManager.getCreditorId(),
                    entry.getValue().getMeta().getId(),
                    entry.getValue().getId(),
                    BasicOptimizer.getKeyString(entry.getValue()));

            eod.setId(eavOptimizerDao.find(entry.getValue().getId()));
            eavOptimizerDao.update(eod);
        }
        sqlStats.put("java::applyToDb", (System.currentTimeMillis() - applyToDbTime));

        history.end();

    }

    @Override
    public Boolean isTestMode() {
        return testMode;
    }

}



