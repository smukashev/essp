package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.output.BaseEntityOutput;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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

    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Reporting date on which instance of BaseEntity was loaded.
     */
    private Date reportDate;

    private boolean withClosedValues = false;

    private Date maxReportDate;

    private Date minReportDate;

    /**
     * The list of available reporting dates for this instance BaseEntity.
     */
    private Set<Date> availableReportDates = new HashSet<Date>();

    /**
     * Holds data about entity structure
     * @see MetaClass
     */
    private MetaClass meta;
    
    /**
     * Holds attributes values
     */
    private HashMap<String, IBaseValue> values = new HashMap<String, IBaseValue>();

    private Set<String> validationErrors = new HashSet<String>();

    /**
     * Initializes entity.
     */
    public BaseEntity()
    {

    }

    /**
     * Initializes entity with a class name.
     *
     * @param meta MetaClass of the entity..
     */
    public BaseEntity(MetaClass meta, Date reportDate)
    {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.reportDate = newReportDate;
        this.meta = meta;
        this.availableReportDates.add(newReportDate);
    }

    public BaseEntity(long id, MetaClass meta, Date reportDate, Set<Date> availableReportDates)
    {
        this(id, meta, reportDate, availableReportDates, false);
    }

    public BaseEntity(long id, MetaClass meta, Date reportDate, Set<Date> availableReportDates, boolean withClosedValues)
    {
        super(id);
        this.meta = meta;
        this.availableReportDates = availableReportDates;
        this.withClosedValues = withClosedValues;

        if (reportDate == null)
        {
            throw new IllegalArgumentException("Can not create instance of BaseEntity " +
                    "with report date equal to null.");
        }
        else
        {
            Date newReportDate = (Date)reportDate.clone();
            DataUtils.toBeginningOfTheDay(newReportDate);

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

    @Override
    public Set<Date> getAvailableReportDates() {
        return availableReportDates;
    }

    @Override
    public void setAvailableReportDates(Set<Date> availableReportDates) {
        this.availableReportDates = availableReportDates;
        this.maxReportDate = null;
        this.minReportDate = null;
    }

    @Override
    public Date getMaxReportDate()
    {
        if (maxReportDate == null)
        {
            if (availableReportDates.size() != 0)
            {
                maxReportDate = Collections.max(availableReportDates);
            }
        }
        return maxReportDate;
    }

    @Override
    public Date getMinReportDate()
    {
        if (minReportDate == null)
        {
            if (availableReportDates.size() != 0)
            {
                minReportDate = Collections.min(availableReportDates);
            }
        }
        return minReportDate;
    }

    public boolean isWithClosedValues()
    {
        return withClosedValues;
    }

    public void setWithClosedValues(boolean withClosedValues)
    {
        this.withClosedValues = withClosedValues;
    }


    @Override
    public boolean isMaxReportDate()
    {
        if (this.reportDate == null)
        {
            throw new IllegalStateException("The report date can not be equal to null.");
        }

        Date maxReportDate = getMaxReportDate();
        if (maxReportDate == null)
        {
            throw new IllegalStateException("The maximum report date can not be equal to null.");
        }
        return DataUtils.compareBeginningOfTheDay(reportDate, maxReportDate) == 0;
    }

    @Override
    public boolean isMinReportDate()
    {
        if (this.reportDate == null)
        {
            throw new IllegalStateException("The report date can not be equal to null.");
        }

        Date minReportDate = getMinReportDate();
        if (minReportDate == null)
        {
            throw new IllegalStateException("The minimum report date can not be equal to null.");
        }
        return DataUtils.compareBeginningOfTheDay(reportDate, minReportDate) == 0;
    }

    /**
     * Retrieves key titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     *
     * @param attribute key name. Must exist in entity meta
     * @return key value, null if value is not set
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     * 	                                or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    @Override
    public IBaseValue getBaseValue(String attribute)
    {
        if (attribute.contains("."))
        {
            int index = attribute.indexOf(".");
            String parentAttribute = attribute.substring(0, index);
            String childAttribute = attribute.substring(index, attribute.length() - 1);

            IMetaType metaType = meta.getMemberType(parentAttribute);
            if (metaType == null)
            {
                throw new IllegalArgumentException(String.format("Instance of MetaClass with class name {0} " +
                        "does not contain attribute {1}.", meta.getClassName(), parentAttribute));
            }

            if (metaType.isComplex() && !metaType.isSet())
            {
                IBaseValue baseValue = values.get(parentAttribute);
                if (baseValue == null)
                {
                    return null;
                }

                IBaseEntity baseEntity = (IBaseEntity)baseValue.getValue();
                if (baseEntity == null)
                {
                    return null;
                }
                else
                {
                    return baseEntity.getBaseValue(childAttribute);
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            IMetaType metaType = meta.getMemberType(attribute);

            if (metaType == null)
            {
                throw new IllegalArgumentException(String.format("Instance of MetaClass with class name {0} " +
                        "does not contain attribute {1}.", meta.getClassName(), attribute));
            }

            return values.get(attribute);
        }
    }

    /**
     * Retrieves key titled <code>name</code>.
     *
     * @param attribute name key name. Must exist in entity meta
     * @param baseValue new value of the key
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     * 	                                or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    //TODO: Add exception on metaClass mismatch
    @Override
    public void put(final String attribute, IBaseValue baseValue)
    {
        IMetaType type = meta.getMemberType(attribute);

        if(type == null)
            throw new IllegalArgumentException("Type: " + attribute +
                    ", not found in class: " + meta.getClassName());

        if (baseValue == null)
            throw new IllegalArgumentException("Value not be equal to null.");

        if (baseValue.getValue() != null)
        {
            Class<?> valueClass = baseValue.getValue().getClass();
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
                        valueClass = baseValue.getValue().getClass();
                    }
                    else
                    {
                        expValueClass = metaValue.getTypeCode().getDataTypeClass();
                        valueClass = ((MetaValue)(((BaseSet)baseValue.getValue()).getMemberType())).getTypeCode().
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

        boolean listening = isListening();
        if (listening)
        {
            if (values.containsKey(attribute))
            {
                IBaseValue existingBaseValue = values.get(attribute);
                if (existingBaseValue.getValue() == null)
                {
                    removeListeners(attribute);
                }

                values.put(attribute, baseValue);

                if (baseValue.getValue() != null)
                {
                    setListeners(attribute);
                }
                fireValueChange(attribute);
            }
            else
            {
                values.put(attribute, baseValue);
                fireValueChange(attribute);
            }
        }
        else
        {
            values.put(attribute, baseValue);
        }
    }

    public void remove(String name) {
        fireValueChange(name);
        values.remove(name);
    }

    @Override
    public Collection<IBaseValue> get() {
        return values.values();
    }

    @Override
    public IMetaType getMemberType(String name) {
        if (name.contains("."))
        {
            int index = name.indexOf(".");
            String parentIdentifier = name.substring(0, index);

            IMetaType metaType = meta.getMemberType(parentIdentifier);
            if (metaType.isComplex() && !metaType.isSet())
            {
                MetaClass childMeta = (MetaClass)metaType;
                String childIdentifier = name.substring(index, name.length() - 1);
                return childMeta.getMemberType(childIdentifier);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return meta.getMemberType(name);
        }
    }

    @Override
    public IMetaAttribute getMetaAttribute(String attribute)
    {
        return meta.getMetaAttribute(attribute);
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
    public Set<String> getAttributes() {
        return values.keySet();
    }

    public int getValueCount() {

        return values.size();
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.reportDate = newReportDate;
        this.availableReportDates.add(newReportDate);

        this.minReportDate = null;
        this.maxReportDate = null;
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

        int thisValueCount = this.getValueCount();
        int thatValueCount = that.getValueCount();

        if (thisValueCount != thatValueCount)
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
                    DataUtils.toBeginningOfTheDay((Date) thisObject);
                    DataUtils.toBeginningOfTheDay((Date) thatObject);
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
        if (this.getAttributes().contains(name))
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

    public String toJava(String fName)
    {
        return BaseEntityOutput.getJavaFunction(fName,this);
    }


    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + meta.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }


    private Object[] queue;
    private int queueStart;
    private int queueEnd;

    private void initQueue(){
        queue = new Object[10000];
        queueStart = 0;
        queueEnd = 0;
    }

    private void enQueue(Object o){
        queue[queueEnd++] = o;
    }

    private int queueSize(){
        return queueEnd - queueStart;
    }

    private Object deQueue(){
        if(queueSize()==0) throw new RuntimeException("queue is empty");
        return queue[queueStart++];
    }


    public Object getEls(String path){
      initQueue();
      StringBuilder str = new StringBuilder();
      String[] operations = new String[500];
      boolean[] isFilter = new boolean[500];
      String function = null;

      if(!path.startsWith("{")) throw new RuntimeException("function must be specified");
      for(int i=0;i<path.length();i++){
          if(path.charAt(i) == '}')
          {
              function = path.substring(1,i);
              path = path.substring(i+1);
              break;
          }
      }

      if(function == null) throw new RuntimeException("no function");

        Set allowedSet = new TreeSet<Long>();

        if(function.startsWith("set")){
            String[] elems = function.substring(function.indexOf('(') + 1,function.indexOf(')')).split(",");
            if(function.startsWith("setInt")){
                allowedSet = new TreeSet<Integer>();
                for(String e: elems)
                    allowedSet.add(Integer.parseInt(e.trim()));
            } else if( function.startsWith("setLong")){
                allowedSet = new TreeSet<Long>();
                for(String e: elems)
                    allowedSet.add(Long.parseLong(e.trim()));
            } else if( function.startsWith("setString")){
                allowedSet = new TreeSet<String>();
                for(String e: elems)
                    allowedSet.add(e.trim());
            }
        }


      int yk = 0;
      int open = 0;
      int eqCnt = 0;

      for(int i=0;i<=path.length();i++) {
          if(i==path.length()) {
              if(open!=0)
                  throw  new RuntimeException("opening bracket not correct");
              break;
          }
          if(path.charAt(i) == '=') eqCnt++;
          if(path.charAt(i) == '[') open++;
          if(path.charAt(i) == ']') {
              open--;
              if(eqCnt!=1) throw new RuntimeException("only exactly one equal sign in filter and only in filter");
              eqCnt = 0;
          }
          if(open < 0 || open > 1) throw new RuntimeException("brackets not correct");
      }

      for(int i=0;i<=path.length();i++){
          if(i==path.length()){
              if(str.length() > 0)
              {
                  String[] arr = str.toString().split("\\.");
                  for(int j=0;j<arr.length;j++)
                  {
                      operations[yk] = arr[j];
                      isFilter[yk]=false;
                      yk++;
                  }
              }
              break;
          }
          char c = path.charAt(i);
          if( c=='[' || c==']'){
              if(str.length() > 0){
                  String[] arr = str.toString().split("\\.");
                  for(int j = 0;j<arr.length;j++){
                      //operations[yk] = str.toString();
                      operations[yk] = arr[j];
                      isFilter[yk] = c==']';
                      yk++;
                  }
                  str.setLength(0);
              }
          } else{
              str.append(c);
          }
      }

        List ret = new LinkedList();
        enQueue(this);
        enQueue(0);
        int retCount = 0;

        while(queueSize() > 0){
            Object curO = deQueue();
            int step = (Integer) deQueue();

            if(step == yk)
            {
                if(function.startsWith("count")) {
                    if(curO != null )retCount ++;
                }
                else if(function.startsWith("set"))
                    if(allowedSet.contains( curO ))
                        retCount++;
                ret.add(curO);
                continue;
            }


            BaseEntity curBE = (BaseEntity) curO;
            MetaClass curMeta = curBE.getMeta();

           if(!isFilter[step]){
                 IMetaAttribute nextAttribute = curMeta.getMetaAttribute(operations[step]);

               if(nextAttribute == null){ // transition to BASIC type
                   enQueue(curBE.getEl(operations[step]));
                   enQueue(step + 1);
               } else if(nextAttribute.getMetaType().isSet()){ //transition to array
                   BaseSet next = (BaseSet)curBE.getEl(operations[step]);
                     for(Object o: next.get()){
                         {
                             enQueue(((BaseValue) o).getValue());
                             enQueue(step+1);
                         }
                     }
               } else{ //transition to simple
                   BaseEntity next =  (BaseEntity) curBE.getEl(operations[step]);
                     enQueue(next);
                     enQueue(step+1);
                 }
           }else{
               String[] parts = operations[step].split("=");
               Object o = curBE.getEl(parts[0]);

               if( (o==null && parts[1].equals("null")) || o.toString().equals(parts[1]))
               {
                   enQueue(curO);
                   enQueue(step+1);
               }
           }
        }

        return retCount;
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
            String arrayIndexes = null;

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
                if (arrayIndexes != null) {
                    valueOut = ((BaseSet)valueOut).getEl(arrayIndexes.replaceAll("->", "."));
                    type = ((MetaSet)type).getMemberType();
                } else {
                    return valueOut;
                }
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
            String ownFieldName;
            String innerPath = null;
            if (fieldName.contains(".")) {
                ownFieldName = fieldName.substring(0, fieldName.indexOf("."));
                innerPath = fieldName.substring(fieldName.indexOf(".") + 1);
            } else {
                ownFieldName = fieldName;
            }

            //System.out.println(ownFieldName + " " + innerPath);


            IMetaType mtype = meta.getMemberType(ownFieldName);

            if (mtype == null)
                throw new IllegalArgumentException("No such field: " + fieldName);

            if (mtype.isSet())
                throw new IllegalArgumentException("Can't handle arrays: " + fieldName);

            BaseValue bvalue = (BaseValue)getBaseValue(ownFieldName);

            if (mtype.isComplex()) {
                bvalue = (BaseValue)((BaseEntity)(bvalue.getValue())).getBaseValue(innerPath);
                mtype = ((MetaClass)mtype).getMemberType(innerPath);
            }

            if (!((BaseValue)bvalue).equalsToString(params.get(fieldName), ((MetaValue)mtype).getTypeCode()))
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

    @Override
    protected void setListeners()
    {
        this.addListener(new IValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                addModifiedIdentifier(event.getIdentifier());
            }
        });

        for (String attribute : values.keySet())
        {
            setListeners(attribute);
        }
    }

    private void setListeners(String attribute)
    {
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        if (metaAttribute.isImmutable())
        {
            return;
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (metaType.isComplex())
        {
            IBaseValue baseValue = values.get(attribute);
            if (baseValue.getValue() != null)
            {
                if (metaType.isSet())
                {
                    BaseSet baseSet = (BaseSet)baseValue.getValue();
                    baseSet.addListener(new ValueChangeListener(this, attribute) {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            String identifier = this.getIdentifier();

                            IBaseContainer target = this.getTarget();
                            if (target instanceof BaseContainer)
                            {
                                ((BaseContainer)target).addModifiedIdentifier(identifier);
                            }

                            fireValueChange(identifier);
                        }
                    });
                    baseSet.setListening(true);
                }
                else
                {

                    BaseEntity baseEntity = (BaseEntity)baseValue.getValue();
                    baseEntity.addListener(new ValueChangeListener(this, attribute) {

                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            String childIdentifier = event.getIdentifier();
                            String parentIdentifier = this.getIdentifier();
                            String identifier = parentIdentifier + "." + childIdentifier;

                            IBaseContainer target = this.getTarget();
                            if (target instanceof BaseContainer)
                            {
                                ((BaseContainer)target).addModifiedIdentifier(identifier);
                            }

                            fireValueChange(identifier);
                        }
                    });

                    baseEntity.setListening(true);
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

        for (String attribute : values.keySet())
        {
            removeListeners(attribute);
        }
    }

    public void removeListeners(String attribute)
    {

        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        if (metaAttribute.isImmutable())
        {
            return;
        }

        IMetaType metaType = metaAttribute.getMetaType();
        if (!metaType.isSet() && metaType.isComplex())
        {
            IBaseValue baseValue = values.get(attribute);
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

    @Override
    public BaseEntity clone()
    {
        BaseEntity baseEntity = null;
        try
        {
            baseEntity = (BaseEntity)super.clone();
            baseEntity.setReportDate((Date)reportDate.clone());

            HashSet<Date> availableReportDatesCloned = new HashSet<Date>();
            Iterator<Date> availableReportDatesIt = availableReportDates.iterator();
            while(availableReportDatesIt.hasNext())
            {
                availableReportDatesCloned.add((Date)availableReportDatesIt.next().clone());
            }
            baseEntity.setAvailableReportDates(availableReportDatesCloned);

            HashMap<String, IBaseValue> valuesCloned = new HashMap<String, IBaseValue>();
            Iterator<String> attributesIt = values.keySet().iterator();
            while(attributesIt.hasNext())
            {
                String attribute = attributesIt.next();

                IBaseValue baseValue = values.get(attribute);
                IBaseValue baseValueCloned = (IBaseValue)((BaseValue)baseValue).clone();
                valuesCloned.put(attribute, baseValueCloned);
            }
            baseEntity.setAvailableReportDates(availableReportDatesCloned);

        }
        catch(CloneNotSupportedException ex)
        {
            throw new RuntimeException("BaseEntity class does not implement interface Cloneable.");
        }
        return baseEntity;
    }

    public boolean applyKeyFilter(HashMap<String, ArrayList<String>> arrayKeyFilter) throws ParseException
    {
        for (String attrName : arrayKeyFilter.keySet()) {
            IMetaType type = meta.getMemberType(attrName);

            IBaseValue value = safeGetValue(attrName);

            if(value == null) {
                throw new IllegalArgumentException("Key attribute " + attrName + " can't be null, " +
                        "it is used in array filter");
            }

            if (testValueOnString(type, value, arrayKeyFilter.get(attrName))) {
                return true;
            }
        }

        return false;
    }

    private boolean testValueOnString(IMetaType type, IBaseValue value, ArrayList<String> filter) throws ParseException
    {
        MetaValue simple_value = (MetaValue)type;

        for (String strValue : filter) {
            switch (simple_value.getTypeCode())
            {
                case BOOLEAN:
                    Boolean booleanValue = (Boolean)value.getValue();
                    if (booleanValue == Boolean.parseBoolean(strValue)) return true;
                    break;
                case DATE:
                    java.sql.Date dateValue = DataUtils.convert((java.util.Date) value.getValue());
                    if (dateValue == dateFormat.parse(strValue)) return true;
                    break;
                case DOUBLE:
                    Double doubleValue = (Double)value.getValue();
                    if (doubleValue == Double.parseDouble(strValue)) return true;
                    break;
                case INTEGER:
                    Integer integerValue = (Integer)value.getValue();
                    if (integerValue == Integer.parseInt(strValue)) return true;
                    break;
                case STRING:
                    String stringValue = (String)value.getValue();
                    if (stringValue.equals(strValue)) return true;
                    break;
                default:
                    throw new IllegalStateException("Unknown data type: " + simple_value.getTypeCode());
            }
        }

        return false;
    }

    public long getBatchId() {
        for (IBaseValue v :values.values()) {
            return v.getBatch().getId();
        }

        return 0;
    }

    public long getBatchIndex() {
        for (IBaseValue v :values.values()) {
            return v.getIndex();
        }

        return 0;
    }

    @Override
    public boolean isSet() {
        return false;
    }

    public void setIndex(long index) {
        for (IBaseValue value : values.values()) {
            value.setIndex(index);
        }
    }
}
