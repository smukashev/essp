package kz.bsbnb.usci.receiver.model;

import java.util.*;

/**
 * Batch model to save in NoSQL database with all parameters
 *
 * @author k.tulbassiyev
 */
public class BatchModel {
    private Long id;
    private String fileName;
    private byte[] content;

    public BatchModel(Long id, String fileName, byte[] content) {
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
}
