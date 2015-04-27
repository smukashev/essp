package kz.bsbnb.usci.porltet.entity_merge;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author abukabayev
 */
public class BatchJson {
    private Long id;
    private Date repDate;
    private Timestamp receiptDate;

    public BatchJson() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getRepDate() {
        return repDate;
    }

    public void setRepDate(Date repDate) {
        this.repDate = repDate;
    }

    public Timestamp getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Timestamp receiptDate) {
        this.receiptDate = receiptDate;
    }
}
