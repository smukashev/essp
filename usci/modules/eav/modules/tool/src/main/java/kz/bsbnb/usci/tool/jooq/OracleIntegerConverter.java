package kz.bsbnb.usci.tool.jooq;

import org.jooq.Converter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 */
public class OracleIntegerConverter implements Converter<Long, Integer> {

    @Override
    public Integer from(Long value) {
        return value == null ? null : value.intValue();
    }

    @Override
    public Long to(Integer value) {
        return value == null ? null : value.longValue();
    }

    @Override
    public Class<Long> fromType() {
        return Long.class;
    }

    @Override
    public Class<Integer> toType() {
        return Integer.class;
    }

}
