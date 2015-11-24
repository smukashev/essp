package kz.bsbnb.usci.portlets.signing.data;

import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {
   List<Creditor> getCreditorsList(long userId);
    
    List<FileSignatureRecord> getFilesToSign(long userId);

    String getBaseUrl();
    
    void addInputFileToQueue(FileSignatureRecord record);

    void signFile(long fileId, String sign);
}
