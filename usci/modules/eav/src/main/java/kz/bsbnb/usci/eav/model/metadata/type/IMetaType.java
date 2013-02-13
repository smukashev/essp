package kz.bsbnb.usci.eav.model.metadata.type;

public interface IMetaType
{
	/**
	 * 
	 * @return <code>true</code>, when attribute is a key attribute 
	 */
	public boolean isKey();

	/**
	 * 
	 * @param isKey <code>true</code>, when attribute is a key attribute
	 */
	public void setKey(boolean isKey);

	/**
	 * 
	 * @return <code>true</code>, when attribute can have null value
	 */
	public boolean isNullable();

	/**
	 * 
	 * @param isNullable <code>true</code>, when attribute can have null value
	 */
	public void setNullable(boolean isNullable);

	public boolean isArray();

	public boolean isComplex();

}