package kz.bsbnb.usci.eav.model.json;

import java.util.Date;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchFullStatusJModel
{
    private Long id;
    private String fileName;
    private Date received;
    private BatchStatusArrayJModel status;

    public BatchFullStatusJModel(Long id, String fileName, Date received) {
        this.id = id;
        this.fileName = fileName;
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

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public BatchStatusArrayJModel getStatus()
    {
        return status;
    }

    public void setStatus(BatchStatusArrayJModel status)
    {
        this.status = status;
    }
}
