package kz.bsbnb.usci.eav.model.metadata.type.impl;

import java.util.ArrayList;
import java.util.HashMap;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;

public class MetaClassArray extends GenericMetaArray<MetaClassHolder> {

	/**
     * When attribute is an array, and is a key attribute, and has type DataTypes.COMPLEX - 
     * sets array elements filter.
     * First parameter is array elements attribute name (must be a key attribute).
     * Second parameter is a list of it's values string representation.
     * 
     * Example:
     * A document array as a key value for person.
     * <code>arrayKeyType</code> is set to <code>ArrayKeyTypes.ALL</code>.
     * And <code>arrayKeyFilter</code> has a value (pseudo code) <code>["docType" => ["RNN", "UDL"]]</code>
     * Then we will get persons with the same RNN and UDL documents.
     * Other documents won't be used by DAO.
     * 
     * @see DataTypes
     */
    private HashMap<String, ArrayList<String>> arrayKeyFilter = new HashMap<String, ArrayList<String>>();

    public MetaClassArray(MetaClassHolder memberClass) {
        super(memberClass);
    }

    public MetaClassArray(long id, MetaClassHolder memberClass) {
        super(id, memberClass);
    }

	public MetaClassArray(String className, boolean isKey, boolean isNullable) {
		super(new MetaClassHolder(new MetaClass(className), isKey, isNullable));
	}

	public void addArrayKeyFilterValues(String attributeName, ArrayList<String> values)
    {
    	arrayKeyFilter.put(attributeName, values);
    }

    /*public ComplexKeyTypes getComplexKeyType()
    {
        return memberType.getMeta().getComplexKeyType();
    }

    public void setComplexKeyType(ComplexKeyTypes type)
    {
        memberType.getMeta().setComplexKeyType(type);
    }*/
	
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
			MetaClassArray tmp = (MetaClassArray) obj;
            return !(tmp.isKey() != this.isKey() ||
                    tmp.isNullable() != this.isNullable() ||
                    !tmp.arrayKeyType.equals(this.arrayKeyType) ||
                    !tmp.memberType.equals(this.memberType) ||
                    !tmp.arrayKeyFilter.equals(this.arrayKeyFilter));

        }
	}
}
