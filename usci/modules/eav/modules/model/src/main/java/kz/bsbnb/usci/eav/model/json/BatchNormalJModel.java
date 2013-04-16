package kz.bsbnb.usci.eav.model.json;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchNormalJModel {
    private Long id;
    private String fileName;
    private StatusJModel status;

    public BatchNormalJModel(Long id, String fileName) {
        this.id = id;
        this.fileName = fileName;
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

    public StatusJModel getStatus() {
        return status;
    }

    public void setStatus(StatusJModel status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BatchNormalJModel{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                '}';
    }
}
