package kz.bsbnb.usci.portlets.upload.ui;

//import com.bsbnb.creditregistry.dm.Report;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

//import com.bsbnb.creditregistry.dm.maintenance.InputFile;
//import com.bsbnb.creditregistry.dm.maintenance.InputInfo;
//import com.bsbnb.creditregistry.dm.ref.Creditor;
//import com.bsbnb.creditregistry.dm.ref.Shared;
//import com.bsbnb.creditregistry.dm.ref.shared.ReportType;
//import com.bsbnb.creditregistry.ejb.api.ReportBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputFileBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.InputInfoBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.api.maintenance.PortalUserBeanRemoteBusiness;
//import com.bsbnb.creditregistry.ejb.ref.business.remote.IRemoteSharedBusiness;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultInconsistentException;
//import com.bsbnb.creditregistry.ejb.ref.exception.ResultNotFoundException;
import kz.bsbnb.usci.eav.util.ReportStatus;
import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.core.service.*;
import kz.bsbnb.usci.cr.model.*;
import kz.bsbnb.usci.portlets.upload.UploadApplication;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 *
 * @author Aidar.Myrzahanov
 */
public abstract class AbstractUploadComponent extends VerticalLayout {
    private RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean;
    private RmiProxyFactoryBean reportBusinessFactoryBean;
    private RmiProxyFactoryBean batchProcessServiceFactoryBean;

    private PortalUserBeanRemoteBusiness portalUserBusiness;
    //private IRemoteSharedBusiness sharedBusiness;
    //private InputInfoBeanRemoteBusiness inputInfoBusiness;
    //private InputFileBeanRemoteBusiness inputFileBusiness;
    private ReportBeanRemoteBusiness reportBusiness;
    private IBatchProcessService batchProcessService;
    private Creditor creditor;
    private VerticalLayout statusPanel;
    private Label errorMessageLabel;
    private static final String UPLOADS_PATH = "/tmp/";
    public static final long MAX_FILE_LENGTH = 5 * (1L << 20);
    private PortletEnvironmentFacade portletEnvironment;

    private void initializeBeans() {
        portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
        portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/portalUserBeanRemoteBusiness");
        portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

        portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
        portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        reportBusinessFactoryBean = new RmiProxyFactoryBean();
        reportBusinessFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/reportBeanRemoteBusiness");
        reportBusinessFactoryBean.setServiceInterface(ReportBeanRemoteBusiness.class);

        reportBusinessFactoryBean.afterPropertiesSet();
        reportBusiness = (ReportBeanRemoteBusiness) reportBusinessFactoryBean.getObject();

        batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
        batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchProcessService");
        batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);

