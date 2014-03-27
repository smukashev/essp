package kz.bsbnb.usci.eav.model.meta;

import java.util.Set;

/**
 *
 */
public interface IMetaClass extends IMetaContainer {

    public String getClassName();

    public IMetaAttribute getMetaAttribute(String name);

    public IMetaType getMemberType(String name);

    public boolean isReference();

    public boolean isSearchable();

    public Set<String> getAttributeNames();

}
