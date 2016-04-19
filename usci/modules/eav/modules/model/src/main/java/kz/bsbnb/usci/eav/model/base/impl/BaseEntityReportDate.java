package kz.bsbnb.usci.eav.model.base.impl;

import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate;
import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.DataUtils;

import java.util.Date;

public class BaseEntityReportDate extends Persistable implements IBaseEntityReportDate {
    private IBaseEntity baseEntity;

    private long creditorId;

    private Date reportDate;

    private boolean isClosed;

    private long integerValuesCount = 0;

    private long dateValuesCount = 0;

    private long stringValuesCount = 0;

    private long booleanValuesCount = 0;

    private long doubleValuesCount = 0;

    private long complexValuesCount = 0;

    private long simpleSetsCount = 0;

    private long complexSetsCount = 0;

    public BaseEntityReportDate(long id, long creditorId, Date reportDate, long integerValuesCount, long dateValuesCount,
                                long stringValuesCount, long booleanValuesCount, long doubleValuesCount,
                                long complexValuesCount, long simpleSetsCount, long complexSetsCount) {
        super(id);

        this.creditorId = creditorId;

        if (reportDate == null) {
            throw new IllegalArgumentException(Errors.compose(Errors.E29));
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

    public BaseEntityReportDate(long id, long creditorId, Date reportDate, long integerValuesCount, long dateValuesCount,
                                long stringValuesCount, long booleanValuesCount, long doubleValuesCount,
                                long complexValuesCount, long simpleSetsCount, long complexSetsCount,
                                boolean isClosed) {
        super(id);

        this.creditorId = creditorId;

        if (reportDate == null) {
            throw new IllegalArgumentException(Errors.compose(Errors.E29));
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
        this.isClosed = isClosed;
    }

    public BaseEntityReportDate(IBaseEntity baseEntity, Date reportDate, long creditorId) {
        if (reportDate == null) {
            throw new IllegalArgumentException(Errors.compose(Errors.E28));
        } else {
            Date newReportDate = (Date) reportDate.clone();
            DataUtils.toBeginningOfTheDay(newReportDate);
            this.reportDate = newReportDate;
        }

        this.baseEntity = baseEntity;
        this.creditorId = creditorId;
    }

    @Override
    public IBaseEntity getBaseEntity() {
        return baseEntity;
    }

    @Override
    public void setBaseEntity(IBaseEntity baseEntity) {
        this.baseEntity = baseEntity;
    }

    @Override
    public Date getReportDate() {
        return reportDate;
    }

    @Override
    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    @Override
    public long getIntegerValuesCount() {
        return integerValuesCount;
    }

    @Override
    public void setIntegerValuesCount(long integerValuesCount) {
        this.integerValuesCount = integerValuesCount;
    }

    @Override
    public long getDateValuesCount() {
        return dateValuesCount;
    }

    @Override
    public void setDateValuesCount(long dateValuesCount) {
        this.dateValuesCount = dateValuesCount;
    }

    @Override
    public long getStringValuesCount() {
        return stringValuesCount;
    }

    @Override
    public void setStringValuesCount(long stringValuesCount) {
        this.stringValuesCount = stringValuesCount;
    }

    @Override
    public long getBooleanValuesCount() {
        return booleanValuesCount;
    }

    @Override
    public void setBooleanValuesCount(long booleanValuesCount) {
        this.booleanValuesCount = booleanValuesCount;
    }

    @Override
    public long getDoubleValuesCount() {
        return doubleValuesCount;
    }

    @Override
    public void setDoubleValuesCount(long doubleValuesCount) {
        this.doubleValuesCount = doubleValuesCount;
    }

    @Override
    public long getComplexValuesCount() {
        return complexValuesCount;
    }

    @Override
    public void setComplexValuesCount(long complexValuesCount) {
        this.complexValuesCount = complexValuesCount;
    }

    @Override
    public long getSimpleSetsCount() {
        return simpleSetsCount;
    }

    @Override
    public void setSimpleSetsCount(long simpleSetsCount) {
        this.simpleSetsCount = simpleSetsCount;
    }

    @Override
    public long getComplexSetsCount() {
        return complexSetsCount;
    }

    @Override
    public void setComplexSetsCount(long complexSetsCount) {
        this.complexSetsCount = complexSetsCount;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void setClosed(boolean closed) {
        this.isClosed = closed;
    }

    @Override
    public long getCreditorId() {
        return creditorId;
    }

    @Override
    public void setCreditorId(long creditorId) {
        this.creditorId = creditorId;
    }

    @Override
    public BaseEntityReportDate clone() {
        return new BaseEntityReportDate(baseEntity, new Date(reportDate.getTime()), this.getCreditorId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseEntityReportDate that = (BaseEntityReportDate) o;

        if (creditorId != that.creditorId) return false;
        if (booleanValuesCount != that.booleanValuesCount) return false;
        if (complexSetsCount != that.complexSetsCount) return false;
        if (complexValuesCount != that.complexValuesCount) return false;
        if (dateValuesCount != that.dateValuesCount) return false;
        if (doubleValuesCount != that.doubleValuesCount) return false;
        if (integerValuesCount != that.integerValuesCount) return false;
        if (simpleSetsCount != that.simpleSetsCount) return false;
        if (stringValuesCount != that.stringValuesCount) return false;
        if (!reportDate.equals(that.reportDate)) return false;
        if (isClosed != that.isClosed) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isClosed ? 1 : 0);
        result = 31 * result + reportDate.hashCode();
        result = 31 * result + (int) (creditorId ^ (creditorId >>> 32));
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
    public String toString() {
        return "BaseEntityReportDate{" +
                "id=" + (baseEntity == null ? 0 : baseEntity.getId()) +
                ", reportDate=" + DataTypes.dateFormatDot.format(reportDate) +
                '}';
    }
}
