package kz.bsbnb.usci.tool.jooq;

import org.jooq.Converter;

import java.math.BigInteger;

/**
 *
 */
public class OracleBooleanConverter implements Converter<Byte, Boolean> {

    @Override
    public Boolean from(Byte value) {
        return value == null ? null : value.equals(Byte.valueOf("1")) ? true : false;
    }

    @Override
    public Byte to(Boolean value) {
        return value == null ? null : value ? Byte.valueOf("1") : 0;
    }

    @Override
    public Class<Byte> fromType() {
        return Byte.class;
    }

    @Override
    public Class<Boolean> toType() {
        return Boolean.class;
    }
}
