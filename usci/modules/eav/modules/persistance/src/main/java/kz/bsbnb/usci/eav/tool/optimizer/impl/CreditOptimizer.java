package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreditOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(CreditOptimizer.class);

    public static String getKeyString(final IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue primaryContractBaseValue = iBaseEntity.getBaseValue("primary_contract");
        IBaseValue creditorBaseValue = iBaseEntity.getBaseValue("creditor");

        if (primaryContractBaseValue == null || creditorBaseValue == null ||
                primaryContractBaseValue.getValue() == null || creditorBaseValue.getValue() == null) {
            logger.error(Errors.getError(Errors.E184) + " : \n" + iBaseEntity);
            throw new IllegalStateException(Errors.compose(Errors.E184));
        }

        IBaseEntity primaryContractEntity = (IBaseEntity) primaryContractBaseValue.getValue();
        IBaseEntity creditorEntity = (IBaseEntity) creditorBaseValue.getValue();

        if (creditorEntity.getId() == 0) {
            logger.error(Errors.getError(Errors.E185) + " : \n" + iBaseEntity);
            throw new IllegalStateException(Errors.compose(Errors.E185));
        }

        if (primaryContractEntity.getId() == 0)
            return null;

        stringBuilder.append(primaryContractEntity.getId());
        stringBuilder.append(Errors.SEPARATOR).append(creditorEntity.getId());

        return stringBuilder.toString();
    }
}
