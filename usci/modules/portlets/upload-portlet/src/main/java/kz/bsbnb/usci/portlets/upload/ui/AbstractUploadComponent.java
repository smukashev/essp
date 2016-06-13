package kz.bsbnb.usci.portlets.upload.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.cr.model.Report;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.util.ReportStatus;
import kz.bsbnb.usci.portlets.upload.PortletEnvironmentFacade;
import kz.bsbnb.usci.portlets.upload.UploadApplication;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class AbstractUploadComponent extends VerticalLayout {
    private PortalUserBeanRemoteBusiness portalUserBusiness;

    private ReportBeanRemoteBusiness reportBusiness;

    private IBatchProcessService batchProcessService;

    private Creditor creditor;

    private VerticalLayout statusPanel;

    private static final String UPLOADS_PATH = "\\\\" + StaticRouter.getAsIP() + "\\tmp$\\";

    public static final long MAX_FILE_LENGTH = 5 * (1L << 20);

    private PortletEnvironmentFacade portletEnvironment;

    private final Logger logger = Logger.getLogger(AbstractUploadComponent.class);

    private void initializeBeans() {
        try {
            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");

            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            RmiProxyFactoryBean reportBusinessFactoryBean = new RmiProxyFactoryBean();
            reportBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/reportBeanRemoteBusiness");
            reportBusinessFactoryBean.setServiceInterface(ReportBeanRemoteBusiness.class);

            reportBusinessFactoryBean.afterPropertiesSet();
            reportBusiness = (ReportBeanRemoteBusiness) reportBusinessFactoryBean.getObject();

            RmiProxyFactoryBean batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1097/batchProcessService");
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            logger.error("Can't initialise services: " + e.getMessage());
        }
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
                logger.warn("Can't create dir");
                return null;
            }
        }
        if (creditor == null || creditor.getId() <= 0) {
            logger.warn("No creditor id");
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
            if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-') {
                normalFileNameBuilder.append(fileName.charAt(i));
            } else {
                normalFileNameBuilder.append("_");
            }
        }
        if (normalFileNameBuilder.length() == 0) {
            normalFileNameBuilder.append("dummyName");
        }
        normalFileNameBuilder.append("/");
        String normalFileName;
        if (normalFileNameBuilder.length() > 128) {
            normalFileName = normalFileNameBuilder.substring(normalFileNameBuilder.length() - 128,
                    normalFileNameBuilder.length());
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
            logger.info("#%% " + fileName);
            String path = saveFileOnDisk(array, fileName);
            logger.info("Path: "+path);
            logger.info("### " + path);
            batchProcessService.processBatch(path, portletEnvironment.getUserID(), portletEnvironment.isNB());
            addStatusMessage(String.format(getResourceString(Localization.UPLOAD_SUCCEDED_MESSAGE.getKey()),
                    fileName), false);
        } catch (IOException ioe) {
            logger.error("Can't save file : "+ fileName);
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
        Label errorMessageLabel = new Label("<h2>" + errorMessage + "<h2>", Label.CONTENT_XHTML);
        addComponent(errorMessageLabel);
    }

    protected boolean isFileValid(long length, String filename) {
        if (length > MAX_FILE_LENGTH) {
            addStatusMessage(String.format(getResourceString(Localization.FILE_TOO_LARGE_MESSAGE.getKey()),
                    filename, MAX_FILE_LENGTH / (1 << 20), length), true);
            return false;
        }
        if (filename == null || !filename.endsWith(".zip")) {
            addStatusMessage(String.format(getResourceString(Localization.NOT_A_ZIP_FILE_MESSAGE.getKey()),
                    filename), true);
            return false;
        }
        return true;
    }

    /**
     * Метод проверяет возможность загрузки пакетного файла
     *
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
            } else if (creditorsNumber > 1 && !portletEnvironment.isNB()) {
                //Устанавливаем СтрокуОшибки - "У вас более одного кредитора"
                result = getResourceString(Localization.USER_HAS_MORE_THAN_ONE_CREDITOR_MESSAGE.getKey());
            } else {
                creditor = creditors.get(0);
                Date reportDate = reportBusiness.getReportDate(creditor.getId());
                Report report = reportBusiness.getByCreditor_ReportDate(creditor, reportDate);
                if (report != null && ReportStatus.ORGANIZATION_APPROVED.code().equals(report.getStatus().getCode())) {
                    result = getResourceString(Localization.ORGANIZATION_APPROVED_DATA_MESSAGE.getKey());
                }
            }
        }
        return result;
    }
}
