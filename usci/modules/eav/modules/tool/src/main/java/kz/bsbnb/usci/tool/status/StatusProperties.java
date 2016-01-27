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

        for (String attr: entity.getAttributes()) {
            if(attr.equals("code"))
                return (String) entity.getBaseValue(attr).getValue();
        }
        return null;
    }

    public static String getSpecificParams(BaseEntity entity) {
        String result = "";

        if (entity.getMeta().getClassName().equals("credit")) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            Date contractDate = (Date) entity.getEl("primary_contract.date");
            String sContractDate = (contractDate == null) ? "" : df.format(contractDate);
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
