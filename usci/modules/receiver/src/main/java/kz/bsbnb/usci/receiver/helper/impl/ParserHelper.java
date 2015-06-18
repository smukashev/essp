package kz.bsbnb.usci.receiver.helper.impl;

import kz.bsbnb.usci.receiver.common.Global;
import kz.bsbnb.usci.receiver.helper.IHelper;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
@Component
public class ParserHelper implements IHelper {
    public Object getCastObject(DataTypes typeCode, String value) {
        switch(typeCode) {
            case INTEGER:
                return Integer.parseInt(value);
            case DATE:
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Global.DATE_FORMAT);
                SimpleDateFormat simpleDateFormatDot = new SimpleDateFormat(Global.DATE_FORMAT_DOT);

                Date date = null;

                try {
                    date = simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    try {
                        date = simpleDateFormatDot.parse(value);
                    } catch (ParseException ex) {
                        e.printStackTrace();
                    }
                }

                return date;
            case STRING:
                return value;
            case BOOLEAN:
                try {
                    int i = Integer.parseInt(value);
                    return i == 1;
                } catch (Exception e) {
                    return Boolean.parseBoolean(value);
                }
            case DOUBLE:
                return Double.parseDouble(value);
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }
}
