package kz.bsbnb.usci.eav.model.metadata.type;

public interface IMetaType {

	/**
	 * 
	 * @return <code>true</code>, when attribute is a key attribute 
	 */
	public abstract boolean isKey();

	/**
	 * 
	 * @param isKey <code>true</code>, when attribute is a key attribute
	 */
	public abstract void setKey(boolean isKey);

	/**
	 * 
	 * @return <code>true</code>, when attribute can have null value
	 */
	public abstract boolean isNullable();

	/**
	 * 
	 * @param isNullable <code>true</code>, when attribute can have null value
	 */
	public abstract void setNullable(boolean isNullable);

	public abstract boolean isArray();

	public abstract boolean isComplex();

}