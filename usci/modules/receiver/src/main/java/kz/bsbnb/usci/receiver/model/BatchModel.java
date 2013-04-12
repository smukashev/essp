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
    private String status;
    private String statusDescription;
    private Map<String, ContractModel> contracts = new HashMap<String, ContractModel>();

    public BatchModel(Long id, String fileName, byte[] content, String status) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.status = status;
    }

    public BatchModel(Long id, String fileName, byte[] content, String status, String statusDescription, Map<String,
            ContractModel> contracts) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.status = status;
        this.statusDescription = statusDescription;
        this.contracts = contracts;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Map<String, ContractModel> getContracts() {
        return contracts;
    }

    public void setContracts(Map<String, ContractModel> contracts) {
        this.contracts = contracts;
    }
}
