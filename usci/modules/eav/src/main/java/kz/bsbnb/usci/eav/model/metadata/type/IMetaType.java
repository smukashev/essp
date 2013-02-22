package kz.bsbnb.usci.eav.model.metadata.type;

import java.io.Serializable;

public interface IMetaType extends Serializable
{
	public boolean isArray();

	public boolean isComplex();
}