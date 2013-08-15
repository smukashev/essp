package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.output.BaseEntityOutput;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DateUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implements EAV entity object. 
 *
 * @version 1.0, 17.01.2013
 * @author a.tkachenko
 * @see MetaClass
 * @see DataTypes
 */
public class BaseEntity extends BaseContainer implements IBaseEntity
{

    Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    /**
     * Reporting date on which instance of BaseEntity was loaded.
     */
    private Date reportDate;

    /**
     * The list of available reporting dates for this instance BaseEntity.
     */
    private Set<Date> availableReportDates = new HashSet<Date>();

    private Set<String> modifiedObjects = new HashSet<String>();

    /**
     * Holds data about entity structure
     * @see MetaClass
     */
    private MetaClass meta;
    
    /**
     * Holds attributes values
     */
    private HashMap<String, IBaseValue> values =
            new HashMap<String, IBaseValue>();

    /**
     * Initializes entity.
     */
    public BaseEntity()
    {

    }

    private Set<String> validationErrors = new HashSet<String>();

    /**
     * Initializes entity with a class name.
     *
     * @param meta MetaClass of the entity..
     */
    public BaseEntity(MetaClass meta, Date reportDate)
    {
        this.reportDate = reportDate;
        this.meta = meta;
    }

    public BaseEntity(long id, MetaClass meta, Date reportDate, Set<Date> availableReportDates)
    {
        super(id);
        this.meta = meta;
        this.availableReportDates = availableReportDates;

        if (reportDate == null)
        {
            throw new IllegalArgumentException("Can not create instance of BaseEntity " +
                    "with report date equal to null.");
        }
        else
        {
            Date newReportDate = (Date)reportDate.clone();
            DateUtils.toBeginningOfTheDay(newReportDate);

            this.reportDate = newReportDate;
        }
    }

    /**
     * Used to retrieve object structure description. Can be used to modify meta.
     * 
     * @return Object structure
     */
    public MetaClass getMeta()
    {
        return meta;
    }

    public Set<Date> getAvailableReportDates() {
        return availableReportDates;
    }

