package kz.bsbnb.usci.eav.model.metadata.type.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;

public class MetaClass extends AbstractMetaType {
	/**
	 * Name of the metadata. Used as a key value for database search if <code>id</code> is 0 
	 */
    private String className;

    private Timestamp beginDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());

    private boolean isDisabled = false;
	
	/**
	 * Holds type values. Keys of hash are type names.
	 */
    private HashMap<String, IMetaType> members = new HashMap<String, IMetaType>();
	
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

    public MetaClass() {
    }

    public MetaClass(MetaClass meta) {
        this.className = meta.className;
        this.id = meta.id;
        this.isDisabled = meta.isDisabled;
        this.beginDate = meta.beginDate;

        members.putAll(meta.members);
    }

	public MetaClass(String className) {
		this.className = className;
	}

    public MetaClass(String className, Timestamp beginDate) {
        this.className = className;
        this.beginDate = beginDate;
    }

    public MetaClass(boolean isKey, boolean isNullable) {
        super(isKey, isNullable);
        this.className = className;
    }

	public MetaClass(String className, boolean isKey, boolean isNullable) {
		super(isKey, isNullable);
		this.className = className;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	public ComplexKeyTypes getComplexKeyType() {
		return complexKeyType;
	}

	public void setComplexKeyType(ComplexKeyTypes complexKeyType) {
		this.complexKeyType = complexKeyType;
	}
	
	/**
	 * Used to get entity class name
	 * 
	 * @return entity class name
	 */
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Used to retrieve all attribute names 
	 * 
	 * @return list of attribute names
	 */
	public Set<String> getMemberNames() {
		return members.keySet();
	}
	
	/**
	 * Used to get type of the attribute with name <code>name</code>
	 * 
	 * @param name name of the attribute
	 * @return type of that attribute
	 * @see MetaValue
	 */
	public IMetaType getMemberType(String name) {
		return members.get(name);
	}

	public void removeMemberType(String name) {
		members.remove(name);
	}

	/**
	 * Used to set attribute type. If there is no such attribute, then creates one.
	 * 
	 * @param name attributes name
	 * @param type type to be set
	 * @see MetaValue
	 */
	public void setMemberType(String name, IMetaType type) {
		members.put(name, type);
	}

	public String getSearchProcedureName() {
		return searchProcedureName;
	}

	public void setSearchProcedureName(String searchProcedureName) {
		this.searchProcedureName = searchProcedureName;
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

            Set<String> thisNames = this.members.keySet();

            for (String name : thisNames)
            {
                if(!(this.getMemberType(name).equals(tmp.getMemberType(name))))
                {
                    return false;
                }
            }

            return !(tmp.isDisabled() != this.isDisabled() ||
                    !tmp.getBeginDate().equals(this.getBeginDate()) ||
                    tmp.isKey() != this.isKey() ||
                    tmp.isNullable() != this.isNullable() ||
                    !tmp.getClassName().equals(this.getClassName()) ||
                    !tmp.complexKeyType.equals(this.complexKeyType));

        }
	}

    public Timestamp getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Timestamp beginDate) {
        this.beginDate = beginDate;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public void removeMembers()
    {
        members.clear();
    }

    /**
     * Used to get list of the attribute names with type <code>dataType</code>
     *
     * @param dataType type of the attribute
     * @return list of the attribute names
     * @see DataTypes
     * @see MetaValue
     */
    public Set<String> getSimpleAttributesNames(DataTypes dataType) {
        if (!this.isComplex()) {
            throw new UnsupportedOperationException("This method can only be done for complex types.");
        }

        Set<String> allAttributeNames = this.members.keySet();
        Set<String> filteredAttributeNames = new HashSet<String>();

        Iterator it = allAttributeNames.iterator();
        while (it.hasNext()) {
            String attributeName = (String)it.next();
            IMetaType type = this.getMemberType(attributeName);
            if (!type.isArray() && !type.isComplex()) {
                MetaValue metaValue = (MetaValue)type;
                if (metaValue.getTypeCode().equals(dataType)) {
                    filteredAttributeNames.add(attributeName);
                }
            }
        }
        return filteredAttributeNames;
    }

    /**
     * Used to get list of the attribute names with type <code>DataTypes.BOOLEAN</code>.
     *
     * @return list of the attribute names
     * @see MetaValue
     */
    public Set<String> getBooleanAttributeNames() {
        return getSimpleAttributesNames(DataTypes.BOOLEAN);
    }

    /**
     * Used to get list of the attribute names with type <code>DataTypes.DATE</code>.
     *
     * @return list of the attribute names
     * @see MetaValue
     */
    public Set<String> getDateAttributeNames() {
        return getSimpleAttributesNames(DataTypes.DATE);
    }

    /**
     * Used to get list of the attribute names with type <code>DataTypes.DOUBLE</code>.
     *
     * @return list of the attribute names
     * @see MetaValue
     */
    public Set<String> getDoubleAttributeNames() {
        return getSimpleAttributesNames(DataTypes.DOUBLE);
    }

    /**
     * Used to get list of the attribute names with type <code>DataTypes.DOUBLE</code>.
     *
     * @return list of the attribute names
     * @see MetaValue
     */
    public Set<String> getIntegerAttributeNames() {
        return getSimpleAttributesNames(DataTypes.INTEGER);
    }

    /**
     * Used to get list of the attribute names with type <code>DataTypes.STRING</code>.
     *
     * @return list of the attribute names
     * @see MetaValue
     */
    public Set<String> getStringAttributeNames() {
        return getSimpleAttributesNames(DataTypes.STRING);
    }

}
