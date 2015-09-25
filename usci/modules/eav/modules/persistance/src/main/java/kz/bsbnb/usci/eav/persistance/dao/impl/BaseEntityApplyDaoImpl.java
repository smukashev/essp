package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.cr.model.DataTypeUtil;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public class BaseEntityApplyDaoImpl extends JDBCSupport implements IBaseEntityApplyDao {
    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;
    @Autowired
    IRefRepository refRepositoryDao;

    @Override
    public IBaseEntity apply(long creditorId, IBaseEntity baseEntitySaving, IBaseEntityManager baseEntityManager,
                             EntityHolder entityHolder) {
        IBaseEntity baseEntityLoaded;
        IBaseEntity baseEntityApplied;

        // Новые сущности или сущности не имеющие ключевые атрибуты
        if (baseEntitySaving.getId() < 1 || !baseEntitySaving.getMeta().isSearchable()) {
            baseEntityApplied = applyBaseEntityBasic(creditorId, baseEntitySaving, baseEntityManager);
        } else {
            Date reportDateSaving = baseEntitySaving.getReportDate();

            IBaseEntityReportDateDao baseEntityReportDateDao =
                    persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

            // Получение максимальной отчетной даты из прошедших периодов
            Date maxReportDate = baseEntityReportDateDao
                    .getMaxReportDate(baseEntitySaving.getId(), reportDateSaving);

            if (maxReportDate == null) {
                // Получение минимальной отчетной даты из будущих периодов
                Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntitySaving.getId(),
                        reportDateSaving);

                if (minReportDate == null)
                    throw new UnsupportedOperationException("Найденный объект (" + baseEntitySaving.getId()
                            + ") не имеет отчетный даты;");

                baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), minReportDate, reportDateSaving);

                if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                    throw new UnsupportedOperationException("Запись с ID(" + baseEntityLoaded.getId() +
                            ") является закрытой с даты " + baseEntityLoaded.getBaseEntityReportDate().getReportDate()
                            + ". Обновление после закрытия сущностей не является возможным;");

                baseEntityApplied = applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded,
                        baseEntityManager);
            } else {
                baseEntityLoaded = baseEntityLoadDao.load(baseEntitySaving.getId(), maxReportDate, reportDateSaving);

                if (baseEntityLoaded.getBaseEntityReportDate().isClosed())
                    throw new UnsupportedOperationException("Запись с ID(" + baseEntityLoaded.getId() +
                            ") является закрытой с даты " + baseEntityLoaded.getBaseEntityReportDate().getReportDate()
                            + ". Обновление после закрытия сущностей не является возможным;");

                baseEntityApplied = applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded,
                        baseEntityManager);
            }
        }

        if (entityHolder != null) {
            // entityHolder.setSaving(baseEntitySaving);
            // entityHolder.setLoaded(baseEntityLoaded);
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

        baseEntityApplied.calculateValueCount();
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
                        throw new UnsupportedOperationException("Запись класса " + childBaseEntity.getMeta().
                                getClassName() + " не найдена;" + "\n" + childBaseEntity.toString());

                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, baseEntityManager, null);

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
                            throw new UnsupportedOperationException("Запись класса " +
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
                    IBaseEntity childBaseEntityApplied = apply(creditorId, childBaseEntity, baseEntityManager, null);

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

        baseEntityApplied.calculateValueCount();

        IBaseEntityReportDate baseEntityReportDate = baseEntityApplied.getBaseEntityReportDate();

        Date reportDateSaving = baseEntitySaving.getReportDate();
        Date reportDateLoaded = baseEntityLoaded.getReportDate();

        int reportDateCompare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

        if (reportDateCompare == 0) {
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

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

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
                        baseEntityManager.registerAsDeleted(baseValueDeleted);

                        if (baseValueLoaded.isLast()) {
                            IBaseValueDao valueDao = persistableDaoPool
                                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

                            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);

                            if (baseValuePrevious != null) {
                                baseValuePrevious.setBaseContainer(baseEntityApplied);
                                baseValuePrevious.setMetaAttribute(metaAttribute);
                                baseValuePrevious.setCreditorId(creditorId);
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
                                baseValuePrevious.setCreditorId(creditorId);
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

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

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

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                }
            } else {
                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

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

                    baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                    // case#8
                } else if (compare == 1) {
                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            returnCastedValue(metaValue, baseValueSaving),
                            false,
                            baseValueLoaded.isLast());

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

                        if (baseValueClosed.isLast()) {
                            baseValuePrevious.setLast(true);
                            baseEntityApplied.put(metaAttribute.getName(), baseValuePrevious);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        } else {
                            baseEntityApplied.put(metaAttribute.getName(), baseValuePrevious);
                        }
                    } else {
                        baseValueClosed.setValue(returnCastedValue(metaValue, baseValueSaving));
                        baseValueClosed.setClosed(false);

                        baseEntityApplied.put(metaAttribute.getName(), baseValueClosed);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                    // case#11
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

                    boolean last = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast) != -1;

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

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    // case#1
                    if (metaAttribute.isFinal()) {
                        if (metaClass.hasNotFinalAttributes() && !metaClass.isSearchable())
                            throw new IllegalStateException("Оперативные атрибуты могут сожержать только оперативные "
                                    + "данные. Мета: " + baseEntity.getMeta().getClassName()
                                    + ", атрибут: " + metaAttribute.getName());

                        IBaseValue baseValueDeleted = ((BaseValue) baseValueLoaded).clone();
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

                        return;
                        // case#2
                    } else {
                        IBaseValue baseValueDeleted = ((BaseValue) baseValueLoaded).clone();
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
                    }
                    // case#3
                } else if (compare == 1) {
                    if (metaAttribute.isFinal())
                        throw new IllegalStateException("Оперативные данные могут удалятся только за " +
                                "существующий период(" + metaAttribute.getName() + ").");

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
            if (baseValueSaving.equalsByValue(baseValueLoaded) || !metaClass.isSearchable()) {
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new UnsupportedOperationException("Запись класса " + baseEntitySaving.getMeta().
                                getClassName() + " не найдена;" + "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = metaClass.isSearchable() ?
                            apply(creditorId, baseEntitySaving, baseEntityManager, null) :
                            applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityLoaded, baseEntityManager);
                }

                int compare = DataTypeUtil.compareBeginningOfTheDay(baseValueSaving.getRepDate(),
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

                baseEntity.put(metaAttribute.getName(), baseValueApplied);

                if (compare == -1)
                    baseEntityManager.registerAsUpdated(baseValueApplied);
                // case#5
            } else {
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new UnsupportedOperationException("Запись класса " +
                                baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    baseEntityApplied = apply(creditorId, baseEntitySaving, baseEntityManager, null);
                }

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

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

            IBaseValue baseValueClosed = null;
            // case#6
            if (!metaAttribute.isFinal()) {
                baseValueClosed = valueDao.getClosedBaseValue(baseValueSaving);

                if (baseValueClosed != null) {
                    baseValueClosed.setBaseContainer(baseEntity);
                    baseValueClosed.setMetaAttribute(metaAttribute);

                    IBaseEntity baseEntityClosed = (IBaseEntity) baseValueClosed.getValue();

                    if (baseValueClosed.equalsByValue(baseValueSaving)) {
                        baseEntityManager.registerAsDeleted(baseValueClosed);

                        IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueClosed);

                        if (baseValuePrevious == null)
                            throw new IllegalStateException("Предыдущее значение не найдено;\n" + baseValueClosed);

                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);

                        IBaseEntity baseEntityApplied;
                        if (metaAttribute.isImmutable()) {
                            if (baseEntitySaving.getId() < 1)
                                throw new UnsupportedOperationException("Запись класса " +
                                        baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                        "\n" + baseEntitySaving.toString());

                            baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, baseEntityManager, null) :
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
                                throw new UnsupportedOperationException("Запись класса " +
                                        baseEntitySaving.getMeta().getClassName() + " не найдена;" +
                                        "\n" + baseEntitySaving.toString());

                            baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                                    baseEntitySaving.getReportDate());
                        } else {
                            baseEntityApplied = metaClass.isSearchable() ?
                                    apply(creditorId, baseEntitySaving, baseEntityManager, null) :
                                    applyBaseEntityAdvanced(creditorId, baseEntitySaving, baseEntityClosed,
                                            baseEntityManager);
                        }

                        baseValueClosed.setValue(baseEntityApplied);
                        baseValueClosed.setClosed(false);
                        baseEntity.put(metaAttribute.getName(), baseValueClosed);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                }
                // case#7
            } else {
                IBaseValue baseValueLast = valueDao.getLastBaseValue(baseValueSaving);
                IBaseEntity baseEntityApplied;

                if (metaAttribute.isImmutable()) {
                    if (baseEntitySaving.getId() < 1)
                        throw new UnsupportedOperationException("Запись класса " + baseEntitySaving.getMeta().
                                getClassName() + " не найдена;" + "\n" + baseEntitySaving.toString());

                    baseEntityApplied = baseEntityLoadDao.loadByMaxReportDate(baseEntitySaving.getId(),
                            baseEntitySaving.getReportDate());
                } else {
                    IBaseValue previousBaseValue = valueDao.getPreviousBaseValue(baseValueSaving);

                    if (previousBaseValue != null) {
                        baseEntityApplied = applyBaseEntityAdvanced(creditorId, baseEntitySaving,
                                (IBaseEntity) previousBaseValue.getValue(), baseEntityManager);
                    } else {
                        baseEntityApplied = apply(creditorId, baseEntitySaving, baseEntityManager, null);
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

                    int reportDateCompare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving, reportDateLast);

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

        if (baseValueLoaded != null) {
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = baseValueLoaded.getRepDate();

                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving,
                        reportDateLoaded);

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
                // case#2
                } else if(compare == 1) {
                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
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
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                childBaseSetLoaded,
                                baseValueLoaded.isClosed(),
                                false);

                        baseValueLast.setBaseContainer(baseEntity);
                        baseValueLast.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueLast);
                    }
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

                if (baseValuePrevious != null) {
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
                } else {
                    throw new IllegalStateException("Запись открытия не была найдена(" +
                            metaAttribute.getName() + ");");
                }
            // case#6
            } else {
                IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);

                if (baseValuePrevious != null) {
                    childBaseSetLoaded = (IBaseSet) baseValuePrevious.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            baseValuePrevious.isLast());

                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (baseValuePrevious.isLast()) {
                        baseValuePrevious.setBaseContainer(baseEntity);
                        baseValuePrevious.setMetaAttribute(metaAttribute);
                        baseValuePrevious.setLast(false);
                        baseEntityManager.registerAsUpdated(baseValuePrevious);
                    }
                } else {
                    childBaseSetApplied = new BaseSet(childMetaType);
                    baseEntityManager.registerAsInserted(childBaseSetApplied);

                    // TODO: Check next value

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
                    if (childBaseValueClosed.isLast()) {
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

        if (childBaseSetLoaded != null) {
            for (IBaseValue childBaseValueLoaded : childBaseSetLoaded.get()) {
                if (processedUuids.contains(childBaseValueLoaded.getUuid()))
                    continue;

                Date reportDateSaving = baseValueSaving.getRepDate();
                Date reportDateLoaded = childBaseValueLoaded.getRepDate();

                IBaseSetValueDao setValueDao = persistableDaoPool
                        .getPersistableDao(childBaseValueLoaded.getClass(), IBaseSetValueDao.class);

                int compare = DataTypeUtil.compareBeginningOfTheDay(reportDateSaving,
                        reportDateLoaded);

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

        if (baseValueLoaded != null) {
            reportDateLoaded = baseValueLoaded.getRepDate();
            childBaseSetLoaded = (IBaseSet) baseValueLoaded.getValue();

            if (childBaseSetSaving == null || childBaseSetSaving.getValueCount() <= 0) {
                childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);
                //set deletion
                reportDateSaving = baseValueSaving.getRepDate();
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (compare == 0) {
                    //case#7
                    if (metaAttribute.isFinal()) {
                        if (!childMetaClass.isSearchable() && childMetaClass.hasNotFinalAttributes()) {
                            throw new RuntimeException("Оперативные данные " +
                                    "должны состоять только из оперативных данных. Наименование класса: " +
                                    baseEntity.getMeta().getClassName() + ", атрибут: " + metaAttribute.getName());
                        }

                        IBaseValue baseValueDeleted = ((BaseValue) baseValueLoaded).clone();

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
                                                    creditorId,
                                                    new Date(baseValueSaving.getRepDate().getTime()),
                                                    null,
                                                    false,
                                                    true));
                                }
                                applyBaseEntityAdvanced(creditorId, childBaseEntitySaving, childBaseEntityLoaded,
                                        baseEntityManager);

                                IBaseSetComplexValueDao baseSetComplexValueDao = persistableDaoPool
                                        .getPersistableDao(childBaseValueLoaded.getClass(),
                                                IBaseSetComplexValueDao.class);

                                boolean singleBaseValue = baseSetComplexValueDao.
                                        isSingleBaseValue(childBaseValueLoaded);

                                if (singleBaseValue)
                                    baseEntityManager.registerAsDeleted(childBaseEntityLoaded);
                            }
                        }
                        return;
                        //case#8
                    } else {
                        IBaseValue baseValueClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                baseValueLoaded.getId(),
                                creditorId,
                                new Date(baseValueLoaded.getRepDate().getTime()),
                                childBaseSetLoaded,
                                true,
                                baseValueLoaded.isLast());

                        baseValueClosed.setBaseContainer(baseEntity);
                        baseValueClosed.setMetaAttribute(metaAttribute);
                        baseEntityManager.registerAsUpdated(baseValueClosed);
                    }
                    //case#9
                } else if (compare == 1) {
                    if (metaAttribute.isFinal()) {
                        throw new RuntimeException("Отчетная дата оперативных данных " +
                                "неправильно выгружена из базы.");
                    }

                    IBaseValue baseValueClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            0,
                            creditorId,
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
            } else {
                reportDateSaving = baseValueSaving.getRepDate();
                //boolean reportDateEquals = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded) == 0;
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                if (metaAttribute.isFinal() && !(compare == 0)) {
                    throw new RuntimeException("Instance of BaseValue with incorrect report date and final flag " +
                            "mistakenly loaded from the database.");
                    //case#10
                } else if (compare >= 0) {
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
                    //case#11
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
            }

            reportDateSaving = baseValueSaving.getRepDate();

            IBaseValueDao baseValueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

            IBaseValue baseValueClosed = null;
            //case#12
            if (!metaAttribute.isFinal()) {
                baseValueClosed = baseValueDao.getClosedBaseValue(baseValueSaving); // searches for closed BV at reportDateSaving

                //case#13 TODO:DUPLICATES OCCUR
                if (baseValueClosed != null) {
                    reportDateLoaded = baseValueClosed.getRepDate();

                    childBaseSetLoaded = (IBaseSet) baseValueClosed.getValue();
                    childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                    IBaseValue baseValueApplied = BaseValueFactory.create(
                            MetaContainerTypes.META_CLASS,
                            metaType,
                            baseValueClosed.getId(),
                            creditorId,
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
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class,
                                IBaseEntityReportDateDao.class);

                reportDateLoaded = baseEntityReportDateDao.getMinReportDate(baseEntity.getId());
                int compare = DataUtils.compareBeginningOfTheDay(reportDateSaving, reportDateLoaded);

                boolean isNew = true;
                //case#14
                if (compare == 1) {
                    IBaseValue baseValuePrevious = baseValueDao.getPreviousBaseValue(baseValueSaving);
                    if (baseValuePrevious != null) {
                        reportDateLoaded = baseValuePrevious.getRepDate();

                        childBaseSetLoaded = (IBaseSet) baseValuePrevious.getValue();
                        childBaseSetApplied = new BaseSet(childBaseSetLoaded.getId(), childMetaType);

                        IBaseValue baseValueApplied = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
                                new Date(baseValueSaving.getRepDate().getTime()),
                                childBaseSetApplied,
                                false,
                                baseValuePrevious.isLast());

                        baseEntity.put(metaAttribute.getName(), baseValueApplied);
                        baseEntityManager.registerAsInserted(baseValueApplied);

                        if (baseValuePrevious.isLast()) {
                            baseValuePrevious.setBaseContainer(baseEntity);
                            baseValuePrevious.setMetaAttribute(metaAttribute);
                            baseValuePrevious.setLast(false);
                            baseEntityManager.registerAsUpdated(baseValuePrevious);
                        }
                        isNew = false;
                    }
                    //case#15
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
                                0,
                                creditorId,
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
                                    0,
                                    creditorId,
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
                            0,
                            creditorId,
                            new Date(baseValueSaving.getRepDate().getTime()),
                            childBaseSetApplied,
                            false,
                            compare != -1);
                    // if compare = -1, closing value will be inserted, it will become last
                    baseEntity.put(metaAttribute.getName(), baseValueApplied);
                    baseEntityManager.registerAsInserted(baseValueApplied);

                    if (compare == -1) {
                        // Close inserted value at reportDateLoaded
                        //childBaseSetApplied = new BaseSet(childMetaType);
                        //baseEntityManager.registerAsInserted(childBaseSetApplied);

                        IBaseValue baseValueAppliedClosed = BaseValueFactory.create(
                                MetaContainerTypes.META_CLASS,
                                metaType,
                                0,
                                creditorId,
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

        Set<UUID> processedUuids = new HashSet<>();
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
                                            creditorId,
                                            reportDateApplied,
                                            //new Date(childBaseValueLoaded.getRepDate().getTime()),
                                            applyBaseEntityAdvanced(creditorId, childBaseEntitySaving,
                                                    childBaseEntityLoaded, baseEntityManager),
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
                                            applyBaseEntityAdvanced(creditorId, childBaseEntitySaving, childBaseEntityLoaded, baseEntityManager);

                                    IBaseValue baseValueApplied = BaseValueFactory.create(
                                            MetaContainerTypes.META_SET,
                                            childMetaType,
                                            childBaseValueLoaded.getId(),
                                            creditorId,
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
                                                creditorId,
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
                        IBaseValue childBaseValueClosed = setValueDao.getClosedBaseValue(baseValueSaving);

                        if (childBaseValueClosed != null) {
                            childBaseValueClosed.setBaseContainer(childBaseSetApplied);
                            baseEntityManager.registerAsDeleted(childBaseValueClosed);

                            IBaseValue childBaseValuePrevious = setValueDao.getPreviousBaseValue(childBaseValueClosed);
                            if (childBaseValuePrevious != null && childBaseValuePrevious.getValue() != null) {
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

                    IBaseValue childBaseValueLast = setValueDao.getLastBaseValue(childBaseValueSaving);
                    if (childBaseValueLast != null) {
                        childBaseValueLast.setBaseContainer(childBaseSetApplied);
                        childBaseValueLast.setLast(false);

                        baseEntityManager.registerAsUpdated(childBaseValueLast);
                    }
                }

                IBaseEntity baseEntitySavingTmp = apply(creditorId, childBaseEntitySaving, baseEntityManager, null);
                IBaseEntityReportDateDao baseEntityReportDateDao =
                        persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);
                Date minReportDate = baseEntityReportDateDao.getMinReportDate(baseEntity.getId());

                boolean isClosing = compare == -1 || (compare == -2 && reportDateSaving != null &&
                        DataUtils.compareBeginningOfTheDay(reportDateSaving, minReportDate) == -1);

                IBaseValue childBaseValueApplied = BaseValueFactory.create(
                        MetaContainerTypes.META_SET,
                        childMetaType,
                        0,
                        creditorId,
                        childBaseValueSaving.getRepDate(),
                        //apply(childBaseEntitySaving, baseEntityManager),
                        baseEntitySavingTmp,
                        false,
                        !isClosing);

                childBaseSetApplied.put(childBaseValueApplied);
                baseEntityManager.registerAsInserted(childBaseValueApplied);

                if (isClosing) { // case when compare=-2 && reportDateLoaded = null
                    //IBaseEntity baseEntityAAA = apply(childBaseEntitySaving, baseEntityManager);
                    IBaseValue childBaseValueAppliedClosed = BaseValueFactory.create(
                            MetaContainerTypes.META_SET,
                            childMetaType,
                            0,
                            creditorId,
                            reportDateLoaded != null ? reportDateLoaded : minReportDate,
                            //baseEntityAAA,
                            baseEntitySavingTmp,
                            true,
                            isClosing);

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
                                    0,
                                    creditorId,
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
                                        creditorId,
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
                    persistableDao.insert(insertedObject);

                    if (insertedObjects instanceof IBaseEntity) {
                        IBaseEntity baseEntity = (IBaseEntity) insertedObjects;
                        if (baseEntity.getMeta().isReference()) {
                            if (refRepositoryDao.getRef(baseEntity.getId(), baseEntity.getReportDate()) == null)
                                refRepositoryDao.setRef(baseEntity.getId(), baseEntity.getReportDate(), baseEntity);
                        }
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
                    persistableDao.update(updatedObject);

                    if (updatedObject instanceof IBaseEntity) {
                        IBaseEntity baseEntity = (IBaseEntity) updatedObject;
                        if (baseEntity.getMeta().isReference()) {
                            refRepositoryDao.setRef(baseEntity.getId(), baseEntity.getReportDate(), baseEntity);
                        }
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
                    persistableDao.delete(deletedObject);

                    if (deletedObject instanceof IBaseEntity) {
                        IBaseEntity baseEntity = (IBaseEntity) deletedObject;
                        if (baseEntity.getMeta().isReference()) {
                            refRepositoryDao.delRef(baseEntity.getId(), baseEntity.getReportDate());
                        }
                    }
                }

            }
        }

        for (IBaseEntity unusedBaseEntity : baseEntityManager.getUnusedBaseEntities()) {
            IBaseEntityDao baseEntityDao = persistableDaoPool
                    .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
            baseEntityDao.deleteRecursive(unusedBaseEntity.getId(), unusedBaseEntity.getMeta());
            if (unusedBaseEntity.getMeta().isReference()) {
                refRepositoryDao.delRef(unusedBaseEntity.getId(), unusedBaseEntity.getReportDate());
            }
        }
    }

    private Object returnCastedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE ? new Date(((Date) baseValue.getValue()).getTime()) :
                baseValue.getValue();
    }
}
