package kz.bsbnb.usci.tool.jooq;

import org.jooq.Converter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 */
public class OracleDoubleConverter implements Converter<BigDecimal, Double> {

    @Override
    public Double from(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    @Override
    public BigDecimal to(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    @Override
    public Class<BigDecimal> fromType() {
        return BigDecimal.class;
    }

    @Override
    public Class<Double> toType() {
        return Double.class;
    }

}
