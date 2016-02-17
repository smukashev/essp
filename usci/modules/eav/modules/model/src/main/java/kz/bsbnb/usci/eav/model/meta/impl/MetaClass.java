package kz.bsbnb.usci.eav.model.meta.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;

import java.util.*;

public class MetaClass extends MetaContainer implements IMetaClass {

    private static final long serialVersionUID = 1L;

    private String className;

    private String classTitle;

    private Date beginDate;

    private boolean disabled = false;

    private boolean searchable = false;

    private boolean reference = false;

    private boolean parentIsKey = false;

    private HashMap<String, IMetaAttribute> members = new HashMap<>();

    private ComplexKeyTypes complexKeyType = ComplexKeyTypes.ALL;

    public MetaClass() {
        super(MetaContainerTypes.META_CLASS);

        this.beginDate = new Date();
        DataUtils.toBeginningOfTheDay(beginDate);
    }

    public MetaClass(MetaClass meta) {
        super(meta.id, MetaContainerTypes.META_CLASS);

        this.className = meta.className;
        this.classTitle = meta.classTitle;
        this.disabled = meta.disabled;
        this.beginDate = meta.beginDate;
        this.complexKeyType = meta.complexKeyType;
        this.reference = meta.reference;
        this.parentIsKey = meta.parentIsKey;

        members.putAll(meta.members);
    }

    public MetaClass(String className) {
        super(MetaContainerTypes.META_CLASS);

        this.className = className;
        this.beginDate = new Date();
        DataUtils.toBeginningOfTheDay(beginDate);
    }

    public MetaClass(String className, Date beginDate) {
        super(MetaContainerTypes.META_CLASS);

        this.className = className;
        this.beginDate = beginDate;
    }

    public ComplexKeyTypes getComplexKeyType() {
        return complexKeyType;
    }

    public void setComplexKeyType(ComplexKeyTypes complexKeyType) {
        this.complexKeyType = complexKeyType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Set<String> getMemberNames() {
        return members.keySet();
    }

    public IMetaType getMemberType(String name) {
        IMetaAttribute metaAttribute = members.get(name);

        if (metaAttribute == null)
            throw new IllegalArgumentException(Errors.E45 + "|" + name + "|" + this.getClassName());

        return metaAttribute.getMetaType();
    }

    public IMetaAttribute getMetaAttribute(String name) {
        return members.get(name);
    }

    public void removeMemberType(String name) {
        members.remove(name);

        searchable = false;

        for (IMetaAttribute metaAttribute : members.values()) {
            if (metaAttribute.isKey()) {
                searchable = true;
                break;
            }
        }
    }

    public void setMetaAttribute(String name, IMetaAttribute metaAttribute) {
        if (!searchable && metaAttribute.isKey()) {
            searchable = true;
        }
        members.put(name, metaAttribute);
        metaAttribute.setName(name);
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        Date newBeginDate = (Date) beginDate.clone();
        DataUtils.toBeginningOfTheDay(newBeginDate);

        this.beginDate = newBeginDate;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void removeMembers() {
        searchable = false;
        members.clear();
    }

    public Set<String> getAttributeNames() {
        return members.keySet();
    }

    public Set<String> getSimpleAttributesNames(DataTypes dataType) {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && !type.isComplex()) {
                MetaValue metaValue = (MetaValue) type;
                if (metaValue.getTypeCode().equals(dataType))
                    filteredAttributeNames.add(attributeName);
            }
        }
        return filteredAttributeNames;
    }

