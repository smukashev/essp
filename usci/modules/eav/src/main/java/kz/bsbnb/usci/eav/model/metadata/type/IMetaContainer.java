package kz.bsbnb.usci.eav.model.metadata.type;

public interface IMetaContainer {
    public void setMetaAttribute(String name, IMetaAttribute metaAttribute);
    public long getId();
}
