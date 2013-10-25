package kz.bsbnb.usci.porltet.metaeditor;

import java.sql.Date;

/**
 * @author abukabayev
 */
public class BaseValueJson{
    private BatchJson batch;

    private Object value;

    private Date repDate;

    public BaseValueJson() {
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
