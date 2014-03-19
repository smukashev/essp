package kz.bsbnb.usci.eav.model.meta;

public interface IMetaContainer extends IMetaType  {

    public void setMetaAttribute(String name, IMetaAttribute metaAttribute);

    public long getId();

    public int getType();

    public void setType(int type);

}
