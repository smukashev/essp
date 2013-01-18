package kz.bsbnb.usci.eav.util;

import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.AbstractMetaArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.GenericMetaArray;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValue;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaValueArray;

public class MetaTypeHelper {
	public static DataTypes getDataType(IMetaType metaType)
	{
		if(metaType.isComplex())
		{
			return null;
		}
		else
		{
			if(metaType.isArray())
			{
				return ((MetaValueArray)metaType).getTypeCode();
			}
			else
			{
				return ((MetaValue)metaType).getTypeCode();
			}
		}
	}
	
	public static ComplexKeyTypes getArrayKeyType(IMetaType metaType)
	{
		if(metaType.isArray())
		{
			return ((AbstractMetaArray)metaType).getArrayKeyType();
		}
		else
		{
			return null;
		}
	}
	
	public static ComplexKeyTypes getClassKeyType(IMetaType metaType)
	{
		if(metaType.isComplex())
		{
			return ((MetaClass)metaType).getComplexKeyType();
		}
		else
		{
			return null;
		}
	}
}
