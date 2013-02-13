package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.persistance.Persistable;

public abstract class AbstractMetaArray extends Persistable implements IMetaType
{
	/**
     * When attribute is an array, and is a key attribute - sets key usage strategy.
     * Defaults to <code>ArrayKeyTypes.ALL</code>
     * @see ComplexKeyTypes
     */
    ComplexKeyTypes arrayKeyType = ComplexKeyTypes.ALL;
    
    public ComplexKeyTypes getArrayKeyType()
    {
		return arrayKeyType;
	}

	public void setArrayKeyType(ComplexKeyTypes arrayKeyType)
    {
		this.arrayKeyType = arrayKeyType;
	}
	
	public boolean isArray()
    {
		return true;
	}
}
