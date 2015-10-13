package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;

import java.util.*;

public class BaseValue<T> extends Persistable implements IBaseValue<T> {

    public static final boolean DEFAULT_LAST = true;

    public static final boolean DEFAULT_CLOSED = false;

    private UUID uuid = UUID.randomUUID();

    private IBaseContainer baseContainer;

    private long creditorId;

    private IMetaAttribute metaAttribute;

    private IBaseValue newBaseValue = null;

    private T value;

    private Date reportDate;

    private boolean last = DEFAULT_LAST;

    private boolean closed = DEFAULT_CLOSED;

    public BaseValue(long creditorId, Date reportDate, T value) {
        this(DEFAULT_ID, creditorId, reportDate, value, DEFAULT_CLOSED, DEFAULT_LAST);
    }

    public BaseValue(long creditorId, Date reportDate, T value, boolean closed, boolean last) {
        this(DEFAULT_ID, creditorId, reportDate, value, closed, last);
    }

    public BaseValue(long id, long creditorId, Date reportDate, T value) {
        this(id, creditorId, reportDate, value, DEFAULT_CLOSED, DEFAULT_LAST);
    }

    //used in rules
    public BaseValue(Date reportDate, T value) {
        this(0, reportDate, value);
    }

    //used in rules
    public BaseValue(T value) {
        this(new Date(), value);
    }

    public BaseValue(long id, long creditorId, Date reportDate, T value, boolean closed,
                     boolean last) {
        super(id);

        if (reportDate == null)
            throw new IllegalArgumentException
                    ("reportDate is null. Initialization of the BaseValue ​​is not possible.");

        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.creditorId = creditorId;
        this.value = value;
        this.reportDate = newReportDate;
        this.closed = closed;
        this.last = last;
    }

    @Override
    public IBaseContainer getBaseContainer() {
        return baseContainer;
    }

    @Override
    public void setBaseContainer(IBaseContainer baseContainer) {
        this.baseContainer = baseContainer;
    }

    @Override
    public long getCreditorId() {
        return creditorId;
    }

    @Override
    public void setCreditorId(long creditorId) {
        this.creditorId = creditorId;
    }

    @Override
    public IMetaAttribute getMetaAttribute() {
        return metaAttribute;
    }

