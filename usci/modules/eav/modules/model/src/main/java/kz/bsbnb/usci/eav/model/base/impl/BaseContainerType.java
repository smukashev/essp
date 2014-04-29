package kz.bsbnb.usci.eav.model.base.impl;

/**
 * @author alexandr.motov
 */
public enum BaseContainerType {

    BASE_ENTITY(BaseContainerTypes.BASE_ENTITY),
    BASE_SET(BaseContainerTypes.BASE_SET);

    private int value;

    BaseContainerType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
