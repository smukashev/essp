package kz.bsbnb.usci.eav.model.meta;

public interface IMetaContainer extends IMetaType  {
    void setMetaAttribute(String name, IMetaAttribute metaAttribute);

    long getId();

    int getType();

    void setType(int type);

}
