package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.util.Date;

public class Protocol implements Serializable {
    private static final long serialVersionUID = 8626348715892412222L;

    private long id;

    private String note;

    private long packNo;

    private Message message;

    private Shared messageType;

    private InputInfo inputInfo;

    private String typeDescription;

    private Date primaryContractDate;

    private Shared protocolType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessageType(Shared messageType) {
        this.messageType = messageType;
    }

    public Shared getMessageType() {
        return this.messageType;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public InputInfo getInputInfo() {
        return inputInfo;
    }

    public void setInputInfo(InputInfo inputInfo) {
        this.inputInfo = inputInfo;
    }

    public Shared getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(Shared protocolType) {
        this.protocolType = protocolType;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public Date getPrimaryContractDate() {
        return primaryContractDate;
    }

    public void setPrimaryContractDate(Date primaryContractDate) {
        this.primaryContractDate = primaryContractDate;
    }

    public long getPackNo() {
        return packNo;
    }

    public void setPackNo(long packNo) {
        this.packNo = packNo;
    }
}
