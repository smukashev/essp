package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.util.DataUtils;

/**
 * Created by emles on 30.08.17
 */
public class CompareFactory {

    protected IBaseContainer baseContainer;
    protected IMetaAttribute metaAttribute;
    protected IMetaType metaType;
    protected IMetaClass metaClass;
    protected IMetaValue metaValue;
    protected IMetaSet metaSet;
    protected IMetaType childMetaType;
    protected IMetaValue childMetaValue;
    protected IMetaClass childMetaClass;
    protected IMetaSet childMetaSet;

    public CompareFactory(ApplyHistoryFactory history) {
        this(history == null ? null : history.baseValueSaving);
    }

    public CompareFactory(IBaseValue baseValue) {

        if (baseValue == null) return;

        this.baseContainer = baseValue.getBaseContainer();
        this.metaAttribute = baseValue.getMetaAttribute();
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

    }

    public boolean NEW(IPersistable value) {
        return value.getId() < 1;
    }

    public boolean NOT_NEW(IPersistable value) {
        return value.getId() > 0;
    }

    public boolean EQUALS(IPersistable first, IPersistable second) {
        return first.equals(second);
    }

    public boolean ID_EQUALS(IPersistable first, IPersistable second) {
        return first.getId() == second.getId();
    }

    public boolean ID_NOT_EQUALS(IPersistable first, IPersistable second) {
        return !ID_EQUALS(first, second);
    }

    public boolean SEARCHABLE(IBaseEntity value) {
        return value.getMeta().isSearchable();
    }

    public boolean NOT_SEARCHABLE(IBaseEntity value) {
        return !value.getMeta().isSearchable();
    }

    public boolean EMPTY(Object value) {
        if (value == null) return true;
        if (value instanceof String && ((String) value).trim().length() == 0) return true;
        return false;
    }

    public boolean NOT_EMPTY(Object value) {
        return !EMPTY(value);
    }

    public boolean VALUE_EMPTY(IBaseValue value) {
        return value.getValue() == null;
    }

    public boolean VALUE_NOT_EMPTY(IBaseValue value) {
        return !VALUE_EMPTY(value);
    }

    public boolean NEW_VALUE_EMPTY(IBaseValue value) {
        return value.getNewBaseValue() == null;
    }

    public boolean NEW_VALUE_NOT_EMPTY(IBaseValue value) {
        return !NEW_VALUE_EMPTY(value);
    }

    public boolean COMPLEX() {
        return metaType.isComplex();
    }

    public boolean SIMPLE() {
        return !COMPLEX();
    }

    public boolean SET() {
        return metaType.isSet();
    }

    public boolean ATTRIBUTE() {
        return !SET();
    }

    public boolean META_ATTRIBUTE_EMPTY() {
        return metaAttribute == null;
    }

    public boolean META_ATTRIBUTE_NOT_EMPTY() {
        return !META_ATTRIBUTE_EMPTY();
    }

    public boolean BASE_CONTAINER_EMPTY() {
        return baseContainer == null;
    }

    public boolean BASE_CONTAINER_NOT_EMPTY() {
        return !BASE_CONTAINER_EMPTY();
    }

    public boolean BASE_CONTAINER_ENTITY() {
        return baseContainer.getBaseContainerType() == BaseContainerType.BASE_ENTITY;
    }

    public boolean BASE_CONTAINER_SET() {
        return baseContainer.getBaseContainerType() == BaseContainerType.BASE_SET;
    }

    public boolean VALUE_COUNT_EMPTY(IBaseSet value) {
        return value.getValueCount() == 0;
    }

    public boolean VALUE_COUNT_NOT_EMPTY(IBaseSet value) {
        return !VALUE_COUNT_EMPTY(value);
    }

    public boolean VALUE_COUNT_EMPTY(IBaseEntity value) {
        return value.getValueCount() == 0;
    }

    public boolean VALUE_COUNT_NOT_EMPTY(IBaseEntity value) {
        return !VALUE_COUNT_EMPTY(value);
    }

    public boolean LAST(IBaseSet value) {
        return value.isLast();
    }

    public boolean NOT_LAST(IBaseSet value) {
        return !LAST(value);
    }

    public boolean LAST(IBaseValue value) {
        return value.isLast();
    }

    public boolean NOT_LAST(IBaseValue value) {
        return !LAST(value);
    }

    public boolean CLOSED(IBaseValue value) {
        return value.isClosed();
    }

    public boolean NOT_CLOSED(IBaseValue value) {
        return !CLOSED(value);
    }

    public boolean META_ATTRIBUTE_NULL() {
        return metaAttribute == null;
    }

