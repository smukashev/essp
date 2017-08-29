package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by emles on 22.08.17
 */
public class ApplyHistoryFactory {

    public IBaseSet childBaseSetSaving;
    public IBaseSet childBaseSetLoaded;
    public IBaseSet childBaseSetApplied;
    @Autowired
    protected IPersistableDaoPool persistableDaoPool;
    protected long creditorId;
    protected IBaseEntity baseEntityApplied;
    protected IBaseValue baseValueSaving;
    protected IBaseValue baseValueLoaded;
    protected IBaseEntity baseEntityLoaded;
    protected IBaseEntity baseEntitySaving;
    protected IBaseEntityManager baseEntityManager;
    protected IBaseContainer baseContainer;
    protected IMetaAttribute metaAttribute;
    protected IMetaType metaType;
    protected IMetaClass metaClass;
    protected IMetaValue metaValue;
    protected IMetaType childMetaType;
    protected IMetaValue childMetaValue;
    protected IMetaSet childMetaSet;
    protected IMetaClass childMetaClass;

    protected IBaseValueDao valueDao;

    private BaseValueDSLFactory lastBase = null;

    private InitializingBaseValueDSLFactory lastApplied = null;

    private HistoricalBaseValueDSLFactory lastExisting = null;

    private HistoricalBaseValueDSLFactory lastClosed = null;

    private HistoricalBaseValueDSLFactory lastPrevious = null;

    private HistoricalBaseValueDSLFactory lastNext = null;

    private HistoricalBaseValueDSLFactory lastLast = null;

    private Boolean with = false;
    private IBaseValue withPersistable = null;
    private Class withPersistableDaoClass = null;

    private Set<UUID> processed = new HashSet<>();


