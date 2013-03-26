package kz.bsbnb.usci.eav.model.meta;

public interface IMetaContainer {
    public void setMetaAttribute(String name, IMetaAttribute metaAttribute);
    public long getId();
    public int getType();
}
