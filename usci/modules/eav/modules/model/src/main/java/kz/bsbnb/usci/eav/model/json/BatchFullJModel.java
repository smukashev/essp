package kz.bsbnb.usci.eav.model.json;

import java.util.Arrays;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchFullJModel {
    private Long id;
    private String fileName;
    private byte[] content;
    private StatusJModel status;

    public BatchFullJModel(Long id, String fileName, byte[] content) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
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

    public StatusJModel getStatus() {
        return status;
    }

    public void setStatus(StatusJModel status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BatchFullJModel{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                '}';
    }
}
