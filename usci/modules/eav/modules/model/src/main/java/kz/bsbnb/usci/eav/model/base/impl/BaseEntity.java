package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.output.BaseEntityOutput;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseEntity extends BaseContainer implements IBaseEntity {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(BaseEntity.class);

    private UUID uuid = UUID.randomUUID();

    private MetaClass meta;

    private OperationType operationType;

    private IBaseEntityReportDate baseEntityReportDate;

    private HashMap<String, IBaseValue> values = new HashMap<>();

    private Set<String> validationErrors = new HashSet<>();

    private final List<IBaseEntity> keyElements = new ArrayList<>();

    private boolean keyElementsInstalled = false;

    private AdditionalInfo additionalInfo;

    private boolean isLastInBatch;

    @Override
    public AdditionalInfo getAddInfo() {
        return additionalInfo;
    }

    @Override
    public void setAddInfo (IBaseEntity parentEntity, boolean isSet, long attributeId) {
        additionalInfo = new AdditionalInfo(parentEntity, isSet, attributeId);
    }

    @Override
    public OperationType getOperation() {
        return operationType;
    }

    public void setOperation(OperationType type) {
        operationType = type;
    }

    public BaseEntity() {
        super(BaseContainerType.BASE_ENTITY);
    }

    public BaseEntity(IBaseEntity baseEntity, Date reportDate) {
        super(baseEntity.getId(), BaseContainerType.BASE_ENTITY);

        IBaseEntityReportDate thatBaseEntityReportDate = baseEntity.getBaseEntityReportDate();

        IBaseEntityReportDate thisBaseEntityReportDate = new BaseEntityReportDate(
                thatBaseEntityReportDate.getId(),
                thatBaseEntityReportDate.getCreditorId(),
                reportDate,
                thatBaseEntityReportDate.getIntegerValuesCount(),
                thatBaseEntityReportDate.getDateValuesCount(),
                thatBaseEntityReportDate.getStringValuesCount(),
                thatBaseEntityReportDate.getBooleanValuesCount(),
                thatBaseEntityReportDate.getDoubleValuesCount(),
                thatBaseEntityReportDate.getComplexValuesCount(),
                thatBaseEntityReportDate.getSimpleSetsCount(),
                thatBaseEntityReportDate.getComplexSetsCount());

        thisBaseEntityReportDate.setBaseEntity(this);

        this.meta = baseEntity.getMeta();
        this.baseEntityReportDate = thisBaseEntityReportDate;
    }

    public BaseEntity(MetaClass meta, Date reportDate, long creditorId) {
        super(BaseContainerType.BASE_ENTITY);

        this.meta = meta;
        this.baseEntityReportDate = new BaseEntityReportDate(this, reportDate, creditorId);
    }

    public BaseEntity(MetaClass meta, Date reportDate) {
        super(BaseContainerType.BASE_ENTITY);
        this.meta = meta;
        this.baseEntityReportDate = new BaseEntityReportDate(this, reportDate, 0L);
    }

    public BaseEntity(long id, MetaClass meta) {
        super(id, BaseContainerType.BASE_ENTITY);
        this.meta = meta;
    }

    public BaseEntity(long id, MetaClass meta, Date reportDate, long creditorId) {
        super(id, BaseContainerType.BASE_ENTITY);
        this.meta = meta;
        this.baseEntityReportDate = new BaseEntityReportDate(this, reportDate, creditorId);
    }

    public BaseEntity(long id, MetaClass meta, IBaseEntityReportDate baseEntityReportDate) {
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
    public MetaClass getMeta() {
        return meta;
    }

    /**
     * Retrieves key titled <code>name</code>. Attribute must have type of <code>DataTypes.DATE</code>
     *
     * @param attribute key name. Must exist in entity meta
     * @return key value, null if value is not set
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     *                                  or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    @Override
    public IBaseValue getBaseValue(String attribute) {
        if (attribute.contains(".")) {
            int index = attribute.indexOf(".");
            String parentAttribute = attribute.substring(0, index);
            String childAttribute = attribute.substring(index, attribute.length() - 1);

            IMetaType metaType = meta.getMemberType(parentAttribute);
            if (metaType == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E12,meta.getClassName(),parentAttribute));

            if (metaType.isComplex() && !metaType.isSet()) {
                IBaseValue baseValue = values.get(parentAttribute);
                if (baseValue == null) {
                    return null;
                }

                IBaseEntity baseEntity = (IBaseEntity) baseValue.getValue();
                if (baseEntity == null) {
                    return null;
                } else {
                    return baseEntity.getBaseValue(childAttribute);
                }
            } else {
                return null;
            }
        } else {
            IMetaType metaType = meta.getMemberType(attribute);

            if (metaType == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E12,meta.getClassName(),attribute));

            return values.get(attribute);
        }
    }

    /**
     * Retrieves key titled <code>name</code>.
     *
     * @param attribute name key name. Must exist in entity meta
     * @param baseValue new value of the key
     * @throws IllegalArgumentException if key name does not exist in entity meta,
     *                                  or key has type different from <code>DataTypes.DATE</code>
     * @see DataTypes
     */
    @Override
    public void put(final String attribute, IBaseValue baseValue) {
        IMetaAttribute metaAttribute = meta.getMetaAttribute(attribute);
        IMetaType type = metaAttribute.getMetaType();

        if (type == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E25,attribute,meta.getClassName()));

        if (baseValue == null)
            throw new IllegalArgumentException(Errors.compose(Errors.E26));

        if (baseValue.getValue() != null) {
            Class<?> valueClass = baseValue.getValue().getClass();
            Class<?> expValueClass;

            if (type.isComplex())
                if (type.isSet()) {
                    expValueClass = BaseSet.class;
                } else {
                    expValueClass = BaseEntity.class;
                }
            else {
                if (type.isSet()) {
                    MetaSet metaValue = (MetaSet) type;

                    if (type.isSet()) {
                        expValueClass = BaseSet.class;
                        valueClass = baseValue.getValue().getClass();
                    } else {
                        expValueClass = metaValue.getTypeCode().getDataTypeClass();
                        valueClass = ((MetaValue) (((BaseSet) baseValue.getValue()).getMemberType())).getTypeCode().
                                getDataTypeClass();
                    }

                } else {
                    MetaValue metaValue = (MetaValue) type;
                    expValueClass = metaValue.getTypeCode().getDataTypeClass();
                }

            }

            if (expValueClass == null || !expValueClass.isAssignableFrom(valueClass))
                throw new IllegalArgumentException(Errors.compose(Errors.E27,
                        meta.getClassName(),expValueClass,valueClass));
        }

        baseValue.setBaseContainer(this);
        baseValue.setMetaAttribute(metaAttribute);

        values.put(attribute, baseValue);
    }

    public void remove(String name) {
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
        if (name.contains(".")) {
            int index = name.indexOf(".");
            String parentIdentifier = name.substring(0, index);

            IMetaType metaType = meta.getMemberType(parentIdentifier);
            if (metaType.isComplex() && !metaType.isSet()) {
                MetaClass childMeta = (MetaClass) metaType;
                String childIdentifier = name.substring(index, name.length() - 1);
                return childMeta.getMemberType(childIdentifier);
            } else {
                return null;
            }
        } else {
            return meta.getMemberType(name);
        }
    }

    @Override
    public IMetaAttribute getMetaAttribute(String attribute) {
        return meta.getMetaAttribute(attribute);
    }

    /**
     * Names of all attributes that are actually set in entity
     *
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
            throw new RuntimeException(Errors.compose(Errors.E11));

        return baseEntityReportDate.getReportDate();
    }

    @Override
    public IBaseEntityReportDate getBaseEntityReportDate() {
        if (baseEntityReportDate == null) {
            throw new RuntimeException(Errors.compose(Errors.E11));
        }
        return baseEntityReportDate;
    }

    @Override
    public void setBaseEntityReportDate(IBaseEntityReportDate baseEntityReportDate) {
        this.baseEntityReportDate = baseEntityReportDate;
    }

    public void calculateValueCount(IBaseEntity baseEntityLoaded) {
        long integerValuesCount = 0;
        long dateValuesCount = 0;
        long stringValuesCount = 0;
        long booleanValuesCount = 0;
        long doubleValuesCount = 0;
        long complexValuesCount = 0;
        long simpleSetsCount = 0;
        long complexSetsCount = 0;

        if (baseEntityLoaded != null) {
            if (baseEntityLoaded.getBaseEntityReportDate() == null)
                throw new IllegalStateException(Errors.compose(Errors.E6));

            integerValuesCount = baseEntityLoaded.getBaseEntityReportDate().getIntegerValuesCount();
            dateValuesCount = baseEntityLoaded.getBaseEntityReportDate().getDateValuesCount();
            stringValuesCount = baseEntityLoaded.getBaseEntityReportDate().getStringValuesCount();
            booleanValuesCount = baseEntityLoaded.getBaseEntityReportDate().getBooleanValuesCount();
            doubleValuesCount = baseEntityLoaded.getBaseEntityReportDate().getDoubleValuesCount();
            complexValuesCount = baseEntityLoaded.getBaseEntityReportDate().getComplexValuesCount();
            simpleSetsCount = baseEntityLoaded.getBaseEntityReportDate().getSimpleSetsCount();
            complexSetsCount = baseEntityLoaded.getBaseEntityReportDate().getComplexSetsCount();
        }

        for (String attribute : values.keySet()) {
            IMetaType metaType = meta.getMemberType(attribute);
            if (metaType.isSet()) {
                if (metaType.isComplex()) {
                    complexSetsCount++;
                } else {
                    simpleSetsCount++;
                }
            } else {
                if (metaType.isComplex()) {
                    complexValuesCount++;
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
                    switch (metaValue.getTypeCode()) {
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
                            throw new RuntimeException(Errors.compose(Errors.E7));
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

    public List<IBaseEntity> getKeyElements() {
        if (!keyElementsInstalled) {
            if (!this.containsComplexKey() && meta.isSearchable())
                keyElements.add(this);

            for (String name : this.meta.getAttributeNames()) {
                IMetaAttribute metaAttribute = this.meta.getMetaAttribute(name);
                IMetaType metaType = metaAttribute.getMetaType();

                if (metaAttribute.isImmutable())
                    continue;

                if (metaType.isComplex()) {
                    IBaseValue baseValue = getBaseValue(name);

                    if (baseValue == null || baseValue.getValue() == null) continue;

                    if (!metaType.isSet()) {
                        keyElements.addAll(((IBaseEntity) baseValue.getValue()).getKeyElements());
                    } else {
                        BaseSet baseSet = (BaseSet) baseValue.getValue();
                        for (IBaseValue childBaseValue : baseSet.get())
                            keyElements.addAll(((IBaseEntity) childBaseValue.getValue()).getKeyElements());
                    }
                }
            }

            keyElementsInstalled = true;
        }

        return keyElements;
    }

    @Override
    public boolean equalsByKey(IBaseEntity baseEntity) {
        if (baseEntity == this)
            return true;

        if (baseEntity == null)
            return false;

        if (!(getClass() == baseEntity.getClass()))
            return false;

        BaseEntity that = (BaseEntity) baseEntity;

        if (this.getBaseEntityReportDate().getCreditorId() != that.getBaseEntityReportDate().getCreditorId())
            return false;

        if (!this.getMeta().equals(that.getMeta()))
            return false;

        for (String name : this.meta.getAttributeNames()) {
            IMetaAttribute metaAttribute = this.meta.getMetaAttribute(name);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaAttribute.isKey() && metaType.isSet())
                continue;

            if (metaAttribute.isKey()) {
                IBaseValue thisBaseValue = this.getBaseValue(name);
                IBaseValue thatBaseValue = that.getBaseValue(name);

                if (metaType.isComplex()) {
                    if (!((BaseEntity) thisBaseValue.getValue()).equalsByKey((IBaseEntity) thatBaseValue.getValue()))
                        return false;
                } else {
                    try{
                        if (!thisBaseValue.getValue().equals(thatBaseValue.getValue()))
                            return false;
                    }catch (NullPointerException ex){
                        logger.debug("NullPointerException baseEntityId=" + baseEntity.getId() + " , batchId=" + baseEntity.getBatchId() + ", attributeName=" + name);
                        return false;
                    }
                }
            }

            if(metaAttribute.isNullableKey()) {
                if(!metaType.isComplex()) {
                    IBaseValue thisBaseValue = this.getBaseValue(name);
                    IBaseValue thatBaseValue = that.getBaseValue(name);

                    if(thisBaseValue == null && thatBaseValue != null) return false;
                    if(thisBaseValue != null && thatBaseValue == null) return false;

                    if(thisBaseValue != null && thatBaseValue != null){
                        if(thisBaseValue.getValue() == null && thatBaseValue.getValue() != null) return false;
                        if(thisBaseValue.getValue() != null && thatBaseValue.getValue() == null) return false;

                        if(thisBaseValue.getValue() != null && thatBaseValue.getValue() != null)
                            if(!thisBaseValue.getValue().equals(thatBaseValue.getValue()))
                                return false;
                    }

                } else {
                    //TODO: isComplex isNullableKey not implemented
                }
            }
        }

        if (this.meta.parentIsKey()) {
            //non-searchable entities NPE
            if(this.getAddInfo() == null || that.getAddInfo() == null)
                return false;

            if(!this.getAddInfo().equals(that.getAddInfo()))
                return false;
        }

        return true;
    }

    /* Проверяет на соответсвие атрибутов */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null)
            return false;

        if (!(getClass() == obj.getClass()))
            return false;

        BaseEntity that = (BaseEntity) obj;

        if (this.getMeta().getId() != that.getMeta().getId())
            return false;

        int thisValueCount = this.getValueCount();
        int thatValueCount = that.getValueCount();

        if (thisValueCount != thatValueCount)
            return false;

        for (String attributeName : meta.getAttributeNames()) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attributeName);
            IMetaType metaType = metaAttribute.getMetaType();

            IBaseValue thisBaseValue = this.getBaseValue(attributeName);
            IBaseValue thatBaseValue = that.getBaseValue(attributeName);

            if (thisBaseValue == null && thatBaseValue == null)
                continue;

            if (thisBaseValue == null || thatBaseValue == null)
                return false;

            if (metaType.isSet()) {
                IBaseSet thisBaseSet = (BaseSet) thisBaseValue.getValue();
                IBaseSet thatBaseSet = (BaseSet) thatBaseValue.getValue();

                if (thisBaseSet == null && thatBaseSet == null)
                    continue;

                if (thisBaseSet == null || thatBaseSet == null)
                    return false;

                if (thisBaseSet.get().size() != thatBaseSet.get().size())
                    return false;

                for (IBaseValue thisChildBaseValue : thisBaseSet.get()) {
                    Object thisChildValue = thisChildBaseValue.getValue();

                    boolean childValueFound = false;


                    for (IBaseValue thatChildBaseValue : thatBaseSet.get()) {
                        Object thatChildValue = thatChildBaseValue.getValue();

                        if (metaType.isComplex() && metaAttribute.isImmutable()) {
                            if (!(((IBaseEntity) thisChildValue).getId() == ((IBaseEntity) thatChildValue).getId()))
                                return false;

                            continue;
                        }

                        if (thisChildValue.equals(thatChildValue))
                            childValueFound = true;
                    }

                    if (!childValueFound)
                        return false;
                }
            } else {
                Object thisValue = thisBaseValue.getValue();
                Object thatValue = thatBaseValue.getValue();

                if (thisValue == null && thatValue == null)
                    continue;

                if (thisValue == null || thatValue == null)
                    return false;

                if (metaType.isComplex() && metaAttribute.isImmutable()) {
                    if (!(((IBaseEntity) thisValue).getId() == ((IBaseEntity) thatValue).getId()))
                        return false;

                    continue;
                }

                if (!thisValue.equals(thatValue))
                    return false;

                // Проверка на изменение ключевых полей
                if (!metaType.isComplex() && (thisBaseValue.getNewBaseValue() != null ||
                        thatBaseValue.getNewBaseValue() != null))
                    return false;
            }
        }

        return true;
    }

    @Override
    public Object getInnerValue(String attributeName) {
        IBaseValue baseValue = getBaseValue(attributeName);

        if (baseValue != null)
            return baseValue.getValue();

        return null;
    }

    @Override
    public IBaseValue safeGetValue(String name) {
        if (this.getAttributes().contains(name)) {
            return getBaseValue(name);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return BaseEntityOutput.toString(this);
    }


    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * result + meta.hashCode();
        result += 31 * result + values.hashCode();

        return result;
    }

    public Object getEls(String path) {
        Queue<Object> queue = new LinkedList<>();

        StringBuilder str = new StringBuilder();
        String[] operations = new String[500];
        boolean[] isFilter = new boolean[500];
        String function = null;

        if (!path.startsWith("{")) throw new RuntimeException(Errors.compose(Errors.E14));
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '}') {
                function = path.substring(1, i);
                path = path.substring(i + 1);
                break;
            }
        }

        if (function == null) throw new RuntimeException(Errors.compose(Errors.E15));

        Set<Object> allowedSet = new TreeSet<>();

        if (function.startsWith("set")) {
            String[] elems = function.substring(function.indexOf('(') + 1, function.indexOf(')')).split(",");
            if (function.startsWith("setInt")) {
                allowedSet = new TreeSet<>();
                for (String e : elems)
                    allowedSet.add(Integer.parseInt(e.trim()));
            } else if (function.startsWith("setLong")) {
                allowedSet = new TreeSet<>();
                for (String e : elems)
                    allowedSet.add(Long.parseLong(e.trim()));
            } else if (function.startsWith("setString")) {
                allowedSet = new TreeSet<>();
                for (String e : elems)
                    allowedSet.add(e.trim());
            }
        }

        if (function.startsWith("hasDuplicates")) {
            String pattern = "hasDuplicates\\((\\S+)\\)";
            Matcher m = Pattern.compile(pattern).matcher(function);
            String downPath;
            boolean ret = false;

            if (m.find()) {
                downPath = m.group(1);
            } else {
                throw new RuntimeException(Errors.compose(Errors.E16));
            }

            LinkedList list = (LinkedList) getEls("{get}" + downPath);

            String[] fields = path.split(",");

            Set<Object> controlSet;

            if (fields.length == 1)
                controlSet = new HashSet<>();
            else if (fields.length == 2)
                controlSet = new HashSet<>();
            else
                throw new RuntimeException(Errors.compose(Errors.E17));

            for (Object o : list) {
                BaseEntity entity = (BaseEntity) o;
                Object entry;

                if (fields.length == 1)
                    entry = entity.getEl(fields[0]);
                else { // fields.length  == 2
                    entry = new AbstractMap.SimpleEntry<>(entity.getEl(fields[0]), entity.getEl(fields[1]));
                }


                if (controlSet.contains(entry)) {
                    ret = true;
                } else {
                    controlSet.add(entry);
                }
            }
            return ret;
        }


        int yk = 0;
        int open = 0;
        int eqCnt = 0;

        for (int i = 0; i <= path.length(); i++) {
            if (i == path.length()) {
                if (open != 0)
                    throw new RuntimeException(Errors.compose(Errors.E18));
                break;
            }
            if (path.charAt(i) == '=') eqCnt++;
            if (path.charAt(i) == '!' && (i + 1 == path.length() || path.charAt(i + 1) != '='))
                throw new RuntimeException(Errors.compose(Errors.E21));

            if (path.charAt(i) == '[') open++;
            if (path.charAt(i) == ']') {
                open--;
                if (eqCnt != 1) throw new RuntimeException(Errors.compose(Errors.E20));
                eqCnt = 0;
            }
            if (open < 0 || open > 1) throw new RuntimeException(Errors.compose(Errors.E22));
        }

        for (int i = 0; i <= path.length(); i++) {
            if (i == path.length()) {
                if (str.length() > 0) {
                    String[] arr = str.toString().split("\\.");
                    for (String anArr : arr) {
                        operations[yk] = anArr;
                        isFilter[yk] = false;
                        yk++;
                    }
                }
                break;
            }
            char c = path.charAt(i);
            if (c == '[' || c == ']') {
                if (str.length() > 0) {
                    if (c == ']') {
                        operations[yk] = str.toString();
                        isFilter[yk] = true;
                        yk++;
                    } else {
                        String[] arr = str.toString().split("\\.");
                        for (String anArr : arr) {
                            operations[yk] = anArr;
                            isFilter[yk] = false;
                            yk++;
                        }
                    }
                    str.setLength(0);
                }
            } else {
                str.append(c);
            }
        }

        List<Object> ret = new LinkedList<>();
        queue.add(this);
        queue.add(0);
        int retCount = 0;

        while (queue.size() > 0) {
            Object curO = queue.poll();
            int step = (Integer) queue.poll();

            if (curO == null)
                continue;

            if (step == yk) {
                if (function.startsWith("count")) {
                    retCount++;
                } else if (function.startsWith("set"))
                    if (allowedSet.contains(curO))
                        retCount++;
                ret.add(curO);
                continue;
            }

            //noinspection ConstantConditions
            BaseEntity curBE = (BaseEntity) curO;
            MetaClass curMeta = curBE.getMeta();

            if (!isFilter[step]) {
                IMetaAttribute nextAttribute = curMeta.getMetaAttribute(operations[step]);

                if (!nextAttribute.getMetaType().isComplex()) { // transition to BASIC type
                    queue.add(curBE.getEl(operations[step]));
                    queue.add(step + 1);
                } else if (nextAttribute.getMetaType().isSet()) { //transition to array
                    BaseSet next = (BaseSet) curBE.getEl(operations[step]);
                    if (next != null) {
                        for (Object o : next.get()) {
                            {
                                queue.add(((BaseValue) o).getValue());
                                queue.add(step + 1);
                            }
                        }
                    }
                } else { //transition to simple
                    BaseEntity next = (BaseEntity) curBE.getEl(operations[step]);
                    queue.add(next);
                    queue.add(step + 1);
                }
            } else {
                String[] parts;
                boolean inv = false;

                if (operations[step].contains("!")) {
                    parts = operations[step].split("!=");
                    inv = true;
                } else
                    parts = operations[step].split("=");

                Object o = curBE.getEl(parts[0]);

                boolean expr = (o == null && parts[1].equals("null")) || (o != null && o.toString().equals(parts[1]));
                if (inv) expr = !expr;

                if (expr) {
                    queue.add(curO);
                    queue.add(step + 1);
                }
            }
        }

        if (function.startsWith("get"))
            return ret;

        return retCount;
    }

    public Object getEl(String path) {
        if (path.equals("ROOT"))
            return getId();

        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        BaseEntity entity = this;
        MetaClass theMeta = meta;
        Object valueOut = null;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String arrayIndexes = null;

            if (token.contains("[")) {
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

            if (type == null)
                throw new IllegalStateException(Errors.compose(Errors.E46));

            if (type.isSet()) {
                if (arrayIndexes != null) {
                    valueOut = ((BaseSet) valueOut).getEl(arrayIndexes.replaceAll("->", "."));
                    type = ((MetaSet) type).getMemberType();
                } else {
                    return valueOut;
                }
            }

            if (type.isComplex()) {
                entity = (BaseEntity) valueOut;
                theMeta = (MetaClass) type;
            } else {
                if (tokenizer.hasMoreTokens()) {
                    throw new IllegalArgumentException(Errors.compose(Errors.E13));
                }
            }
        }

        return valueOut;
    }

    public List<Object> getElWithArrays(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        BaseEntity entity = this;
        MetaClass theMeta = meta;
        ArrayList<Object> valueOut = new ArrayList<>();
        Object currentValue;

        try {
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                String arrayIndexes = null;

                if (token.contains("[")) {
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

                if (type.isSet()) {
                    BaseSet set = (BaseSet) currentValue;
                    if (arrayIndexes != null) {
                        currentValue = set.getEl(arrayIndexes.replaceAll("->", "."));
                        type = ((MetaSet) type).getMemberType();
                    } else {
                        if (tokenizer.hasMoreTokens()) {
                            if (!set.getMemberType().isComplex()) {
                                throw new IllegalArgumentException(Errors.compose(Errors.E23));
                            }

                            if (set.getMemberType().isSet()) {
                                throw new IllegalArgumentException(Errors.compose(Errors.E23));
                            }

                            String restOfPath = "";
                            boolean first = true;
                            while (tokenizer.hasMoreTokens()) {
                                if (first) {
                                    restOfPath += tokenizer.nextToken();
                                    first = false;
                                } else {
                                    restOfPath += "." + tokenizer.nextToken();
                                }
                            }

                            for (IBaseValue obj : set.get()) {
                                BaseEntity currentEntity = (BaseEntity) (obj.getValue());
                                if (currentEntity != null)
                                    valueOut.addAll(currentEntity.getElWithArrays(restOfPath));
                                else
                                    logger.warn("Null in set");
                            }

                            return valueOut;
                        }
                    }
                }

                if (type.isComplex() && !type.isSet()) {
                    //noinspection ConstantConditions
                    entity = (BaseEntity) currentValue;
                    theMeta = (MetaClass) type;
                } else {
                    if (tokenizer.hasMoreTokens()) {
                        throw new IllegalArgumentException(Errors.compose(Errors.E13));
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

    boolean equalsToString(HashMap<String, String> params) {
        for (String fieldName : params.keySet()) {
            String ownFieldName;
            String innerPath = null;
            if (fieldName.contains(".")) {
                ownFieldName = fieldName.substring(0, fieldName.indexOf("."));
                innerPath = fieldName.substring(fieldName.indexOf(".") + 1);
            } else {
                ownFieldName = fieldName;
            }

            IMetaType mtype = meta.getMemberType(ownFieldName);

            if (mtype == null)
                throw new IllegalArgumentException(Errors.compose(Errors.E9,fieldName));

            if (mtype.isSet())
                throw new IllegalArgumentException(Errors.compose(Errors.E10,fieldName));

            BaseValue baseValue = (BaseValue) getBaseValue(ownFieldName);

            if (mtype.isComplex()) {
                baseValue = (BaseValue) ((BaseEntity) (baseValue.getValue())).getBaseValue(innerPath);
                mtype = ((MetaClass) mtype).getMemberType(innerPath);
            }

            if (!baseValue.equalsToString(params.get(fieldName), ((MetaValue) mtype).getTypeCode()))
                return false;
        }

        return true;
    }

    public void addValidationError(String errorMsg) {
        validationErrors.add(errorMsg);
    }

    public void clearValidationErrors() {
        validationErrors.clear();
    }

    @Override
    public Set<String> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public BaseEntity clone() {
        BaseEntity baseEntityCloned;

        try {
            baseEntityCloned = (BaseEntity) super.clone();

            BaseEntityReportDate baseEntityReportDateCloned = ((BaseEntityReportDate) baseEntityReportDate).clone();
            baseEntityReportDateCloned.setBaseEntity(baseEntityCloned);
            baseEntityCloned.setBaseEntityReportDate(baseEntityReportDateCloned);

            HashMap<String, IBaseValue> valuesCloned = new HashMap<>();

            for (String attribute : values.keySet()) {
                IBaseValue baseValue = values.get(attribute);
                IBaseValue baseValueCloned = ((BaseValue) baseValue).clone();
                baseValueCloned.setBaseContainer(baseEntityCloned);
                valuesCloned.put(attribute, baseValueCloned);
            }

            baseEntityCloned.values = valuesCloned;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(Errors.compose(Errors.E8));
        }

        return baseEntityCloned;
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean containsComplexKey() {
        for (String name : meta.getAttributeNames()) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(name);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaType.isComplex() && metaAttribute.isKey() && !metaAttribute.isImmutable())
                return true;
        }

        return false;
    }

    public UUID getUuid() {
        return uuid;
    }


    public boolean isInsert() {
        return operationType == null || operationType.equals(OperationType.INSERT);
    }

    public boolean isUpdate() {
        return OperationType.UPDATE.equals(operationType);
    }

    public boolean isLastInBatch() {
        return isLastInBatch;
    }

    public void setLastInBatch(boolean lastInBatch) {
        isLastInBatch = lastInBatch;
    }
}
