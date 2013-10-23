package kz.bsbnb.usci.porltet.entity_portlet;

import java.util.Date;

/**
 * @author abukabayev
 */
public class BatchJson {
    private Long id;
    private Date repDate;
    private Date receiptDate;

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

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }
}
