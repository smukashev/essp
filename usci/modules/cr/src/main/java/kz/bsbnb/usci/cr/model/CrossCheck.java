package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.Date;

public class CrossCheck implements Serializable {
    private static final long serialVersionUID = 2115312865112267120L;

    private Long id;

    private Shared status;

    private String username;

    private Creditor creditor;

    private Date dateBegin;

    private Date dateEnd;

    private Date reportDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shared getStatus() {
        return status;
    }

    public void setStatus(Shared status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Creditor getCreditor() {
        return creditor;
    }

    public void setCreditor(Creditor creditor) {
        this.creditor = creditor;
    }

    public Date getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(Date dateBegin) {
        this.dateBegin = dateBegin;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
}
