package kz.bsbnb.usci.tool.jooq;

import org.jooq.Converter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 */
public class OracleLongConverter implements Converter<BigInteger, Long> {

    @Override
    public Long from(BigInteger value) {
        return value == null ? null : value.longValue();
    }

    @Override
    public BigInteger to(Long value) {
        return value == null ? null : BigInteger.valueOf(value);
    }

    @Override
    public Class<BigInteger> fromType() {
        return BigInteger.class;
    }

    @Override
    public Class<Long> toType() {
        return Long.class;
    }

}
