package kz.bsbnb.usci.portlets.upload.data;

import kz.bsbnb.usci.cr.model.Creditor;

import java.util.List;

/**
 *
 * @author Aidar.Myrzahanov
 */
public interface DataProvider {

    public String getUploadsPath();

    public void saveFile(long userId, boolean isSigning, Creditor creditor, String filename, byte[] content, String path) throws DatabaseException;

    public List<Creditor> getUserCreditors(long userId);
    
    public List<Creditor> getOrganizations();
    
    public List<Integer> getIdsForOrganizationsUsingDigitalSigning();
    
    public void saveOrganizationsUsingDigitalSigning(List<Creditor> creditors);
}