package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

import java.io.Serializable;
import java.util.Date;

public class BatchEntry extends Persistable
{
    private String value;
    private Date repDate;
    private Date updateDate;
    private long userId;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Date getRepDate() {
        return repDate;
    }

    public void setRepDate(Date repDate) {
        this.repDate = repDate;
    }

    public Date getUpdateDate()
    {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate)
    {
        this.updateDate = updateDate;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }
}
