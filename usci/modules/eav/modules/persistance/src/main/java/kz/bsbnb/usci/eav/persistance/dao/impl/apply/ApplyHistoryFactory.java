package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
public class ApplyHistoryFactory {

    @Autowired
    protected IPersistableDaoPool persistableDaoPool;

    protected long creditorId;

    protected IBaseEntity baseEntityApplied;
    protected IBaseValue baseValueSaving;
    protected IBaseValue baseValueLoaded;

    protected IBaseEntity baseEntityLoaded;
    protected IBaseEntity baseEntitySaving;

    protected IBaseEntityManager baseEntityManager;

    protected IMetaAttribute metaAttribute;
    protected IMetaType metaType;
    protected IMetaClass metaClass;
    protected IMetaValue metaValue;

    protected IBaseValueDao valueDao;

    private BaseValueDSLFactory lastBase = null;

    private InitializingBaseValueDSLFactory lastApplied = null;

    private HistoricalBaseValueDSLFactory lastExisting = null;

    private HistoricalBaseValueDSLFactory lastClosed = null;

    private HistoricalBaseValueDSLFactory lastPrevious = null;

    private HistoricalBaseValueDSLFactory lastNext = null;

    private HistoricalBaseValueDSLFactory lastLast = null;


    public ApplyHistoryFactory(long creditorId,
                               IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                               IBaseEntityManager baseEntityManager) {

        this.creditorId = creditorId;

        this.baseEntityApplied = baseEntityApplied;
        this.baseValueSaving = baseValueSaving;
        this.baseValueLoaded = baseValueLoaded;

        this.baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
        this.baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();

        this.baseEntityManager = baseEntityManager;

        this.metaAttribute = baseValueSaving.getMetaAttribute();
        this.metaType = metaAttribute.getMetaType();
        try {
            this.metaValue = (IMetaValue) metaType;
        } catch (Exception e) {
        }
        try {
            this.metaClass = (IMetaClass) metaType;
        } catch (Exception e) {
        }

        valueDao = persistableDaoPool
                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public BaseValueDSLFactory base() {
        BaseValueDSLFactory base = new BaseValueDSLFactory(this).from(null);
        this.lastBase = base;
        return base;
    }

    public BaseValueDSLFactory base(IBaseValue from) {
        BaseValueDSLFactory base = new BaseValueDSLFactory(this).from(from);
        this.lastBase = base;
        return base;
    }

    public InitializingBaseValueDSLFactory applied(IBaseValue from) {
        InitializingBaseValueDSLFactory initializing = new InitializingBaseValueDSLFactory(this).from(from);
        this.lastApplied = initializing;
        return initializing;
    }

    public InitializingBaseValueDSLFactory applied(IBaseValue from, Boolean closed, Boolean last) {
        InitializingBaseValueDSLFactory initializing = new InitializingBaseValueDSLFactory(this).from(from, closed, last);
        this.lastApplied = initializing;
        return initializing;
    }

    public InitializingBaseValueDSLFactory applied(Long id, Date date, Boolean closed, Boolean last) {
        InitializingBaseValueDSLFactory initializing = new InitializingBaseValueDSLFactory(this).from(id, date, closed, last);
        this.lastApplied = initializing;
        return initializing;
    }

    public HistoricalBaseValueDSLFactory existing(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromExisting(value);
        this.lastExisting = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory closed(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromClosed(value);
        this.lastClosed = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory previous(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromPrevious(value);
        this.lastPrevious = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory next(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromNext(value);
        this.lastNext = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory last(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromLast(value);
        this.lastLast = historical;
        return historical;
    }

    public BaseValueDSLFactory from() {
        return lastBase;
    }

    public IBaseValue fromValue() {
        return lastBase.result();
    }

    public InitializingBaseValueDSLFactory applied() {
        return lastApplied;
    }

    public IBaseValue appliedValue() {
        return lastApplied.result();
    }

    public HistoricalBaseValueDSLFactory existing() {
        return lastExisting;
    }

    public IBaseValue existingValue() {
        return lastExisting.result();
    }

    public HistoricalBaseValueDSLFactory closed() {
        return lastClosed;
    }

    public IBaseValue closedValue() {
        return lastClosed.result();
    }

    public HistoricalBaseValueDSLFactory previous() {
        return lastPrevious;
    }

    public IBaseValue previousValue() {
        return lastPrevious.result();
    }

    public HistoricalBaseValueDSLFactory next() {
        return lastNext;
    }

    public IBaseValue nextValue() {
        return lastNext.result();
    }

    public HistoricalBaseValueDSLFactory last() {
        return lastLast;
    }

    public IBaseValue lastValue() {
        return lastLast.result();
    }

    public interface IEachAttribute {
        void execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, String attributeName, IMetaAttribute childMetaAttribute, IMetaType childMetaType);
    }

    public void eachAttribute(IEachAttribute attribute) {

        IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
        IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded, baseValueSaving.getRepDate());

        for (String attributeName : metaClass.getAttributeNames()) {
            IMetaAttribute childMetaAttribute = metaClass.getMetaAttribute(attributeName);
            IMetaType childMetaType = childMetaAttribute.getMetaType();

            attribute.execute(baseEntityLoaded, baseEntitySaving, attributeName, childMetaAttribute, childMetaType);

        }

    }

    public interface IGetFunction {
        IBaseEntity execute(IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving, IMetaClass metaClass, IMetaAttribute metaAttribute);
    }

    public IBaseEntity get(IGetFunction function, IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving) {
        return function.execute(baseEntityLoaded, baseEntitySaving, metaClass, metaAttribute);
    }

    public IBaseEntity get(IGetFunction function) {
        return function.execute(baseEntityLoaded, baseEntitySaving, metaClass, metaAttribute);
    }

    public Object castedValue(IBaseValue value) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) value.getValue()).getTime()) : value.getValue();
    }

    public Object newValue(IBaseValue value) {
        if (value.getNewBaseValue() != null)
            return value.getNewBaseValue().getValue();
        else return null;
    }

}



