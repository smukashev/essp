package kz.bsbnb.usci.batch.helper.impl;

import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.helper.IHelper;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@Component
public class ParserHelper implements IHelper
{
    public Object getCastObject(DataTypes typeCode, String value)
    {
        switch(typeCode)
        {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Global.DATE_FORMAT);
                Date date = null;

                try
                {
                    date = simpleDateFormat.parse(value);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                return date;
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
