package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.exceptions.ImmutableElementException;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.impl.apply.ApplyHistoryFactory;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.tool.optimizer.EavOptimizerData;
import kz.bsbnb.usci.eav.tool.optimizer.impl.BasicOptimizer;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.ErrorHandler;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class BaseEntityApplyDaoImpl extends JDBCSupport implements IBaseEntityApplyDao {
    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;

    @Autowired
    private IEavOptimizerDao eavOptimizerDao;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    IRefRepository refRepository;

    @Override
    public IBaseEntity apply(long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntityManager baseEntityManager) {
        IBaseEntity baseEntityApplied;

        // Новые сущности или сущности не имеющие ключевые атрибуты
        if (baseEntitySaving.getId() < 1 || !baseEntitySaving.getMeta().isSearchable()) {
            baseEntityApplied = applyBaseEntityBasic(creditorId, baseEntitySaving, baseEntityManager);
        } else {
            if (baseEntityLoaded == null) {
                Date reportDateSaving = baseEntitySaving.getReportDate();

                // Получение максимальной отчетной даты из прошедших периодов
                Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(baseEntitySaving.getId(), reportDateSaving);

                if (maxReportDate == null) {
                    // Получение минимальной отчетной даты из будущих периодов
                    Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntitySaving.getId(), reportDateSaving);

                    if (minReportDate == null)
                        throw new UnsupportedOperationException(Errors.compose(Errors.E56, baseEntitySaving.getId()));

                    baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), minReportDate, reportDateSaving);

                    if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                        throw new UnsupportedOperationException(Errors.compose(Errors.E57,
                                baseEntityLoaded.getId(), baseEntityLoaded.getBaseEntityReportDate().getReportDate()));
                } else {
                    baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), maxReportDate, reportDateSaving);

                    if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                        throw new UnsupportedOperationException(Errors.compose(Errors.E57, baseEntityLoaded.getId(),
                                baseEntityLoaded.getBaseEntityReportDate().getReportDate()));

                }
            }

            baseEntityApplied = applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
        }

        return baseEntityApplied;
    }

    @Override
    public IBaseEntity applyBaseEntityBasic(long creditorId, IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager) {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMeta(), baseEntitySaving.getReportDate(), creditorId);
        if (baseEntitySaving.getAddInfo() != null)
            baseEntityApplied.setAddInfo(baseEntitySaving.getAddInfo().parentEntity, baseEntitySaving.getAddInfo().isSet,
                    baseEntitySaving.getAddInfo().attributeId);

        for (String attribute : baseEntitySaving.getAttributes()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);

            // Пропускает закрытые теги на новые сущности <tag/>
            if (baseValueSaving.getValue() == null)
                continue;

            applyBaseValueBasic(creditorId, baseEntityApplied, baseValueSaving, baseEntityManager);
        }

        baseEntityApplied.calculateValueCount(null);
        baseEntityManager.registerAsInserted(baseEntityApplied);

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();
        baseEntityManager.registerAsInserted(baseEntityReportDate);
        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);

        return baseEntityApplied;
    }

    @Override
    public void applyBaseValueBasic(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        if (metaAttribute == null)
            throw new IllegalStateException(Errors.compose(Errors.E60));

        IBaseContainer baseContainer = baseValueSaving.getBaseContainer();
        if (baseContainer != null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
            throw new IllegalStateException(Errors.compose(Errors.E59, metaAttribute.getName()));

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValueSaving.getValue();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType(), creditorId);
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                    if (metaAttribute.isImmutable() && childBaseEntity.getValueCount() != 0 && childBaseEntity.getId() < 1)
                        throw new ImmutableElementException(childBaseEntity);

                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null, baseEntityManager);

                    IBaseValue childBaseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaSet.getMemberType(),
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
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
                        0,
                        creditorId,
                        new Date(baseValueSaving.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true);

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            } else {
                if (metaAttribute.isImmutable()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValueSaving.getValue();

                    if (childBaseEntity.getValueCount() != 0) {
                        if (childBaseEntity.getId() < 1)
                            throw new ImmutableElementException(childBaseEntity);

                        IBaseEntity childBaseEntityImmutable;

                        if (metaAttribute.getMetaType().isReference()) {
                            childBaseEntityImmutable = refRepository.get(childBaseEntity);
                        } else {
                            childBaseEntityImmutable = baseEntityLoadDao.loadByMaxReportDate(
                                    childBaseEntity.getId(), childBaseEntity.getReportDate());
                        }

                        if (childBaseEntityImmutable == null)
                            throw new RuntimeException(Errors.compose(Errors.E63, childBaseEntity.getId(),
                                    childBaseEntity.getReportDate()));

                        if (childBaseEntityImmutable.getBaseEntityReportDate().isClosed())
                            errorHandler.throwClosedExceptionForImmutable(childBaseEntityImmutable);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseEntityImmutable,
                                false,
                                true);

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    } else {
                        throw new IllegalStateException(Errors.compose(Errors.E64, childBaseEntity.getMeta().getClassName()));
                    }
                } else {
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValueSaving.getValue();
                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null, baseEntityManager);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseEntityApplied,
                            false,
                            true);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        } else {
            if (metaType.isSet()) {
                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValueSaving.getValue();
                IMetaValue metaValue = (IMetaValue) childMetaSet.getMemberType();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType(), creditorId);
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseValue childBaseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaSet.getMemberType(),
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, childBaseValue),
                            false,
                            true);

                    childBaseSetApplied.put(childBaseValueApplied);
                    baseEntityManager.registerAsInserted(childBaseValueApplied);
                }

                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        0,
                        creditorId,
                        new Date(baseValueSaving.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true);

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            } else {
                IMetaValue metaValue = (IMetaValue) metaType;
                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        0,
                        creditorId,
                        new Date(baseValueSaving.getRepDate().getTime()),
                        returnCastedValue(metaValue, baseValueSaving),
                        false,
                        true);

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
        }
    }

    @Override
    public IBaseEntity applyBaseEntityAdvanced(long creditorId, IBaseEntity baseEntitySaving,
                                               IBaseEntity baseEntityLoaded, IBaseEntityManager baseEntityManager) {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        IMetaClass metaClass = baseEntitySaving.getMeta();

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntityLoaded, baseEntitySaving.getReportDate());
        baseEntityApplied.setUserId(baseEntitySaving.getUserId());
        baseEntityApplied.setBatchId(baseEntitySaving.getBatchId());

        // Устанавливает ID для !metaClass.isSearchable()
        if (baseEntitySaving.getId() < 1 && baseEntityLoaded.getId() > 0)
            baseEntitySaving.setId(baseEntityLoaded.getId());


        if (baseEntityReportDateDao.exists(baseEntitySaving.getId(), baseEntitySaving.getReportDate())) {
            baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), baseEntitySaving.getReportDate(), baseEntitySaving.getReportDate());
            baseEntityLoaded.setUserId(baseEntitySaving.getUserId());
            baseEntityLoaded.setBatchId(baseEntitySaving.getBatchId());
        }

        for (String attrName : metaClass.getAttributeNames()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attrName);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attrName);

            final IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attrName);
            final IMetaType metaType = metaAttribute.getMetaType();

            if (baseValueSaving == null && baseValueLoaded != null && !metaAttribute.isNullable())
                baseEntityApplied.put(attrName, baseValueLoaded);

            if (baseValueSaving == null && metaAttribute.isNullable()) {
                baseValueSaving = BaseValueFactory.create(baseEntitySaving.getBaseContainerType(), metaType,
                        0, creditorId, baseEntitySaving.getReportDate(), null, false, true);
                baseValueSaving.setBaseContainer(baseEntitySaving);
                baseValueSaving.setMetaAttribute(metaAttribute);
            }

            if (baseValueSaving == null)
                continue;

            if (metaType.isComplex()) {
                if (metaType.isSet())
                    applyComplexSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applyComplexValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            } else {
                if (metaType.isSet())
                    applySimpleSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
                else
                    applySimpleValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded, baseEntityManager);
            }
        }

        int compare = DataUtils.compareBeginningOfTheDay(baseEntitySaving.getReportDate(), baseEntityLoaded.getReportDate());

        if (compare == 0 || compare == 1) {
            baseEntityApplied.calculateValueCount(baseEntityLoaded);
        } else {
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
        return baseEntityApplied;
    }
