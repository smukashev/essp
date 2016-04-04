package kz.bsbnb.usci.eav.tool.struct;

import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.Table;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DATE_VALUES;

public class StructType {
    public static Table getSimpleTableName(DataTypes dataTypes) {
        Table table;

        switch(dataTypes) {
            case BOOLEAN:
                table =  EAV_BE_BOOLEAN_VALUES;
                break;
            case STRING:
                table =  EAV_BE_STRING_VALUES;
                break;
            case INTEGER:
                table =  EAV_BE_INTEGER_VALUES;
                break;
            case DOUBLE:
                table =  EAV_BE_DOUBLE_VALUES;
                break;
            case DATE:
                table =  EAV_BE_DATE_VALUES;
                break;
            default:
                throw new java.lang.IllegalStateException(Errors.getMessage(Errors.E190, dataTypes));

        }

        return table;
    }

    public static Object getSimpleValue(DataTypes dataTypes, Object value) {
        Object obj;

        switch(dataTypes) {
            case BOOLEAN:
                obj = DataUtils.convert((Boolean) value);
                break;
            case STRING:
                obj = value;
                break;
            case INTEGER:
                obj = value;
                break;
            case DOUBLE:
                obj = value;
                break;
            case DATE:
                obj =  DataUtils.convert((java.util.Date) value);
                break;
            default:
                throw new java.lang.IllegalStateException(Errors.getMessage(Errors.E190, dataTypes));

        }

        return obj;
    }
}
