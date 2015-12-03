package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;

public final class CreditOptimizer {
    private CreditOptimizer() {
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue primaryContractBaseValue = iBaseEntity.getBaseValue("primary_contract");
        IBaseValue creditorBaseValue = iBaseEntity.getBaseValue("creditor");

        if (primaryContractBaseValue == null || creditorBaseValue == null ||
                primaryContractBaseValue.getValue() == null || creditorBaseValue.getValue() == null)
            throw new IllegalStateException("Документ не содержит обязательные поля; \n" + iBaseEntity);

        IBaseEntity primaryContractEntity = (IBaseEntity) primaryContractBaseValue.getValue();
        IBaseEntity creditorEntity = (IBaseEntity) creditorBaseValue.getValue();

        if (creditorEntity.getId() == 0)
            throw new IllegalStateException("Кредитор не найден в справочнике; \n" + iBaseEntity);

        if (primaryContractEntity.getId() == 0)
            return null;

        stringBuilder.append(primaryContractEntity.getId());
        stringBuilder.append("|").append(creditorEntity.getId());

        return stringBuilder.toString();
    }
}