-
    @Override
    public void applySimpleValue(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValueSaving,
                                 IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {

        final ApplyHistoryFactory history = new ApplyHistoryFactory(creditorId, baseEntityApplied, baseValueSaving,
                baseValueLoaded, baseEntityManager);

        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    if (metaAttribute.isFinal()) {
                        history.applied(baseValueLoaded).deleted().attribute(false).execute();
                        if (baseValueLoaded.isLast()) {
                            history.previous(baseValueLoaded).last(true).updated().execute();
                        }
                        IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            history.next().deleted().execute();
                        }
                    } else {
                        history.applied(baseValueLoaded).closed(true).attribute(false).deleted().execute();
                        if (baseValueLoaded.isLast()) {
                            history.previous(baseValueLoaded).last(true).updated().execute();
                        }
                        IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            history.next().deleted().execute();
                        }
                    }
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException(Errors.compose(Errors.E66, metaAttribute.getName()));

                    if (baseValueLoaded.isLast()) {
                        history.applied(baseValueLoaded).last(false).attribute(false).updated().execute();
                    }

                    IBaseValue baseValueNext = history.next(baseValueLoaded).result();
                    if (baseValueNext != null)
                        if (baseValueNext.isClosed()) {
                            history.next().dateFrom(baseValueSaving).updated().execute();
                        } else {
                            if (StaticRouter.exceptionOnForbiddenCloseE299())
                                throw new UnsupportedOperationException(Errors.compose(Errors.E299, DataTypes.formatDate(baseValueNext.getRepDate()), baseValueNext.getValue()));
                        }
                    else {
                        history.applied(baseValueLoaded, true, true).id(0L).attribute(false).inserted().execute();
                    }
                } else {
                    //throw new UnsupportedOperationException(Errors.compose(Errors.E75, baseValueSaving.getMetaAttribute().getName()));
                }
                return;
            }

            if (baseValueSaving.equalsByValue(baseValueLoaded)) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // Именение ключевых полей
                // <tag operation="new" data="new_value">old_value</tag>
                Object baseValue;
                if (baseValueSaving.getNewBaseValue() != null) {
                    baseValue = history.newValue(baseValueSaving);
                    history.base().optimizer().execute();
                } else {
                    baseValue = history.castedValue(baseValueLoaded);
                }

                if (compare == 0 || compare == 1) {
                    history.applied(baseValueLoaded).value(baseValue).execute();
                    // Запуск на изменение ключевого поля
                    if (baseValueSaving.getNewBaseValue() != null)
                        history.from().updated().execute();
                } else if (compare == -1) {
                    history.applied(baseValueLoaded).value(baseValue).dateFrom(baseValueSaving).updated().execute();
                }

            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    history.applied(baseValueLoaded).castedValue(baseValueSaving).updated().execute();
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException(Errors.compose(Errors.E69, metaAttribute.getName()));

                    history.applied(baseValueLoaded).id(0L).dateFrom(baseValueSaving).inserted().execute();

                    if (baseValueLoaded.isLast()) {
                        history.applied(baseValueLoaded).last(false).updated().execute();
                    }
                } else if (compare == -1) {
                    history.applied(baseValueSaving).id(0L).closed(false).last(false).inserted().execute();
                }
            }
        } else {
            if (baseValueSaving.getValue() == null) {
                return;
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            if (!metaAttribute.isFinal()) {
                IBaseValue baseValueClosed = history.closed(baseValueSaving).parent(true).result();
                if (baseValueClosed != null) {
                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseValueClosed = history.closed().deleted().result();

                        IBaseValue baseValuePrevious = history.previous(baseValueClosed).result();

                        if (baseValuePrevious == null)
                            throw new IllegalStateException(Errors.compose(Errors.E70, baseValueClosed.getMetaAttribute().getName()));

                        history.previous().last(true).parent(true).updated().execute();
                    } else {
                        history.closed().castedValue(baseValueSaving).closed(false).updated().execute();
                    }
                } else {
                    IBaseValue baseValueNext = history.closed(baseValueSaving).result();

                    if (baseValueNext != null) {
                        history.applied(baseValueSaving).idFrom(baseValueNext).closedFrom(baseValueNext).lastFrom(baseValueNext).parent(false).updated().execute();
                    } else {
                        history.applied(baseValueSaving).id(0L).closed(false).last(true).parent(false).inserted().execute();
                    }
                }
            } else {
                IBaseValue baseValueLast = history.closed(baseValueSaving).result();

                if (baseValueLast == null) {
                    history.applied(baseValueSaving).id(0L).closed(false).last(true).parent(false).inserted().execute();
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();

                    boolean last = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLast) != -1;

                    if (last) {
                        history.closed().last(true).parent(true).updated().execute();
                    }

                    history.applied(baseValueSaving).id(0L).closed(false).last(last).parent(false).inserted().execute();
                }
            }
        }
    }

    @Override
    public void applyComplexValue(final long creditorId, final IBaseEntity baseEntity, final IBaseValue baseValueSaving,
                                  final IBaseValue baseValueLoaded, final IBaseEntityManager baseEntityManager) {
    final ApplyHistoryFactory history = new ApplyHistoryFactory(creditorId, baseEntity, baseValueSaving,
                baseValueLoaded, baseEntityManager);

        ApplyHistoryFactory.IGetFunction getEntityApplied = new ApplyHistoryFactory.IGetFunction() {
            @Override
            public IBaseEntity execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, IMetaClass metaClass, IMetaAttribute metaAttribute) {
                IBaseEntity baseEntityApplied = null;
                if (metaAttribute.isImmutable()) {
                    if (metaAttribute.getMetaType().isReference()) {
                        baseEntityApplied = refRepository.get(baseEntitySaving);
                    } else {
                        baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(), baseEntitySaving.getReportDate());
                    }
                    if (baseEntityApplied.getBaseEntityReportDate().isClosed())
                        errorHandler.throwClosedExceptionForImmutable(baseEntityApplied);
                }
                return baseEntityApplied;
            }
        };

        ApplyHistoryFactory.IGetFunction getEntityAppliedAdvanced = new ApplyHistoryFactory.IGetFunction() {
            @Override
            public IBaseEntity execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, IMetaClass metaClass, IMetaAttribute metaAttribute) {
                return applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
            }
        };

        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    if (metaAttribute.isFinal()) {

                        history.applied(baseValueLoaded).attribute(false).deleted().execute();

                        if (baseValueLoaded.isLast()) {
                            IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();

                            if (baseValuePrevious != null) {
                                history.previous().last(true).parent(true).updated().execute();
                            }
                        }

                        if (!metaClass.isSearchable() && !metaAttribute.isImmutable()) {

                            history.eachAttribute(new ApplyHistoryFactory.IEachAttribute() {
                                @Override
                                public void execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, String attributeName, IMetaAttribute childMetaAttribute, IMetaType childMetaType) {
                                    baseEntitySaving.put(
                                            attributeName,
                                            history.applied(baseValueSaving).metaType(childMetaType).id(0L).value(null).closed(false).last(true).result()
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

                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            history.next().parent(true).deleted().execute();
                        }

                        return;
                    } else {

                        history.applied(baseValueLoaded).deleted().execute();

                        if (baseValueLoaded.isLast()) {
                            IBaseValue baseValuePrevious = history.previous(baseValueLoaded).result();
                            if (baseValuePrevious != null) {
                                history.previous().last(true).parent(true).updated().execute();
                            }
                        }

                        if (!metaClass.isSearchable() && !metaAttribute.isImmutable()) {

                            history.eachAttribute(new ApplyHistoryFactory.IEachAttribute() {
                                @Override
                                public void execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, String attributeName, IMetaAttribute childMetaAttribute, IMetaType childMetaType) {
                                    baseEntitySaving.put(
                                            attributeName,
                                            history.applied(null).metaType(childMetaType).id(0L).value(null).dateFrom(baseValueSaving).closed(false).last(true).result()
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

                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            history.previous().parent(true).deleted().execute();
                        }
                    }
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException(Errors.compose(Errors.E66, metaAttribute.getName()));

                    if (baseValueLoaded.isLast()) {
                        history.last(baseValueLoaded).last(false).updated().execute();
                    }

                    IBaseValue baseValueNext = history.next(baseValueLoaded).result();

                    // check for next closed value
                    if (baseValueNext != null)
                        if (baseValueNext.isClosed()) {
                            history.next().dateFrom(baseValueSaving).parent(true).updated().execute();
                        } else  {
                            if(StaticRouter.exceptionOnForbiddenCloseE299())
                                throw new UnsupportedOperationException(Errors.compose(Errors.E299,
                                        DataTypes.formatDate(baseValueNext.getRepDate()), ((IBaseEntity) baseValueNext.getValue()).getId()));
                        }
                    else {
                        history.applied(baseValueLoaded).id(0L).dateFrom(baseValueSaving).closed(true).last(true).inserted().execute();
                    }
                }/* else if (compare == -1) {
                    throw new UnsupportedOperationException("Закрытие атрибута за прошлый период не является возможным"
                            + ". " + baseValueSaving.getMetaAttribute().getName() + ";");
                }*/

                return;
            }

            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

            if (metaAttribute.isImmutable() && baseEntitySaving.getId() == 0)
                throw new ImmutableElementException(baseEntitySaving);

            if (baseEntitySaving.getId() == baseEntityLoaded.getId() || !metaClass.isSearchable()) {

                IBaseEntity baseEntityApplied = history.get(getEntityApplied, baseEntityLoaded, baseEntitySaving);
                if (!metaAttribute.isImmutable()) {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager) :
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                int compare = DataUtils.compareBeginningOfTheDay(baseValueSaving.getRepDate(), baseValueLoaded.getRepDate());

                history.applied(baseValueLoaded).value(baseEntityApplied);
                if(compare == -1) history.applied().dateFrom(baseValueSaving);
                if (compare == -1) history.applied().updated().execute();
            } else {
                IBaseEntity baseEntityApplied = history.get(getEntityApplied, baseEntityLoaded, baseEntitySaving);
                if (!metaAttribute.isImmutable()) {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    history.applied(baseValueLoaded).value(baseEntityApplied).updated().execute();
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException(Errors.compose(Errors.E69, metaAttribute.getName()));

                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).inserted().execute();

                    if (baseValueLoaded.isLast()) {
                        history.previous(baseValueLoaded).last(false).parent(true).updated().execute();
                    }
                } else if (compare == -1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException(Errors.compose(Errors.E69, metaAttribute.getName()));

                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(false).parent(false).inserted().execute();

                }
            }
        } else {
            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            if (baseEntitySaving == null) {
                return;
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            if (metaAttribute.isImmutable() && baseEntitySaving.getId() == 0)
                throw new IllegalStateException(Errors.compose(Errors.E71, metaAttribute.getName()));

            if (!metaAttribute.isFinal()) {
                IBaseValue baseValueClosed = history.closed(baseValueSaving).parent(true).result();

                if (baseValueClosed != null) {

                    IBaseEntity baseEntityClosed = (IBaseEntity) baseValueClosed.getValue();

                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        history.closed().deleted().execute();

                        IBaseValue baseValuePrevious = history.previous(baseValueClosed).parent(true).result();

                        if (baseValuePrevious == null)
                            throw new IllegalStateException(Errors.compose(Errors.E70, baseValueClosed.getMetaAttribute().getName()));

                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (!metaAttribute.isImmutable()) {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, null, baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager);
                        }

                        history.previous().value(baseEntityApplied);

                        if (baseValueClosed.isLast()) {
                            history.previous().last(true).attribute(true).updated().execute();
                        } else {
                            history.previous().attribute(true);
                        }
                    } else {
                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (!metaAttribute.isImmutable()) {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager);
                        }

                        history.closed().value(baseEntityApplied).closed(false).attribute(true).updated().execute();
                    }
                } else {
                    IBaseValue<IBaseEntity> baseValueNext = history.next(baseValueSaving).parent(true).result();

                    if (baseValueNext != null) {
                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (!metaAttribute.isImmutable()) {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, baseValueNext.getValue(), baseEntityManager) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseValueNext.getValue(), baseEntityManager);
                        }

                        history.next().dateFrom(baseValueSaving).value(baseEntityApplied).attribute(true).updated().execute();

                    } else {
                        IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                        if (!metaAttribute.isImmutable()) {
                            baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);
                        }

                        history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).inserted().execute();
                    }
                }
            } else {
                IBaseValue baseValueLast = history.last(baseValueSaving).result();

                IBaseEntity baseEntityApplied = history.get(getEntityApplied, null, baseEntitySaving);
                if (!metaAttribute.isImmutable()) {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager);

                    IBaseValue previousBaseValue = history.previous(baseValueSaving).result();

                    if (previousBaseValue != null && previousBaseValue.isLast()) {
                        history.applied(previousBaseValue).last(false).attribute(true).updated().execute();
                    }
                }

                if (baseValueLast == null) {
                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(true).attribute(true).inserted().execute();
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();

                    boolean last = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLast) != -1;

                    if (last) {
                        history.last().last(false).parent(true).updated().execute();
                    }

                    history.applied(baseValueSaving).id(0L).value(baseEntityApplied).closed(false).last(last).attribute(true).inserted().execute();
                }
            }
        }
    }

    @Override
    public void applySimpleSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                               IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();

        if (metaAttribute.isFinal())
            throw new UnsupportedOperationException(Errors.compose(Errors.E2));

        IMetaSet metaSet = (IMetaSet) metaType;
        IMetaType childMetaType = metaSet.getMemberType();
        IMetaValue childMetaValue = (IMetaValue) childMetaType;

        IBaseSet childBaseSetSaving = (IBaseSet) baseValueSaving.getValue();
        IBaseSet childBaseSetLoaded = null;
        IBaseSet childBaseSetApplied = null;

        boolean isBaseSetDeleted = false;

        if (baseValueLoaded != null) {
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() == 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // case#1
                if (compare == 0) {
                    IBaseValue baseValueDeleted = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            childBaseSetLoaded,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

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

                    // delete next closed value
                    IBaseValueDao valueDao = persistableDaoPool
                            .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                    IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

                    if (baseValueNext != null && baseValueNext.isClosed()) {
                        baseValueNext.setBaseContainer(baseEntity);
                        baseValueNext.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsDeleted(baseValueNext);
                    }

                    isBaseSetDeleted = true;
                    // case#2
                } else if (compare == 1) {
                    IBaseValueDao valueDao = persistableDaoPool
                            .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                    IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

                    // check for next closed value
                    if (baseValueNext != null && baseValueNext.isClosed()) {
                        baseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseValueNext.setBaseContainer(baseEntity);
                        baseValueNext.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValueNext);
                    } else {
                        IBaseValue baseValueClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetLoaded,
                                true,
                                true);

                        baseValueClosed.setBaseContainer(baseEntity);
                        baseValueClosed.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsInserted(baseValueClosed);

                        if (baseValueLoaded.isLast()) {
                            IBaseValue baseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_CLASS,
                                    metaType,
                                    baseValueLoaded.getId(),
                                    creditorId,
                                    new Date(baseValueLoaded.getRepDate().getTime()),
                                    childBaseSetLoaded,
                                    baseValueLoaded.isClosed(),
                                    false);

                            baseValueLast.setBaseContainer(baseEntity);
                            baseValueLast.setMetaAttribute(metaAttribute);
                            baseEntityManager.registerAsUpdated(baseValueLast);
                        }
                    }

                    isBaseSetDeleted = true; // todo: check for cumulative arrays
                } else {
                    throw new IllegalStateException(Errors.compose(Errors.E72, metaAttribute.getName()));
                }
            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // case#3
                if (compare == 0 || compare == 1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    // case#4
                } else if (compare == -1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
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
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);

            // case#5
            if (baseValueClosed != null) {
                baseValueClosed.setBaseContainer(baseValueSaving.getBaseContainer());
                baseValueClosed.setMetaAttribute(baseValueSaving.getMetaAttribute());

                IBaseValue baseValueDeleted = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueClosed.getId(),
                        creditorId,
                        new Date(baseValueClosed.getRepDate().getTime()),
                        null,
                        baseValueClosed.isClosed(),
                        baseValueClosed.isLast());

                baseEntityManager.registerAsDeleted(baseValueDeleted);

                IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueClosed);

                if (baseValuePrevious == null)
                    throw new IllegalStateException(Errors.compose(Errors.E70, baseValueClosed.getMetaAttribute().getName()));

                baseValuePrevious.setBaseContainer(baseValueSaving.getBaseContainer());
                baseValuePrevious.setMetaAttribute(baseValueSaving.getMetaAttribute());

                childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValuePrevious.getId(),
                        creditorId,
                        new Date(baseValuePrevious.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true);

                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsUpdated(baseValueApplied);
                // case#6
            } else {
                IBaseValue baseValueNext = baseValueDao.getNextBaseValue(baseValueSaving);

                if (baseValueNext != null) {
                    childBaseSetLoaded = (IBaseSet) baseValueNext.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueNext.getId(),
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            baseValueNext.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                } else {
                    IBaseValue baseValueExisting = baseValueDao.getNextBaseValue(baseValueSaving);

                    if (baseValueExisting != null) {
                        childBaseSetLoaded = (IBaseSet) baseValueExisting.getValue();
                        childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueExisting.getId(),
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                baseValueNext.isLast());

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsUpdated(baseValueApplied);
                    } else {
                        childBaseSetApplied = new BaseSet(childMetaType, creditorId);
                        baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                true);

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
            }
        }

        Set<UUID> processedUUIDS = new HashSet<>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                if (childBaseSetLoaded != null) {
                    baseValueFound = false;

                    for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                        if (processedUUIDS.contains(childBaseValueLoaded.getUuid()))
                            continue;

                        if (childBaseValueSaving.equalsByValue(childMetaValue, childBaseValueLoaded)) {
                            processedUUIDS.add(childBaseValueLoaded.getUuid());
                            baseValueFound = true;

                            int compareBaseValueRepDate = DataUtils.compareBeginningOfTheDay(
                                    childBaseValueSaving.getRepDate(), childBaseValueLoaded.getRepDate());

                            IBaseValue baseValueApplied;

                            if (compareBaseValueRepDate == -1) {
                                baseValueApplied = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        childBaseValueLoaded.getId(),
                                        creditorId,
                                        new Date(childBaseValueSaving.getRepDate().getTime()),
                                        returnCastedValue(childMetaValue, childBaseValueLoaded),
                                        childBaseValueLoaded.isClosed(),
                                        childBaseValueLoaded.isLast());

                                baseEntityManager.registerAsUpdated(baseValueApplied);
                            } else {
                                baseValueApplied = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        childBaseValueLoaded.getId(),
                                        creditorId,
                                        new Date(childBaseValueLoaded.getRepDate().getTime()),
                                        returnCastedValue(childMetaValue, childBaseValueLoaded),
                                        childBaseValueLoaded.isClosed(),
                                        childBaseValueLoaded.isLast());
                            }

                            childBaseSetApplied.put(baseValueApplied);
                            break;
                        }
                    }

                    if (baseValueFound)
                        continue;
                }

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);

                // Check closed value
                IBaseValue baseValueForSearch = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        0,
                        creditorId,
                        new Date(childBaseValueSaving.getRepDate().getTime()),
                        returnCastedValue(childMetaValue, childBaseValueSaving),
                        childBaseValueSaving.isClosed(),
                        childBaseValueSaving.isLast());

                baseValueForSearch.setBaseContainer(childBaseSetApplied);

                IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);

                if (childBaseValueClosed != null) {
                    childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsDeleted(childBaseValueClosed);

                    IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                    if (childBaseValuePrevious != null) {
                        if (childBaseValueClosed.isLast()) {
                            childBaseValuePrevious.setLast(true);

                            childBaseSetApplied.put(childBaseValuePrevious);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        } else {
                            childBaseSetApplied.put(childBaseValuePrevious);
                        }
                    } else {
                        throw new IllegalStateException(Errors.compose(Errors.E73, metaAttribute.getName()));
                    }

                    continue;
                }

                // Check next value
                IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueSaving);
                if (childBaseValueNext != null) {
                    childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                    childBaseSetApplied.put(childBaseValueNext);
                    baseEntityManager.registerAsUpdated(childBaseValueNext);
                    continue;
                }

                IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                if (childBaseValueLast != null) {
                    int compareValueRepDate = DataUtils.compareBeginningOfTheDay(childBaseValueSaving.getRepDate(),
                            childBaseValueLast.getRepDate());

                    if (compareValueRepDate == -1) {
                        IBaseValue childBaseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                0,
                                creditorId,
                                childBaseValueSaving.getRepDate(),
                                returnCastedValue(childMetaValue, childBaseValueSaving),
                                false,
                                false);

                        childBaseSetApplied.put(childBaseValueApplied);
                        baseEntityManager.registerAsInserted(childBaseValueApplied);
                    } else {
                        throw new IllegalStateException(Errors.compose(Errors.E74));
                    }

                    continue;
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        0,
                        creditorId,
                        childBaseValueSaving.getRepDate(),
                        returnCastedValue(childMetaValue, childBaseValueSaving),
                        false,
                        true);

                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }

        /* Удаляет элементы массива, если массив не накопительный или массив накопительный и родитель был удалён */
        if (childBaseSetLoaded != null &&
                ((metaAttribute.isCumulative() && isBaseSetDeleted) || !metaAttribute.isCumulative())) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUUIDS.contains(childBaseValueLoaded.getUuid()))
                    continue;

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = childBaseValueLoaded.getRepDate();

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == -1)
                    continue;

                if (compare == 0) {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);

                    if (childBaseValueLoaded.isLast()) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);

                        if (childBaseValuePrevious != null) {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                } else if (compare == 1) {
                    IBaseValue childBaseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            0,
                            creditorId,
                            baseValueSaving.getRepDate(),
                            returnCastedValue(childMetaValue, childBaseValueLoaded),
                            true,
                            childBaseValueLoaded.isLast());

                    childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                    baseEntityManager.registerAsInserted(childBaseValueClosed);

                    if (childBaseValueLoaded.isLast()) {
                        IBaseValue childBaseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                childBaseValueLoaded.getId(),
                                creditorId,
                                childBaseValueLoaded.getRepDate(),
                                childMetaValue.getTypeCode() == DataTypes.DATE ?
                                        new Date(((Date) childBaseValueLoaded.getValue()).getTime()) :
                                        childBaseValueLoaded.getValue(),
                                childBaseValueLoaded.isClosed(),
                                false);

                        childBaseValueLast.setBaseContainer(childBaseSetApplied);
                        baseEntityManager.registerAsUpdated(childBaseValueLast);
                    }
                }
            }
        }
    }

    @Override
    public void applyComplexSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                                IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {
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

        boolean isBaseSetDeleted = false;

        if (baseValueLoaded != null) {
            reportDateLoaded = baseValueLoaded.getRepDate();
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);
                reportDateSaving = baseValueSaving.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    // case#1
                    if (metaAttribute.isFinal()) {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueLoaded.getCreditorId(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast());

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

                        // delete next closed value
                        IBaseValueDao valueDao = persistableDaoPool
                                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                        IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            baseValueNext.setBaseContainer(baseEntity);
                            baseValueNext.setMetaAttribute(metaAttribute);

                            baseEntityManager.registerAsDeleted(baseValueNext);
                        }

                        isBaseSetDeleted = true;
                        // case#2
                    } else {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                baseValueLoaded.getCreditorId(),
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                childBaseSetLoaded,
                                true,
                                baseValueLoaded.isLast());

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

                        // delete next closed value
                        IBaseValueDao valueDao = persistableDaoPool
                                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                        IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

                        if (baseValueNext != null && baseValueNext.isClosed()) {
                            baseValueNext.setBaseContainer(baseEntity);
                            baseValueNext.setMetaAttribute(metaAttribute);

                            baseEntityManager.registerAsDeleted(baseValueNext);
                        }

                        isBaseSetDeleted = true;
                    }
                    // case#3
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException(Errors.compose(Errors.E66, metaAttribute.getName()));

                    IBaseValueDao valueDao = persistableDaoPool
                            .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                    IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

                    // check for next closed value
                    if (baseValueNext != null && baseValueNext.isClosed()) {
                        baseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseValueNext.setBaseContainer(baseEntity);
                        baseValueNext.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValueNext);
                    } else {
                        IBaseValue baseValueClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                baseValueSaving.getCreditorId(),
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
                                    baseValueLoaded.getCreditorId(),
                                    new Date(baseValueLoaded.getRepDate().getTime()),
                                    childBaseSetLoaded,
                                    baseValueLoaded.isClosed(),
                                    false);

                            baseValueLast.setBaseContainer(baseEntity);
                            baseValueLast.setMetaAttribute(metaAttribute);

                            baseEntityManager.registerAsUpdated(baseValueLast);
                        }
                    }

                    isBaseSetDeleted = true;
                }/* else if (compare == -1) {
                    throw new UnsupportedOperationException("Закрытие атрибута за прошлый период не является возможным"
                            + "( " + baseValueSaving.getMetaAttribute().getName() + ");");
                }*/
                // case#4
            } else {
                reportDateSaving = baseValueSaving.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (metaAttribute.isFinal() && compare != 0)
                    throw new IllegalStateException(Errors.compose(Errors.E67, metaAttribute.getName()));

                if (compare == 0 || compare == 1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getCreditorId(),
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntity);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                } else if (compare == -1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            baseValueLoaded.getCreditorId(),
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntity);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            }
        } else {
            if (childBaseSetSaving == null) {
                return;
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            reportDateSaving = baseValueSaving.getRepDate();

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = null;
            // case#5
            if (!metaAttribute.isFinal()) {
                baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving);

                if (baseValueClosed != null) {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    baseEntityManager.registerAsDeleted(baseValueClosed);

                    reportDateLoaded = baseValueClosed.getRepDate();

                    IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueClosed);

                    if (baseValuePrevious == null)
                        throw new IllegalStateException(Errors.compose(Errors.E68, metaAttribute.getName()));

                    baseValuePrevious.setBaseContainer(baseEntity);
                    baseValuePrevious.setMetaAttribute(metaAttribute);

                    childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
                    childBaseSetApplied = new BaseSet(baseValuePrevious.getId(), childMetaType, creditorId);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValuePrevious.getId(),
                            baseValuePrevious.getCreditorId(),
                            new Date(baseValuePrevious.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            true);

                    baseValueApplied.setBaseContainer(baseEntity);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }

                // case#6
                if (baseValueClosed == null) {
                    IBaseValue baseValueNext = baseValueDao.getNextBaseValue(baseValueSaving);

                    if (baseValueNext != null) {
                        reportDateLoaded = baseValueNext.getRepDate();

                        childBaseSetLoaded = (IBaseSet) baseValueNext.getValue();
                        childBaseSetApplied = new BaseSet(baseValueNext.getId(), childMetaType, creditorId);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueNext.getId(),
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                baseValueNext.isClosed(),
                                baseValueNext.isLast());

                        baseValueApplied.setBaseContainer(baseEntity);
                        baseValueApplied.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValueApplied);
                    } else {
                        childBaseSetApplied = new BaseSet(childMetaType, creditorId);
                        baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                true);

                        baseValueApplied.setBaseContainer(baseEntity);
                        baseValueApplied.setMetaAttribute(metaAttribute);

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
            } else {
                childBaseSetApplied = new BaseSet(childMetaType, creditorId);
                baseEntityManager.registerAsInserted(childBaseSetApplied);

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        0,
                        creditorId,
                        new Date(baseValueSaving.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true);

                baseValueApplied.setBaseContainer(baseEntity);
                baseValueApplied.setMetaAttribute(metaAttribute);

                baseEntity.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            }
        }

        Set<UUID> processedUUIDSet = new HashSet<>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                IBaseEntity childBaseEntitySaving = (IBaseEntity) childBaseValueSaving.getValue();

                if (childBaseSetLoaded != null) {
                    int compareDates = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                    if (!metaAttribute.isFinal() || (metaAttribute.isFinal() && compareDates == 0)) {
                        baseValueFound = false;

                        for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                            if (processedUUIDSet.contains(childBaseValueLoaded.getUuid()))
                                continue;

                            IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                            if (childBaseValueSaving.equals(childBaseValueLoaded) || childBaseEntitySaving.getId() == childBaseEntityLoaded.getId()) {
                                processedUUIDSet.add(childBaseValueLoaded.getUuid());
                                baseValueFound = true;

                                IBaseEntity baseEntityApplied = applyBaseEntityAdvanced(
                                        creditorId,
                                        childBaseEntitySaving,
                                        childBaseEntityLoaded,
                                        baseEntityManager);

                                IBaseValue baseValueApplied = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        childBaseValueLoaded.getId(),
                                        childBaseValueLoaded.getCreditorId(),
                                        new Date(childBaseValueLoaded.getRepDate().getTime()),
                                        baseEntityApplied,
                                        childBaseValueLoaded.isClosed(),
                                        childBaseValueLoaded.isLast());

                                childBaseSetApplied.put(baseValueApplied);

                                int compareValueDates = DataUtils.compareBeginningOfTheDay(childBaseValueSaving.getRepDate(), childBaseValueLoaded.getRepDate());

                                if (compareValueDates == -1) {
                                    baseValueApplied.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));
                                    baseEntityManager.registerAsUpdated(baseValueApplied);
                                }

                                break;
                            }
                        }

                        if (baseValueFound)
                            continue;
                    }
                }

                // Если значение было закрыто и оно не ключевое, элемент массива не будет идентифицирован.
                if (childBaseEntitySaving.getId() > 0) {
                    IBaseSetValueDao setValueDao = persistableDaoPool
                            .getPersistableDao(childBaseValueSaving.getClass(), IBaseSetValueDao.class);

                    if (!metaAttribute.isFinal()) {
                        IBaseValue baseValueForSearch = BaseValueFactory.create(
                                MetaContainerTypes.META_SET,
                                childMetaType,
                                0,
                                childBaseValueSaving.getCreditorId(),
                                new Date(childBaseValueSaving.getRepDate().getTime()),
                                childBaseValueSaving.getValue(),
                                childBaseValueSaving.isClosed(),
                                childBaseValueSaving.isLast());

                        baseValueForSearch.setBaseContainer(childBaseSetApplied);
                        baseValueForSearch.setMetaAttribute(metaAttribute);

                        IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueForSearch);

                        if (childBaseValueClosed != null) {
                            childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                            childBaseValueClosed.setMetaAttribute(metaAttribute);

                            baseEntityManager.registerAsDeleted(childBaseValueClosed);

                            // todo: UNQ_CONST
                            IBaseValue childBaseValuePrevious = childBaseValueClosed; //setValueDao.getPreviousBaseValue(childBaseValueClosed);

                            if (childBaseValuePrevious != null && childBaseValuePrevious.getValue() != null) {
                                childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                                childBaseValuePrevious.setMetaAttribute(metaAttribute);

                                IBaseEntity childBaseEntityPrevious = (IBaseEntity) childBaseValuePrevious.getValue();

                                childBaseValuePrevious.setValue(applyBaseEntityAdvanced(creditorId,
                                        childBaseEntitySaving, childBaseEntityPrevious, baseEntityManager));

                                if (childBaseValueClosed.isLast()) {
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

                            childBaseValueNext.setValue(applyBaseEntityAdvanced(creditorId, childBaseEntitySaving,
                                    childBaseEntityNext, baseEntityManager));
                            childBaseValueNext.setRepDate(new Date(childBaseValueSaving.getRepDate().getTime()));

                            childBaseSetApplied.put(childBaseValueNext);
                            baseEntityManager.registerAsUpdated(childBaseValueNext);
                            continue;
                        }
                    }
                }

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        0,
                        creditorId,
                        childBaseValueSaving.getRepDate(),
                        apply(creditorId, childBaseEntitySaving, null, baseEntityManager),
                        false,
                        true);

                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);
            }
        }

        /* Удаляет элементы массива, если массив не накопительный или массив накопительный и родитель был удалён */
        if (childBaseSetLoaded != null &&
                ((metaAttribute.isCumulative() && isBaseSetDeleted) || !metaAttribute.isCumulative())) {
            //одно закрытие на несколько одинаковых записей
            Set<Long> closedChildBaseEntityIds = new HashSet<>();

            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUUIDSet.contains(childBaseValueLoaded.getUuid()))
                    continue;

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, childBaseValueLoaded.getRepDate());

                if (compare == -1)
                    continue;

                if (compare == 0) {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);

                    IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                    if (childBaseEntityLoaded != null && !childMetaClass.isSearchable())
                        baseEntityManager.registerAsDeleted(childBaseEntityLoaded);

                    boolean last = childBaseValueLoaded.isLast();

                    if (!metaAttribute.isFinal()) {
                        IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);

                        if (childBaseValueNext != null && childBaseValueNext.isClosed()) {
                            baseEntityManager.registerAsDeleted(childBaseValueNext);
                            last = childBaseValueNext.isLast();
                        }
                    }

                    if (last && !(metaAttribute.isFinal() && !childMetaClass.isSearchable())) {
                        IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueLoaded);
                        if (childBaseValuePrevious != null) {
                            childBaseValuePrevious.setBaseContainer(childBaseSetApplied);
                            childBaseValuePrevious.setLast(true);
                            baseEntityManager.registerAsUpdated(childBaseValuePrevious);
                        }
                    }
                } else if (compare == 1) {
                    IBaseValue childBaseValueNext = setValueDao.getNextBaseValue(childBaseValueLoaded);

                    if (childBaseValueNext == null || !childBaseValueNext.isClosed()) {

                        /* Не идентифицируемый элемент массива не может быт закрыт если не указан как закрываемый*/
                        if (!childMetaClass.isSearchable() && !childMetaClass.isClosable()) {
                            childBaseSetApplied.put(childBaseValueLoaded);
                            continue;
                        }

                        long closedChildBaseEntityId = ((IBaseEntity) childBaseValueLoaded.getValue()).getId();
                        if(!closedChildBaseEntityIds.contains(closedChildBaseEntityId)) {
                            closedChildBaseEntityIds.add(closedChildBaseEntityId);

                            IBaseValue childBaseValueClosed = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    0,
                                    baseValueSaving.getCreditorId(),
                                    baseValueSaving.getRepDate(),
                                    childBaseValueLoaded.getValue(),
                                    true,
                                    childBaseValueLoaded.isLast());

                            childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsInserted(childBaseValueClosed);
                        }

                        if (childBaseValueLoaded.isLast()) {
                            IBaseValue childBaseValueLast = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    childMetaType,
                                    childBaseValueLoaded.getId(),
                                    childBaseValueLoaded.getCreditorId(),
                                    childBaseValueLoaded.getRepDate(),
                                    childBaseValueLoaded.getValue(),
                                    childBaseValueLoaded.isClosed(),
                                    false);
                            childBaseValueLast.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsUpdated(childBaseValueLast);
                        }
                    } else {
                        childBaseValueNext.setBaseContainer(childBaseSetApplied);
                        childBaseValueNext.setRepDate(baseValueSaving.getRepDate());

                        baseEntityManager.registerAsUpdated(childBaseValueNext);
                    }
                }
            }
        }

        /* Обработка накопительных массивов для витрин */
        if (metaAttribute.isCumulative() && !isBaseSetDeleted && childBaseSetLoaded != null) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUUIDSet.contains(childBaseValueLoaded.getUuid()))
                    continue;

                if (childBaseSetApplied != null) childBaseSetApplied.put(childBaseValueLoaded);
            }
        }
    }

    @Override
    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager) {
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

                        if(insertedObject instanceof BaseEntityReportDate) {
                            IBaseEntity baseEntity = ((BaseEntityReportDate) insertedObject).getBaseEntity();
                            if(baseEntity.getMeta().isReference())
                                refRepository.invalidate(baseEntity);
                        }

                    } catch (Exception insertException) {
                        throw new IllegalStateException(Errors.compose(Errors.E76, insertedObject, insertException.getMessage()));
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

                        if(updatedObject instanceof BaseEntityReportDate) {
                            IBaseEntity baseEntity = ((BaseEntityReportDate) updatedObject).getBaseEntity();
                            if(baseEntity.getMeta().isReference())
                                refRepository.invalidate(baseEntity);
                        }

                    } catch (Exception updateException) {
                        throw new IllegalStateException(Errors.compose(Errors.E77, updatedObject, updateException.getMessage()));
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

                        if(deletedObject instanceof IBaseEntity) {
                            if(((IBaseEntity) deletedObject).getMeta().isReference())
                                refRepository.invalidate(((IBaseEntity) deletedObject));
                        }
                    } catch (Exception deleteException) {
                        throw new IllegalStateException(Errors.compose(Errors.E78, deletedObject, deleteException.getMessage()));
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
    }

    private Object returnCastedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) baseValue.getValue()).getTime()) : baseValue.getValue();
    }
}
