package kz.bsbnb.usci.porltet.entity_portlet;

import kz.bsbnb.usci.eav.util.DataUtils;

import java.sql.Date;

/**
 * @author abukabayev
 */
public class BaseValueJson{
    private BatchJson batch;

    private Object value;

    private Date repDate;

    private long index;

    public BaseValueJson() {
    }

    public BaseValueJson(BatchJson batch, long index, Date repDate, Object value)
    {
        if (repDate == null)
            throw new IllegalArgumentException
                    ("repDate is null. Initialization of the BaseValue ​​is not possible.");

        if (batch == null)
            throw new IllegalArgumentException
                    ("Batch is null. Initialization of the BaseValue ​​is not possible.");

        if (batch.getId() < 1)
            throw new IllegalArgumentException
                    ("Batch has no id. Initialization of the BaseValue ​​is not possible.");


        this.batch = batch;
        this.index = index;
        this.value = value;
        this.repDate = new Date(DataUtils.cutOffTime(repDate));
    }

    public BatchJson getBatch() {
        return batch;
    }

    public Object getValue() {
        return value;
    }

    public Date getRepDate() {
        return repDate;
    }

    public void setBatch(BatchJson batch) {
        this.batch = batch;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setRepDate(Date repDate) {
        this.repDate = repDate;
    }
}
