package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.tool.optimizer.EavOptimizerData;
import kz.bsbnb.usci.eav.tool.optimizer.impl.BasicOptimizer;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private IRefRepository refRepositoryDao;

    @Autowired
    private IEavOptimizerDao eavOptimizerDao;

    @Value("${refs.cache.enabled}")
    private boolean isReferenceCacheEnabled;

    @Override
    public IBaseEntity apply(long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded,
                             IBaseEntityManager baseEntityManager, EntityHolder entityHolder) {
        IBaseEntity baseEntityApplied;

        // Новые сущности или сущности не имеющие ключевые атрибуты
        if (baseEntitySaving.getId() < 1 || !baseEntitySaving.getMeta().isSearchable()) {
            baseEntityApplied = applyBaseEntityBasic(creditorId, baseEntitySaving, baseEntityManager);
        } else {
            if (baseEntityLoaded == null) {
                Date reportDateSaving = baseEntitySaving.getReportDate();

                IBaseEntityReportDateDao baseEntityReportDateDao =
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

                // Получение максимальной отчетной даты из прошедших периодов
                Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(baseEntitySaving.getId(), reportDateSaving);

                if (maxReportDate == null) {
                    // Получение минимальной отчетной даты из будущих периодов
                    Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntitySaving.getId(),
                            reportDateSaving);

                    if (minReportDate == null)
                        throw new UnsupportedOperationException("Найденный объект (" + baseEntitySaving.getId()
                                + ") не имеет отчетный даты;");

                    baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), minReportDate,
                                reportDateSaving);

                    if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                        throw new UnsupportedOperationException("Запись с ID(" + baseEntityLoaded.getId() +
                                ") является закрытой с даты " + baseEntityLoaded.getBaseEntityReportDate().getReportDate()
                                + ". Обновление после закрытия сущностей не является возможным;");

                } else {
                    baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), maxReportDate, reportDateSaving);

                    if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                        throw new UnsupportedOperationException("Запись с ID(" + baseEntityLoaded.getId() +
                                ") является закрытой с даты " + baseEntityLoaded.getBaseEntityReportDate().getReportDate()
                                + ". Обновление после закрытия сущностей не является возможным;");

                }
            }

            baseEntityApplied = applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded,
                    baseEntityManager);
        }

        if (entityHolder != null) {
            // entityHolder.setSaving(baseEntitySaving);
            entityHolder.setLoaded(baseEntityLoaded);
            entityHolder.setApplied(baseEntityApplied);
        }

        return baseEntityApplied;
    }

    @Override
    public IBaseEntity applyBaseEntityBasic(long creditorId, IBaseEntity baseEntitySaving,
                                            IBaseEntityManager baseEntityManager) {
        IBaseEntity foundProcessedBaseEntity = baseEntityManager.getProcessed(baseEntitySaving);

        if (foundProcessedBaseEntity != null)
            return foundProcessedBaseEntity;

        IBaseEntity baseEntityApplied = new BaseEntity(baseEntitySaving.getMeta(), baseEntitySaving.getReportDate());

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
    public void applyBaseValueBasic(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValue,
                                    IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValue.getMetaAttribute();
        if (metaAttribute == null)
            throw new IllegalStateException("Атрибут должен содержать мета данные;");

        IBaseContainer baseContainer = baseValue.getBaseContainer();
        if (baseContainer != null && baseContainer.getBaseContainerType() != BaseContainerType.BASE_ENTITY)
            throw new IllegalStateException("Родитель атрибута(" + metaAttribute.getName() + ") должна быть сущность;");

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Поддержка массив массивов не реализовано;");

                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                    if (metaAttribute.isImmutable() && childBaseEntity.getValueCount() != 0 &&
                            childBaseEntity.getId() < 1)
                        throw new KnownException("Запись класса " + childBaseEntity.getMeta().
                                getClassName() + " не найдена;" + "\n" + childBaseEntity.toString());

                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null,
                            baseEntityManager, null);

                    IBaseValue childBaseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaSet.getMemberType(),
                            0,
                            creditorId,
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
                        0,
                        creditorId,
                        new Date(baseValue.getRepDate().getTime()),
                        childBaseSetApplied,
                        false,
                        true);

                baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                baseEntityManager.registerAsInserted(baseValueApplied);
            } else {
                if (metaAttribute.isImmutable()) {
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();

                    if (childBaseEntity.getValueCount() != 0) {
                        if (childBaseEntity.getId() < 1)
                            throw new KnownException("Запись класса " +
                                    childBaseEntity.getMeta().getClassName() + " не найдена;" +
                                    "\n" + childBaseEntity.toString());

                        IBaseEntity childBaseEntityImmutable = baseEntityLoadDao.loadByMaxReportDate(
                                childBaseEntity.getId(), childBaseEntity.getReportDate());

                        if (childBaseEntityImmutable == null)
                            throw new RuntimeException("В базе нет данных для записи(" + childBaseEntity.getId()
                                    + ") до отчетной даты(включительно): " + childBaseEntity.getReportDate() + ";");

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValue.getRepDate().getTime()),
                                childBaseEntityImmutable,
                                false,
                                true);

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    } else {
                        throw new IllegalStateException("Комплексный элелемент не содержит внутренних элементов(" +
                                childBaseEntity.getMeta().getClassName() + ");");
                    }
                } else {
                    IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, null,
                            baseEntityManager, null);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValue.getRepDate().getTime()),
                            childBaseEntityApplied,
                            false,
                            true);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        } else {
            if (metaType.isSet()) {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Не реализовано;");

                IMetaSet childMetaSet = (IMetaSet) metaType;
                IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();
                IMetaValue metaValue = (IMetaValue) childMetaSet.getMemberType();

                IBaseSet childBaseSetApplied = new BaseSet(childMetaSet.getMemberType());
                for (IBaseValue childBaseValue : childBaseSet.get()) {
                    IBaseValue childBaseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaSet.getMemberType(),
                            0,
                            creditorId,
                            new Date(baseValue.getRepDate().getTime()),
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
                        new Date(baseValue.getRepDate().getTime()),
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
                        new Date(baseValue.getRepDate().getTime()),
                        returnCastedValue(metaValue, baseValue),
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

        if (baseEntitySaving.equals(baseEntityLoaded))
            return baseEntityLoaded;

        // Устанавливает ID для !metaClass.isSearchable()
        if (baseEntitySaving.getId() < 1 && baseEntityLoaded.getId() > 0)
            baseEntitySaving.setId(baseEntityLoaded.getId());

        for (String attribute : metaClass.getAttributeNames()) {
            IBaseValue baseValueSaving = baseEntitySaving.getBaseValue(attribute);
            IBaseValue baseValueLoaded = baseEntityLoaded.getBaseValue(attribute);

            if (baseValueSaving == null)
                continue;

            IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
            if (metaAttribute == null)
                throw new RuntimeException("Атрибут должен иметь мета данные;");

            IBaseContainer baseContainerSaving = baseValueSaving.getBaseContainer();
            if (baseContainerSaving != null && baseContainerSaving.getBaseContainerType()
                    != BaseContainerType.BASE_ENTITY)
                throw new RuntimeException("Родитель атрибута(" + baseValueSaving.getMetaAttribute().getName()
                        + ") должна быть сущность;");

            IMetaType metaType = metaAttribute.getMetaType();

            if (metaType.isComplex()) {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Не реализовано;");

                if (metaType.isSet())
                    applyComplexSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded,
                            baseEntityManager);
                else
                    applyComplexValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded,
                            baseEntityManager);
            } else {
                if (metaType.isSetOfSets())
                    throw new UnsupportedOperationException("Не реализовано;");

                if (metaType.isSet())
                    applySimpleSet(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded,
                            baseEntityManager);
                else
                    applySimpleValue(creditorId, baseEntityApplied, baseValueSaving, baseValueLoaded,
                            baseEntityManager);
            }
        }

        int compare = DataUtils.compareBeginningOfTheDay(baseEntitySaving.getReportDate(),
                baseEntityLoaded.getReportDate());

        if (compare == 0 || compare == 1) {
            baseEntityApplied.calculateValueCount(baseEntityLoaded);
        } else {
            baseEntityApplied.calculateValueCount(null);
        }

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();

        if (baseEntityReportDateDao.exists(baseEntityApplied.getId(), baseEntityApplied.getReportDate())) {
            baseEntityManager.registerAsUpdated(baseEntityReportDate);
        } else {
            baseEntityManager.registerAsInserted(baseEntityReportDate);
        }

        baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);
        return baseEntityApplied;
    }

    @Override
    public void applySimpleValue(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValueSaving,
                                 IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaValue metaValue = (IMetaValue) metaType;

        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    // case #1
                    if (metaAttribute.isFinal()) {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                returnCastedValue(metaValue, baseValueLoaded),
                                baseValueLoaded.isClosed(),
                                baseValueLoaded.isLast());

                        baseValueDeleted.setBaseContainer(baseEntityApplied);
                        baseValueDeleted.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);

                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntityApplied);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(baseValuePrevious);
                            }
                        }
                        // case#2
                    } else {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                returnCastedValue(metaValue, baseValueLoaded),
                                true,
                                baseValueLoaded.isLast());

                        baseValueDeleted.setBaseContainer(baseEntityApplied);
                        baseValueDeleted.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);

                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntityApplied);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setLast(true);
                                baseEntityManager.registerAsUpdated(baseValuePrevious);
                            }
                        }
                    }
                    // case#3
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException("Оперативные данные могут быть закрыты только за " +
                                "существующий отчетный период(" + metaAttribute.getName() + ");");

                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                returnCastedValue(metaValue, baseValueLoaded),
                                baseValueLoaded.isClosed(),
                                false);

                        baseValueLast.setBaseContainer(baseEntityApplied);
                        baseValueLast.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueLoaded),
                            true,
                            true);

                    baseValueClosed.setBaseContainer(baseEntityApplied);
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    baseEntityManager.registerAsInserted(baseValueClosed);
                } else {
                    throw new UnsupportedOperationException("Закрытие атрибута за прошлый период не является " +
                            "возможным. " + baseValueSaving.getMetaAttribute().getName() + ";");
                }

                return;
            }

            if (baseValueSaving.equalsByValue(baseValueLoaded)) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // Именение ключевых полей
                // <tag operation="new" data="new_value">old_value</tag>
                // case#4
                Object baseV;
                if (baseValueSaving.getNewBaseValue() != null) {
                    baseV = baseValueSaving.getNewBaseValue().getValue();
                } else {
                    baseV = returnCastedValue(metaValue, baseValueLoaded);
                }

                // case#5
                if (compare == 0 || compare == 1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseV,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntityApplied);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);

                    // Запуск на изменение ключевого поля
                    if (baseValueSaving.getNewBaseValue() != null)
                        baseEntityManager.registerAsUpdated(baseValueApplied);
                    // case#6
                } else if (compare == -1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseV,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntityApplied);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // case#7
                if (compare == 0) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueSaving),
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntityApplied);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                    // case#8
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException("Оперативные данные(" + metaAttribute.getName() + ")" +
                                " могут изменятся только за существующие периоды;");

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueSaving),
                            false,
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntityApplied);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                returnCastedValue(metaValue, baseValueLoaded),
                                baseValueLoaded.isClosed(),
                                false);

                        baseValuePrevious.setBaseContainer(baseEntityApplied);
                        baseValuePrevious.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                    // case#9
                } else if (compare == -1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueSaving),
                            false,
                            false);

                    baseValueApplied.setBaseContainer(baseEntityApplied);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        } else {
            if (baseValueSaving.getValue() == null) {
                return;
                // TODO: uncomment
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            if (!metaAttribute.isFinal()) {
                IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);

                // case#10
                if (baseValueClosed != null) {
                    baseValueClosed.setMetaAttribute(metaAttribute);
                    baseValueClosed.setBaseContainer(baseEntityApplied);

                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseEntityManager.registerAsDeleted(baseValueClosed);

                        IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                        if (baseValuePrevious == null)
                            throw new IllegalStateException("Предыдущая запись не найдена(" +
                                    baseValueClosed.getMetaAttribute().getName() + ");");

                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseValuePrevious.setBaseContainer(baseEntityApplied);
                        baseValuePrevious.setLast(true);

                        baseEntityApplied.put(metaAttribute.getName(), baseValuePrevious);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    } else {
                        baseValueClosed.setValue(returnCastedValue(metaValue, baseValueSaving));
                        baseValueClosed.setClosed(false);

                        baseEntityApplied.put(metaAttribute.getName(), baseValueClosed);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                    // case#11
                } else {
                    IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueSaving);

                    if (baseValueNext != null) {
                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueNext.getId(),
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                returnCastedValue(metaValue, baseValueSaving),
                                baseValueNext.isClosed(),
                                baseValueNext.isLast());

                        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsUpdated(baseValueApplied);
                    } else {
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
            } else {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);

                // case#12
                if (baseValueLast == null) {
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
                    // case#13
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();

                    boolean last = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLast) != -1;

                    if (last) {
                        baseValueLast.setBaseContainer(baseEntityApplied);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueSaving),
                            false,
                            last);

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                }
            }
        }
    }

    @Override
    public void applyComplexValue(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueSaving,
                                  IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {
        IMetaAttribute metaAttribute = baseValueSaving.getMetaAttribute();
        IMetaType metaType = metaAttribute.getMetaType();
        IMetaClass metaClass = (IMetaClass) metaType;

        if (baseValueLoaded != null) {
            if (baseValueSaving.getValue() == null) {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    // case#1
                    if (metaAttribute.isFinal()) {
                        if (metaClass.hasNotFinalAttributes() && !metaClass.isSearchable())
                            throw new IllegalStateException("Оперативные атрибуты могут сожержать только оперативные "
                                    + "данные. Мета: " + baseEntity.getMeta().getClassName()
                                    + ", атрибут: " + metaAttribute.getName());

                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
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

                        if (!metaClass.isSearchable() && !metaAttribute.isImmutable()) {
                            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

                            IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded,
                                    baseValueSaving.getRepDate());

                            for (String attributeName : metaClass.getAttributeNames()) {
                                IMetaAttribute childMetaAttribute = metaClass.getMetaAttribute(attributeName);
                                IMetaType childMetaType = childMetaAttribute.getMetaType();

                                baseEntitySaving.put(attributeName,
                                        BaseValueFactory.create(
                                                MetaContainerTypes.META_CLASS,
                                                childMetaType,
                                                0,
                                                creditorId,
                                                new Date(baseValueSaving.getRepDate().getTime()),
                                                null,
                                                false,
                                                true));
                            }
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);

                            IBaseEntityComplexValueDao baseEntityComplexValueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseEntityComplexValueDao.class);

                            boolean singleBaseValue = baseEntityComplexValueDao.isSingleBaseValue(baseValueLoaded);

                            if (singleBaseValue) {
                                baseEntityManager.registerAsDeleted(baseEntityLoaded);
                            }
                        }

                        return;
                        // case#2
                    } else {
                        IBaseValue baseValueDeleted = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
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

                        if (!metaClass.isSearchable() && !metaAttribute.isImmutable()) {
                            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

                            IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded,
                                    baseValueSaving.getRepDate());

                            for (String attributeName : metaClass.getAttributeNames()) {
                                IMetaAttribute childMetaAttribute = metaClass.getMetaAttribute(attributeName);
                                IMetaType childMetaType = childMetaAttribute.getMetaType();

                                baseEntitySaving.put(attributeName,
                                        BaseValueFactory.create(
                                                MetaContainerTypes.META_CLASS,
                                                childMetaType,
                                                0,
                                                creditorId,
                                                new Date(baseValueSaving.getRepDate().getTime()),
                                                null,
                                                false,
                                                true));
                            }
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);

                            IBaseEntityComplexValueDao baseEntityComplexValueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseEntityComplexValueDao.class);

                            boolean singleBaseValue = baseEntityComplexValueDao.isSingleBaseValue(baseValueLoaded);

                            if (singleBaseValue) {
                                baseEntityManager.registerAsDeleted(baseEntityLoaded);
                            }
                        }
                    }
                    // case#3
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException("Оперативные данные могут быть закрыты только за " +
                                "существующий отчетный период(" + metaAttribute.getName() + ");");

                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValueLast = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
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
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseValueLoaded.getValue(),
                            true,
                            true);

                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    baseEntityManager.registerAsInserted(baseValueClosed);
                } else if (compare == -1) {
                    throw new UnsupportedOperationException("Закрытие атрибута за прошлый период не является возможным"
                            + ". " + baseValueSaving.getMetaAttribute().getName() + ";");
                }

                return;
            }

            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

            // case#4
            if (baseEntitySaving.getId() == baseEntityLoaded.getId() || !metaClass.isSearchable()) {
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new KnownException("Запись класса " + baseEntitySaving.getMeta().
                                getClassName() + " не найдена;" + "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager, null) :
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                int compare = DataUtils.compareBeginningOfTheDay(baseValueSaving.getRepDate(),
                        baseValueLoaded.getRepDate());

                IBaseValue baseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_CLASS,
                        metaType,
                        baseValueLoaded.getId(),
                        creditorId,
                        compare == -1 ? new Date(baseValueSaving.getRepDate().getTime()) :
                                new Date(baseValueLoaded.getRepDate().getTime()),
                        baseEntityApplied,
                        baseValueLoaded.isClosed(),
                        baseValueLoaded.isLast());

                baseValueApplied.setBaseContainer(baseEntity);
                baseValueApplied.setMetaAttribute(metaAttribute);

                baseEntity.put(metaAttribute.getName(), baseValueApplied);

                if (compare == -1)
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                // case#5
            } else {
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new KnownException("Запись класса " +
                                baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager, null);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueLoaded.getId(),
                            creditorId,
                            new Date(baseValueLoaded.getRepDate().getTime()),
                            baseEntityApplied,
                            baseValueLoaded.isClosed(),
                            baseValueLoaded.isLast());

                    baseValueApplied.setBaseContainer(baseEntity);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException("Оперативные данные(" + metaAttribute.getName() + ")" +
                                " могут изменятся только за существующие периоды;");

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            true);

                    baseValueApplied.setBaseContainer(baseEntity);
                    baseValueApplied.setMetaAttribute(metaAttribute);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValueLoaded.isLast()) {
                        IBaseValue baseValuePrevious = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                baseValueLoaded.getValue(),
                                baseValueLoaded.isClosed(),
                                false);

                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);

                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                } else if (compare == -1) {
                    if (metaAttribute.isFinal())
                        throw new RuntimeException("Оперативные данные(" + metaAttribute.getName() + ")" +
                                " могут изменятся только за существующие периоды;");

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            false);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                }
            }
        } else {
            IBaseEntity baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
            if (baseEntitySaving == null) {
                return;
                // TODO: uncomment
                /*throw new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                        baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");*/
            }

            IBaseValueDao valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            // case#6
            if (!metaAttribute.isFinal()) {
                IBaseValue baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);

                if (baseValueClosed != null) {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    IBaseEntity baseEntityClosed = (IBaseEntity) baseValueClosed.getValue();

                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseEntityManager.registerAsDeleted(baseValueClosed);

                        IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                        if (baseValuePrevious == null)
                            throw new IllegalStateException("Предыдущая запись не найдена(" +
                                    baseValueClosed.getMetaAttribute().getName() + ");");

                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);

                        IBaseEntity baseEntityApplied;
                        if (metaAttribute.isImmutable()) {
                            if (baseEntitySaving.getId() < 1)
                                throw new KnownException("Запись класса " +
                                        baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                        "\n" + baseEntitySaving.toString());

                            baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, null, baseEntityManager, null) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed,
                                            baseEntityManager);
                        }

                        baseValuePrevious.setValue(baseEntityApplied);

                        if (baseValueClosed.isLast()) {
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
                                throw new KnownException("Запись класса " +
                                        baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                        "\n" + baseEntitySaving.toString());

                            baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, baseEntityClosed, baseEntityManager, null) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed,
                                            baseEntityManager);
                        }

                        baseValueClosed.setValue(baseEntityApplied);
                        baseValueClosed.setClosed(false);

                        baseEntity.put(metaAttribute.getName(), baseValueClosed);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                } else {
                    IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueSaving);

                    if (metaAttribute.isImmutable())
                        throw new IllegalStateException("Запись класс " + metaAttribute.getName() + " не найдена;");

                    if (baseValueNext != null) {
                        baseValueNext.setBaseContainer(baseEntity);
                        baseValueNext.setMetaAttribute(metaAttribute);

                        IBaseEntity baseEntityApplied = metaClass.isSearchable() ?
                                apply(creditorId, baseEntitySaving, (IBaseEntity)
                                        baseValueNext.getValue(), baseEntityManager, null) :
                                applyBaseEntityAdvanced(creditorId, baseEntitySaving, (IBaseEntity)
                                        baseValueNext.getValue(), baseEntityManager);

                        baseValueNext.setRepDate(baseValueSaving.getRepDate());
                        baseValueNext.setValue(baseEntityApplied);

                        baseEntity.put(metaAttribute.getName(), baseValueNext);
                        baseEntityManager.registerAsUpdated(baseValueNext);

                    } else {
                        IBaseEntity baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager, null);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                baseEntityApplied,
                                false,
                                true);

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);
                    }
                }
                // case#7
            } else {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new KnownException("Запись класса " + baseEntitySaving.getMeta().
                                getClassName() + " не найдена;" + "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    IBaseValue previousBaseValue = valueDao.getPreviousBaseValue(baseValueSaving);

                    baseEntityApplied = apply(creditorId, baseEntitySaving, null, baseEntityManager, null);

                    if (previousBaseValue != null && previousBaseValue.isLast()) {
                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                previousBaseValue.getId(),
                                creditorId,
                                new Date(previousBaseValue.getRepDate().getTime()),
                                previousBaseValue.getValue(),
                                previousBaseValue.isClosed(),
                                false);

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsUpdated(baseValueApplied);
                    }
                }

                if (baseValueLast == null) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            true);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
                } else {
                    Date reportDateSaving = baseValueSaving.getRepDate();
                    Date reportDateLast = baseValueLast.getRepDate();

                    boolean last = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLast) != -1;

                    if (last) {
                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseValueLast.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            baseEntityApplied,
                            false,
                            last);

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);
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
            throw new UnsupportedOperationException("Не реализовано;");

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
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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

                    isBaseSetDeleted = true;
                    // case#2
                } else if (compare == 1) {
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
                    isBaseSetDeleted = true;
                } else {
                    throw new IllegalStateException("Дата закрытия атрибута(" + metaAttribute.getName() + ") должна "
                            + "быть больше или равна дате открытия атрибута;");
                }
            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                // case#3
                if (compare == 0 || compare == 1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                // TODO: uncomment
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
                    throw new IllegalStateException("Предыдущая запись не найдена(" +
                            baseValueClosed.getMetaAttribute().getName() + ");");

                baseValuePrevious.setBaseContainer(baseValueSaving.getBaseContainer());
                baseValuePrevious.setMetaAttribute(baseValueSaving.getMetaAttribute());

                childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                    childBaseSetApplied = new BaseSet(childMetaType);
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

        Set<UUID> processedUuids = new HashSet<>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                if (childBaseSetLoaded != null) {
                    baseValueFound = false;

                    for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                        if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                            continue;

                        if (childBaseValueSaving.equalsByValue(childMetaValue, childBaseValueLoaded)) {
                            processedUuids.add(childBaseValueLoaded.getUuid());
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
                        throw new IllegalStateException("Запись открытия не была найдена(" +
                                metaAttribute.getName() + ");");
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
                        throw new IllegalStateException("Last значение выгружено неправильно;");
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
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
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
        List<IBaseValue> savedDocTypes = new ArrayList<>();

        if (baseValueLoaded != null) {
            reportDateLoaded = baseValueLoaded.getRepDate();
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                reportDateSaving = baseValueSaving.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    // case#1
                    if (metaAttribute.isFinal()) {
                        if (!childMetaClass.isSearchable() && childMetaClass.hasNotFinalAttributes())
                            throw new IllegalStateException("Оперативные атрибуты могут сожержать только оперативные "
                                    + "данные. Мета: " + baseEntity.getMeta().getClassName() + ", атрибут: " +
                                    metaAttribute.getName());

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

                        if (!metaAttribute.isImmutable() && !childMetaClass.isSearchable()) {
                            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                                IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                                IBaseEntity childBaseEntitySaving = new BaseEntity(childBaseEntityLoaded,
                                        baseValueSaving.getRepDate());

                                for (String attributeName : childMetaClass.getAttributeNames()) {
                                    childBaseEntitySaving.put(attributeName,
                                            BaseValueFactory.create(
                                                    MetaContainerTypes.META_CLASS,
                                                    childMetaType,
                                                    0,
                                                    baseValueSaving.getCreditorId(),
                                                    new Date(baseValueSaving.getRepDate().getTime()),
                                                    null,
                                                    false,
                                                    true));
                                }
                                applyBaseEntityAdvanced(creditorId, childBaseEntitySaving, childBaseEntityLoaded,
                                        baseEntityManager);

                                IBaseSetComplexValueDao baseSetComplexValueDao = persistableDaoPool
                                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetComplexValueDao.class);

                                boolean singleBaseValue = baseSetComplexValueDao.isSingleBaseValue(childBaseValueLoaded);

                                if (singleBaseValue)
                                    baseEntityManager.registerAsDeleted(childBaseEntityLoaded);
                            }
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

                        if (!metaAttribute.isImmutable() && !childMetaClass.isSearchable()) {
                            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                                IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                                IBaseEntity childBaseEntitySaving = new BaseEntity(childBaseEntityLoaded,
                                        baseValueSaving.getRepDate());

                                for (String attributeName : childMetaClass.getAttributeNames()) {
                                    childBaseEntitySaving.put(attributeName,
                                            BaseValueFactory.create(
                                                    MetaContainerTypes.META_CLASS,
                                                    childMetaType,
                                                    0,
                                                    baseValueSaving.getCreditorId(),
                                                    new Date(baseValueSaving.getRepDate().getTime()),
                                                    null,
                                                    false,
                                                    true));
                                }
                                applyBaseEntityAdvanced(creditorId, childBaseEntitySaving, childBaseEntityLoaded,
                                        baseEntityManager);

                                IBaseSetComplexValueDao baseSetComplexValueDao = persistableDaoPool
                                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetComplexValueDao.class);

                                boolean singleBaseValue = baseSetComplexValueDao.isSingleBaseValue(childBaseValueLoaded);

                                if (singleBaseValue)
                                    baseEntityManager.registerAsDeleted(childBaseEntityLoaded);
                            }
                        }

                        isBaseSetDeleted = true;
                    }
                    // case#3
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException("Оперативные данные могут быть закрыты только за " +
                                "существующий отчетный период(" + metaAttribute.getName() + ");");

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

                    isBaseSetDeleted = true;
                } else if (compare == -1) {
                    throw new UnsupportedOperationException("Закрытие атрибута за прошлый период не является возможным"
                            + "( " + baseValueSaving.getMetaAttribute().getName() + ");");
                }
                // case#4
            } else {
                reportDateSaving = baseValueSaving.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (metaAttribute.isFinal() && compare != 0)
                    throw new IllegalStateException("Оперативные данные выгружены неправильно(" +
                            metaAttribute.getName() + "); ");

                if (compare == 0 || compare == 1) {
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
                // TODO: uncomment
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

                    IBaseValue baseValueDeleted = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueClosed.getId(),
                            baseValueClosed.getCreditorId(),
                            new Date(baseValueClosed.getRepDate().getTime()),
                            childBaseSetApplied,
                            baseValueClosed.isClosed(),
                            baseValueClosed.isLast());

                    baseValueDeleted.setBaseContainer(baseEntity);
                    baseValueDeleted.setMetaAttribute(metaAttribute);

                    baseEntityManager.registerAsDeleted(baseValueDeleted);

                    reportDateLoaded = baseValueClosed.getRepDate();

                    IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueClosed);

                    if (baseValuePrevious == null)
                        throw new IllegalStateException("Предыдущая запись не была найдена(" +
                                metaAttribute.getName() + ");");

                    baseValuePrevious.setBaseContainer(baseEntity);
                    baseValuePrevious.setMetaAttribute(metaAttribute);

                    childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

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
            }

            // case#6
            if (baseValueClosed == null) {
                IBaseValue baseValueNext = baseValueDao.getNextBaseValue(baseValueSaving);

                if (baseValueNext != null) {
                    reportDateLoaded = baseValueNext.getRepDate();

                    childBaseSetLoaded = (IBaseSet) baseValueNext.getValue();
                    childBaseSetApplied = new BaseSet(baseValueNext.getId(), childMetaType);

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
                    childBaseSetApplied = new BaseSet(childMetaType);
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
        }

        Set<UUID> processedUuids = new HashSet<>();
        if (childBaseSetSaving != null && childBaseSetSaving.getValueCount() > 0) {
            boolean baseValueFound;

            for (IBaseValue childBaseValueSaving : childBaseSetSaving.get()) {
                IBaseEntity childBaseEntitySaving = (IBaseEntity) childBaseValueSaving.getValue();

                if (childBaseSetLoaded != null) {
                    int compareDates = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                    if (!metaAttribute.isFinal() || (metaAttribute.isFinal() && compareDates == 0)) {
                        baseValueFound = false;

                        for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                            if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                                continue;

                            IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                            if (childBaseValueSaving.equals(childBaseValueLoaded)) {
                                processedUuids.add(childBaseValueLoaded.getUuid());
                                baseValueFound = true;

                                IBaseValue baseValueApplied = BaseValueFactory.create(
                                        MetaContainerTypes.META_SET,
                                        childMetaType,
                                        childBaseValueLoaded.getId(),
                                        childBaseValueLoaded.getCreditorId(),
                                        new Date(childBaseValueLoaded.getRepDate().getTime()),
                                        applyBaseEntityAdvanced(creditorId, childBaseEntitySaving,
                                                childBaseEntityLoaded, baseEntityManager),
                                        childBaseValueLoaded.isClosed(),
                                        childBaseValueLoaded.isLast());

                                childBaseSetApplied.put(baseValueApplied);

                                int compareValueDates = DataUtils.compareBeginningOfTheDay(
                                        childBaseValueSaving.getRepDate(), childBaseValueLoaded.getRepDate());

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

                // TODO:  Если значение было закрыто и оно не ключевое, элемент массива не будет идентифицирован.
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

                            IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);

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
                        apply(creditorId, childBaseEntitySaving, null, baseEntityManager, null),
                        false,
                        true);

                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);

                /* Сохранение документов по типу для последующей обработки */
                if (childBaseEntitySaving.getMeta().getClassName().equals("document"))
                    savedDocTypes.add(childBaseValueSaving);
            }
        }

        /* Удаляет элементы массива, если массив не накопительный или массив накопительный и родитель был удалён */
        if (childBaseSetLoaded != null &&
                ((metaAttribute.isCumulative() && isBaseSetDeleted) || !metaAttribute.isCumulative())) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                    continue;

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == -1)
                    continue;

                if (compare == 0) {
                    baseEntityManager.registerAsDeleted(childBaseValueLoaded);

                    IBaseEntity childBaseEntityLoaded = (IBaseEntity) childBaseValueLoaded.getValue();

                    if (childBaseEntityLoaded != null)
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

        /* Обработка документов по типу*/
        if(savedDocTypes.size() > 0 && childBaseSetLoaded != null) {
            for (IBaseValue savedValue : savedDocTypes) {
                for (IBaseValue loadedValue : childBaseSetLoaded.get()) {
                    BaseEntity savedDocument = (BaseEntity) savedValue.getValue();
                    BaseEntity loadedDocument = (BaseEntity) loadedValue.getValue();

                    BaseEntity savedDocType = (BaseEntity) savedDocument.getBaseValue("doc_type").getValue();
                    BaseEntity loadedDocType = (BaseEntity) loadedDocument.getBaseValue("doc_type").getValue();

                    if (savedDocType.equals(loadedDocType)) {
                        int compare = DataUtils.compareBeginningOfTheDay(savedValue.getRepDate(),
                                loadedValue.getRepDate());

                        if (compare == 0) {
                            IBaseValue deletedDocument = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    loadedDocument.getMeta(),
                                    loadedValue.getId(),
                                    creditorId,
                                    loadedValue.getRepDate(),
                                    loadedDocument,
                                    loadedValue.isClosed(),
                                    loadedValue.isLast());

                            deletedDocument.setBaseContainer(loadedValue.getBaseContainer());
                            baseEntityManager.registerAsDeleted(deletedDocument);

                            /* Для удаления из витрин */
                            loadedDocument.setOperation(OperationType.DELETE);
                            childBaseSetApplied.put(deletedDocument);
                        } else if (compare == 1) {
                            IBaseValue closedDocument = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    loadedDocument.getMeta(),
                                    0,
                                    creditorId,
                                    savedValue.getRepDate(),
                                    loadedDocument,
                                    true,
                                    true);

                            closedDocument.setBaseContainer(loadedValue.getBaseContainer());
                            baseEntityManager.registerAsInserted(closedDocument);

                            /* Для закрытия записи в витринах*/
                            loadedDocument.setOperation(OperationType.CLOSE);
                            childBaseSetApplied.put(closedDocument);
                        } else if (compare == -1) {
                            IBaseValue closedDocument = BaseValueFactory.create(
                                    MetaContainerTypes.META_SET,
                                    savedDocument.getMeta(),
                                    0,
                                    creditorId,
                                    loadedValue.getRepDate(),
                                    savedDocument,
                                    true,
                                    true);

                            closedDocument.setBaseContainer(loadedValue.getBaseContainer());
                            baseEntityManager.registerAsInserted(closedDocument);

                            savedDocument.setOperation(OperationType.CLOSE);
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void applyToDb(IBaseEntityManager baseEntityManager) {
        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> insertedObjects = baseEntityManager.getInsertedObjects(objectClass);
            if (insertedObjects != null && insertedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable insertedObject : insertedObjects) {
                    try {
                        persistableDao.insert(insertedObject);

                        if (insertedObject instanceof BaseEntity) {
                            BaseEntity be = (BaseEntity) insertedObject;
                            if (BasicOptimizer.metaList.contains(be.getMeta().getClassName())) {
                                EavOptimizerData eod = new EavOptimizerData(be.getMeta().getId(), be.getId(),
                                        BasicOptimizer.getKeyString(be));
                                eavOptimizerDao.insert(eod);
                            }
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("Ошибка при вставке: " + e.getMessage() +
                                "\n" + insertedObject);
                    }

                    if (isReferenceCacheEnabled && (insertedObject instanceof IBaseEntity)) {
                        IBaseEntity baseEntity = (IBaseEntity) insertedObject;

                        if (baseEntity.getMeta().isReference())
                            refRepositoryDao.setRef(baseEntity.getId(), baseEntity.getReportDate(), baseEntity);
                    }
                }
            }
        }

        for (int i = 0; i < BaseEntityManager.CLASS_PRIORITY.size(); i++) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> updatedObjects = baseEntityManager.getUpdatedObjects(objectClass);
            if (updatedObjects != null && updatedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable updatedObject : updatedObjects) {
                    try {
                        persistableDao.update(updatedObject);
                    } catch (Exception e) {
                        throw new IllegalStateException("Ошибка при обновлений: " + e.getMessage() +
                                "\n" + updatedObject);
                    }

                    if (isReferenceCacheEnabled && (updatedObject instanceof IBaseEntity)) {
                        IBaseEntity baseEntity = (IBaseEntity) updatedObject;

                        if (baseEntity.getMeta().isReference())
                            refRepositoryDao.setRef(baseEntity.getId(), baseEntity.getReportDate(), baseEntity);
                    }
                }
            }
        }

        for (int i = BaseEntityManager.CLASS_PRIORITY.size() - 1; i >= 0; i--) {
            Class objectClass = BaseEntityManager.CLASS_PRIORITY.get(i);
            List<IPersistable> deletedObjects = baseEntityManager.getDeletedObjects(objectClass);
            if (deletedObjects != null && deletedObjects.size() != 0) {
                IPersistableDao persistableDao = persistableDaoPool.getPersistableDao(objectClass);

                for (IPersistable deletedObject : deletedObjects) {
                    try {
                        persistableDao.delete(deletedObject);
                    } catch (Exception e) {
                        throw new IllegalStateException("Ошибка при удалений: " + e.getMessage() +
                                "\n" + deletedObject);
                    }

                    if (isReferenceCacheEnabled && (deletedObject instanceof IBaseEntity)) {
                        IBaseEntity baseEntity = (IBaseEntity) deletedObject;

                        if (baseEntity.getMeta().isReference())
                            refRepositoryDao.delRef(baseEntity.getId(), baseEntity.getReportDate());
                    }
                }
            }
        }

        // TODO: fix problem with repeated deletion
        /*for (IBaseEntity unusedBaseEntity : baseEntityManager.getUnusedBaseEntities()) {
            IBaseEntityDao baseEntityDao = persistableDaoPool
                    .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);

            baseEntityDao.deleteRecursive(unusedBaseEntity.getId(), unusedBaseEntity.getMeta());

            if (isReferenceCacheEnabled && unusedBaseEntity.getMeta().isReference())
                refRepositoryDao.delRef(unusedBaseEntity.getId(), unusedBaseEntity.getReportDate());
        }*/
    }

    private Object returnCastedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE ? new Date(((Date) baseValue.getValue()).getTime()) :
                baseValue.getValue();
    }
}