    /**
     * Retrieves key titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     *
     * @param name key name. Must exist in entity meta
     * @return key value, null if value is not set
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     * 	                                or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    public IBaseValue getBaseValue(String name)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        IBaseValue batchValue = values.get(name);

        if(batchValue == null)
            return null;

        return batchValue;
    }

    /**
     * Retrieves key titled <code>name</code>.
     *
     * @param name name key name. Must exist in entity meta
     * @param value new value of the key
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     * 	                                or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    //TODO: Add exception on metaClass mismatch
    @Override
    public void put(final String name, IBaseValue value)
    {
        IMetaType type = meta.getMemberType(name);

        if(type == null)
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + meta.getClassName());

        if (value == null)
            throw new IllegalArgumentException("Value not be equal to null.");

        if (value.getValue() != null)
        {
            Class<?> valueClass = value.getValue().getClass();
            Class<?> expValueClass;

            if (type.isComplex())
                if(type.isSet())
                {
                    expValueClass = BaseSet.class;
                }
                else
                {
                    expValueClass = BaseEntity.class;
                }
            else
            {
                if(type.isSet())
                {
                    MetaSet metaValue = (MetaSet)type;

                    if (type.isSet())
                    {
                        expValueClass = BaseSet.class;
                        valueClass = value.getValue().getClass();
                    }
                    else
                    {
                        expValueClass = metaValue.getTypeCode().getDataTypeClass();
                        valueClass = ((MetaValue)(((BaseSet)value.getValue()).getMemberType())).getTypeCode().
                                getDataTypeClass();
                    }

                }
                else
                {
                    MetaValue metaValue = (MetaValue)type;
                    expValueClass = metaValue.getTypeCode().getDataTypeClass();
                }

            }

            if(expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException("Type mismatch in class: " +
                        meta.getClassName() + ". Needed " + expValueClass + ", got: " +
                        valueClass);
        }

        if (values.containsKey(name))
        {
            if (value.getValue() == null || !value.equals(values.get(name)))
            {
                fireValueChange(name);
            }
        }
        else
        {
            fireValueChange(name);
        }

        values.put(name, value);
    }

    public void remove(String name) {
        values.remove(name);
    }

    @Override
    public Collection<IBaseValue> get() {
        return values.values();
    }

    @Override
    public IMetaType getMemberType(String name) {
        return meta.getMemberType(name);
    }

    /**
     * Set of simple key names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return - set of needed attributes
     */
    public Set<String> getPresentSimpleAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complex key names that are actually set in entity
     *
     * @return - set of needed attributes
     */
    public Set<String> getPresentComplexAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexAttributesNames(), values.keySet());
    }

    /**
     * Set of simpleSet key names that are actually set in entity
     *
     * @param dataType - attributes are filtered by this type
     * @return - set of needed attributes
     */
    public Set<String> getPresentSimpleSetAttributeNames(DataTypes dataType)
    {
        return SetUtils.intersection(meta.getSimpleSetAttributesNames(dataType), values.keySet());
    }

    /**
     * Set of complexSet key names that are actually set in entity
     *
     * @return - set of needed attributes
     */
    public Set<String> getPresentComplexArrayAttributeNames()
    {
        return SetUtils.intersection(meta.getComplexArrayAttributesNames(), values.keySet());
    }

    /**
     * Names of all attributes that are actually set in entity
     * @return - set of needed attributes
     */
    public Set<String> getAttributeNames() {
        return values.keySet();
    }

    public int getAttributeCount() {

        return values.size();
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
        this.availableReportDates.add(reportDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (!(getClass() == obj.getClass()))
        {
            return false;
        }

        BaseEntity that = (BaseEntity) obj;

        int thisAttributeCount = this.getAttributeCount();
        int thatAttributeCount = that.getAttributeCount();

        if (thisAttributeCount != thatAttributeCount)
        {
            return false;
        }

        for (String attribute : values.keySet())
        {
            Object thisObject = this.safeGetValue(attribute).getValue();
            Object thatObject = that.safeGetValue(attribute).getValue();

            if (thisObject == null && thatObject == null)
            {
                continue;
            }

            if (thisObject == null || thatObject == null)
            {
                return false;
            }

            IMetaType metaType = this.getMemberType(attribute);
            if (!metaType.isSet() && !metaType.isComplex())
            {
                MetaValue metaValue = (MetaValue)metaType;
                if (metaValue.getTypeCode().equals(DataTypes.DATE))
                {
                    DateUtils.toBeginningOfTheDay((Date)thisObject);
                    DateUtils.toBeginningOfTheDay((Date)thatObject);
                }
            }

            if (!thisObject.equals(thatObject))
            {
                return false;
            }
        }

        return true;
    }

    public IBaseValue safeGetValue(String name)
    {
        if (this.getAttributeNames().contains(name))
        {
            return getBaseValue(name);
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return BaseEntityOutput.toString(this);
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + meta.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    public Object getEl(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        BaseEntity entity = this;
        MetaClass theMeta = meta;
        Object valueOut = null;

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            String arrayIndexes = "";

            if (token.contains("["))
            {
                arrayIndexes = token.substring(token.indexOf("[") + 1, token.length() - 1);
                token = token.substring(0, token.indexOf("["));
            }

            IMetaAttribute attribute = theMeta.getMetaAttribute(token);
            IMetaType type = attribute.getMetaType();

            if (entity == null)
                return null;

            IBaseValue value = entity.getBaseValue(token);

            if (value == null || value.getValue() == null) {
                valueOut = null;
                break;
            }

            valueOut = value.getValue();

            if (type.isSet())
            {
                valueOut = ((BaseSet)valueOut).getEl(arrayIndexes);
                type = ((MetaSet)type).getMemberType();
            }

            if (type.isComplex())
            {
                entity = (BaseEntity)valueOut;
                theMeta = (MetaClass)type;
            } else {
                if (tokenizer.hasMoreTokens())
                {
                    throw new IllegalArgumentException("Path can't have intermediate simple values");
                }
            }
        }

        return valueOut;
    }

    public boolean equalsToString(HashMap<String, String> params)
    {
        for (String fieldName : params.keySet())
        {
            IMetaType mtype = meta.getMemberType(fieldName);

            if (mtype == null)
                throw new IllegalArgumentException("No such field: " + fieldName);

            if (mtype.isComplex() || mtype.isSet())
                throw new IllegalArgumentException("Can't handle complex fields or arrays: " + fieldName);

            BaseValue bvalue = (BaseValue)getBaseValue(fieldName);

            if (!bvalue.equalsToString(params.get(fieldName), ((MetaValue)mtype).getTypeCode()))
                return false;
        }

        return true;
    }

    public void addValidationError(String errorMsg)
    {
        validationErrors.add(errorMsg);
    }

    public void clearValidationErrors()
    {
        validationErrors.clear();
    }

    public Set<String> getValidationErrors()
    {
        return validationErrors;
    }

    public void clearModifiedObjects()
    {
        this.modifiedObjects.clear();
    }

    public Set<String> getModifiedObjects()
    {
        return this.modifiedObjects;
    }

    public void setListeners()
    {
        this.addListener(new IValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                modifiedObjects.add(event.getIdentifier());
            }
        });

        for (String key : values.keySet())
        {
            IMetaType metaType = meta.getMemberType(key);
            if (metaType.isComplex())
            {
                IBaseValue baseValue = values.get(key);
                if (metaType.isSet())
                {
                    BaseSet baseSet = (BaseSet)baseValue.getValue();
                    baseSet.addListener(new ValueChangeListener(key) {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            String parentIdentifier = this.getParentIdentifier();
                            String identifier = parentIdentifier;

                            modifiedObjects.add(identifier);
                            fireValueChange(identifier);
                        }
                    });
                    baseSet.setListeners();
                }
                else
                {
                    BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                    baseEntity.addListener(new ValueChangeListener(key) {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            String childIdentifier = event.getIdentifier();
                            String parentIdentifier = this.getParentIdentifier();
                            String identifier = parentIdentifier + "." + childIdentifier;

                            modifiedObjects.add(identifier);
                            fireValueChange(identifier);
                        }
                    });

                    baseEntity.setListeners();
                }
            }
        }
    }

    public void removeListeners()
    {
        List<IValueChangeListener> parentListeners =
                (List<IValueChangeListener>)this.getListeners(ValueChangeEvent.class);
        final Iterator<IValueChangeListener> parentIt = parentListeners.iterator();
        while (parentIt.hasNext())
        {
            final IValueChangeListener listener = parentIt.next();
            this.removeListener(listener);
        }

        for (String key : values.keySet())
        {
            IMetaType metaType = meta.getMemberType(key);
            if (!metaType.isSet() && metaType.isComplex())
            {
                IBaseValue baseValue = values.get(key);
                BaseEntity baseEntity = (BaseEntity)baseValue.getValue();

                List<IValueChangeListener> childListeners =
                        (List<IValueChangeListener>)baseEntity.getListeners(ValueChangeEvent.class);
                final Iterator<IValueChangeListener> childIt = childListeners.iterator();
                while (childIt.hasNext())
                {
                    final IValueChangeListener listener = childIt.next();
                    baseEntity.removeListener(listener);
                }
            }
        }
    }



}
