package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;

public class MetaValueArray extends GenericMetaArray<MetaValue> {
	
	public MetaValueArray(DataTypes typeCode, boolean isKey, boolean isNullable) {
		super(isKey, isNullable);
		memberType.setTypeCode(typeCode);
	}
}
