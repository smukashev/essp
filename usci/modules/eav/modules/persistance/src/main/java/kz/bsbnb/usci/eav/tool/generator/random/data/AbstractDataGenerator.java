package kz.bsbnb.usci.eav.tool.generator.random.data;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author k.tulbassiyev
 */
public abstract class AbstractDataGenerator {
    protected Random rand = new Random(10000);

    protected final String date_format = "yyyy-MM-dd";
    protected final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(date_format);

    protected Object getCastObject(DataTypes typeCode) {
        switch(typeCode) {
            case INTEGER:
                return rand.nextInt(1000);
            case DATE:
                long offset = Timestamp.valueOf("2000-01-01 00:00:00").getTime();
                long end = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
                long diff = end - offset + 1;

                Timestamp timestamp = new Timestamp(offset + (long)(Math.random() * diff));

                Date date = null;

                try {
                    date = simpleDateFormat.parse(simpleDateFormat
                            .format(new Date(timestamp.getTime())));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                return date;
            case STRING:
                return "string_" + rand.nextInt(1000);
            case BOOLEAN:
                return rand.nextBoolean();
            case DOUBLE:
                return rand.nextDouble()*10000;
            default:
                throw new IllegalArgumentException(Errors.E49+"");
        }
    }
}
