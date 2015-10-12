package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.Date;

public class ReportMessage implements Serializable {
    private static final long serialVersionUID = 8626348715892412142L;

    private Long id;

    private transient Report report;

    private String username;

    private Date sendDate;

    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
