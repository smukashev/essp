package kz.bsbnb.usci.tool.couchbase.singleton;

import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by a.tkachenko on 4/30/14.
 */
public class StatusProperties
{
    public static final String CONTRACT_NO = "ContractNo";
    public static final String CONTRACT_DATE = "ContractDate";

    public static final String REF_NAME = "REF_NAME";

    public static void fillSpecificProperties(EntityStatusJModel entityStatus, BaseEntity entity) {
        if (entity.getMeta().getClassName().equals("credit")) {
            Date contractDate = (Date)entity.getEl("primary_contract.date");
            String contractNo = (String)entity.getEl("primary_contract.no");

            entityStatus.addProperty(CONTRACT_DATE, contractDate);
            entityStatus.addProperty(CONTRACT_NO, contractNo);
        } else if (entity.getMeta().getClassName().equals("portfolio_data")) {
            // TODO decide what to fill about portfolio_data
        } else if (entity.getMeta().isReference()) {
            String refName = getEntityNameAttrValue(entity);
            entityStatus.addProperty(REF_NAME, refName);
        }
    }

    private static String getEntityNameAttrValue(BaseEntity entity) {
        for (String attr : entity.getAttributes()) {
            if (attr.startsWith("name")) {
                return (String) entity.getBaseValue(attr).getValue();
            }
        }
        return null;
    }

    public static Map<String, String> getSpecificParams(BaseEntity entity) {
        Map<String, String> params = new HashMap<>();

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        if (entity.getMeta().getClassName().equals("credit")) {
            Date contractDate = (Date)entity.getEl("primary_contract.date");
            String sContractDate = df.format(contractDate);
            String contractNo = (String)entity.getEl("primary_contract.no");

            params.put(CONTRACT_DATE, sContractDate);
            params.put(CONTRACT_NO, contractNo);

        } else if (entity.getMeta().getClassName().equals("portfolio_data")) {
            // TODO decide what to fill about portfolio_data

        } else if (entity.getMeta().isReference()) {
            String refName = getEntityNameAttrValue(entity);
            params.put(REF_NAME, refName);
        }

        return params;
    }

}
