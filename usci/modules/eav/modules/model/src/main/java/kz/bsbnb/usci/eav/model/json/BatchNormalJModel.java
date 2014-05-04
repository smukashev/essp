package kz.bsbnb.usci.eav.model.json;

import java.util.Date;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchNormalJModel {
    private Long id;
    private String type = "batch";
    private String fileName;
    private EntityStatusArrayJModel status;
    private Date received;

    public BatchNormalJModel(Long id, String fileName, EntityStatusArrayJModel status, Date received) {
        this.id = id;
        this.fileName = fileName;
        this.status = status;
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

    public EntityStatusArrayJModel getStatus() {
        return status;
    }

    public void setStatus(EntityStatusArrayJModel status) {
        this.status = status;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }
}
