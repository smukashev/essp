package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.exceptions.ImmutableElementException;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by emles on 22.08.17
 */
public class ApplyHistoryFactory {

    private final SimpleDateFormat REP_DATES_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");
    public IMetaAttribute metaAttribute;
    public IMetaType metaType;
    public IMetaValue metaValue;
    public IMetaSet childMetaSet;
    protected IPersistableDaoPool persistableDaoPool;
    protected long creditorId;
    protected IBaseEntity baseEntityApplied;
    protected IBaseValue baseValueSaving;
    protected IBaseValue baseValueLoaded;
    protected IBaseEntity baseEntityLoaded;
    protected IBaseEntity baseEntitySaving;
    protected IBaseEntityManager baseEntityManager;
    protected IBaseContainer baseContainer;
    protected IMetaClass metaClass;
    protected IMetaSet metaSet;
    protected IMetaType childMetaType;
    protected IMetaValue childMetaValue;
    protected IMetaClass childMetaClass;
    protected IBaseValueDao valueDao;
    protected CompareFactory compareFactory;
    private Boolean testMode = false;
    private HistoricalBaseValueDSLFactory lastBase = null;
    private InitializingBaseValueDSLFactory lastApplied = null;
    private HistoricalBaseValueDSLFactory lastExisting = null;
    private HistoricalBaseValueDSLFactory lastClosed = null;
    private HistoricalBaseValueDSLFactory lastPrevious = null;
    private HistoricalBaseValueDSLFactory lastNext = null;
    private HistoricalBaseValueDSLFactory lastLast = null;
    private Boolean with = false;
    private IBaseValue withPersistable = null;
    private Class withPersistableDaoClass = null;
    private Set<UUID> processedValue = new HashSet<>();
    private Set<Long> processedEntity = new HashSet<>();
    private Map<Integer, EventsChainInfo> chain = new TreeMap<>();

    public ApplyHistoryFactory(Boolean testMode, IBaseEntityManager baseEntityManager) {

        this.testMode = testMode;

        this.baseEntityManager = baseEntityManager;

    }

