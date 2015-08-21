package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * General information about the batch, such as:
 * date of receipt, start date of processing,
 * end date of processing, status, etc.
 *
 * @author a.motov
 */
public class Batch extends Persistable
{
    Logger logger = LoggerFactory.getLogger(Batch.class);
    /**
     * Date and time of receipt of the batch.
     */
    private Date receiptDate;

    private Date repDate;

    private Long userId;

    private  Long creditorId;

    /**
     * Initializes batch with the default values.
     */

    public Batch(Date reportDate, Long userId){
        super();

        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = new Date();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
        this.userId = userId;
    }

    public Batch(Date reportDate)
    {
        super();

        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = new Date();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
    }

    /**
     * Initializes batch with a date and time of receipt of the batch.
     *
     * @param receiptDate the date and time of receipt of the batch.
     */
    public Batch(Date receiptDate, Date reportDate)
    {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        Date newReceiptDate = (Date)receiptDate.clone();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.repDate = newReportDate;
        this.receiptDate = newReceiptDate;
    }

    public void setReceiptDate(Date receiptDate)
    {
        Date newReceiptDate = (Date)receiptDate.clone();
        DataUtils.toBeginningOfTheSecond(newReceiptDate);

        this.receiptDate = newReceiptDate;
    }

    public Date getReceiptDate()
    {
        return receiptDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Batch)) return false;
        if (!super.equals(o)) return false;

        Batch batch = (Batch) o;

        logger.debug("Batch receipt date: " + receiptDate + ", " + batch.receiptDate);
        return receiptDate.equals(batch.receiptDate);
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

    public void setRepDate(Date reportDate)
    {
        Date newReportDate = (Date)reportDate.clone();
        DataUtils.toBeginningOfTheDay(newReportDate);

        this.repDate = newReportDate;
    }

    public Long getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(Long creditorId) {
        this.creditorId = creditorId;
    }
}
