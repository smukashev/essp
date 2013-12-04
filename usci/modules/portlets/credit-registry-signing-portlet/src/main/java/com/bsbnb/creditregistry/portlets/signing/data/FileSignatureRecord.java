package com.bsbnb.creditregistry.portlets.signing.data;

import kz.bsbnb.usci.cr.model.InputFile;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class FileSignatureRecord {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    
    private InputFile inputFile;
    private String signature;

    public FileSignatureRecord(InputFile inputFile) {
        this.inputFile = inputFile;
    }
    
    public String getId() {
        return "" + inputFile.getId();
    }
    
    public BigInteger getInputFileId() {
        return BigInteger.valueOf(inputFile.getId());
    }
    
    public String getFilename(){
        //return inputFile.getInputInfo().getFileName();
        return inputFile.getFilePath();
    }
    
    public String getSentDate() {
        //return DATE_FORMAT.format(inputFile.getInputInfo().getReceiverDate());
        return DATE_FORMAT.format(new Date());
    }
    
    public String getHash() {
        return inputFile.getMd5();
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }

    public InputFile getInputFile() {
        return inputFile;
    }
}
