package kz.bsbnb.usci.eav_model.model;

import kz.bsbnb.usci.eav_model.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav_model.util.DateUtils;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * General information about the batch, such as:
 * date of receipt, start date of processing,
 * end date of processing, status, etc.
 *
 * @author a.motov
 */
public class Batch extends Persistable
{
    /**
     * Date and time of receipt of the batch.
     */
    private Timestamp receiptDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());

    private Date repDate;

    /**
     * Initializes batch with the default values.
     */
    public Batch(Date repDate)
    {
        super();

        this.repDate = repDate;
    }

    /**
     * Initializes batch with a date and time of receipt of the batch.
     *
     * @param receiptDate the date and time of receipt of the batch.
     */
    public Batch(Timestamp receiptDate, Date repDate)
    {
        this.receiptDate = receiptDate;
        this.repDate = new Date(DateUtils.cutOffTime(repDate));
    }

    public void setReceiptDate(Timestamp receiptDate)
    {
        this.receiptDate = receiptDate;
    }

    public Timestamp getReceiptDate()
    {
        return receiptDate;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Batch)) return false;
        if (!super.equals(o)) return false;

        Batch batch = (Batch) o;

        if (!receiptDate.equals(batch.receiptDate)) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + receiptDate.hashCode();
        return result;
    }

    public Date getRepDate()
    {
        return repDate;
    }

    public void setRepDate(Date repDate)
    {
        this.repDate = repDate;
    }
}
