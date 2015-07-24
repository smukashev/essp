package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.persistable.IBaseObject;

import java.util.Date;

public interface IBaseEntityReportDate extends IBaseObject {
    IBaseEntity getBaseEntity();

    void setBaseEntity(IBaseEntity baseEntity);

    Date getReportDate();

    void setReportDate(Date reportDate);

    long getIntegerValuesCount();

    void setIntegerValuesCount(long integerValuesCount);

    long getDateValuesCount();

    void setDateValuesCount(long dateValuesCount);

    long getStringValuesCount();

    void setStringValuesCount(long stringValuesCount);

    long getBooleanValuesCount();

    void setBooleanValuesCount(long booleanValuesCount);

    long getDoubleValuesCount();

    void setDoubleValuesCount(long doubleValuesCount);

    long getComplexValuesCount();

    void setComplexValuesCount(long complexValuesCount);

    long getSimpleSetsCount();

    void setSimpleSetsCount(long simpleSetsCount);

    long getComplexSetsCount();

    void setComplexSetsCount(long complexSetsCount);

    boolean isClosed();

    void setClosed(boolean is_closed);
}
