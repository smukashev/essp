package kz.bsbnb.usci.eav.model.base.impl;

public enum OperationType {
    NEW(OperationTypes.NEW),
    DELETE(OperationTypes.DELETE),
    CLOSE(OperationTypes.CLOSE),
    INSERT(OperationTypes.INSERT),
    UPDATE(OperationTypes.UPDATE);

    private int value;

    OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
