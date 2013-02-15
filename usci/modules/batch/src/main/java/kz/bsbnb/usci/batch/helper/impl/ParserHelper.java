package kz.bsbnb.usci.batch.helper.impl;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.helper.IHelper;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
@Component
public class ParserHelper implements IHelper
{
    public Object getCastObject(DataTypes typeCode, String value) throws ParseException
    {
        switch(typeCode)
        {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                return new SimpleDateFormat(Global.DATE_FORMAT).parse(value);
            case STRING:
                return value;
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }
}
