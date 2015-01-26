package kz.bsbnb.usci.eav.model.base.impl;

/**
 * Created by Bauyrzhan.Makhambeto on 26.01.2015.
 */
public enum OperationType {

    DELETE(OperationTypes.DELETE);

    private int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
