package kz.bsbnb.usci.eav_model.model.meta;

import java.io.Serializable;

public interface IMetaType extends Serializable
{
	public boolean isArray();

	public boolean isComplex();
}