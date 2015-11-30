package kz.bsbnb.usci.tool.status;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusProperties {
    private static String getEntityNameAttrValue(BaseEntity entity) {
        for (String attr : entity.getAttributes()) {
            if (attr.startsWith("name")) {
                return (String) entity.getBaseValue(attr).getValue();
            }
        }
        return null;
    }

    public static String getSpecificParams(BaseEntity entity) {
        String result = "";

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        if (entity.getMeta().getClassName().equals("credit")) {
            Date contractDate = (Date) entity.getEl("primary_contract.date");
            String sContractDate = df.format(contractDate);
            String contractNo = (String) entity.getEl("primary_contract.no");

            result = contractNo + " | " + sContractDate;
        } else if (entity.getMeta().getClassName().equals("portfolio_data")) {
            result = "Портфель " + entity.getId();
        } else if (entity.getMeta().isReference()) {
            result = getEntityNameAttrValue(entity);
        }

        return result;
    }

}