        batchProcessServiceFactoryBean.afterPropertiesSet();
        batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
    }

    public AbstractUploadComponent(PortletEnvironmentFacade portletEnvironment) {
        this.portletEnvironment = portletEnvironment;
        initializeBeans();
    }

    protected String getResourceString(String resourceKey) {
        return portletEnvironment.getResourceString(resourceKey);
    }

    protected Label addStatusMessage(String text, boolean isError) {
        String color = isError ? "red" : "black";
        String htmlString = String.format("<h3 style='color:%s'>%s</h3>", color, text);
        Label label = new Label(htmlString, Label.CONTENT_XHTML);
        label.setSizeUndefined();
        statusPanel.addComponent(label);
        statusPanel.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        return label;
    }

    protected void clearStatus() {
        statusPanel.removeAllComponents();
    }

    private String saveFileOnDisk(byte[] array, String fileName) throws IOException {
        File uploadsDirectory = new File(UPLOADS_PATH);
        if (!uploadsDirectory.exists()) {
            if (!uploadsDirectory.mkdir()) {
                System.out.println("Can't create dir");
                return null;
            }
        }
        if (creditor == null || creditor.getId() <= 0) {
            System.out.println("No creditor id");
            return null;
        }
        String creditorsPath = UPLOADS_PATH + creditor.getId() + "/";
        File creditorsDir = new File(creditorsPath);
        if (!creditorsDir.exists()) {
            if (!creditorsDir.mkdir()) {
                return null;
            }
        }
        String dirName = (new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss")).format(new Date());
        creditorsPath += dirName;
        int counter = 0;
        while ((new File(creditorsPath + (counter == 0 ? "" : counter + "") + "/")).exists()) {
            counter++;
        }
        String dirPath = creditorsPath + (counter == 0 ? "" : counter + "") + "/";
        File newDirectory = new File(dirPath);
        if (!newDirectory.mkdir()) {
            return null;
        }
        StringBuilder normalFileNameBuilder = new StringBuilder(fileName.length());
        for (int i = 0; i < fileName.length(); i++) {
            char ch = fileName.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.') {
                normalFileNameBuilder.append(fileName.charAt(i));
            }
        }
        if (normalFileNameBuilder.length() == 0) {
            normalFileNameBuilder.append("dummyName");
        }
        normalFileNameBuilder.append("/");
        String normalFileName;
        if (normalFileNameBuilder.length() > 25) {
            normalFileName = normalFileNameBuilder.substring(normalFileNameBuilder.length() - 20, normalFileNameBuilder.length());
        } else {
            normalFileName = normalFileNameBuilder.toString();
        }
        String filePath = dirPath + normalFileName;
        File newFile = new File(filePath);
        FileOutputStream fos = new FileOutputStream(newFile);
        fos.write(array);
        fos.close();
        return newFile.getAbsolutePath();
    }

    protected void handleFile(byte[] array, String fileName) throws IllegalArgumentException {
        try {
            System.out.println("#%% " + fileName);
            String path = saveFileOnDisk(array, fileName);
            UploadApplication.log.log(Level.INFO, "Path: {0}", path);
            //Shared webServiceLoadType = sharedBusiness.findByC_T("WS", "input_type");
            //Shared inQueueStatus = sharedBusiness.findByC_T("IN_QUEUE", "input_info_status");
            //InputInfo ii = inputInfoBusiness.insert(portletEnvironment.getUserID(), creditor,
              //      fileName, new Date(), webServiceLoadType, inQueueStatus);
            //log.log(Level.INFO, "Input info ID: {0}", ii.getId());
            //InputFile inputFile = new InputFile();
            //inputFile.setFilePath(path);
            //inputFile.setInputInfo(ii);
            //inputFileBusiness.insertInputFile(inputFile);
            System.out.println("### " + path);
            batchProcessService.processBatch(path, portletEnvironment.getUserID());
            addStatusMessage(String.format(getResourceString(Localization.UPLOAD_SUCCEDED_MESSAGE.getKey()), fileName), false);
        } catch (IOException ioe) {
            UploadApplication.log.log(Level.SEVERE, "Can't save file {0}", fileName);
        }
    }

    @Override
    public void attach() {
        initializeComponents();
    }

    private void initializeComponents() {
        //Создаем строку сообщения об ошибке количества кредиторов - СтрокаОшибки
        String errorMessage = checkCreditors();
        //Если СтрокаОшибки содержит сообщение, то
        if (!errorMessage.isEmpty()) {
            //загружаем интерфейс отображения ошибки
            initializeErrorComponents(errorMessage);
            //В противном случае
        } else {
            //загружаем обычный интерфейс
            initializeNormalComponents();
        }
    }

    private void initializeNormalComponents() {
        removeAllComponents();
        initializeUploadComponents();
        //statusPanel
        statusPanel = new VerticalLayout();
        statusPanel.setImmediate(true);

        addComponent(statusPanel);
        setComponentAlignment(statusPanel, Alignment.TOP_CENTER);
    }

    protected abstract void initializeUploadComponents();

    /**
     * Метод загружает интерфейс, содержащий только сообщение об ошибке
     */
    private void initializeErrorComponents(String errorMessage) {
        errorMessageLabel = new Label("<h2>" + errorMessage + "<h2>", Label.CONTENT_XHTML);
        addComponent(errorMessageLabel);
    }

    protected boolean isFileValid(long length, String filename) {
        if (length > MAX_FILE_LENGTH) {
            addStatusMessage(String.format(getResourceString(Localization.FILE_TOO_LARGE_MESSAGE.getKey()), filename, MAX_FILE_LENGTH / (1 << 20), length), true);
            return false;
        }
        if (filename == null || !filename.endsWith(".zip")) {
            addStatusMessage(String.format(getResourceString(Localization.NOT_A_ZIP_FILE_MESSAGE.getKey()), filename), true);
            return false;
        }
        return true;
    }

    /**
     * Метод проверяет возможность загрузки пакетного файла
     * @return сообщение об ошибке
     * если сообщение пусто, то пользователь имеет возможность загрузки
     */
    private String checkCreditors() {
        String result = "";
        //Если не инициализирован пользователь, то
        if (portalUserBusiness == null) {
            //  Устанавливаем СтрокуОшибки - "Невозможно определить пользователя"
            result = getResourceString(Localization.USER_UNKNOWN_MESSAGE.getKey());
            //В противном случае
        } else {
            //  Получаем список кредиторов, к которым у пользователя есть доступ - СписокКредиторов
            List<Creditor> creditors = portalUserBusiness.getPortalUserCreditorList(portletEnvironment.getUserID());
            //  Получаем количество кредиторов - КоличествоКредиторов
            int creditorsNumber = creditors.size();
            //  Если КоличествоКредиторов равно нулю, то
            if (creditorsNumber == 0) {
                //Устанавливаем СтрокуОшибки - "У вас нет доступа к кредиторам"
                result = getResourceString(Localization.USER_DOES_NOT_HAVE_ACCESS_TO_CREDITORS_MESSAGE.getKey());
                //В противном случае, если КоличествоКредиторов больше одного
            } else if (creditorsNumber > 1) {
                //Устанавливаем СтрокуОшибки - "У вас более одного кредитора"
                result = getResourceString(Localization.USER_HAS_MORE_THAN_ONE_CREDITOR_MESSAGE.getKey());
            } else {
                creditor = creditors.get(0);
                Date reportDate = reportBusiness.getReportDate(creditor.getId());
                Report report = reportBusiness.getByCreditor_ReportDate(creditor, reportDate);
                if(report!=null && ReportStatus.ORGANIZATION_APPROVED.code().equals(report.getStatus().getCode())) {
                    result = getResourceString(Localization.ORGANIZATION_APPROVED_DATA_MESSAGE.getKey());
                }
            }
        }
        return result;
    }
}
