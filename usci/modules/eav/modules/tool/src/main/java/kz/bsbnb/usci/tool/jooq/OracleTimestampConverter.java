package kz.bsbnb.usci.tool.jooq;

import org.jooq.Converter;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 */
public class OracleTimestampConverter implements Converter<Date, Timestamp> {

    @Override
    public Timestamp from(Date value) {
        return value == null ? null : new Timestamp(value.getTime());
    }

    @Override
    public Date to(Timestamp value) {
        return value == null ? null : new Date(value.getTime());
    }

    @Override
    public Class<Date> fromType() {
        return Date.class;
    }

    @Override
    public Class<Timestamp> toType() {
        return Timestamp.class;
    }

}
