package kz.bsbnb.usci.eav_model.model.meta;

import java.io.Serializable;

public interface IMetaType extends Serializable
{
	public boolean isSet();

	public boolean isComplex();

    public boolean isSetOfSets();

}