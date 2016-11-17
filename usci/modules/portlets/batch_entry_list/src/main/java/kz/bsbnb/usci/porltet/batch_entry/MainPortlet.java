package kz.bsbnb.usci.porltet.batch_entry;

import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.ReportBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.w3c.dom.Document;

import javax.portlet.*;
import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainPortlet extends MVCPortlet {
    private IBatchProcessService batchProcessService;
    private PortalUserBeanRemoteBusiness portalUserBusiness;

    //должен быть отличен от C:/zips (т.е папки receiver-а)
    //private final static String TMP_FILE_DIR = "\\\\" + StaticRouter.getAsIP() + "\\batch_entry_list_temp_folder";
    private final static String TMP_FILE_DIR = StaticRouter.isDevMode() ? "/home/usci_data/batch_entry_list_temp_folder" :
            "\\\\" + StaticRouter.getAsIP() + "\\download$\\batch_entry_list_temp_folder";

    private IBatchEntryService batchEntryService;
    private IEntityService entityService;
    private Logger logger = Logger.getLogger(MainPortlet.class);
    private Exception currentException;
    private boolean retry;
    private ReportBeanRemoteBusiness reportBusiness;

    public void connectToServices() {
        try {
            RmiProxyFactoryBean batchEntryServiceFactoryBean = new RmiProxyFactoryBean();
            batchEntryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/batchEntryService");
            batchEntryServiceFactoryBean.setServiceInterface(IBatchEntryService.class);
            batchEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchEntryServiceFactoryBean.afterPropertiesSet();
            batchEntryService = (IBatchEntryService) batchEntryServiceFactoryBean.getObject();

            RmiProxyFactoryBean batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1097/batchProcessService");
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();

            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

            // reportBeanRemoteBusiness
            RmiProxyFactoryBean reportBusinessFactoryBean = new RmiProxyFactoryBean();
            reportBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/reportBeanRemoteBusiness");
            reportBusinessFactoryBean.setServiceInterface(ReportBeanRemoteBusiness.class);
            reportBusinessFactoryBean.afterPropertiesSet();
            reportBusiness = (ReportBeanRemoteBusiness) reportBusinessFactoryBean.getObject();

            RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/entityService");
            entityServiceFactoryBean.setServiceInterface(IEntityService.class);
            entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityServiceFactoryBean.afterPropertiesSet();
            entityService = (IEntityService) entityServiceFactoryBean.getObject();
        } catch (Exception e) {
            throw new RuntimeException(Errors.getError(Errors.E286));
        }
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
        try {
            boolean hasRights = false;

            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if (user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                            || role.getName().equals("NationalBankEmployee"))
                        hasRights = true;
                }
            }

            if (!hasRights) {
                throw new AccessControlException(Errors.compose(Errors.E238));
            }

        } catch (Exception e) {
            currentException = e;
        }
        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_ENTRIES,
        SEND_XML,
        GET_ENTRY,
        GET_REPORT_DATE, DELETE_ENTRY
    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException
    {
        PrintWriter writer = resourceResponse.getWriter();

        try {

            if(currentException != null)
                throw currentException;

            if(batchEntryService == null || portalUserBusiness == null || batchProcessService == null){
                connectToServices();
            }

            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));

            boolean isNB = false;

            User currentUser = PortalUtil.getUser(resourceRequest);

            if(currentUser != null) {
                for (Role role : currentUser.getRoles()) {
                    if (role.getName().equals("NationalBankEmployee"))
                        isNB = true;
                }
            }

            if (currentUser == null) {
                writer.write("{\"success\": false, \"errorMessage\": \"Not logged in\"}");
                return;
            }

            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            switch (operationType) {
                case LIST_ENTRIES:
                    DateFormat dfRep = dateFormat;
                    DateFormat dfUpd = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                    List<BatchEntry> entries = batchEntryService.getListByUser(currentUser.getUserId());

                    writer.write("{\"total\":" + entries.size());
                    writer.write(",\"data\":[");

                    boolean first = true;

                    for (BatchEntry batchEntry : entries) {
                        if (first) {
                            first = false;
                        } else {
                            writer.write(",");
                        }

                        writer.write("{");

                        writer.write("\"id\":\"" + batchEntry.getId() + "\",");
                        writer.write("\"u_date\":\"" + dfUpd.format(batchEntry.getUpdateDate()) + "\",");
                        writer.write("\"rep_date\":\"" + dfRep.format(batchEntry.getRepDate()) + "\"");
                        writer.write("}");
                    }

                    writer.write("]}");

                    break;
                case SEND_XML:
                    entries = batchEntryService.getListByUser(currentUser.getUserId());

                    DateFormat df = dateFormat;
                    Document document = null;
                    DocumentBuilder documentBuilder = null;
                    List<Long> batchEntryIds = new ArrayList<Long>();
                    Creditor creditor = getCreditor(currentUser);
                    String creditorName = creditor.getName();
                    String creditorCode = creditor.getCode();
                    String creditorBIN = creditor.getBIN();
                    String creditorBIK = creditor.getBIK();

                    Date reportDate = reportBusiness.getReportDate(creditor.getId());

                    for (BatchEntry batchEntry : entries) {
                        String sRepDate = df.format(batchEntry.getRepDate());

                        String xml =
                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                        "<batch>\n" +
                                        "<entities>\n";

                        xml += batchEntry.getValue() + "\n";

                        xml += "\n</entities>\n" +
                                "</batch>";


                        String manifest = "<manifest>\n" +
                                "\t<type>1</type>\n" +
                                "\t<name>data.xml</name>\n" +
                                "\t<userid>" + currentUser.getUserId() + "</userid>\n" +
                                "\t<size>1</size>\n" +
                                "\t<date>" +
                                sRepDate +
                                "</date>\n";

                                Date prevReportDate = entityService.getPreviousReportDate(batchEntry.getEntityId(), batchEntry.getRepDate());

                                if ((batchEntry.getMaintenance() && prevReportDate != null
                                        || batchEntry.getRepDate().compareTo(reportDate) < 0) && !isNB) {
                                    manifest += "\t<maintenance>true</maintenance>\n";
                                }

                                manifest += "\t<properties>"+
                                    "\t<property>" +
                                        "\t<name>CODE</name>\n"+
                                        "\t<value>"+ creditorCode +"</value>\n"+
                                    "</property>\n"+
                                    "\t<property>" +
                                        "\t<name>NAME</name>\n"+
                                        "\t<value>"+ creditorName +"</value>\n"+
                                    "</property>\n"+
                                    "\t<property>" +
                                        "\t<name>BIN</name>\n"+
                                        "\t<value>"+ creditorBIN +"</value>\n"+
                                    "</property>\n"+
                                    "\t<property>" +
                                        "\t<name>BIK</name>\n"+
                                        "\t<value>"+ creditorBIK +"</value>\n"+
                                    "</property>\n"+
                                "</properties>\n"+
                                "</manifest>";

                        File f = File.createTempFile("tmp", ".zip", new File(TMP_FILE_DIR));

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ZipOutputStream zipfile = new ZipOutputStream(bos);

                        ZipEntry zipentry = new ZipEntry("data.xml");
                        zipfile.putNextEntry(zipentry);
                        zipfile.write(xml.getBytes());

                        zipentry = new ZipEntry("manifest.xml");
                        zipfile.putNextEntry(zipentry);
                        zipfile.write(manifest.getBytes());

                        zipfile.close();
                        byte[] zipBytes = bos.toByteArray();

                        FileOutputStream fileOutputStream = new FileOutputStream(f);

                        fileOutputStream.write(zipBytes);

                        fileOutputStream.close();

                        batchProcessService.processBatch(f.getPath(), currentUser.getUserId(), isNB);

                        batchEntryIds.add(batchEntry.getId());
                    }

                    writer.write("{ \"success\": true}");
                    batchEntryService.delete(batchEntryIds);

                    break;
                case GET_ENTRY: {
                    String id = resourceRequest.getParameter("id");
                    BatchEntry batchEntry = batchEntryService.load(Long.valueOf(id));
                    writer.write(batchEntry.getValue());
                    break;
                }
                case DELETE_ENTRY: {
                    String id = resourceRequest.getParameter("id");
                    batchEntryService.delete(Long.valueOf(id));
                    break;
                }
                case GET_REPORT_DATE: {
                    creditor = getCreditor(currentUser);
                    reportDate = reportBusiness.getReportDate(creditor.getId());
                    writer.write("{ \"success\": true, \"data\": \""
                            + dateFormat.format(reportDate)+ "\"}");
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            String originalError = castJsonString(e);
            if(originalError.contains("connect") || originalError.contains("rmi"))
                if(!retry) {
                    retry = true;
                    logger.info("connect failed, reconnect triggered");
                    try {
                        connectToServices();
                        serveResource(resourceRequest, resourceResponse);
                    } catch (Exception e1) {
                        logger.info("reconnect failed, seems services are down");
                        originalError = Errors.decompose(castJsonString(e1));
                        writer.write("{ \"success\": false, \"errorMessage\": \""+ originalError + "\"}");
                    } finally {
                        retry = false;
                        return;
                    }
                }

            originalError = Errors.decompose(originalError);
            writer.write("{ \"success\": false, \"errorMessage\": \""+ originalError +"\"}");
        }


    }

    private Creditor getCreditor(User currentUser) {
        List<Creditor> creditors = portalUserBusiness.getMainCreditorsInAlphabeticalOrder(currentUser.getUserId());

        if(creditors.size() < 1)
            throw new RuntimeException("Нет доступных кредиторов");

        return creditors.get(0);
    }

    private String castJsonString(Exception e) {
        return e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;")
                .replaceAll("\n","").replaceAll("\t"," ") : e.getClass().getName();
    }
}
