package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;
import java.util.*;

/**
 * @author k.tulbassiyev
 */
public class BaseSet extends BaseContainer implements IBaseSet {
    private UUID uuid = UUID.randomUUID();

    /**
     * Holds data about entity structure
     *
     * @see kz.bsbnb.usci.eav.model.meta.impl.MetaClass
     */
    private IMetaType metaType;

    private Map<String, IBaseValue> values = new HashMap<String, IBaseValue>();

    private long level = 1;

    private boolean last = true;

    /**
     * Initializes entity with a class name.
     *
     * @param metaType MetaClass of the entity..
     */
    public BaseSet(IMetaType metaType) {
        super(BaseContainerType.BASE_SET);
        this.metaType = metaType;
    }

    public BaseSet(long id, IMetaType metaType) {
        super(id, BaseContainerType.BASE_SET);
        this.metaType = metaType;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public IMetaType getMemberType(String name) {
        return metaType;
    }

    public IMetaType getMemberType() {
        return metaType;
    }

    public Set<String> getAttributes() {
        return values.keySet();
    }

    @Override
    public long getLevel() {
        return level;
    }

    @Override
    public void setLevel(long level) {
        this.level = level;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    @Override
    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public void put(String name, IBaseValue value) {
        if (name == null) {
            UUID uuid = UUID.randomUUID();
            put(uuid.toString(), value);
        }
        value.setBaseContainer(this);

        values.put(name, value);
    }

    public BaseSet put(IBaseValue value) {
        value.setBaseContainer(this);

        UUID uuid = UUID.randomUUID();
        put(uuid.toString(), value);

        return this;
    }

    public void remove(String identifier) {
        values.remove(identifier);
    }

    @Override
    public Collection<IBaseValue> get() {
        return values.values();
    }

    public IBaseValue getBaseValue(String identifier) {
        return values.get(identifier);
    }

    public int getValueCount() {
        return values.size();
    }

    @Override
    public String toString() {
        String str = "[";
        boolean first = true;

        for (IBaseValue value : values.values()) {
            if (first) {
                str += value.getValue().toString();
                first = false;
            } else {
                str += ", " + value.getValue().toString();
            }
        }

        str += "]";

        return str;
    }

    public Object getElSimple(String filter) {
        if (metaType.isComplex() || metaType.isSet()) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E35));
        }

        for (IBaseValue value : values.values()) {
            Object innerValue = value.getValue();
            if (innerValue == null) {
                continue;
            }

            if (((BaseValue) value).equalsToString(filter, ((MetaValue) metaType).getTypeCode()))
                return innerValue;
        }

        return null;
    }

    public Object getElComplex(String filter) {
        if (!metaType.isComplex() || metaType.isSet()) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.E33));
        }

        StringTokenizer tokenizer = new StringTokenizer(filter, ",");

        Object valueOut = null;
        HashMap<String, String> params = new HashMap<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            StringTokenizer innerTokenizer = new StringTokenizer(token, "=");

            String fieldName = innerTokenizer.nextToken().trim();
            if (!innerTokenizer.hasMoreTokens())
                throw new IllegalStateException(Errors.getMessage(Errors.E34));

            String fieldValue = innerTokenizer.nextToken().trim();

            params.put(fieldName, fieldValue);
        }

        for (IBaseValue value : values.values()) {
            Object innerValue = value.getValue();
            if (innerValue == null) {
                continue;
            }

            if (((BaseEntity) innerValue).equalsToString(params))
                return innerValue;
        }

        return valueOut;
    }

    public Object getEl(String filter) {
        if (metaType.isComplex())
            return getElComplex(filter);
        return getElSimple(filter);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(getClass() == obj.getClass())) {
            return false;
        }

        BaseSet that = (BaseSet) obj;

        IMetaType thisMetaType = this.getMemberType();
        IMetaType thatMetaType = that.getMemberType();
        if (!thisMetaType.equals(thatMetaType)) {
            return false;
        }

        boolean date = false;
        if (!thisMetaType.isSet() && !thisMetaType.isComplex()) {
            MetaValue metaValue = (MetaValue) thisMetaType;
            if (metaValue.getTypeCode().equals(DataTypes.DATE)) {
                date = true;
            }
        }

        if (this.getValueCount() != that.getValueCount()) {
            return false;
        }

        Set<UUID> uuids = new HashSet<>();
        for (IBaseValue thisBaseValue : this.get()) {
            boolean found = false;

            for (IBaseValue thatBaseValue : that.get()) {
                if (!uuids.contains(thatBaseValue.getUuid())) {
                    Object thisObject = thisBaseValue.getValue();
                    if (thisObject == null) {
                        throw new RuntimeException(Errors.getMessage(Errors.E32));
                    }

                    Object thatObject = thatBaseValue.getValue();
                    if (thatObject == null) {
                        throw new RuntimeException(Errors.getMessage(Errors.E32));
                    }

                    if (date) {
                        if (DataUtils.compareBeginningOfTheDay((Date) thisObject, (Date) thatObject) == 0) {
                            uuids.add(thatBaseValue.getUuid());
                            found = true;
                        }
                    } else {
                        if (thisObject.equals(thatObject)) {
                            uuids.add(thatBaseValue.getUuid());
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    public BaseSet clone() {
        BaseSet baseSetCloned;
        try {
            baseSetCloned = (BaseSet) super.clone();

            HashMap<String, IBaseValue> valuesCloned = new HashMap<>();
            Iterator<String> items = values.keySet().iterator();

            while (items.hasNext()) {
                String attribute = items.next();

                IBaseValue baseValue = values.get(attribute);
                IBaseValue baseValueCloned = ((BaseValue) baseValue).clone();
                baseValueCloned.setBaseContainer(baseSetCloned);
                valuesCloned.put(attribute, baseValueCloned);
            }

            baseSetCloned.values = valuesCloned;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.getMessage(Errors.E31));
        }
        return baseSetCloned;
    }

    @Override
    public boolean isSet() {
        return true;
    }
}
