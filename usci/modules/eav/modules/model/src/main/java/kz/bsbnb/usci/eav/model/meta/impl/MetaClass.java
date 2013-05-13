package kz.bsbnb.usci.eav.model.meta.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kz.bsbnb.usci.eav.model.base.ContainerTypes;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaContainer;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.model.type.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;

public class MetaClass extends Persistable implements IMetaType, IMetaContainer
{
	/**
	 * Name of the meta. Used as a key value for database search if <code>id</code> is 0
	 */
    private String className;

    private Timestamp beginDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());

    private boolean disabled = false;

    private boolean searchable = false;

    private boolean immutable = false;

    private boolean reference = false;
	
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
        super();
    }

    public MetaClass(MetaClass meta)
    {
        this.className = meta.className;
        this.id = meta.id;
        this.disabled = meta.disabled;
        this.beginDate = meta.beginDate;
        this.complexKeyType = meta.complexKeyType;

        members.putAll(meta.members);
    }

	public MetaClass(String className)
    {
		this.className = className;
	}

    public MetaClass(String className, Timestamp beginDate)
    {
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
	}

    @Override
    public int getType()
    {
        return ContainerTypes.CLASS;
    }

    public String getSearchProcedureName()
    {
		return searchProcedureName;
	}

	public void setSearchProcedureName(String searchProcedureName)
    {
		this.searchProcedureName = searchProcedureName;
	}

    public Timestamp getBeginDate()
    {
        return beginDate;
    }

    public void setBeginDate(Timestamp beginDate)
    {
        this.beginDate = beginDate;
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
        return searchable;
    }

    public String toString(String prefix)
    {
        String str = "metaClass;";

        for (String memberName : members.keySet())
        {
            IMetaAttribute attribute = members.get(memberName);
            IMetaType type = attribute.getMetaType();

            str += "\n" + prefix + memberName + ": " + type.toString(prefix + "\t");
        }

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
    public boolean isImmutable()
    {
        return immutable;
    }

    @Override
    public boolean isReference()
    {
        return reference;
    }

    @Override
    public void setImmutable(boolean value)
    {
        immutable = value;
    }

    @Override
    public void setReference(boolean value)
    {
        reference = value;
    }
}
