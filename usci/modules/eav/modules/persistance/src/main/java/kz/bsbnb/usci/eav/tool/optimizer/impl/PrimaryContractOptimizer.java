package kz.bsbnb.usci.eav.tool.optimizer.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.util.DataUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PrimaryContractOptimizer {
    private PrimaryContractOptimizer() {
    }

    public static String getKeyString(IBaseEntity iBaseEntity) {
        StringBuilder stringBuilder = new StringBuilder();

        IBaseValue noBaseValue = iBaseEntity.getBaseValue("no");
        IBaseValue dateBaseValue = iBaseEntity.getBaseValue("date");

        if (noBaseValue == null || dateBaseValue == null ||
                noBaseValue.getValue() == null || dateBaseValue.getValue() == null)
            throw new IllegalStateException(Errors.E187+"|" + iBaseEntity);

        stringBuilder.append(noBaseValue.getValue());

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        stringBuilder.append("|").append(df.format((Date) dateBaseValue.getValue()));

        return stringBuilder.toString();
    }
}
