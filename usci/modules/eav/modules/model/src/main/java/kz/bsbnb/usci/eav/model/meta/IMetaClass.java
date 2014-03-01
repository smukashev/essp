package kz.bsbnb.usci.eav.model.meta;

/**
 *
 */
public interface IMetaClass extends IMetaContainer {

    public String getClassName();

    public IMetaType getMemberType(String name);

    public boolean isReference();

    public boolean isSearchable();

}
