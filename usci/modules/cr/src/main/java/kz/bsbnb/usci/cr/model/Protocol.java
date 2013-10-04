package kz.bsbnb.usci.cr.model;

import java.io.Serializable;
import java.math.BigInteger;

import java.util.Date;

/**
 * Сущность представляет собой запись ошибки. Содержит идентификатор (id), идентификатор пользователя (userId),
 * ссылку на ошибку (errorMessage), дату возникновения ошибки (errorDate), ссылку на батч (batchInfo).
 * 
 * @author <a href="mailto:dmitriy.zakomirnyy@bsbnb.kz">Dmitriy Zakomirnyy</a>
 */
public class Protocol implements Serializable {

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

    public void setMessageType(Shared messageType){
    	this.messageType = messageType;
    }
    
    public Shared getMessageType(){
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

    /**
     * @return the protocolType
     */
    public Shared getProtocolType() {
        return protocolType;
    }

    /**
     * @param protocolType the protocolType to set
     */
    public void setProtocolType(Shared protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * @return the typeDescription
     */
    public String getTypeDescription() {
        return typeDescription;
    }

    /**
     * @param typeDescription the typeDescription to set
     */
    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }
    
    /**
     * @return the primaryContractDate
     */
    public Date getPrimaryContractDate() {
        return primaryContractDate;
    }

    /**
     * @param primaryContractDate the primaryContractDate to set
     */
    public void setPrimaryContractDate(Date primaryContractDate) {
        this.primaryContractDate = primaryContractDate;
    }

    /**
     * @return the packNo
     */
    public long getPackNo() {
        return packNo;
    }

    /**
     * @param packNo the packNo to set
     */
    public void setPackNo(long packNo) {
        this.packNo = packNo;
    }
}
