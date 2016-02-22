package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PrimaryContractOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(PrimaryContractOptimizer.class);

    private PrimaryContractOptimizer() {
    }

    public static String getKeyString(IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue noBaseValue = iBaseEntity.getBaseValue("no");
        IBaseValue dateBaseValue = iBaseEntity.getBaseValue("date");

        if (noBaseValue == null || dateBaseValue == null ||
                noBaseValue.getValue() == null || dateBaseValue.getValue() == null){
            logger.error(Errors.getError(String.valueOf(Errors.E187))+" : \n"+iBaseEntity);
            throw new IllegalStateException(String.valueOf(Errors.E187));
        }

        stringBuilder.append(noBaseValue.getValue());

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        stringBuilder.append("|").append(df.format((Date) dateBaseValue.getValue()));

        return stringBuilder.toString();
    }
}
