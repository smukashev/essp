package kz.bsbnb.usci.porltet.batch_entry;

import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainPortlet extends MVCPortlet {
    private IBatchProcessService batchProcessService;
    private PortalUserBeanRemoteBusiness portalUserBusiness;

    //должен быть отличен от C:/zips (т.е папки receiver-а)
    private final static String TMP_FILE_DIR = "\\\\" + StaticRouter.getAsIP() + "\\batch_entry_list_temp_folder";

    private IBatchEntryService batchEntryService;
    private Logger logger = Logger.getLogger(MainPortlet.class);
    private Exception currentException;

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
        DELETE_ENTRY
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

            switch (operationType) {
                case LIST_ENTRIES:
                    DateFormat dfRep = new SimpleDateFormat("dd.MM.yyyy");
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

                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                    Document document = null;
                    DocumentBuilder documentBuilder = null;
                    List<Long> batchEntryIds = new ArrayList<Long>();

                    for (BatchEntry batchEntry : entries) {
                        String sRepDate = df.format(batchEntry.getRepDate());

                        String xml =
                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                        "<batch>\n" +
                                        "<entities>\n";

                        xml += batchEntry.getValue() + "\n";

                        xml += "\n</entities>\n" +
                                "</batch>";
                        List<Creditor> creditors = portalUserBusiness.getMainCreditorsInAlphabeticalOrder(currentUser.getUserId());
                        Creditor creditor = creditors.get(0);
                        String creditorName = creditor.getName();
                        String creditorCode = creditor.getCode();
                        String creditorBIN = creditor.getBIN();
                        String creditorBIK = creditor.getBIK();
                        String manifest = "<manifest>\n" +
                                "\t<type>1</type>\n" +
                                "\t<name>data.xml</name>\n" +
                                "\t<userid>" + currentUser.getUserId() + "</userid>\n" +
                                "\t<size>10</size>\n" +
                                "\t<date>" +
                                sRepDate +
                                "</date>\n" +
                                "\t<properties>"+
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
                default:
                    break;
            }
        } catch (Exception e) {
            currentException = null;
            logger.error(e.getMessage(),e);
            String originalError = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            originalError = Errors.decompose(originalError);
            writer.write("{\"success\": false, \"errorMessage\": \"" + originalError + "\"}");
        }
    }
}
