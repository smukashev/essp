package kz.bsbnb.usci.eav.model.metadata;

import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author a.tkachenko
 */
public class MetaData {
	long id = 0;
	String className;
	HashMap<String, Type> storage = new HashMap<String, Type>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MetaData(String className) {
		this.className = className;
	}

	public Set<String> getTypeNames() {
		return storage.keySet();
	}

	public Type getType(String name) {
		return storage.get(name);
	}

	public void setType(String name, Type type) {
		storage.put(name, type);
	}

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
