package kz.bsbnb.usci.eav.model.meta.impl;

import java.util.*;

import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;

public class MetaClass extends MetaContainer implements IMetaClass
{
	/**
	 * Name of the meta. Used as a key value for database search if <code>id</code> is 0
	 */
    private String className;

    private String classTitle;

    private Date beginDate;

    private boolean disabled = false;

    private boolean searchable = false;

    private boolean reference = false;

    private boolean parentIsKey = false;

	/**
	 * Holds type values. Keys of hash are type names.
	 */
    private HashMap<String, IMetaAttribute> members = new HashMap<String, IMetaAttribute>();
	
	/**
     * When attribute is an entity, and is a key attribute - sets key usage strategy.
     * Defaults to <code>ArrayKeyTypes.ALL</code>
     * @see ComplexKeyTypes
     */
    private ComplexKeyTypes complexKeyType = ComplexKeyTypes.ALL;
    
    /**
	 * When additional searching logic is needed. This attribute could be set to stored procedure name.
	 * This SP will get key attributes of entity and return entities from BD.
	 */
    private String searchProcedureName = null;

    public MetaClass()
    {
        super(MetaContainerTypes.META_CLASS);

        this.beginDate = new Date();
        DataUtils.toBeginningOfTheDay(beginDate);
    }

    public MetaClass(MetaClass meta)
    {
        super(meta.id, MetaContainerTypes.META_CLASS);

        this.className = meta.className;
        this.disabled = meta.disabled;
        this.beginDate = meta.beginDate;
        this.complexKeyType = meta.complexKeyType;

        members.putAll(meta.members);
    }

	public MetaClass(String className)
    {
        super(MetaContainerTypes.META_CLASS);

		this.className = className;
        this.beginDate = new Date();
        DataUtils.toBeginningOfTheDay(beginDate);
	}

    public MetaClass(String className, Date beginDate)
    {
        super(MetaContainerTypes.META_CLASS);

        this.className = className;
        this.beginDate = beginDate;
    }

	public ComplexKeyTypes getComplexKeyType()
    {
		return complexKeyType;
	}

	public void setComplexKeyType(ComplexKeyTypes complexKeyType)
    {
		this.complexKeyType = complexKeyType;
	}
	
	/**
	 * Used to get entity class name
	 * 
	 * @return entity class name
	 */
	public String getClassName()
    {
		return className;
	}

	public void setClassName(String className)
    {
		this.className = className;
	}
	
	/**
	 * Used to retrieve all attribute names 
	 * 
	 * @return list of attribute names
	 */
	public Set<String> getMemberNames()
    {
		return members.keySet();
	}
	
	/**
	 * Used to get type of the attribute with name <code>name</code>
	 * 
	 * @param name name of the attribute
	 * @return type of that attribute
	 * @see MetaValue
	 */
	public IMetaType getMemberType(String name)
    {
        IMetaAttribute metaAttribute = members.get(name);
        if (metaAttribute == null) {
            throw new IllegalArgumentException("Type: " + name +
                    ", not found in class: " + this.getClassName());
        }
        return metaAttribute.getMetaType();
	}

    /**
     * Used to get type of the attribute with name <code>name</code>
     *
     * @param name name of the attribute
     * @return type of that attribute
     * @see MetaValue
     */
    public IMetaAttribute getMetaAttribute(String name)
    {
        return members.get(name);
    }

	public void removeMemberType(String name)
    {
        members.remove(name);

        searchable = false;
        Iterator<IMetaAttribute> it = members.values().iterator();
        while (it.hasNext())
        {
            IMetaAttribute metaAttribute = it.next();
            if (metaAttribute.isKey())
            {
                searchable = true;
                break;
            }
        }
	}

	/**
	 * Used to set attribute type. If there is no such attribute, then creates one.
	 * 
	 * @param name attributes name
	 * @param metaAttribute type to be set
	 * @see MetaValue
	 */
	public void setMetaAttribute(String name, IMetaAttribute metaAttribute)
    {
		if (!searchable && metaAttribute.isKey())
        {
            searchable = true;
        }
        members.put(name, metaAttribute);
        metaAttribute.setName(name);
	}

