package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.apache.commons.lang.NotImplementedException;

import java.util.Date;

public class BaseEntityReportDate extends Persistable implements IBaseEntityReportDate {

    private IBaseEntity baseEntity;
    private Date reportDate;
    private boolean is_closed;

    private long integerValuesCount = 0;
    private long dateValuesCount = 0;
    private long stringValuesCount = 0;
    private long booleanValuesCount = 0;
    private long doubleValuesCount = 0;
    private long complexValuesCount = 0;
    private long simpleSetsCount = 0;
    private long complexSetsCount = 0;

    public BaseEntityReportDate(long id, Date reportDate, long integerValuesCount, long dateValuesCount,
                                long stringValuesCount, long booleanValuesCount, long doubleValuesCount,
                                long complexValuesCount, long simpleSetsCount, long complexSetsCount) {
        super(id);

        if (reportDate == null) {
            throw new IllegalArgumentException("Отчетная не можеть быть NULL;");
        } else {
            Date newReportDate = (Date) reportDate.clone();
            DataUtils.toBeginningOfTheDay(newReportDate);
            this.reportDate = newReportDate;
        }

        this.integerValuesCount = integerValuesCount;
        this.dateValuesCount = dateValuesCount;
        this.stringValuesCount = stringValuesCount;
        this.booleanValuesCount = booleanValuesCount;
        this.doubleValuesCount = doubleValuesCount;
        this.complexValuesCount = complexValuesCount;
        this.simpleSetsCount = simpleSetsCount;
        this.complexSetsCount = complexSetsCount;
    }

    public BaseEntityReportDate(long id, Date reportDate, long integerValuesCount, long dateValuesCount,
                                long stringValuesCount, long booleanValuesCount, long doubleValuesCount,
                                long complexValuesCount, long simpleSetsCount, long complexSetsCount,
                                boolean is_closed) {
        super(id);

        if (reportDate == null) {
            throw new IllegalArgumentException("Отчетная не можеть быть NULL;");
        } else {
            Date newReportDate = (Date) reportDate.clone();
            DataUtils.toBeginningOfTheDay(newReportDate);
            this.reportDate = newReportDate;
        }

        this.integerValuesCount = integerValuesCount;
        this.dateValuesCount = dateValuesCount;
        this.stringValuesCount = stringValuesCount;
        this.booleanValuesCount = booleanValuesCount;
        this.doubleValuesCount = doubleValuesCount;
        this.complexValuesCount = complexValuesCount;
        this.simpleSetsCount = simpleSetsCount;
        this.complexSetsCount = complexSetsCount;
        this.is_closed = is_closed;
    }

    public BaseEntityReportDate(IBaseEntity baseEntity, Date reportDate) {
        if (reportDate == null) {
            throw new IllegalArgumentException("Can not create instance of BaseEntityReportDate " +
                    "with report date equal to null.");
        } else {
            Date newReportDate = (Date) reportDate.clone();
            DataUtils.toBeginningOfTheDay(newReportDate);
            this.reportDate = newReportDate;
        }

        this.baseEntity = baseEntity;
    }

    public IBaseEntity getBaseEntity() {
        return baseEntity;
    }

    public void setBaseEntity(IBaseEntity baseEntity) {
        this.baseEntity = baseEntity;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public long getIntegerValuesCount() {
        return integerValuesCount;
    }

    public void setIntegerValuesCount(long integerValuesCount) {
        this.integerValuesCount = integerValuesCount;
    }

    public long getDateValuesCount() {
        return dateValuesCount;
    }

    public void setDateValuesCount(long dateValuesCount) {
        this.dateValuesCount = dateValuesCount;
    }

    public long getStringValuesCount() {
        return stringValuesCount;
    }

    public void setStringValuesCount(long stringValuesCount) {
        this.stringValuesCount = stringValuesCount;
    }

    public long getBooleanValuesCount() {
        return booleanValuesCount;
    }

    public void setBooleanValuesCount(long booleanValuesCount) {
        this.booleanValuesCount = booleanValuesCount;
    }

    public long getDoubleValuesCount() {
        return doubleValuesCount;
    }

    public void setDoubleValuesCount(long doubleValuesCount) {
        this.doubleValuesCount = doubleValuesCount;
    }

    public long getComplexValuesCount() {
        return complexValuesCount;
    }

    public void setComplexValuesCount(long complexValuesCount) {
        this.complexValuesCount = complexValuesCount;
    }

    public long getSimpleSetsCount() {
        return simpleSetsCount;
    }

    public void setSimpleSetsCount(long simpleSetsCount) {
        this.simpleSetsCount = simpleSetsCount;
    }

    public long getComplexSetsCount() {
        return complexSetsCount;
    }

    public void setComplexSetsCount(long complexSetsCount) {
        this.complexSetsCount = complexSetsCount;
    }

    public boolean is_closed() {
        return is_closed;
    }

    public void setIs_closed(boolean is_closed) {
        this.is_closed = is_closed;
    }

    @Override
    public BaseEntityReportDate clone() {
        BaseEntityReportDate baseEntityReportDate = null;
        try {
            baseEntityReportDate = (BaseEntityReportDate) super.clone();
            baseEntity.setReportDate((Date) reportDate.clone());
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("BaseEntityReportDate class does not implement interface Cloneable.");
        }
        return baseEntityReportDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseEntityReportDate that = (BaseEntityReportDate) o;

        if (booleanValuesCount != that.booleanValuesCount) return false;
        if (complexSetsCount != that.complexSetsCount) return false;
        if (complexValuesCount != that.complexValuesCount) return false;
        if (dateValuesCount != that.dateValuesCount) return false;
        if (doubleValuesCount != that.doubleValuesCount) return false;
        if (integerValuesCount != that.integerValuesCount) return false;
        if (simpleSetsCount != that.simpleSetsCount) return false;
        if (stringValuesCount != that.stringValuesCount) return false;
        if (!reportDate.equals(that.reportDate)) return false;
        if (is_closed != that.is_closed) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (is_closed ? 1 : 0);
        result = 31 * result + reportDate.hashCode();
        result = 31 * result + (int) (integerValuesCount ^ (integerValuesCount >>> 32));
        result = 31 * result + (int) (dateValuesCount ^ (dateValuesCount >>> 32));
        result = 31 * result + (int) (stringValuesCount ^ (stringValuesCount >>> 32));
        result = 31 * result + (int) (booleanValuesCount ^ (booleanValuesCount >>> 32));
        result = 31 * result + (int) (doubleValuesCount ^ (doubleValuesCount >>> 32));
        result = 31 * result + (int) (complexValuesCount ^ (complexValuesCount >>> 32));
        result = 31 * result + (int) (simpleSetsCount ^ (simpleSetsCount >>> 32));
        result = 31 * result + (int) (complexSetsCount ^ (complexSetsCount >>> 32));

        return result;
    }

    @Override
    public void addListener(Listener listener) {
        throw new NotImplementedException("Не реализовано");
    }

    @Override
    public void removeListener(Listener listener) {
        throw new NotImplementedException("Не реализовано");
    }

}
