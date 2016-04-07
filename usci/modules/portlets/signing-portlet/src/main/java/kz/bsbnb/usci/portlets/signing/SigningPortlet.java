package kz.bsbnb.usci.portlets.signing;

import com.liferay.portal.model.Role;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.portlets.signing.data.BeanDataProvider;
import kz.bsbnb.usci.portlets.signing.data.DataProvider;
import kz.bsbnb.usci.portlets.signing.data.FileSignatureRecord;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import kz.bsbnb.usci.cr.model.Creditor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

    public class SigningPortlet extends GenericPortlet {

    private final Logger logger = Logger.getLogger(SigningPortlet.class);

    public void init() throws PortletException {
        editJSP = getInitParameter("edit-jsp");
        helpJSP = getInitParameter("help-jsp");
        viewJSP = getInitParameter("view-jsp");
    }

    public void doDispatch(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        String jspPage = renderRequest.getParameter("jspPage");

        if (jspPage != null) {
            include(jspPage, renderRequest, renderResponse);
        } else {
            super.doDispatch(renderRequest, renderResponse);
        }
    }

    public void doEdit(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        if (renderRequest.getPreferences() == null) {
            super.doEdit(renderRequest, renderResponse);
        } else {
            include(editJSP, renderRequest, renderResponse);
        }
    }

    public void doHelp(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        include(helpJSP, renderRequest, renderResponse);
    }

    @Override
    public void doView(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {
        logger.info("View");

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

            if (hasRights) {
                renderRequest.setAttribute("UserId", user.getUserId());
                DataProvider provider = new BeanDataProvider();
                renderRequest.setAttribute("PortalUrl", provider.getBaseUrl());
                renderRequest.setAttribute("ContextPath", renderRequest.getContextPath());
                List<Creditor> userCreditors = provider.getCreditorsList(user.getUserId());
                renderRequest.setAttribute("hasAccess", userCreditors.size() == 1);
                List<FileSignatureRecord> filesToSign = provider.getFilesToSign(user.getUserId());
                renderRequest.setAttribute("noFilesToSign", filesToSign.isEmpty());
                renderRequest.setAttribute("inputFiles", filesToSign);
                renderRequest.setAttribute("actionUrl", renderResponse.createActionURL());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            String errorMessage = e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;").replace("\n","") : e.getClass().getName();
            errorMessage = Errors.decompose(errorMessage);
            renderRequest.setAttribute("errorMessage", errorMessage);
        }

        include(viewJSP, renderRequest, renderResponse);
    }

    @Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
            throws IOException, PortletException {

        try {
            String command = actionRequest.getParameter("COMMAND");
            logger.info("Command: " + command);

            DataProvider provider = new BeanDataProvider();
            long userId = PortalUtil.getUserId(actionRequest);
            List<FileSignatureRecord> filesList = provider.getFilesToSign(userId);
            Map<BigInteger, FileSignatureRecord> filesById = new HashMap<BigInteger, FileSignatureRecord>();
            for (FileSignatureRecord fileSignatureRecord : filesList) {
                filesById.put(fileSignatureRecord.getInputFileId(), fileSignatureRecord);
            }

            Map<String, String[]> parameterMap = actionRequest.getParameterMap();

            if (parameterMap.containsKey("certificateInfo")) {
                String certificate = parameterMap.get("certificateInfo")[0];
                actionRequest.setAttribute("certificate", new String[]{certificate});

                actionRequest.setAttribute("certificateSuccess", true);
                List<FileSignatureRecord> processedFiles = new ArrayList<FileSignatureRecord>();
                for (Entry<String, String[]> entry : parameterMap.entrySet()) {
                    logger.info("Entry key: " + entry.getKey());
                    logger.info("Entry value: " + Arrays.toString(entry.getValue()));
                    String key = entry.getKey();
                    if (key != null && key.startsWith("sign") && entry.getValue().length > 0
                            && StringUtils.isNotEmpty(entry.getValue()[0])) {

                        BigInteger signId = new BigInteger(key.substring(4));
                        FileSignatureRecord record = filesById.get(signId);
                        if (record != null) {
                            record.setSignature(entry.getValue()[0]);
                            provider.addInputFileToQueue(record);
                            processedFiles.add(record);
                        }
                    }
                }
                actionRequest.setAttribute("processedFiles", processedFiles);
                StringBuilder processedFileNames = new StringBuilder();
                for (FileSignatureRecord processedFile : processedFiles) {
                    processedFileNames.append("<br />");
                    processedFileNames.append(processedFile.getFilename());

                    provider.signFile(Long.parseLong(processedFile.getId()), processedFile.getSignature());
                }
                logger.info("Processed file names: " + processedFileNames.toString());
                actionRequest.setAttribute("processedFilenames", new String[]{processedFileNames.toString()});
                actionRequest.setAttribute("hasInfoOnProcessedFiles", true);

                actionRequest.setAttribute("noCertificate", false);
            } else {
                actionRequest.setAttribute("noCertificate", true);
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            String errorMessage = e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;").replace("\n","") : e.getClass().getName();
            errorMessage = Errors.decompose(errorMessage);
            actionRequest.setAttribute("errorMessage", errorMessage);
        }
    }

    protected void include(
            String path, RenderRequest renderRequest,
            RenderResponse renderResponse)
            throws IOException, PortletException {

        PortletRequestDispatcher portletRequestDispatcher =
                getPortletContext().getRequestDispatcher(path);

        if (portletRequestDispatcher == null) {
            logger.error(path+" is not a valid include");
        } else {
            portletRequestDispatcher.include(renderRequest, renderResponse);
        }
    }
    protected String editJSP;
    protected String helpJSP;
    protected String viewJSP;
}