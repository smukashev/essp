package kz.bsbnb.usci.eav.model.meta;

import java.io.Serializable;

public interface IMetaType extends Serializable
{
	public boolean isSet();

	public boolean isComplex();

    public boolean isSetOfSets();

    public String toString(String prefix);

    public boolean isImmutable();

    public boolean isReference();

    public void setImmutable(boolean value);

    public void setReference(boolean value);
}