    public Set<String> getSimpleAttributesNames() {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && !type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getSimpleSetAttributesNames() {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && !type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getComplexAttributesNames() {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getSimpleSetAttributesNames(DataTypes dataType) {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && !type.isComplex()) {
                MetaSet metaValueArray = (MetaSet) type;

                if (metaValueArray.getTypeCode().equals(dataType))
                    filteredAttributeNames.add(attributeName);
            }
        }

        return filteredAttributeNames;
    }

    public Set<String> getComplexArrayAttributesNames() {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<>();

        for (String attributeName : allAttributeNames) {
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(getClass() == obj.getClass()))
            return false;
        else {
            MetaClass tmp = (MetaClass) obj;
            if (this.getId() > 0 && tmp.getId() > 0 && this.getId() == tmp.getId())
                return true;

            if (tmp.getAttributesCount() != this.getAttributesCount())
                return false;

            Set<String> thisNames = this.members.keySet();
            for (String name : thisNames) {
                if (!(this.getMemberType(name).equals(tmp.getMemberType(name))))
                    return false;
            }
            return !(tmp.isDisabled() != this.isDisabled() ||
                    !tmp.getBeginDate().equals(this.getBeginDate()) ||
                    !tmp.getClassName().equals(this.getClassName()) ||
                    !tmp.complexKeyType.equals(this.complexKeyType));
        }
    }

    @Override
    public boolean isSet() {
        return false;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public boolean isSetOfSets() {
        return false;
    }

    public int getAttributesCount() {
        return members.size();
    }

    public boolean isSearchable() {
        //return searchable;
        //TODO:fix this, searchable flag is not set if attribute is changed outside

        Iterator<IMetaAttribute> it = members.values().iterator();
        while (it.hasNext()) {
            IMetaAttribute metaAttribute = it.next();
            if (metaAttribute.isKey()) {
                return true;
            }
        }

        return false;
    }

    public String toString(String prefix) {
        String str = className + ":metaClass(" + getId() + "_" + searchable + "_" + complexKeyType + ");";

        String[] names;

        names = members.keySet().toArray(new String[members.keySet().size()]);

        Arrays.sort(names);

        for (String memberName : names) {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            String key = "";

            if (attribute.isKey()) key = "*";

            str += "\n" + prefix + memberName + "(" + attribute.getId() + ")" + key + ": " +
                    type.toString(prefix + "\t");
        }

        return str;
    }


    public String toJava(String prefix) {
        String str = " ";

        String[] names;

        names = members.keySet().toArray(new String[members.keySet().size()]);

        Arrays.sort(names);

        str += prefix + "MetaClass meta" + className.substring(0, 1).toUpperCase()
                + className.substring(1) + "Holder = new MetaClass(\"" + className + "\");";

        for (String memberName : names) {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();
            if (type.isComplex()) {
                if (!type.isSet()) {
                    str += "\n" + type.toJava(prefix) + "\n " + prefix + "meta" +
                            className.substring(0, 1).toUpperCase()
                            + className.substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\"" +
                            " , new MetaAttribute( " + attribute.isKey() + "," + attribute.isNullable() + ", "
                            + "meta" + memberName.substring(0, 1).toUpperCase()
                            + memberName.substring(1) + "Holder));";
                } else {
                    str += type.toJava(prefix) + "\n " + prefix + "meta" +
                            className.substring(0, 1).toUpperCase()
                            + className.substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\"" +
                            ", new MetaAttribute(new MetaSet(" + "meta" +
                            memberName.substring(0, 1).toUpperCase()
                            + memberName.substring(1) + "Holder)));";
                }
            } else {
                str += "\n " + prefix + "meta" + className.substring(0, 1).toUpperCase()
                        + className.substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\"" +
                        ", new MetaAttribute(" + attribute.isKey() + ", " + attribute.isNullable() + ", "
                        + type.toJava(prefix) + "));";
            }
        }
        return str;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + beginDate.hashCode();
        result = 31 * result + (disabled ? 1 : 0);
        result = 31 * result + (searchable ? 1 : 0);
        result = 31 * result + members.hashCode();
        result = 31 * result + complexKeyType.hashCode();
        return result;
    }

    public String toString() {
        return toString("");
    }

    @Override
    public boolean isReference() {
        return reference;
    }

    @Override
    public void setReference(boolean value) {
        reference = value;
    }

    public void recursiveSet(MetaClass subMeta) {
        for (String memberName : members.keySet()) {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            if (type.isComplex()) {
                if (type.isSet()) {
                    ((MetaSet) type).recursiveSet(subMeta);
                } else {
                    if (subMeta.getClassName().equals(((MetaClass) type).getClassName())) {
                        attribute.setMetaType(subMeta);
                    } else {
                        ((MetaClass) type).recursiveSet(subMeta);
                    }
                }
            }
        }
    }

    public List<String> getAllPaths(MetaClass subMeta) {
        return getAllPaths(subMeta, null);
    }

    public List<String> getAllPaths(MetaClass subMeta, String path) {
        ArrayList<String> paths = new ArrayList<String>();

        for (String memberName : members.keySet()) {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            if (type.isComplex()) {
                if (type.isSet()) {
                    paths.addAll(((MetaSet) type).getAllPaths(subMeta, (path == null ? "" : (path + "."))
                            + memberName));
                } else {
                    if (subMeta.getClassName().equals(((MetaClass) type).getClassName())) {
                        paths.add((path == null ? "" : (path + ".")) + memberName);
                    } else {
                        paths.addAll(((MetaClass) type).getAllPaths(subMeta,
                                (path == null ? "" : (path + ".")) + memberName));
                    }
                }
            }
        }

        return paths;
    }

    public IMetaType getEl(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        MetaClass meta = this;
        IMetaType valueOut = null;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            IMetaAttribute attribute = meta.getMetaAttribute(token);

            if (attribute == null)
                return null;

            IMetaType type = attribute.getMetaType();

            valueOut = type;

            if (type.isSet()) {
                while (type.isSet()) {
                    valueOut = type;
                    type = ((MetaSet) type).getMemberType();
                }
            }

            if (valueOut.isComplex()) {
                if (!valueOut.isSet()) {
                    meta = (MetaClass) valueOut;
                } else {
                    meta = (MetaClass) type;
                }
            } else {
                if (tokenizer.hasMoreTokens()) {
                    throw new IllegalArgumentException(Errors.E44 + "");
                }
            }
        }

        return valueOut;
    }

    public IMetaAttribute getElAttribute(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        MetaClass meta = this;
        IMetaType valueOut;
        IMetaAttribute attribute = null;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            attribute = meta.getMetaAttribute(token);

            if (attribute == null)
                return null;

            IMetaType type = attribute.getMetaType();

            valueOut = type;

            if (type.isSet()) {
                while (type.isSet()) {
                    valueOut = type;
                    type = ((MetaSet) type).getMemberType();
                }
            }

            if (valueOut.isComplex()) {
                if (!valueOut.isSet()) {
                    meta = (MetaClass) valueOut;
                } else {
                    meta = (MetaClass) type;
                }
            } else {
                if (tokenizer.hasMoreTokens())
                    throw new IllegalArgumentException(Errors.E44 + "");
            }
        }

        return attribute;
    }

