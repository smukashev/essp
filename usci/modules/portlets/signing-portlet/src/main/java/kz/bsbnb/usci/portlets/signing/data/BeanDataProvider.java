package kz.bsbnb.usci.portlets.signing.data;

import kz.bsbnb.usci.core.service.InputFileBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.BatchStatus;
import kz.bsbnb.usci.eav.util.BatchStatuses;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.repository.IServiceRepository;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import kz.bsbnb.usci.sync.service.IBatchService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Aidar.Myrzahanov
 */
public class BeanDataProvider implements DataProvider {
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    private InputFileBeanRemoteBusiness inputFileBusiness;
    private IBatchProcessService batchProcessService;
    private static final Logger logger = Logger.getLogger(BeanDataProvider.class);

    public BeanDataProvider() {
        try {
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

            RmiProxyFactoryBean batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1097/batchProcessService");
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.compose(Errors.E286));
        }
    }

    public List<Creditor> getCreditorsList(long userId) {
        return portalUserBusiness.getMainCreditorsInAlphabeticalOrder(userId);
    }

    public List<FileSignatureRecord> getFilesToSign(long creditorId) {
        List<InputFile> inputFiles = inputFileBusiness.getFilesForSigning(creditorId);

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
        return StaticRouter.isDevMode() ? "http://localhost:8083" : "http://essp.nationalbank.kz";
    }

    @Override
    public String getCreditorsBinNumber(Creditor creditor) {
        /*for (CreditorDoc doc : creditorDocBusiness.getCreditorDocsList(creditor)) {
            if ("07".equals(doc.getType().getCode())) {
                return doc.getNo();
            }
        }*/
        return creditor.getBIN();
        //return null;
    }

    @Override
    public void cancelFile(FileSignatureRecord file) {
        //InputInfo inputInfo = file.getInputFile().getInputInfo();
        //inputInfo.setStatus(getFileStatus(InputInfoStatus.REJECTED));
        batchProcessService.CancelBatch(file.getInputFile().getId());
        //inputInfoBusiness.update(inputInfo);
    }

    public String getOcspServiceUrl() {
        return "http://91.195.226.34:62255";
        /*try {
            return sysconfigBusiness.getSysconfigByKey(OCSP_SERVICE_URL_CONFIG_KEY).getValue();
        } catch (ResultInconsistentException ex) {
            throw new RuntimeException(ex);
        } catch (ResultNotFoundException ex) {
            throw new RuntimeException(ex);
        }*/
    }

    private Shared getFileStatus(InputInfoStatus status) {
        try {
            //return sharedBusiness.find(status, SharedType.INPUT_INFO_STATUS);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public void addInputFileToQueue(FileSignatureRecord record) {
        signFile(record.getInputFile().getId(), record.getSignature());
        batchProcessService.restartBatch(record.getInputFile().getId());
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
