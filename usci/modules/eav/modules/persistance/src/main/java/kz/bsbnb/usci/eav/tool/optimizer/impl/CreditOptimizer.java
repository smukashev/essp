package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreditOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(CreditOptimizer.class);

    private CreditOptimizer() {
    }

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue primaryContractBaseValue = iBaseEntity.getBaseValue("primary_contract");
        IBaseValue creditorBaseValue = iBaseEntity.getBaseValue("creditor");

        if (primaryContractBaseValue == null || creditorBaseValue == null ||
                primaryContractBaseValue.getValue() == null || creditorBaseValue.getValue() == null){
            logger.error(Errors.getError(String.valueOf(Errors.E184))+" : \n"+iBaseEntity);
            throw new IllegalStateException(String.valueOf(Errors.E184));
        }



        IBaseEntity primaryContractEntity = (IBaseEntity) primaryContractBaseValue.getValue();
        IBaseEntity creditorEntity = (IBaseEntity) creditorBaseValue.getValue();

        if (creditorEntity.getId() == 0){
            logger.error(Errors.getError(String.valueOf(Errors.E185))+" : \n"+iBaseEntity);
            throw new IllegalStateException(String.valueOf(Errors.E185));
        }

        if (primaryContractEntity.getId() == 0)
            return null;

        stringBuilder.append(primaryContractEntity.getId());
        stringBuilder.append("|").append(creditorEntity.getId());

        return stringBuilder.toString();
    }
}