    public String getClassTitle() {
        return classTitle;
    }

    public void setClassTitle(String classTitle) {
        this.classTitle = classTitle;
    }

    public boolean isParentIsKey() {
        return parentIsKey;
    }

    public void setParentIsKey(boolean parentIsKey) {
        this.parentIsKey = parentIsKey;
    }

    @Override
    public boolean hasNotFinalAttributes() {
        for (String attribute : getAttributeNames()) {
            IMetaAttribute metaAttribute = getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaAttribute.isImmutable())
                continue;

            if (!metaAttribute.isFinal())
                return true;

            boolean hasNotFinalAttributes = false;

            if (metaType.isComplex()) {
                if (metaType.isSet()) {
                    if (metaType.isSetOfSets())
                        throw new UnsupportedOperationException(Errors.E2+"");

                    IMetaSet childMetaSet = (IMetaSet) metaType;
                    IMetaClass childMetaClass = (IMetaClass) childMetaSet.getMemberType();

                    if (childMetaClass.isSearchable())
                        continue;

                    hasNotFinalAttributes = childMetaClass.hasNotFinalAttributes();
                } else {
                    IMetaClass childMetaClass = (IMetaClass) metaType;

                    if (childMetaClass.isSearchable())
                        continue;

                    hasNotFinalAttributes = childMetaClass.hasNotFinalAttributes();
                }
            }

            if (hasNotFinalAttributes)
                return true;
        }

        return false;
    }
}
