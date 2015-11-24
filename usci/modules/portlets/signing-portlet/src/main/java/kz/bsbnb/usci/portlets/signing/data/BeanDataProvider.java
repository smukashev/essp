package kz.bsbnb.usci.portlets.signing.data;

import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.InputInfoBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.InputFile;
import kz.bsbnb.usci.eav.StaticRouter;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;

    public BeanDataProvider() {
        RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                ":1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        RmiProxyFactoryBean inputFileBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        inputFileBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                ":1099/inputFileBeanRemoteBusiness");
        inputFileBeanRemoteBusinessFactoryBean.setServiceInterface(InputFileBeanRemoteBusiness.class);

        inputFileBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        inputFileBusiness = (InputFileBeanRemoteBusiness) inputFileBeanRemoteBusinessFactoryBean.getObject();
    }

    public List<Creditor> getCreditorsList(long userId) {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(userId);
    }

    public List<FileSignatureRecord> getFilesToSign(long userId) {
        List<InputFile> inputFiles = inputFileBusiness.getFilesForSigning(userId);

        List<FileSignatureRecord> resultList = new ArrayList<FileSignatureRecord>(inputFiles.size());
        for (InputFile inputFile : inputFiles) {
            resultList.add(new FileSignatureRecord(inputFile));
        }

        return resultList;
    }

    public void signFile(long fileId, String sign) {
        inputFileBusiness.signFile(fileId, sign);
    }

    public String getBaseUrl() {
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
