package com.bsbnb.creditregistry.portlets.signing.data;

import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
   
    public List<Creditor> getCreditorsList(long userId);
    
    public List<FileSignatureRecord> getFilesToSign(long userId);
    
    public String getBaseUrl();
    
    public void addInputFileToQueue(FileSignatureRecord record);

    public void signFile(long fileId, String sign);
}
