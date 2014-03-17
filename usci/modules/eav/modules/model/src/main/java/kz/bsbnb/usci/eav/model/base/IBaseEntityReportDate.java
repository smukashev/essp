package kz.bsbnb.usci.eav.model.base;

import kz.bsbnb.usci.eav.model.persistable.IBaseObject;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;

import java.util.Date;

/**
 * Created by Alexandr.Motov on 16.03.14.
 */
public interface IBaseEntityReportDate extends IBaseObject {

    public IBaseEntity getBaseEntity();

    public void setBaseEntity(IBaseEntity baseEntity);

    public Date getReportDate();

    public void setReportDate(Date reportDate);

    public long getIntegerValuesCount();

    public void setIntegerValuesCount(long integerValuesCount);

    public long getDateValuesCount();

    public void setDateValuesCount(long dateValuesCount);

    public long getStringValuesCount();

    public void setStringValuesCount(long stringValuesCount);

    public long getBooleanValuesCount();

    public void setBooleanValuesCount(long booleanValuesCount);

    public long getDoubleValuesCount();

    public void setDoubleValuesCount(long doubleValuesCount);

    public long getComplexValuesCount();

    public void setComplexValuesCount(long complexValuesCount);

    public long getSimpleSetsCount();

    public void setSimpleSetsCount(long simpleSetsCount);

    public long getComplexSetsCount();

    public void setComplexSetsCount(long complexSetsCount);



}
