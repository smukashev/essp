package kz.bsbnb.usci.eav.model.json;

import java.util.Date;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchFullJModel {
    private Long id;
    private String type = "batch";
    private String fileName;
    private byte[] content;
    private Date received;

    public BatchFullJModel(Long id, String fileName, byte[] content, Date received) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.received = received;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