    public ApplyHistoryFactory(long creditorId,
                               IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                               IBaseEntityManager baseEntityManager) {

        this.creditorId = creditorId;

        this.baseEntityApplied = baseEntityApplied;
        this.baseValueSaving = baseValueSaving;
        this.baseValueLoaded = baseValueLoaded;

        try {
            this.baseEntitySaving = (IBaseEntity) baseValueSaving.getValue();
        } catch (Exception e) {
        }
        try {
            this.baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
        } catch (Exception e) {
        }

        this.baseEntityManager = baseEntityManager;

        this.baseContainer = baseValueSaving.getBaseContainer();
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
        try {
            this.childMetaSet = (IMetaSet) metaType;
            this.childMetaType = childMetaSet.getMemberType();
        } catch (Exception e) {
        }
        try {
            this.childMetaValue = (IMetaValue) childMetaType;
        } catch (Exception e) {
        }
        try {
            this.childMetaClass = (IMetaClass) childMetaType;
        } catch (Exception e) {
        }

        this.childBaseSetSaving = null;
        this.childBaseSetLoaded = null;
        this.childBaseSetApplied = null;

        this.valueDao = persistableDaoPool
                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public IBaseSet savingChildFrom(IBaseValue value) {
        try {
            return (childBaseSetSaving = (IBaseSet) value.getValue());
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet savingChildNew(IBaseValue value) {
        try {
            return (childBaseSetSaving = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet savingChildNew(IBaseSet value) {
        try {
            return (childBaseSetSaving = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet savingChildNew() {
        try {
            return (childBaseSetSaving = new BaseSet(childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet loadedChildFrom(IBaseValue value) {
        try {
            return (childBaseSetLoaded = (IBaseSet) value.getValue());
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet loadedChildNew(IBaseValue value) {
        try {
            return (childBaseSetLoaded = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet loadedChildNew(IBaseSet value) {
        try {
            return (childBaseSetLoaded = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet loadedChildNew() {
        try {
            return (childBaseSetLoaded = new BaseSet(childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet appliedChildFrom(IBaseValue value) {
        try {
            return (childBaseSetApplied = (IBaseSet) value.getValue());
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet appliedChildNew(IBaseValue value) {
        try {
            return (childBaseSetApplied = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet appliedChildNew(IBaseSet value) {
        try {
            return (childBaseSetApplied = new BaseSet(value.getId(), childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet appliedChildNew() {
        try {
            return (childBaseSetApplied = new BaseSet(childMetaType, creditorId));
        } catch (Exception e) {
        }
        return null;
    }

    public BaseValueDSLFactory base() {
        BaseValueDSLFactory base = new BaseValueDSLFactory(this).from();
        this.lastBase = base;
        return base;
    }

    public BaseValueDSLFactory base(IBaseValue from) {
        BaseValueDSLFactory base = new BaseValueDSLFactory(this).from(from);
        this.lastBase = base;
        return base;
    }

    public BaseValueDSLFactory base(IBaseSet from) {
        BaseValueDSLFactory base = new BaseValueDSLFactory(this).from(from);
        this.lastBase = base;
        return base;
    }

    public void withValueDao(IBaseValue persistable, Class<?> persistableDaoClass) {
        this.with = true;
        this.withPersistable = persistable;
        this.withPersistableDaoClass = persistableDaoClass;
    }

    public void noWith() {
        this.with = false;
        this.withPersistable = null;
        this.withPersistableDaoClass = null;
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
        if (with) historical.withValueDao(withPersistable, withPersistableDaoClass);
        this.lastExisting = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory closed(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromClosed(value);
        if (with) historical.withValueDao(withPersistable, withPersistableDaoClass);
        this.lastClosed = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory previous(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromPrevious(value);
        if (with) historical.withValueDao(withPersistable, withPersistableDaoClass);
        this.lastPrevious = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory next(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromNext(value);
        if (with) historical.withValueDao(withPersistable, withPersistableDaoClass);
        this.lastNext = historical;
        return historical;
    }

    public HistoricalBaseValueDSLFactory last(IBaseValue value) {
        HistoricalBaseValueDSLFactory historical = new HistoricalBaseValueDSLFactory(this).fromLast(value);
        if (with) historical.withValueDao(withPersistable, withPersistableDaoClass);
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

    public void processed(IBaseValue value) {
        processed.add(value.getUuid());
    }

    public Boolean contains(IBaseValue value) {
        return processed.contains(value.getUuid());
    }

    public void eachAttribute(IEachAttribute attribute) {

        IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
        IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded, baseValueSaving.getRepDate());

        for (String attributeName : metaClass.getAttributeNames()) {
            IMetaAttribute childMetaAttribute = metaClass.getMetaAttribute(attributeName);
            IMetaType childMetaType = childMetaAttribute.getMetaType();

            attribute.execute(new EachAttributeBinding(baseEntitySaving, baseEntityLoaded, attributeName, childMetaAttribute, childMetaType));

        }

    }

    public IBaseEntity get(IGetFunction function, IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving) {
        return function.execute(new IGetFunctionBinding(baseEntitySaving, baseEntityLoaded, metaClass, metaAttribute));
    }

    public IBaseEntity get(IGetFunction function) {
        return function.execute(new IGetFunctionBinding(baseEntitySaving, baseEntityLoaded, metaClass, metaAttribute));
    }

    public Object castedValue(IBaseValue value) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) value.getValue()).getTime()) : value.getValue();
    }

    public Object castedValue(IMetaValue metaValue, IBaseValue value) {
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) value.getValue()).getTime()) : value.getValue();
    }

    public Object newValue(IBaseValue value) {
        if (value.getNewBaseValue() != null)
            return value.getNewBaseValue().getValue();
        else return null;
    }

    public interface IEachAttribute {
        void execute(EachAttributeBinding binding);
    }

    public interface IGetFunction {
        IBaseEntity execute(IGetFunctionBinding binding);
    }

    public class EachAttributeBinding {

        public IBaseEntity baseEntitySaving;
        public IBaseEntity baseEntityLoaded;
        public String attributeName;
        public IMetaAttribute childMetaAttribute;
        public IMetaType childMetaType;

        public EachAttributeBinding(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, String attributeName, IMetaAttribute childMetaAttribute, IMetaType childMetaType) {
            this.baseEntitySaving = baseEntitySaving;
            this.baseEntityLoaded = baseEntityLoaded;
            this.attributeName = attributeName;
            this.childMetaAttribute = childMetaAttribute;
            this.childMetaType = childMetaType;
        }

    }

    public class IGetFunctionBinding {

        public IBaseEntity baseEntitySaving;
        public IBaseEntity baseEntityLoaded;
        public IMetaClass metaClass;
        public IMetaAttribute metaAttribute;

        public IGetFunctionBinding(IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IMetaClass metaClass, IMetaAttribute metaAttribute) {
            this.baseEntitySaving = baseEntitySaving;
            this.baseEntityLoaded = baseEntityLoaded;
            this.metaClass = metaClass;
            this.metaAttribute = metaAttribute;
        }

    }

}



