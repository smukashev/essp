package kz.bsbnb.usci.porltet.entity_merge;

import com.google.gson.Gson;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.receiver.service.IBatchProcessService;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainPortlet extends MVCPortlet {
    private RmiProxyFactoryBean batchEntryServiceFactoryBean;
    private RmiProxyFactoryBean batchProcessServiceFactoryBean;

    private IBatchProcessService batchProcessService;

    private final static String TMP_FILE_DIR = System.getProperty("user.home") + "/Batches";

    private IBatchEntryService batchEntryService;

    public void connectToServices() {
        try {
            batchEntryServiceFactoryBean = new RmiProxyFactoryBean();
            batchEntryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
            batchEntryServiceFactoryBean.setServiceInterface(IBatchEntryService.class);
            batchEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchEntryServiceFactoryBean.afterPropertiesSet();
            batchEntryService = (IBatchEntryService) batchEntryServiceFactoryBean.getObject();

            batchProcessServiceFactoryBean = new RmiProxyFactoryBean();
            batchProcessServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchProcessService");
            batchProcessServiceFactoryBean.setServiceInterface(IBatchProcessService.class);
            batchProcessServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchProcessServiceFactoryBean.afterPropertiesSet();
            batchProcessService = (IBatchProcessService) batchProcessServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can\"t initialise services: " + e.getMessage());
        }
    }

    @Override
    public void init() throws PortletException {
        connectToServices();

        super.init();
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {

        HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(
                PortalUtil.getHttpServletRequest(renderRequest));

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
            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));

            User currentUser = PortalUtil.getUser(resourceRequest);

            if (currentUser == null) {
                writer.write("{\"success\": false, \"errorMessage\": \"Not logged in\"}");
                return;
            }

            Gson gson = new Gson();

            switch (operationType) {
                case LIST_ENTRIES:
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
                        writer.write("\"u_date\":\"" + batchEntry.getUpdateDate() + "\"");
                        writer.write("}");
                    }

                    writer.write("]}");

                    break;
                case SEND_XML:
                    entries = batchEntryService.getListByUser(currentUser.getUserId());

                    String xml =
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                    "<batch>\n" +
                                    "<entities>\n";

                    for (BatchEntry batchEntry : entries) {
                        xml += batchEntry.getValue() + "\n";
                    }

                    xml += "\n</entities>\n" +
                            "</batch>";

                    File f = File.createTempFile("tmp", ".zip", new File(TMP_FILE_DIR));

                    // TODO: fix report date

                    String manifest = "<manifest>\n" +
                            "\t<type>1</type>\n" +
                            "\t<name>data.xml</name>\n" +
                            // "\t<userid>" + currentUser.getUserId() + "</userid>\n" +
                            "\t<userid>100500</userid>\n" +
                            "\t<size>10</size>\n" +
                            "\t<date>01.04.2015</date>\n" +
                            "</manifest>";

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

                    batchProcessService.processBatch(f.getPath(), currentUser.getUserId());

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
            e.printStackTrace();
            writer.write("{\"success\": false, \"errorMessage\": \"" + e.getMessage() + "\"}");
        }
    }
}
