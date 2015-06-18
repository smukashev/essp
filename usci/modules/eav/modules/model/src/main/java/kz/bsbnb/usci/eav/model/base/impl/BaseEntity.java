package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.*;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final long serialVersionUID = 1L;

    Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private UUID uuid = UUID.randomUUID();

    /**
     * Holds data about entity structure
     * @see MetaClass
     */
    private MetaClass meta;

    OperationType operationType;

    private IBaseEntityReportDate baseEntityReportDate;
    
    /**
     * Holds attributes values
     */
    private HashMap<String, IBaseValue> values = new HashMap<String, IBaseValue>();

    private Set<String> validationErrors = new HashSet<String>();

    @Override
    public OperationType getOperation() {
        return operationType;
    }

    public void setOperation(OperationType type){
        operationType = type;
    }

    /**
     * Initializes entity.
     */
    public BaseEntity()
    {
        super(BaseContainerType.BASE_ENTITY);
    }

    public BaseEntity(IBaseEntity baseEntity, Date reportDate)
    {
        super(baseEntity.getId(), BaseContainerType.BASE_ENTITY);

        IBaseEntityReportDate thatBaseEntityReportDate = baseEntity.getBaseEntityReportDate();
        IBaseEntityReportDate thisBaseEntityReportDate = new BaseEntityReportDate(
                thatBaseEntityReportDate.getId(),
                reportDate,
                thatBaseEntityReportDate.getIntegerValuesCount(),
                thatBaseEntityReportDate.getDateValuesCount(),
                thatBaseEntityReportDate.getStringValuesCount(),
                thatBaseEntityReportDate.getBooleanValuesCount(),
                thatBaseEntityReportDate.getDoubleValuesCount(),
                thatBaseEntityReportDate.getComplexValuesCount(),
                thatBaseEntityReportDate.getSimpleSetsCount(),
                thatBaseEntityReportDate.getComplexSetsCount()
        );
        thisBaseEntityReportDate.setBaseEntity(this);

        this.meta = baseEntity.getMeta();
        this.baseEntityReportDate = thisBaseEntityReportDate;
    }

    /**
     * Initializes entity with a class name.
     *
     * @param meta MetaClass of the entity..
     */
    public BaseEntity(MetaClass meta, Date reportDate)
    {
        super(BaseContainerType.BASE_ENTITY);

        this.meta = meta;
        this.baseEntityReportDate = new BaseEntityReportDate(this, reportDate);
    }

    public BaseEntity(long id, MetaClass meta)
    {
        super(id, BaseContainerType.BASE_ENTITY);
        this.meta = meta;
    }

    public BaseEntity(long id, MetaClass meta, Date reportDate)
    {
        super(id, BaseContainerType.BASE_ENTITY);
        this.meta = meta;
        this.baseEntityReportDate = new BaseEntityReportDate(this, reportDate);
    }

    public BaseEntity(long id, MetaClass meta, IBaseEntityReportDate baseEntityReportDate)
    {
        super(id, BaseContainerType.BASE_ENTITY);
        this.meta = meta;

        baseEntityReportDate.setBaseEntity(this);
        this.baseEntityReportDate = baseEntityReportDate;
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
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType type = metaAttribute.getMetaType();

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

        baseValue.setBaseContainer(this);
        baseValue.setMetaAttribute(metaAttribute);

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

        IBaseValue baseValue = values.remove(name);
        baseValue.setBaseContainer(null);
        baseValue.setMetaAttribute(null);
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
        if (baseEntityReportDate == null)
        {
            throw new RuntimeException("Instance of BaseEntityReportDate is null. " +
                    "Check the correctness of instance creation");
        }
        return baseEntityReportDate.getReportDate();
    }

    public void setReportDate(Date reportDate) {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        if (baseEntityReportDate == null)
        {
            this.baseEntityReportDate = new BaseEntityReportDate(this, newReportDate);
        }
        else
        {
            this.baseEntityReportDate.setReportDate(newReportDate);
        }
    }

    @Override
    public IBaseEntityReportDate getBaseEntityReportDate() {
        if (baseEntityReportDate == null)
        {
            throw new RuntimeException("Instance of BaseEntityReportDate is null. " +
                    "Check the correctness of instance creation");
        }
        return baseEntityReportDate;
    }

    @Override
    public void setBaseEntityReportDate(IBaseEntityReportDate baseEntityReportDate) {
        this.baseEntityReportDate = baseEntityReportDate;
    }

    public void calculateValueCount()
    {
        long integerValuesCount = 0;
        long dateValuesCount = 0;
        long stringValuesCount = 0;
        long booleanValuesCount = 0;
        long doubleValuesCount = 0;
        long complexValuesCount = 0;
        long simpleSetsCount = 0;
        long complexSetsCount = 0;

        for (String attribute: values.keySet())
        {
            IMetaType metaType = meta.getMemberType(attribute);
            if (metaType.isSet())
            {
                if (metaType.isComplex())
                {
                    complexSetsCount++;
                }
                else
                {
                    simpleSetsCount++;
                }
            }
            else
            {
                if (metaType.isComplex())
                {
                    complexValuesCount++;
                }
                else
                {
                    MetaValue metaValue = (MetaValue)metaType;
                    switch (metaValue.getTypeCode())
                    {
                        case INTEGER:
                            integerValuesCount++;
                            break;
                        case DATE:
                            dateValuesCount++;
                            break;
                        case STRING:
                            stringValuesCount++;
                            break;
                        case BOOLEAN:
                            booleanValuesCount++;
                            break;
                        case DOUBLE:
                            doubleValuesCount++;
                            break;
                        default:
                            throw new RuntimeException("Unknown data type.");
                    }

                }
            }
        }

        baseEntityReportDate.setIntegerValuesCount(integerValuesCount);
        baseEntityReportDate.setDateValuesCount(dateValuesCount);
        baseEntityReportDate.setStringValuesCount(stringValuesCount);
        baseEntityReportDate.setBooleanValuesCount(booleanValuesCount);
        baseEntityReportDate.setDoubleValuesCount(doubleValuesCount);
        baseEntityReportDate.setComplexValuesCount(complexValuesCount);
        baseEntityReportDate.setSimpleSetsCount(simpleSetsCount);
        baseEntityReportDate.setComplexSetsCount(complexSetsCount);
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

        if (meta.isSearchable())
        {
            if (this.getId() > 0 && that.getId() > 0 && this.getId() == that.getId())
            {
                return true;
            }
            return false;
        }

        int thisValueCount = this.getValueCount();
        int thatValueCount = that.getValueCount();

        if (thisValueCount != thatValueCount)
        {
            return false;
        }

        for (String attribute : values.keySet())
        {
            Object thisObject = null;
            if (this.safeGetValue(attribute) != null) {
                thisObject = this.safeGetValue(attribute).getValue();
            }

            Object thatObject = null;
            if (that.safeGetValue(attribute) != null) {
                thatObject = that.safeGetValue(attribute).getValue();
            }

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
                    if (DataUtils.compareBeginningOfTheDay((Date)thisObject, (Date)thatObject) != 0)
                    {
                        return false;
                    }
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

    private Queue queue;
    public List lastRuleErrors;

    public List getLastRuleErrors(){
        if(lastRuleErrors == null)
            lastRuleErrors = new LinkedList();
        return lastRuleErrors;
    }

    public synchronized Object getEls(String path){
      queue = new LinkedList();
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

        if(function.startsWith("hasDuplicates")) {
            String pattern = "hasDuplicates\\((\\S+)\\)";
            Matcher m = Pattern.compile(pattern).matcher(function);
            String downPath;
            boolean ret = false;
            lastRuleErrors = new LinkedList();

            if(m.find()) {
                downPath = m.group(1);
            } else {
                throw new RuntimeException("function duplicates not correct: " +
                        "example {hasDuplicates(subjects)}doc_type.code,date");
            }

            LinkedList list = (LinkedList) getEls("{get}" + downPath);

            String[] fields = path.split(",");

            Set controlSet;

            if(fields.length == 1)
                controlSet = new HashSet<String>();
            else if(fields.length == 2)
                controlSet = new HashSet<Map.Entry>();
            else throw new RuntimeException("rule not yet implemented");

            for(Object o : list) {
                BaseEntity entity = (BaseEntity) o;
                Object entry = null;

                if(fields.length == 1)
                    entry = entity.getEl(fields[0]);
                else if(fields.length == 2) {
                    entry = new AbstractMap.SimpleEntry(entity.getEl(fields[0]), entity.getEl(fields[1]));
                }

                if(controlSet.contains(entry)) {
                    ret = true;
                    lastRuleErrors.add(entry);
                } else {
                    controlSet.add(entry);
                }
            }
            return ret;
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
          if(path.charAt(i) =='!' && ( i+1 == path.length() || path.charAt(i+1) != '='))
              throw new RuntimeException("equal sign must be present after exlaim");

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
                  if(c==']'){
                      operations[yk] = str.toString();
                      isFilter[yk] = true;
                      yk++;
                  }else{
                      String[] arr = str.toString().split("\\.");
                      for(int j = 0;j<arr.length;j++){
                      //operations[yk] = str.toString();
                      operations[yk] = arr[j];
                      isFilter[yk] = false;
                      yk++;
                      }
                  }
                  str.setLength(0);
              }
          } else{
              str.append(c);
          }
      }

        List ret = new LinkedList();
        queue.add(this);
        queue.add(0);
        int retCount = 0;

        while(queue.size() > 0){
            Object curO = queue.poll();
            int step = (Integer) queue.poll();

            if(curO == null)
                continue;

            if(step == yk)
            {
                if(function.startsWith("count")) {
                    retCount ++;
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

               if(!nextAttribute.getMetaType().isComplex()){ // transition to BASIC type
                   queue.add(curBE.getEl(operations[step]));
                   queue.add(step + 1);
               } else if(nextAttribute.getMetaType().isSet()){ //transition to array
                   BaseSet next = (BaseSet)curBE.getEl(operations[step]);
                   if(next!=null){
                     for(Object o: next.get()){
                         {
                             queue.add(((BaseValue) o).getValue());
                             queue.add(step+1);
                         }
                     }
                   }
               } else{ //transition to simple
                   BaseEntity next =  (BaseEntity) curBE.getEl(operations[step]);
                   queue.add(next);
                   queue.add(step + 1);
                 }
           }else{
               String [] parts;
               boolean inv = false;

               if(operations[step].contains("!")){
                   parts = operations[step].split("!=");
                   inv = true;
               }
               else
                   parts = operations[step].split("=");

               Object o = curBE.getEl(parts[0]);

               boolean expr = (o==null && parts[1].equals("null")) || (o!=null && o.toString().equals(parts[1]));
               if(inv) expr = !expr;

               if(expr){
                   queue.add(curO);
                   queue.add(step+1);
               }
           }
        }

        if(function.startsWith("get"))
            return ret;

        return retCount;
    }

    public Object getEl(String path)
    {
        if(path.equals("ROOT"))
            return getId();

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

            IMetaType type = null;
            try {
                type = attribute.getMetaType();
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    public List<Object> getElWithArrays(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        BaseEntity entity = this;
        MetaClass theMeta = meta;
        ArrayList<Object> valueOut = new ArrayList<Object>();
        Object currentValue = null;

        try {
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
                    return valueOut;

                IBaseValue value = entity.getBaseValue(token);

                if (value == null || value.getValue() == null) {
                    return valueOut;
                }

                currentValue = value.getValue();

                if (type.isSet())
                {
                    BaseSet set = (BaseSet)currentValue;
                    if (arrayIndexes != null) {
                        currentValue = set.getEl(arrayIndexes.replaceAll("->", "."));
                        type = ((MetaSet)type).getMemberType();
                    } else {
                        if (tokenizer.hasMoreTokens())
                        {
                            if(!set.getMemberType().isComplex()) {
                                throw new IllegalArgumentException("Simple sets not supported");
                            }

                            if(set.getMemberType().isSet()) {
                                throw new IllegalArgumentException("Set of sets not supported");
                            }

                            String restOfPath = "";
                            boolean first = true;
                            while(tokenizer.hasMoreTokens()) {
                                if (first) {
                                    restOfPath += tokenizer.nextToken();
                                    first = false;
                                } else {
                                    restOfPath += "." + tokenizer.nextToken();
                                }
                            }

                            for (IBaseValue obj : set.get()) {
                                BaseEntity currentEntity = (BaseEntity)(obj.getValue());
                                if (currentEntity != null)
                                    valueOut.addAll(currentEntity.getElWithArrays(restOfPath));
                                else
                                    logger.warn("Null in set");
                            }

                            return valueOut;
                        }
                    }
                }

                if (type.isComplex() && !type.isSet())
                {
                    entity = (BaseEntity)currentValue;
                    theMeta = (MetaClass)type;
                } else {
                    if (tokenizer.hasMoreTokens())
                    {
                        throw new IllegalArgumentException("Path can't have intermediate simple values");
                    }
                }

                if (!tokenizer.hasMoreTokens()) {
                    valueOut.add(currentValue);
                    return valueOut;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            if (baseEntity != null)
            {
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

    @Override
    public BaseEntity clone()
    {
        BaseEntity baseEntityCloned = null;
        try
        {
            baseEntityCloned = (BaseEntity)super.clone();

            BaseEntityReportDate baseEntityReportDateCloned = ((BaseEntityReportDate)baseEntityReportDate).clone();
            baseEntityReportDateCloned.setBaseEntity(baseEntityCloned);
            baseEntityCloned.setBaseEntityReportDate(baseEntityReportDateCloned);

            HashMap<String, IBaseValue> valuesCloned = new HashMap<String, IBaseValue>();
            Iterator<String> attributesIt = values.keySet().iterator();
            while(attributesIt.hasNext())
            {
                String attribute = attributesIt.next();

                IBaseValue baseValue = values.get(attribute);
                IBaseValue baseValueCloned = ((BaseValue)baseValue).clone();
                baseValueCloned.setBaseContainer(baseEntityCloned);
                valuesCloned.put(attribute, baseValueCloned);
            }
            baseEntityCloned.values = valuesCloned;
        }
        catch(CloneNotSupportedException ex)
        {
            throw new RuntimeException("BaseEntity class does not implement interface Cloneable.");
        }
        return baseEntityCloned;
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

    public int getSearchableChildrenCount() {
        int count = 0;

        for (String attribute: values.keySet())
        {
            IMetaType metaType = meta.getMemberType(attribute);
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
            IBaseValue value = getBaseValue(attribute);

            if (value == null)
                continue;

            if (value.getValue() == null)
                continue;

            if (metaAttribute.isImmutable())
                continue;

            if (metaType.isSet())
            {
                MetaSet metaSet = (MetaSet)metaType;

                if (metaSet.getMemberType().isComplex() && !metaSet.getMemberType().isSet())
                {
                    BaseSet baseSet = (BaseSet)(value.getValue());
                    MetaClass metaClass = (MetaClass)(baseSet.getMemberType());

                    for (IBaseValue setValue : baseSet.get()) {
                        if (setValue.getValue() == null)
                            continue;

                        BaseEntity baseEntity = (BaseEntity)(setValue.getValue());

                        if (metaClass.isSearchable())
                            count++;

                        count += baseEntity.getSearchableChildrenCount();
                    }
                }
            }
            else
            {
                if (metaType.isComplex())
                {
                    MetaClass metaClass = (MetaClass)metaType;
                    BaseEntity baseEntity = (BaseEntity)(value.getValue());
                    if (metaClass.isSearchable())
                        count++;

                    count += baseEntity.getSearchableChildrenCount();
                }
            }
        }

        return count;
    }

    public UUID getUuid() {
        return uuid;
    }

}
