package kz.bsbnb.usci.eav.model.metadata;

import java.util.HashMap;
import java.util.Set;

/**
 * Holds EAV entity description: attribute names, types.
 * 
 * @author a.tkachenko
 * @version 1.0, 17.01.2013
 * @see Type
 */
public class MetaData {
	/**
	 * id fields value of the persisted object
	 */
	long id = 0;
	
	/**
	 * Name of the metadata. Used as a key value for database search if <code>id</code> is 0 
	 */
	String className;
	
	/**
	 * Holds type values. Keys of hash are type names.
	 */
	HashMap<String, Type> storage = new HashMap<String, Type>();

	/**
	 * Used to get id field of persisted entity. Used only on objects retrieved from 
	 * corresponding DAO
	 * 
	 * @return persisted entity identificator 
	 */
	public long getId() {
		return id;
	}

	/**
	 * Used to set connection with persisted object. Do not use this method outside DAO object.
	 * 
	 * @param id database identificator field value
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * <code>className</code> could be used by DAO to retrieve persisted object.
	 * 
	 * @param className name of the entity class
	 */
	public MetaData(String className) {
		this.className = className;
	}

	/**
	 * Used to retrieve all attribute names 
	 * 
	 * @return list of attribute names
	 */
	public Set<String> getTypeNames() {
		return storage.keySet();
	}

	/**
	 * Used to get type of the attribute with name <code>name</code>
	 * 
	 * @param name name of the attribute
	 * @return type of that attribute
	 * @see Type
	 */
	public Type getType(String name) {
		return storage.get(name);
	}

	/**
	 * Used to set attribute type. If there is no such attribute, then creates one.
	 * 
	 * @param name attributes name
	 * @param type type to be set
	 * @see Type
	 */
	public void setType(String name, Type type) {
		storage.put(name, type);
	}

	/**
	 * Used to get entity class name
	 * 
	 * @return entity class name
	 */
	public String getClassName() {
		return className;
	}

	//TODO: performance optimization required
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj == null)
			return false;

		if (!(getClass() == obj.getClass()))
			return false;
		else {
			MetaData tmp = (MetaData) obj;
			if (tmp.getClassName() != this.getClassName())
				return false;
			
			if(this.getTypeNames().size() != tmp.getTypeNames().size())
				return false;
			
			for(String typeName : this.getTypeNames())
			{
				if(tmp.getType(typeName) == null)
					return false;
				if(!tmp.getType(typeName).equals(this.getType(typeName)))
					return false;
			}
			
			return true;
		}
	}
}
