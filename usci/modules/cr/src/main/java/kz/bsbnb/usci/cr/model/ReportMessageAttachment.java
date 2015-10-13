package kz.bsbnb.usci.cr.model;

import java.io.Serializable;

public class ReportMessageAttachment implements Serializable {
    private static final long serialVersionUID = 8626348715892462142L;

    private Long id;

    private ReportMessage reportMessage;

    private String filename;

    private byte[] content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportMessage getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(ReportMessage reportMessage) {
        this.reportMessage = reportMessage;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
