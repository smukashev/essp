package kz.bsbnb.usci.eav.model.metadata.type.impl;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;

public class MetaValueArray extends GenericMetaArray<MetaValue>
{

    public MetaValueArray(DataTypes typeCode)
    {
        super(new MetaValue());
        memberType.setTypeCode(typeCode);
    }

    public MetaValueArray(long id, DataTypes typeCode, boolean isKey, boolean isNullable)
    {
        super(id, new MetaValue(), isKey, isNullable);
        memberType.setTypeCode(typeCode);
    }

	public MetaValueArray(DataTypes typeCode, boolean isKey, boolean isNullable)
    {
		super(new MetaValue(), isKey, isNullable);
		memberType.setTypeCode(typeCode);
	}
	
	public DataTypes getTypeCode()
	{
		return memberType.getTypeCode();
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
			MetaValueArray tmp = (MetaValueArray) obj;
            return !(tmp.getTypeCode() != this.getTypeCode() ||
                    tmp.isKey() != this.isKey() ||
                    tmp.isNullable() != this.isNullable() ||
                    !tmp.arrayKeyType.equals(this.arrayKeyType) ||
                    !tmp.memberType.equals(this.memberType));

        }
	}
}
