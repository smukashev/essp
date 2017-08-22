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
class MemorizingTool {

    long creditorId;
    IBaseEntity baseEntityApplied;
    IBaseValue baseValueSaving;
    IBaseValue baseValueLoaded;
    IBaseEntityManager baseEntityManager;
    IMetaAttribute metaAttribute;
    IMetaType metaType;
    IMetaValue metaValue;
    IBaseValueDao valueDao;
    @Autowired
    private IPersistableDaoPool persistableDaoPool;

    MemorizingTool(long creditorId, IBaseEntity baseEntityApplied, IBaseValue baseValueSaving,
                   IBaseValue baseValueLoaded, IBaseEntityManager baseEntityManager) {

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

    private Object returnCastedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) baseValue.getValue()).getTime()) : baseValue.getValue();
    }

    void initialize() {

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

    IBaseValue getExisting(IBaseValue baseValue) {

        IBaseValue out = valueDao.getExistingBaseValue(baseValue);

        if (out != null) {

            out.setMetaAttribute(metaAttribute);
            out.setBaseContainer(baseEntityApplied);

        }

        return out;

    }

    IBaseValue getClosed(IBaseValue baseValue) {

        IBaseValue out = valueDao.getClosedBaseValue(baseValue);

        if (out != null) {

            out.setMetaAttribute(metaAttribute);
            out.setBaseContainer(baseEntityApplied);

        }

        return out;

    }

    IBaseValue getPrevious(IBaseValue baseValue) {

        IBaseValue out = valueDao.getPreviousBaseValue(baseValue);

        if (out != null) {

            out.setMetaAttribute(metaAttribute);
            out.setBaseContainer(baseEntityApplied);

        }

        return out;

    }

    IBaseValue getNext(IBaseValue baseValue) {

        IBaseValue out = valueDao.getNextBaseValue(baseValue);

        if (out != null) {

            out.setMetaAttribute(metaAttribute);
            out.setBaseContainer(baseEntityApplied);

        }

        return out;

    }

    IBaseValue getLast(IBaseValue baseValue) {

        IBaseValue out = valueDao.getLastBaseValue(baseValue);

        if (out != null) {

            out.setMetaAttribute(metaAttribute);
            out.setBaseContainer(baseEntityApplied);

        }

        return out;

    }

    class MemorizingBaseValue {

        final int PERSISTANCE_INSERTED = 1;
        final int PERSISTANCE_UPDATED = 2;
        final int PERSISTANCE_DELETED = 3;
        final int PERSISTANCE_PROCESSED = 4;

        IBaseValue from = null;

        Long id;
        Date date;
        Boolean closed;
        Boolean last;

        Boolean parent = true;
        Boolean attribute = false;
        Integer persistence = 0;

        IBaseValue value = null;

        public MemorizingBaseValue(IBaseValue baseValue) {
            this.value = baseValue;
        }

        public MemorizingBaseValue() {
        }

        MemorizingBaseValue initialize() {
            return this;
        }

        MemorizingBaseValue from(IBaseValue from) {
            this.from = from;
            return this;
        }

        MemorizingBaseValue parent(Boolean parent) {
            this.parent = parent;
            return this;
        }

        MemorizingBaseValue attribute(Boolean attribute) {
            this.attribute = attribute;
            return this;
        }

        MemorizingBaseValue inserted() {
            this.persistence = PERSISTANCE_INSERTED;
            return this;
        }

        MemorizingBaseValue updated() {
            this.persistence = PERSISTANCE_UPDATED;
            return this;
        }

        MemorizingBaseValue deleted() {
            this.persistence = PERSISTANCE_DELETED;
            return this;
        }

        MemorizingBaseValue processed() {
            this.persistence = PERSISTANCE_PROCESSED;
            return this;
        }

        MemorizingBaseValue create() {

            value = BaseValueFactory.create(
                    MetaContainerTypes.META_CLASS,
                    metaType,
                    id == null
                            ? (from == null ? 0L : from.getId())
                            : id,
                    creditorId,
                    new Date((
                            date == null
                                    ? (from == null ? new Date() : from.getRepDate())
                                    : date
                    ).getTime()),
                    returnCastedValue(metaValue, baseValueSaving),
                    closed == null
                            ? from != null && from.isClosed()
                            : closed,
                    last == null
                            ? from != null && from.isLast()
                            : last);

            if (parent) {
                value.setBaseContainer(baseEntityApplied);
                value.setMetaAttribute(metaAttribute);
            }

            if (attribute) baseEntityApplied.put(metaAttribute.getName(), value);

            switch (persistence) {
                case (PERSISTANCE_INSERTED):
                    baseEntityManager.registerAsInserted(value);
                    break;
                case (PERSISTANCE_UPDATED):
                    baseEntityManager.registerAsUpdated(value);
                    break;
                case (PERSISTANCE_DELETED):
                    baseEntityManager.registerAsDeleted(value);
                    break;
                case (PERSISTANCE_PROCESSED):
                    baseEntityManager.registerProcessedBaseEntity(baseEntityApplied);
                    break;
            }

            return this;

        }

    }

}



