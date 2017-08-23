package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
class ApplyHistoryFactory {

    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    protected long creditorId;
    protected IBaseEntity baseEntityApplied;
    protected IBaseValue baseValueSaving;
    protected IBaseValue baseValueLoaded;
    protected IBaseEntityManager baseEntityManager;

    protected IMetaAttribute metaAttribute;
    protected IMetaType metaType;
    protected IMetaValue metaValue;

    protected IBaseValueDao valueDao;

    public ApplyHistoryFactory(long creditorId,
                               IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                               IBaseEntityManager baseEntityManager) {

        this.creditorId = creditorId;
        this.baseEntityApplied = baseEntityApplied;
        this.baseValueSaving = baseValueSaving;
        this.baseValueLoaded = baseValueLoaded;
        this.baseEntityManager = baseEntityManager;

        this.metaAttribute = baseValueSaving.getMetaAttribute();
        this.metaType = metaAttribute.getMetaType();
        this.metaValue = (IMetaValue) metaType;

        valueDao = persistableDaoPool
                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public BaseValueDSLFactory initialize() {
        return new BaseValueDSLFactory(this).initialize();
    }

    public HistoricalBaseValueDSLFactory existing(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromExisting(value);
    }

    public HistoricalBaseValueDSLFactory closed(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromClosed(value);
    }

    public HistoricalBaseValueDSLFactory previous(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromPrevious(value);
    }

    public HistoricalBaseValueDSLFactory next(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromNext(value);
    }

    public HistoricalBaseValueDSLFactory last(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromLast(value);
    }

    void deleted(Boolean closed, Boolean last) {

        IBaseValue baseValueDeleted = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType,
                baseValueLoaded.getId(),
                creditorId,
                new Date(baseValueLoaded.getRepDate().getTime()),
                returnCastedValue(metaValue, baseValueLoaded),
                closed == null ? baseValueLoaded.isClosed() : closed,
                last == null ? baseValueLoaded.isLast() : last);

        baseValueDeleted.setBaseContainer(baseEntityApplied);
        baseValueDeleted.setMetaAttribute(metaAttribute);

        baseEntityManager.registerAsDeleted(baseValueDeleted);

        if (baseValueLoaded.isLast()) {
            IBaseValue baseValuePrevious = valueDao.getPreviousBaseValue(baseValueLoaded);

            if (baseValuePrevious != null) {
                baseValuePrevious.setBaseContainer(baseEntityApplied);
                baseValuePrevious.setMetaAttribute(metaAttribute);
                baseValuePrevious.setLast(true);
                baseEntityManager.registerAsUpdated(baseValuePrevious);
            }
        }

        // delete closed next value
        IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

        if (baseValueNext != null && baseValueNext.isClosed()) {
            baseValueNext.setBaseContainer(baseEntityApplied);
            baseValueNext.setMetaAttribute(metaAttribute);

            baseEntityManager.registerAsDeleted(baseValueNext);
        }

    }

    void updated() {

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

        IBaseValue baseValueNext = valueDao.getNextBaseValue(baseValueLoaded);

        // check for closed in next periods
        if (baseValueNext != null)
            if (baseValueNext.isClosed()) {
                baseValueNext.setRepDate(baseValueSaving.getRepDate());

                baseValueNext.setBaseContainer(baseEntityApplied);
                baseValueNext.setMetaAttribute(metaAttribute);

                baseEntityManager.registerAsUpdated(baseValueNext);
            } else {
                if (StaticRouter.exceptionOnForbiddenCloseE299())
                    throw new UnsupportedOperationException(Errors.compose(Errors.E299, DataTypes.formatDate(baseValueNext.getRepDate()), baseValueNext.getValue()));
            }
        else {
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
        }

    }

    void updated(Date date, Boolean force) {

        // Именение ключевых полей
        // <tag operation="new" data="new_value">old_value</tag>
        // case#4
        Object baseV;
        if (baseValueSaving.getNewBaseValue() != null) {
            baseV = baseValueSaving.getNewBaseValue().getValue();
                /* Обновление ключевых полей в оптимизаторе */
            baseEntityManager.addOptimizerEntity(baseEntityApplied);
        } else {
            baseV = returnCastedValue(metaValue, baseValueLoaded);
        }

        IBaseValue baseValueApplied = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType,
                baseValueLoaded.getId(),
                creditorId,
                new Date(date.getTime()),
                baseV,
                baseValueLoaded.isClosed(),
                baseValueLoaded.isLast());

        baseValueApplied.setBaseContainer(baseEntityApplied);
        baseValueApplied.setMetaAttribute(metaAttribute);

        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);

        // Запуск на изменение ключевого поля
        if (force)
            baseEntityManager.registerAsUpdated(baseValueApplied);
        else if (baseValueSaving.getNewBaseValue() != null)
            baseEntityManager.registerAsUpdated(baseValueApplied);

    }

    void updated2() {
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
    }

    void insert(Boolean closed, Boolean last, Boolean doLast) {

        IBaseValue baseValueApplied = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType,
                0,
                creditorId,
                new Date(baseValueSaving.getRepDate().getTime()),
                returnCastedValue(metaValue, baseValueSaving),
                closed == null ? baseValueLoaded.isClosed() : closed,
                last == null ? baseValueLoaded.isLast() : last);

        baseValueApplied.setBaseContainer(baseEntityApplied);
        baseValueApplied.setMetaAttribute(metaAttribute);

        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);

        baseEntityManager.registerAsInserted(baseValueApplied);

        if (doLast)
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


    }

    void update3(IBaseValue baseValueNext, Long id, Boolean closed, Boolean last) {

        IBaseValue baseValueApplied = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType,
                id == null ? baseValueNext.getId() : id,
                creditorId,
                new Date(baseValueSaving.getRepDate().getTime()),
                returnCastedValue(metaValue, baseValueSaving),
                closed == null ? baseValueNext.isClosed() : closed,
                last == null ? baseValueNext.isLast() : last);

        baseEntityApplied.put(metaAttribute.getName(), baseValueApplied);
        baseEntityManager.registerAsUpdated(baseValueApplied);

    }

    private Object returnCastedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) baseValue.getValue()).getTime()) : baseValue.getValue();
    }

}



