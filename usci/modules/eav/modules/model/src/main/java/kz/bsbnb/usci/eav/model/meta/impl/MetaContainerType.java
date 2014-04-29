package kz.bsbnb.usci.eav.model.meta.impl;

/**
 * @author alexandr.motov
 */
public enum MetaContainerType {

    META_CLASS(MetaContainerTypes.META_CLASS),
    META_SET(MetaContainerTypes.META_SET);

    private int value;

    MetaContainerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