    public String getSearchProcedureName()
    {
		return searchProcedureName;
	}

	public void setSearchProcedureName(String searchProcedureName)
    {
		this.searchProcedureName = searchProcedureName;
	}

    public Date getBeginDate()
    {
        return beginDate;
    }

    public void setBeginDate(Date beginDate)
    {
        Date newBeginDate = (Date)beginDate.clone();
        DataUtils.toBeginningOfTheDay(newBeginDate);

        this.beginDate = newBeginDate;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public void removeMembers()
    {
        searchable = false;
        members.clear();
    }

    public Set<String> getAttributeNames() {
        return members.keySet();
    }

    /**
     * Used to get list of the attribute names with type <code>dataType</code>
     *
     * @param dataType type of the attribute
     * @return list of the attribute names
     * @see DataTypes
     * @see MetaValue
     */
    public Set<String> getSimpleAttributesNames(DataTypes dataType)
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && !type.isComplex())
            {
                MetaValue metaValue = (MetaValue)type;
                if (metaValue.getTypeCode().equals(dataType))
                    filteredAttributeNames.add(attributeName);
            }
        }
        return filteredAttributeNames;
    }

    public Set<String> getSimpleAttributesNames()
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && !type.isComplex())
            {
                MetaValue metaValue = (MetaValue)type;
                filteredAttributeNames.add(attributeName);
            }
        }
        return filteredAttributeNames;
    }

    public Set<String> getSimpleSetAttributesNames()
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && !type.isComplex())
            {
                filteredAttributeNames.add(attributeName);
            }
        }
        return filteredAttributeNames;
    }

    public Set<String> getComplexAttributesNames() {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (!type.isSet() && type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getSimpleSetAttributesNames(DataTypes dataType)
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && !type.isComplex())
            {
                MetaSet metaValueArray = (MetaSet)type;

                if (metaValueArray.getTypeCode().equals(dataType))
                    filteredAttributeNames.add(attributeName);
            }
        }

        return filteredAttributeNames;
    }

    public Set<String> getComplexArrayAttributesNames()
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && type.isComplex())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getArrayAttributesNames()
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public Set<String> getArrayArrayAttributesNames()
    {
        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();

        while (it.hasNext())
        {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);

            if (type.isSet() && ((MetaSet)type).getMemberType().isSet())
                filteredAttributeNames.add(attributeName);
        }

        return filteredAttributeNames;
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(getClass() == obj.getClass()))
            return false;
        else
        {
            MetaClass tmp = (MetaClass) obj;
            if (tmp.getAttributesCount() != this.getAttributesCount())
                return false;

            Set<String> thisNames = this.members.keySet();
            for (String name : thisNames)
            {
                if(!(this.getMemberType(name).equals(tmp.getMemberType(name))))
                    return false;
            }
            return !(tmp.isDisabled() != this.isDisabled() ||
                    !tmp.getBeginDate().equals(this.getBeginDate()) ||
                    !tmp.getClassName().equals(this.getClassName()) ||
                    !tmp.complexKeyType.equals(this.complexKeyType));
        }
    }

    @Override
    public boolean isSet()
    {
        return false;
    }

    @Override
    public boolean isComplex()
    {
        return true;
    }

    @Override
    public boolean isSetOfSets() {
        return false;
    }

    public int getAttributesCount()
    {
        return members.size();
    }

    public boolean isSearchable() {
        //return searchable;
        //TODO:fix this, searcheble flag is not set if attribute is changed outside

        Iterator<IMetaAttribute> it = members.values().iterator();
        while (it.hasNext())
        {
            IMetaAttribute metaAttribute = it.next();
            if (metaAttribute.isKey())
            {
                return true;
            }
        }

        return false;
    }

    public String toString(String prefix)
    {
        String str = className + ":metaClass(" + getId() + "_" + searchable + "_" + complexKeyType + ");";

        String[] names;

        names = (String[]) members.keySet().toArray(new String[members.keySet().size()]);

        Arrays.sort(names);

        for (String memberName : names)
        {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            String key = "";

            if (attribute.isKey()) key = "*";

            str += "\n" + prefix + memberName + "(" + attribute.getId() + ")" + key + ": " + type.toString(prefix + "\t");
        }

        return str;
    }


    /*
    *   generating Meta Class into Java code
    *
    *  */
    public String toJava(String prefix)
    {
        String str = " ";

        String[] names;

        names = (String[]) members.keySet().toArray(new String[members.keySet().size()]);

        Arrays.sort(names);

        // creates Holder object of Meta Class
        str += prefix + "meta"+className.toString().substring(0, 1).toUpperCase()
                + className.toString().substring(1) + "Holder = new MetaClass( \"" + className + "\" );";

        for (String memberName : names)
        {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();
            if (type.isComplex()) // entity or set
            {
                if(!type.isSet()) // not set
                {
                    str +="\n" + type.toJava(prefix)+"\n " + prefix + "meta" + className.toString().substring(0, 1).toUpperCase()
                            + className.toString().substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\" " +
                            " , new MetaAttribute( " + attribute.isKey() + "," + attribute.isNullable() + " , " + "meta"
                            + memberName.toString().substring(0, 1).toUpperCase()
                            + memberName.toString().substring(1) + "Holder));" ;
                }
                else // set
                {
                    str += type.toJava(prefix)+"\n " + prefix + "meta"+className.toString().substring(0, 1).toUpperCase()
                            + className.toString().substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\" " +
                            ", new MetaAttribute( new MetaSet( " + "meta" + memberName.toString().substring(0, 1).toUpperCase()
                            + memberName.toString().substring(1) + "Holder)));" ;
                }
            }
            else // simple value
            {
                str += "\n " + prefix + "meta" + className.toString().substring(0, 1).toUpperCase()
                        + className.toString().substring(1) + "Holder.setMetaAttribute(\"" + memberName + "\" " +
                        ", new MetaAttribute( " + attribute.isKey() + " , " + attribute.isNullable() + " , " + type.toJava(prefix) + "));";
            }
        }
        return str;
    }

    public String getJavaFunction(String fName) {
        String str  = "protected MetaClass " + fName + "()\n{\n";
        str += toJava("  ");
        str += "\n\n   return meta"+className.toString().substring(0, 1).toUpperCase()
                + className.toString().substring(1) + "Holder;\n";
        str += "}";
        return str;
    }



    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + beginDate.hashCode();
        result = 31 * result + (disabled ? 1 : 0);
        result = 31 * result + (searchable ? 1 : 0);
        result = 31 * result + members.hashCode();
        result = 31 * result + complexKeyType.hashCode();
        result = 31 * result + (searchProcedureName != null ? searchProcedureName.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return toString("");
    }

    @Override
    public boolean isReference()
    {
        return reference;
    }

    @Override
    public void setReference(boolean value)
    {
        reference = value;
    }

    public void recursiveSet(MetaClass subMeta) {
        for (String memberName : members.keySet())
        {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            if (type.isComplex()) {
                if (type.isSet()) {
                    ((MetaSet)type).recursiveSet(subMeta);
                } else {
                    if (subMeta.getClassName().equals(((MetaClass)type).getClassName())) {
                        attribute.setMetaType(subMeta);
                    } else {
                        ((MetaClass)type).recursiveSet(subMeta);
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

        for (String memberName : members.keySet())
        {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            if (type.isComplex()) {
                if (type.isSet()) {
                    paths.addAll(((MetaSet)type).getAllPaths(subMeta, (path == null ? "" : (path + ".")) + memberName));
                } else {
                    if (subMeta.getClassName().equals(((MetaClass)type).getClassName())) {
                        paths.add((path == null ? "" : (path + ".")) + memberName);
                    } else {
                        paths.addAll(((MetaClass)type).getAllPaths(subMeta,
                                (path == null ? "" : (path + ".")) + memberName));
                    }
                }
            }
        }

        return paths;
    }

    public IMetaType getEl(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        MetaClass meta = this;
        IMetaType valueOut = null;

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();

            IMetaAttribute attribute = meta.getMetaAttribute(token);

            if (attribute == null)
                return null;

            IMetaType type = attribute.getMetaType();

            valueOut = type;

            if (type.isSet())
            {
                while(type.isSet()) {
                    valueOut = type;
                    type = ((MetaSet)type).getMemberType();
                }
            }

            if (valueOut.isComplex())
            {
                if (!valueOut.isSet()) {
                    meta = (MetaClass)valueOut;
                } else {
                    meta = (MetaClass)type;
                }
            } else {
                if (tokenizer.hasMoreTokens())
                {
                    throw new IllegalArgumentException("Path can't have intermediate simple values");
                }
            }
        }

        return valueOut;
    }

    public IMetaAttribute getElAttribute(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        MetaClass meta = this;
        IMetaType valueOut = null;
        IMetaAttribute attribute = null;

        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();

            attribute = meta.getMetaAttribute(token);

            if (attribute == null)
                return null;

            IMetaType type = attribute.getMetaType();

            valueOut = type;

            if (type.isSet())
            {
                while(type.isSet()) {
                    valueOut = type;
                    type = ((MetaSet)type).getMemberType();
                }
            }

            if (valueOut.isComplex())
            {
                if (!valueOut.isSet()) {
                    meta = (MetaClass)valueOut;
                } else {
                    meta = (MetaClass)type;
                }
            } else {
                if (tokenizer.hasMoreTokens())
                {
                    throw new IllegalArgumentException("Path can't have intermediate simple values");
                }
            }
        }

        return attribute;
    }

    public boolean arrayInPath(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");

        MetaClass meta = this;
        IMetaType valueOut = null;

        boolean wasArray = false;

        while (tokenizer.hasMoreTokens())
        {
            if (wasArray)
                return true;

            String token = tokenizer.nextToken();

            IMetaAttribute attribute = meta.getMetaAttribute(token);

            if (attribute == null)
                return false;

            IMetaType type = attribute.getMetaType();

            valueOut = type;

            if (type.isSet())
            {
                if(!wasArray)
                    wasArray = true;
                while(type.isSet()) {
                    valueOut = type;
                    type = ((MetaSet)type).getMemberType();
                }
            }

            if (valueOut.isComplex())
            {
                if (!valueOut.isSet()) {
                    meta = (MetaClass)valueOut;
                } else {
                    meta = (MetaClass)type;
                }
            } else {
                if (tokenizer.hasMoreTokens())
                {
                    throw new IllegalArgumentException("Path can't have intermediate simple values");
                }
            }
        }

        return false;
    }

    public String getClassTitle()
    {
        return classTitle;
    }

    public void setClassTitle(String classTitle)
    {
        this.classTitle = classTitle;
    }

    public boolean isParentIsKey()
    {
        return parentIsKey;
    }

    public void setParentIsKey(boolean parentIsKey)
    {
        this.parentIsKey = parentIsKey;
    }

    @Override
    public boolean hasNotFinalAttributes() {
        for (String attribute: getAttributeNames())
        {
            IMetaAttribute metaAttribute = getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaAttribute.isImmutable())
                continue;

            if (!metaAttribute.isFinal())
                return true;

            boolean hasNotFinalAttribites = false;
            if (metaType.isComplex())
            {
                if (metaType.isSet())
                {
                    if (metaType.isSetOfSets())
                    {
                        throw new UnsupportedOperationException("Не реализовано;");
                    }

                    IMetaSet childMetaSet = (IMetaSet)metaType;
                    IMetaClass childMetaClass = (IMetaClass)childMetaSet.getMemberType();
                    if (childMetaClass.isSearchable())
                    {
                        continue;
                    }
                    hasNotFinalAttribites = childMetaClass.hasNotFinalAttributes();
                }
                else
                {
                    IMetaClass childMetaClass = (IMetaClass)metaType;
                    if (childMetaClass.isSearchable())
                    {
                        continue;
                    }
                    hasNotFinalAttribites =  childMetaClass.hasNotFinalAttributes();
                }
            }

            if (hasNotFinalAttribites)
            {
                return true;
            }
        }

        return false;
    }

}
