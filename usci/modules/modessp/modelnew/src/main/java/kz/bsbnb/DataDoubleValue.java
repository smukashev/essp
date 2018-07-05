package kz.bsbnb;

import java.math.BigDecimal;

public class DataDoubleValue extends DataValue<Double> {
    public DataDoubleValue(Double value) {
        super(value);
    }

    public DataDoubleValue(Object value) {
        if(value instanceof BigDecimal)
            this.value = ((BigDecimal) value).doubleValue();
        else if(value instanceof Double) {
            this.value = ((Double) value);
        } else throw new RuntimeException("cannot cast double");
    }
}
