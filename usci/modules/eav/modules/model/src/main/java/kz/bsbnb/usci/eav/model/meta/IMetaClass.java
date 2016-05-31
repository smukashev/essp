package kz.bsbnb.usci.eav.model.meta;

import java.util.Set;

public interface IMetaClass extends IMetaContainer {
    String getClassName();

    IMetaAttribute getMetaAttribute(String name);

    IMetaType getMemberType(String name);

    boolean isReference();

    boolean isSearchable();

    Set<String> getAttributeNames();
}
