package kz.bsbnb.usci.eav.model.json;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchLightJModel {
    private Long id;
    private String fileName;

    public BatchLightJModel(Long id, String fileName) {
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

    @Override
    public String toString() {
        return "BatchLightJModel{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
