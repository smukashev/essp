package kz.bsbnb;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataDateValue extends DataValue<Date> {

    public DataDateValue(Date value) {
        super(value);
    }

    public DataDateValue(Object value) {
        this.value = ((Date) value);
    }
}
