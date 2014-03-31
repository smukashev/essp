package kz.bsbnb.usci.eav.model.base.impl;

/**
 * Created by Alexandr.Motov on 20.03.14.
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