    public ApplyHistoryFactory(Boolean testMode, long creditorId, IBaseEntity baseEntitySaving, IBaseEntity baseEntityLoaded, IBaseEntityManager baseEntityManager, IPersistableDaoPool persistableDaoPool) {

        this.testMode = testMode;

        this.creditorId = creditorId;

        this.baseEntitySaving = baseEntitySaving;
        this.baseEntityLoaded = baseEntityLoaded;

        this.metaClass = baseEntitySaving.getMeta();

        this.baseEntityManager = baseEntityManager;

        this.persistableDaoPool = persistableDaoPool;

        if (baseValueSaving != null)
            this.valueDao = persistableDaoPool
                    .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public ApplyHistoryFactory(Boolean testMode, long creditorId,
                               IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                               IBaseEntityManager baseEntityManager, IPersistableDaoPool persistableDaoPool) {

        this.testMode = testMode;

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
            this.metaSet = (IMetaSet) metaType;
        } catch (Exception e) {
        }
        try {
            this.childMetaType = metaSet.getMemberType();
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
        try {
            this.childMetaSet = (IMetaSet) childMetaType;
        } catch (Exception e) {
        }

        this.persistableDaoPool = persistableDaoPool;

        this.valueDao = persistableDaoPool
                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public void initFromChild(String attrName) {

        this.baseValueSaving = baseEntitySaving.getBaseValue(attrName);
        this.baseValueLoaded = baseEntityLoaded.getBaseValue(attrName);

        try {
            this.baseContainer = baseValueSaving.getBaseContainer();
        } catch (Exception e) {
        }
        this.metaClass = baseEntitySaving.getMeta();
        this.metaAttribute = metaClass.getMetaAttribute(attrName);
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
            this.metaSet = (IMetaSet) metaType;
        } catch (Exception e) {
        }

    }

    public CompareFactory getCompareFactory() {
        if (compareFactory == null) createCompareFactory();
        return compareFactory;
    }

    public CompareFactory createCompareFactory() {
        compareFactory = new CompareFactory(this);
        return compareFactory;
    }

    public CompareFactory createCompareFactory(String attrName) {
        compareFactory = new CompareFactory(baseEntitySaving, attrName);
        return compareFactory;
    }

    public IBaseEntity childEntityFrom(IBaseValue value) {
        try {
            return (IBaseEntity) value.getValue();
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet childFrom(IBaseValue value) {
        try {
            return (IBaseSet) value.getValue();
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet childNew(IBaseValue value) {
        try {
            return new BaseSet(value.getId(), childMetaType, creditorId);
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet childNew(IBaseSet value) {
        try {
            return new BaseSet(value.getId(), childMetaType, creditorId);
        } catch (Exception e) {
        }
        return null;
    }

    public IBaseSet childNew() {
        try {
            return new BaseSet(childMetaType, creditorId);
        } catch (Exception e) {
        }
        return null;
    }

    public HistoricalBaseValueDSLFactory base() {
        if (this.lastBase != null)
            return this.lastBase;
        HistoricalBaseValueDSLFactory base = new HistoricalBaseValueDSLFactory(this).fromBase();
        this.lastBase = base;
        return base;
    }

    public HistoricalBaseValueDSLFactory base(IBaseValue from) {
        HistoricalBaseValueDSLFactory base = new HistoricalBaseValueDSLFactory(this).fromBase(from);
        this.lastBase = base;
        return base;
    }

    public HistoricalBaseValueDSLFactory base(IBaseEntity from) {
        HistoricalBaseValueDSLFactory base = new HistoricalBaseValueDSLFactory(this).fromBase(from);
        this.lastBase = base;
        return base;
    }

    public HistoricalBaseValueDSLFactory base(IBaseSet from) {
        HistoricalBaseValueDSLFactory base = new HistoricalBaseValueDSLFactory(this).fromBase(from);
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
        processedValue.add(value.getUuid());
    }

    public Boolean contains(IBaseValue value) {
        return processedValue.contains(value.getUuid());
    }

    public void processed(IBaseEntity value) {
        processedEntity.add(value.getId());
    }

    public Boolean contains(IBaseEntity value) {
        return processedEntity.contains(value.getUuid());
    }

    public void eachAttribute(IEachAttribute attribute) {

        IBaseEntity baseEntityLoaded = (IBaseEntity) baseValueLoaded.getValue();
        IBaseEntity baseEntitySaving = new BaseEntity(baseEntityLoaded, baseValueSaving.getRepDate());

        for (String attributeName : metaClass.getAttributeNames()) {
            IMetaAttribute childMetaAttribute = metaClass.getMetaAttribute(attributeName);
            IMetaType childMetaType = childMetaAttribute.getMetaType();

            attribute.execute(new EachAttributeBinding(baseEntitySaving, baseEntityLoaded, attributeName, childMetaAttribute, childMetaType), this, this.getCompareFactory());

        }

    }

    public IBaseEntity get(IGetFunction function, IBaseEntity baseEntityLoaded, IBaseEntity baseEntitySaving) {
        return function.execute(new IGetFunctionBinding(baseEntitySaving, baseEntityLoaded, metaClass, metaAttribute), this, this.getCompareFactory());
    }

    public IBaseEntity get(IGetFunction function) {
        return function.execute(new IGetFunctionBinding(baseEntitySaving, baseEntityLoaded, metaClass, metaAttribute), this, this.getCompareFactory());
    }

    public Object castedValue(IBaseValue value) {
        if (metaValue == null) return value.getValue();
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) value.getValue()).getTime()) : value.getValue();
    }

    public Object castedValue(IMetaValue metaValue, IBaseValue value) {
        if (metaValue == null) return value.getValue();
        return metaValue.getTypeCode() == DataTypes.DATE ?
                new Date(((Date) value.getValue()).getTime()) : value.getValue();
    }

    public Object newValue(IBaseValue value) {
        if (value.getNewBaseValue() != null)
            return value.getNewBaseValue().getValue();
        else return null;
    }

    private void addSimpleHistory(String method) {
        baseEntityManager.increment();
        chain.put(baseEntityManager.level(), new EventsChainInfo(baseEntityManager.level(), method, null, null, null));
        baseEntityManager.addHistory(chain.get(baseEntityManager.level()).getBegin());
    }

    private void addValueHistory(String method) {
        baseEntityManager.increment();
        chain.put(baseEntityManager.level(), new EventsChainInfo(baseEntityManager.level(), method, baseValueSaving.getId(), baseValueSaving.getMetaAttribute().getName(), baseValueSaving.getMetaAttribute().getHistoryType()));
        baseEntityManager.addHistory(chain.get(baseEntityManager.level()).getBegin());
    }

    private void addEntityHistory(String method) {
        baseEntityManager.increment();
        chain.put(baseEntityManager.level(), new EventsChainInfo(baseEntityManager.level(), method, baseEntitySaving.getId(), baseEntitySaving.getMeta().getClassName()));
        baseEntityManager.addHistory(chain.get(baseEntityManager.level()).getBegin());
    }

    public void beginApply() {
        if (!testMode) return;
        addEntityHistory("Apply");
    }

    public void beginApplyBaseEntityBasic() {
        if (!testMode) return;
        addEntityHistory("Apply BaseEntity Basic");
    }

    public void beginApplyBaseValueBasic() {
        if (!testMode) return;
        addValueHistory("Apply BaseValue Basic");
    }

    public void beginApplyBaseEntityAdvanced() {
        if (!testMode) return;
        addEntityHistory("Apply BaseEntity Advanced");
    }

    public void beginApplySimpleValue() {
        if (!testMode) return;
        addValueHistory("Apply SimpleValue");
    }

    public void beginApplyComplexValue() {
        if (!testMode) return;
        addValueHistory("Apply ComplexValue");
    }

    public void beginApplySimpleSet() {
        if (!testMode) return;
        addValueHistory("Apply SimpleSet");
    }

    public void beginApplyComplexSet() {
        if (!testMode) return;
        addValueHistory("Apply ComplexSet");
    }

    public void beginApplyToDb() {
        if (!testMode) return;
        addSimpleHistory("Apply To Db");
    }

    public void end() {
        if (!testMode) return;
        baseEntityManager.addHistory(chain.get(baseEntityManager.level()).getEnd());
        baseEntityManager.decrement();
    }

    public void event(String message) {
        if (!testMode) return;
        String tabs = "";
        for (int i = 0; i <= baseEntityManager.level(); i++) tabs += "|\t";
        baseEntityManager.addHistory(tabs + "EVENT [" + message + "]");
    }

    public void persistable(String name, IPersistable persistable) {
        if (!testMode) return;
        String tabs = "";
        for (int i = 0; i <= baseEntityManager.level(); i++) tabs += "|\t";
        String sPersistable = persistable.toString().replaceAll("\n", "\n" + tabs);
        baseEntityManager.addHistory(tabs + "PERSISTABLE " + name + ": " + sPersistable);
    }

    private String date_info(IPersistable persistable) {
        String sPersistable = Long.toString(persistable.getId()) + " ";
        if (persistable instanceof IBaseEntity) {
            IBaseEntity entity = (IBaseEntity) persistable;
            if (entity.getMeta() != null) sPersistable += entity.getMeta().getClassName() + " ";
            sPersistable += REP_DATES_DATE_FORMAT.format(entity.getReportDate());
        } else if (persistable instanceof IBaseSet) {
        } else if (persistable instanceof IBaseValue) {
            IBaseValue value = (IBaseValue) persistable;
            if (value.getMetaAttribute() != null) sPersistable += value.getMetaAttribute().getName() + " ";
            sPersistable += REP_DATES_DATE_FORMAT.format((value).getRepDate());
        }
        return sPersistable;
    }

    public void rep_dates(String firstName, IPersistable first, String secondName, IPersistable second) {
        if (!testMode) return;
        String tabs = "";
        for (int i = 0; i <= baseEntityManager.level(); i++) tabs += "|\t";
        String sFirst = firstName + " " + date_info(first);
        String sSecond = secondName + " " + date_info(second);
        baseEntityManager.addHistory(tabs + "COMPARE DATES [" + sFirst + "] <> [" + sSecond + "]");
    }

    public RuntimeException ErrorIE(IBaseEntity baseEntity) {
        return new ImmutableElementException(baseEntity);
    }

    public RuntimeException ErrorCEFI(IBaseEntity baseEntity) {
        if (baseEntity.getMeta().isReference()) {
            String keyValue = null;
            String[] possibleKeys = new String[]{"no_", "short_name", "code"};
            for (int i = 0; i < possibleKeys.length && keyValue == null; i++) {
                if (baseEntity.getMeta().hasAttribute(possibleKeys[i])) {
                    keyValue = ((String) baseEntity.getEl(possibleKeys[i]));
                }
            }
            if (keyValue != null)
                return new KnownException(Errors.compose(Errors.E298, baseEntity.getMeta().getClassTitle(), keyValue, baseEntity.getReportDate()));
        }
        return new KnownException(Errors.compose(Errors.E57, baseEntity.getId(), baseEntity.getReportDate()));
    }

    public RuntimeException Error02() {
        return new UnsupportedOperationException(Errors.compose(Errors.E2));
    }

    public RuntimeException ErrorUO() {
        return new UnsupportedOperationException("Новое и старое значения являются NULL(" +
                baseValueSaving.getMetaAttribute().getName() + "). Недопустимая операция;");
    }

    public RuntimeException Error56() {
        return new UnsupportedOperationException(Errors.compose(Errors.E56, baseEntitySaving.getId()));
    }

    public RuntimeException Error57() {
        return new UnsupportedOperationException(Errors.compose(Errors.E57,
                baseEntityLoaded.getId(), baseEntityLoaded.getBaseEntityReportDate().getReportDate()));
    }

    public RuntimeException Error59() {
        return new IllegalStateException(Errors.compose(Errors.E59, metaAttribute.getName()));
    }

    public RuntimeException Error60() {
        return new IllegalStateException(Errors.compose(Errors.E60));
    }

    public RuntimeException Error63(IBaseEntity baseEntity) {
        return new RuntimeException(Errors.compose(Errors.E63, baseEntity.getId(),
                baseEntity.getReportDate()));
    }

    public RuntimeException Error64(IBaseEntity baseEntity) {
        return new IllegalStateException(Errors.compose(Errors.E64, baseEntity.getMeta().getClassName()));
    }

    public RuntimeException Error66() {
        return new IllegalStateException(Errors.compose(Errors.E66, metaAttribute.getName()));
    }

    public RuntimeException Error67() {
        return new IllegalStateException(Errors.compose(Errors.E67, metaAttribute.getName()));
    }

    public RuntimeException Error68() {
        return new IllegalStateException(Errors.compose(Errors.E68, metaAttribute.getName()));
    }

    public RuntimeException Error69() {
        return new RuntimeException(Errors.compose(Errors.E69, metaAttribute.getName()));
    }

    public RuntimeException Error70(IBaseValue baseValue) {
        return new IllegalStateException(Errors.compose(Errors.E70, baseValue.getMetaAttribute().getName()));
    }

    public RuntimeException Error71() {
        return new IllegalStateException(Errors.compose(Errors.E71, metaAttribute.getName()));
    }

    public RuntimeException Error72() {
        return new IllegalStateException(Errors.compose(Errors.E72, metaAttribute.getName()));
    }

    public RuntimeException Error73() {
        return new IllegalStateException(Errors.compose(Errors.E73, metaAttribute.getName()));
    }

    public RuntimeException Error74() {
        return new IllegalStateException(Errors.compose(Errors.E74));
    }

    public RuntimeException Error75() {
        return new UnsupportedOperationException(Errors.compose(Errors.E75, baseValueSaving.getMetaAttribute().getName()));
    }

    public RuntimeException Error76(IPersistable persistable, Exception exception) {
        return new IllegalStateException(Errors.compose(Errors.E76, persistable, exception.getMessage()));
    }

    public RuntimeException Error77(IPersistable persistable, Exception exception) {
        return new IllegalStateException(Errors.compose(Errors.E77, persistable, exception.getMessage()));
    }

    public RuntimeException Error78(IPersistable persistable, Exception exception) {
        return new IllegalStateException(Errors.compose(Errors.E78, persistable, exception.getMessage()));
    }

    public RuntimeException Error299(IBaseValue baseValue) {
        if (baseValue.getValue() instanceof IBaseEntity)
            return new UnsupportedOperationException(Errors.compose(Errors.E299, DataTypes.formatDate(baseValue.getRepDate()), ((IBaseEntity) baseValue.getValue()).getId()));
        else
            return new UnsupportedOperationException(Errors.compose(Errors.E299, DataTypes.formatDate(baseValue.getRepDate()), baseValue.getValue()));
    }

    public interface IEachAttribute {
        void execute(EachAttributeBinding binding, ApplyHistoryFactory history, CompareFactory IS);
    }

    public interface IGetFunction {
        IBaseEntity execute(IGetFunctionBinding binding, ApplyHistoryFactory history, CompareFactory IS);
    }

    class EventsChainInfo {

        Integer level;
        String method;
        Long id;
        String name;
        HistoryType historyType;

        EventsChainInfo(Integer level, String method, Long id, String name) {
            this.level = level;
            this.method = method;
            this.id = id;
            this.name = name;
        }

        EventsChainInfo(Integer level, String method, Long id, String name, HistoryType historyType) {
            this(level, method, id, name);
            this.historyType = historyType;
        }

        String getBegin() {
            String tabs = "";
            for (int i = 0; i < level; i++) tabs += "|\t";
            return tabs + "BEGIN [" + method + "]" + (id == null ? "" : " FOR /" + name + ": " + Long.toString(id) + "/ " + (historyType == null ? "" : historyType.toString()));
        }

        String getEnd() {
            String tabs = "";
            for (int i = 0; i < level; i++) tabs += "|\t";
            return tabs + "END [" + method + "]";
        }

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



