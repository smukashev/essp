package kz.bsbnb.usci.portlets.signing;

import kz.bsbnb.usci.portlets.signing.data.BeanDataProvider;
import kz.bsbnb.usci.portlets.signing.data.DataProvider;
import kz.bsbnb.usci.portlets.signing.data.FileSignatureRecord;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import kz.bsbnb.usci.cr.model.Creditor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class SigningPortlet extends GenericPortlet {

    public static Logger log = Logger.getLogger(SigningPortlet.class.getCanonicalName());

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
        try {
            log.log(Level.INFO, "View");
            User user = PortalUtil.getUser(renderRequest);
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
        } catch (PortalException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (SystemException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        include(viewJSP, renderRequest, renderResponse);
    }

    @Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
            throws IOException, PortletException {
        String command = actionRequest.getParameter("COMMAND");
        log.log(Level.INFO, "Command: {0}", command);

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
                log.log(Level.INFO, "Entry key: {0}", entry.getKey());
                log.log(Level.INFO, "Entry value: {0}", Arrays.toString(entry.getValue()));
                String key = entry.getKey();
                if (key != null && key.startsWith("sign") && entry.getValue().length > 0) {
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
            log.log(Level.INFO, "Processed file names: {0}", processedFileNames.toString());
            actionRequest.setAttribute("processedFilenames", new String[]{processedFileNames.toString()});
            actionRequest.setAttribute("hasInfoOnProcessedFiles", true);

            actionRequest.setAttribute("noCertificate", false);
        } else {
            actionRequest.setAttribute("noCertificate", true);
        }

    }

    protected void include(
            String path, RenderRequest renderRequest,
            RenderResponse renderResponse)
            throws IOException, PortletException {

        PortletRequestDispatcher portletRequestDispatcher =
                getPortletContext().getRequestDispatcher(path);

        if (portletRequestDispatcher == null) {
            log.log(Level.SEVERE, "{0} is not a valid include", path);
        } else {
            portletRequestDispatcher.include(renderRequest, renderResponse);
        }
    }
    protected String editJSP;
    protected String helpJSP;
    protected String viewJSP;
}