    @Override
    public void setMetaAttribute(IMetaAttribute metaAttribute) {
        this.metaAttribute = metaAttribute;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    @Override
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void setNewBaseValue(IBaseValue baseValue) {
        this.newBaseValue = baseValue;
    }

    @Override
    public IBaseValue getNewBaseValue() {
        return newBaseValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;
        else {
            BaseValue that = (BaseValue) obj;
            return value != null ? value.equals(that.value) : that.value == null;
        }
    }

    @Override
    public Date getRepDate() {
        return reportDate;
    }

    public void setRepDate(Date reportDate) {
        Date newReportDate = (Date) reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.reportDate = newReportDate;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + reportDate.hashCode();
        return result;
    }

    public boolean equalsByValue(IBaseValue baseValue) {
        IMetaAttribute thisMetaAttribute = this.getMetaAttribute();
        IMetaAttribute thatMetaAttribute = baseValue.getMetaAttribute();

        if (thisMetaAttribute == null || thatMetaAttribute == null)
            throw new IllegalStateException("Comparison values of two instances of BaseValue " +
                    "without meta data is not possible.");

        return thisMetaAttribute.getId() == thatMetaAttribute.getId() &&
                this.equalsByValue(thisMetaAttribute.getMetaType(), baseValue);

    }

    public boolean equalsByValue(IMetaType metaType, IBaseValue baseValue) {
        Object thisValue = this.getValue();
        Object thatValue = baseValue.getValue();

        if (thisValue == null || thatValue == null)
            throw new RuntimeException("Comparison values of two instances of BaseValue " +
                    "with null values is not possible.");

        if (metaType.isSetOfSets())
            throw new UnsupportedOperationException("Not yet implemented");

        if (metaType.isComplex()) {
            if (metaType.isSet()) {
                IBaseSet thisBaseSet = (IBaseSet) thisValue;
                IBaseSet thatBaseSet = (IBaseSet) thatValue;

                List<Long> thisIds = new ArrayList<>();
                for (IBaseValue thisChildBaseValue : thisBaseSet.get()) {
                    IBaseEntity thisBaseEntity = (IBaseEntity) thisChildBaseValue.getValue();
                    thisIds.add(thisBaseEntity.getId());
                }

                List<Long> thatIds = new ArrayList<>();
                for (IBaseValue thatChildBaseValue : thatBaseSet.get()) {
                    BaseEntity thatBaseEntity = (BaseEntity) thatChildBaseValue.getValue();
                    thatIds.add(thatBaseEntity.getId());
                }
                Collections.sort(thatIds);
                Collections.sort(thatIds);

                return thisIds.equals(thatIds);
            } else {
                IBaseEntity thisBaseEntity = (IBaseEntity) thisValue;
                IBaseEntity thatBaseEntity = (IBaseEntity) thatValue;
                return thisBaseEntity.getId() == thatBaseEntity.getId() && thisBaseEntity.getId() > 0;
            }
        } else {
            if (metaType.isSetOfSets()) {
                throw new UnsupportedOperationException("Не реализовано;");
            }

            if (metaType.isSet()) {
                IMetaSet metaSet = (IMetaSet) metaType;
                IMetaType childMetaType = metaSet.getMemberType();

                IBaseSet thisBaseSet = (IBaseSet) thisValue;
                IBaseSet thatBaseSet = (IBaseSet) thatValue;

                boolean baseValueNotFound;
                Set<UUID> processedUuids = new HashSet<>();
                for (IBaseValue thisBaseValue : thisBaseSet.get()) {
                    baseValueNotFound = true;
                    for (IBaseValue thatBaseValue : thatBaseSet.get()) {
                        if (processedUuids.contains(thatBaseValue.getUuid())) {
                            continue;
                        }

                        if (thisBaseValue.equalsByValue(childMetaType, thatBaseValue)) {
                            processedUuids.add(thatBaseValue.getUuid());
                            baseValueNotFound = false;
                            break;
                        }
                    }

                    if (baseValueNotFound) {
                        return false;
                    }
                }

                return true;
            } else {
                IMetaValue metaValue = (IMetaValue) metaType;
                return metaValue.getTypeCode() == DataTypes.DATE ?
                        DataUtils.compareBeginningOfTheDay((Date) thisValue, (Date) thatValue) == 0 :
                        thisValue.equals(thatValue);
            }
        }
    }

    public boolean equalsToString(String str, DataTypes type) {
        switch (type) {
            case INTEGER:
                if (value.equals(Integer.parseInt(str)))
                    return true;
                break;
            case DATE:
                //TODO: add date format
                throw new UnsupportedOperationException("DATE is not supported!;");
            case STRING:
                if (value.equals(str))
                    return true;
                break;
            case BOOLEAN:
                if (value.equals(Boolean.parseBoolean(str)))
                    return true;
                break;
            case DOUBLE:
                if (value.equals(Double.parseDouble(str)))
                    return true;
                break;
            default:
                throw new IllegalStateException("Unknown DataType: " + type);
        }

        return false;
    }

    @Override
    public BaseValue clone() {
        BaseValue baseValue;
        try {
            baseValue = (BaseValue) super.clone();
            baseValue.setRepDate((Date) reportDate.clone());

            if (value != null) {
                if (value instanceof BaseEntity) {
                    baseValue.setValue(((BaseEntity) value).clone());
                }
                if (value instanceof BaseSet) {
                    baseValue.setValue(((BaseSet) value).clone());
                }
                if (value instanceof Date) {
                    baseValue.setValue(((Date) value).clone());
                }
            }
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("BaseValue class does not implement interface Cloneable.");
        }
        return baseValue;
    }

    @Override
    public String toString() {
        if (getValue() == null)
            return null;

        return getValue().toString();
    }
}
