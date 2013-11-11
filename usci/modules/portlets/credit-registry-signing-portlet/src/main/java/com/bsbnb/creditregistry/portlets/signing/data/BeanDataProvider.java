package com.bsbnb.creditregistry.portlets.signing.data;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
//import com.bsbnb.creditregistry.dm.maintenance.InputFileSignature;
//import com.bsbnb.creditregistry.dm.maintenance.InputInfo;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.shared.InputInfoStatus;
//import com.bsbnb.creditregistry.dm.ref.shared.SharedType;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputFileBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputInfoBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.ProtocolBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.SysconfigBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ProtocolBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.cr.model.InputInfo;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import static com.bsbnb.creditregistry.portlets.signing.SigningPortlet.log;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private RmiProxyFactoryBean protocolBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputInfoBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean;

    private static final String PORTAL_BASE_URL_CODE = "PORTAL_BASE_URL";
    private InputInfoBeanRemoteBusiness inputInfoBusiness;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;
    //private SysconfigBeanRemoteBusiness sysconfigBusiness;
    //private IRemoteSharedBusiness sharedBusiness;

    public BeanDataProvider() {
        inputInfoBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputInfoBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputInfoBeanRemoteBusiness");
        inputInfoBeanRemoteBusinessFactoryBean.setServiceInterface(InputInfoBeanRemoteBusiness.class);

        inputInfoBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputInfoBusiness = (InputInfoBeanRemoteBusiness) inputInfoBeanRemoteBusinessFactoryBean.getObject();
        if (inputInfoBusiness == null)
        {
            System.out.println("InputInfoBusiness is null!");
        }

        //////////////////////////////

        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        //////////////////////////////

        inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/inputFileBeanRemoteBusiness");
        inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

        inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
    }

    public List<Creditor> getCreditorsList(long userId) {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(userId);
    }

    public List<FileSignatureRecord> getFilesToSign() {
        //List<InputFile> inputFiles = inputFileBusiness.getFilesForSigning();
        List<InputFile> inputFiles = new ArrayList<InputFile>();
        InputFile inputFile1 = new InputFile();

        inputFile1.setFilePath("batch22838.zip");
        inputFile1.setId(1L);
        inputFiles.add(inputFile1);

        List<FileSignatureRecord> resultList = new ArrayList<FileSignatureRecord>(inputFiles.size());
        for (InputFile inputFile : inputFiles) {
            resultList.add(new FileSignatureRecord(inputFile));
        }

        return resultList;
    }

    public String getBaseUrl() {
        /*try {
            return sysconfigBusiness.getSysconfigByKey(PORTAL_BASE_URL_CODE).getValue();
        } catch (ResultInconsistentException ex) {
            //log.log(Level.SEVERE, null, ex);
        } catch (ResultNotFoundException ex) {
            //log.log(Level.SEVERE, null, ex);
        }
        return null;*/

        return "http://localhost:8085";
    }

    public void addInputFileToQueue(FileSignatureRecord record) {
        /*try {
            final InputFileSignature inputFileSignature = new InputFileSignature();
            inputFileSignature.setSignature(record.getSignature());
            final InputFile inputFile = record.getInputFile();
            inputFileBusiness.addSignatureToFile(inputFile, inputFileSignature);
            InputInfo inputInfo = inputFile.getInputInfo();
            inputInfo.setStatus(sharedBusiness.findByC_T(InputInfoStatus.IN_QUEUE.getCode(), SharedType.INPUT_INFO_STATUS.getType()));
            inputInfoBusiness.update(inputInfo);
        } catch (ResultInconsistentException rie) {
            //log.log(Level.SEVERE, null, rie);
        } catch (ResultNotFoundException rnfe) {
            //log.log(Level.SEVERE, null, rnfe);
        }*/
    }
}