    public boolean META_ATTRIBUTE_NOT_NULL() {
        return !META_ATTRIBUTE_NULL();
    }

    public boolean IMMUTABLE() {
        return metaAttribute.isImmutable();
    }

    public boolean NOT_IMMUTABLE() {
        return !IMMUTABLE();
    }

    public boolean NULLABLE() {
        return metaAttribute.isNullable();
    }

    public boolean NOT_NULLABLE() {
        return !NULLABLE();
    }

    public boolean CUMULATIVE() {
        return metaAttribute.isCumulative();
    }

    public boolean NOT_CUMULATIVE() {
        return !CUMULATIVE();
    }

    public boolean DISABLED() {
        return metaAttribute.isDisabled();
    }

    public boolean NOT_DISABLED() {
        return !DISABLED();
    }

    public boolean FINAL() {
        return metaAttribute.isFinal();
    }

    public boolean NOT_FINAL() {
        return !FINAL();
    }

    public boolean KEY() {
        return metaAttribute.isKey();
    }

    public boolean NOT_KEY() {
        return !KEY();
    }

    public boolean NULLABLE_KEY() {
        return metaAttribute.isNullableKey();
    }

    public boolean NOT_NULLABLE_KEY() {
        return !NULLABLE_KEY();
    }

    public boolean OPTIONAL_KEY() {
        return metaAttribute.isOptionalKey();
    }

    public boolean NOT_OPTIONAL_KEY() {
        return !OPTIONAL_KEY();
    }

    public boolean REQUIRED() {
        return metaAttribute.isRequired();
    }

    public boolean NOT_REQUIRED() {
        return !REQUIRED();
    }

    public boolean REFERENCE() {
        return metaClass.isReference();
    }

    public boolean NOT_REFERENCE() {
        return !REFERENCE();
    }

    public boolean SEARCHABLE() {
        return metaClass.isSearchable();
    }

    public boolean NOT_SEARCHABLE() {
        return !SEARCHABLE();
    }

    public boolean CLOSABLE() {
        return metaClass.isClosable();
    }

    public boolean NOT_CLOSABLE() {
        return !CLOSABLE();
    }

    public boolean CHILD_REFERENCE() {
        return childMetaClass.isReference();
    }

    public boolean CHILD_NOT_REFERENCE() {
        return !CHILD_REFERENCE();
    }

    public boolean CHILD_CLOSABLE() {
        return childMetaClass.isClosable();
    }

    public boolean CHILD_NOT_CLOSABLE() {
        return !CHILD_CLOSABLE();
    }

    public boolean CHILD_SEARCHABLE() {
        return childMetaClass.isSearchable();
    }

    public boolean CHILD_NOT_SEARCHABLE() {
        return !CHILD_SEARCHABLE();
    }

    public boolean VALUE_EQUALS(IBaseValue first, IBaseValue second) {
        return first.equalsByValue(second);
    }

    public boolean VALUE_NOT_EQUALS(IBaseValue first, IBaseValue second) {
        return !VALUE_EQUALS(first, second);
    }

    public boolean VALUE_EQUALS_BY_CHILD_META(IBaseValue first, IBaseValue second) {
        return first.equalsByValue(childMetaValue, second);
    }

    public DATE COMPARE_DATE(IBaseEntity first, IBaseEntity second) {
        int compare = DataUtils.compareBeginningOfTheDay(first.getReportDate(), second.getReportDate());
        return DATE.valueOf(compare);
    }

    public DATE COMPARE_DATE(IBaseValue first, IBaseValue second) {
        int compare = DataUtils.compareBeginningOfTheDay(first.getRepDate(), second.getRepDate());
        return DATE.valueOf(compare);
    }

    public boolean EQUAL(DATE date) {
        return date.compare == 0;
    }

    public boolean NOT_EQUAL(DATE date) {
        return date.compare != 0;
    }

    public boolean MORE(DATE date) {
        return date.compare == 1;
    }

    public boolean EQUAL_MORE(DATE date) {
        return date.compare >= 0;
    }

    public boolean LESS(DATE date) {
        return date.compare == -1;
    }

    public boolean EQUAL_LESS(DATE date) {
        return date.compare <= 0;
    }

    public enum DATE {

        DATE_LESS(-1),
        DATE_EQUAL(0),
        DATE_MORE(1);

        private int compare;

        DATE(int compare) {
            this.compare = compare;
        }

        public synchronized static DATE valueOf(int compare) {
            for (DATE date : DATE.values()) {
                if (date.compare == compare) return date;
            }
            return null;
        }

    }

}



