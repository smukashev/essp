package kz.bsbnb.usci.portlets.signing;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.portlets.signing.data.BeanDataProvider;
import kz.bsbnb.usci.portlets.signing.data.DataProvider;
import kz.bsbnb.usci.portlets.signing.data.FileSignatureRecord;
import kz.bsbnb.usci.portlets.signing.kisc.SignatureChecker;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
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

    public static final Logger log = Logger.getLogger(SigningPortlet.class.getCanonicalName());

    private String editJSP;
    private String helpJSP;
    private String viewJSP;

    @Override
    public void init() throws PortletException {
        editJSP = getInitParameter("edit-jsp");
        helpJSP = getInitParameter("help-jsp");
        viewJSP = getInitParameter("view-jsp");
    }

    @Override
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

    @Override
    public void doEdit(
            RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {

        if (renderRequest.getPreferences() == null) {
            super.doEdit(renderRequest, renderResponse);
        } else {
            include(editJSP, renderRequest, renderResponse);
        }
    }

    @Override
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
            User user = PortalUtil.getUser(renderRequest);
            renderRequest.setAttribute("UserId", user.getUserId());
            DataProvider provider = new BeanDataProvider();
            renderRequest.setAttribute("PortalUrl", provider.getBaseUrl());
            renderRequest.setAttribute("ContextPath", renderRequest.getContextPath());
            List<Creditor> userCreditors = provider.getCreditorsList(user.getUserId());
            renderRequest.setAttribute("hasAccess", userCreditors.size() == 1);
            if (userCreditors.size() == 1) {
                List<FileSignatureRecord> filesToSign = provider.getFilesToSign(userCreditors.get(0).getId());
                renderRequest.setAttribute("noFilesToSign", filesToSign.isEmpty());
                renderRequest.setAttribute("inputFiles", filesToSign);
                renderRequest.setAttribute("actionUrl", renderResponse.createActionURL());
            }
        } catch (PortalException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (SystemException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        include(viewJSP, renderRequest, renderResponse);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        try {
            DataProvider provider = new BeanDataProvider();
            User user = PortalUtil.getUser(request);
            Creditor creditor = provider.getCreditorsList(user.getUserId()).get(0);
            List<FileSignatureRecord> filesList = provider.getFilesToSign(creditor.getId());
            Map<BigInteger, FileSignatureRecord> filesById = new HashMap<BigInteger, FileSignatureRecord>();
            for (FileSignatureRecord fileSignatureRecord : filesList) {
                filesById.put(fileSignatureRecord.getInputFileId(), fileSignatureRecord);
            }

            Map<String, String[]> parameterMap = request.getParameterMap();

            if (parameterMap.containsKey("saveSignatures")) {
                if (parameterMap.containsKey("certificateInfo")) {
                    handleSignedFiles(parameterMap, request, provider, creditor, user, filesById);
                    request.setAttribute("noCertificate", false);
                } else {
                    request.setAttribute("noCertificate", true);
                }
            }
            if (parameterMap.containsKey("cancel")) {
                cancelSelectedFiles(parameterMap, request, provider, filesById);
            }

        } catch (Exception e) {
            String originalError = e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;").replace("\n","") : e.getClass().getName();
            if(originalError.contains("connect") || originalError.contains("rmi"))
                if(!retry) {
                    retry = true;
                    try {
                        init();
                        //serveResource(resourceRequest, resourceResponse);
                        processAction(request, response);
                    } catch (PortletException e1) {
                        //resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
                        /*out.write(("{ \"success\": false, \"errorMessage\": \""+ originalError + e1.getMessage()
                                .replaceAll("\"","").replaceAll("\n","")+"\"}").getBytes());*/
                    } finally {
                        retry = false;
                        return;
                    }
                }

           // out.write(("{\"success\": false, \"errorMessage\": \"" + originalError + "\"}").getBytes());


        }
    }

    boolean retry = false;

    private void handleSignedFiles(Map<String, String[]> parameterMap, ActionRequest request, DataProvider provider, Creditor creditor, User user, Map<BigInteger, FileSignatureRecord> filesById) {
        String certificate = parameterMap.get("certificateInfo")[0];
        request.setAttribute("certificate", new String[]{certificate});
        request.setAttribute("certificateSuccess", true);
        List<FileSignatureRecord> signedFiles = new ArrayList<FileSignatureRecord>();
        StringBuilder signatureErrors = new StringBuilder();
        SigningPortletEnvironment env = new SigningPortletEnvironment(user.getLocale());
        SignatureChecker checker = new SignatureChecker(provider.getCreditorsBinNumber(creditor), provider.getOcspServiceUrl());
        for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            try {
                FileSignatureRecord record = getSignedFile(entry, filesById, parameterMap);
                if (record == null) {
                    continue;
                }
                checker.checkAndUpdate(record, entry.getValue()[0]);
                //provider.addInputFileToQueue(record);
                signedFiles.add(record);
            } catch (SignatureValidationException sve) {
                signatureErrors.append("<br />");
                String messageTemplate = env.getResourceString(sve.getCode());
                String message = String.format(messageTemplate, sve.getArguments());
                signatureErrors.append(message);
            }
        }
        request.setAttribute("processedFiles", signedFiles);
        request.setAttribute("hasSuccessfullySignedFiles", !signedFiles.isEmpty());
        request.setAttribute("signedFilenames", filesToString(signedFiles));
        request.setAttribute("hasFilesWithErrors", signatureErrors.length() > 0);
        request.setAttribute("filesWithErrors", signatureErrors.toString());
        request.setAttribute("hasInfoOnProcessedFiles", true);
    }

    private void cancelSelectedFiles(Map<String, String[]> parameterMap, ActionRequest request, DataProvider provider, Map<BigInteger, FileSignatureRecord> filesById) {
        int canceledFilesCount = 0;
        for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("check")) {
                continue;
            }
            BigInteger fileId = new BigInteger(key.substring(5));
            FileSignatureRecord file = filesById.get(fileId);
            provider.cancelFile(file);
            canceledFilesCount++;
        }
        request.setAttribute("hasCanceledFiles", true);
        request.setAttribute("canceledFilesCount", canceledFilesCount);
    }

    private FileSignatureRecord getSignedFile(Entry<String, String[]> entry, Map<BigInteger, FileSignatureRecord> filesById, Map<String, String[]> parameterMap) throws SignatureValidationException {
        String key = entry.getKey();
        if ((key == null || !key.startsWith("sign")) || entry.getValue().length <= 0) {
            return null;
        }
        String signId = key.substring(4);
        String checkId = "check" + signId;
        if (!parameterMap.containsKey(checkId)) {
            return null;
        }
        return filesById.get(new BigInteger(signId));
    }

    private String filesToString(List<FileSignatureRecord> signedFiles) {
        StringBuilder processedFileNames = new StringBuilder();
        for (FileSignatureRecord processedFile : signedFiles) {
            processedFileNames.append("<br />");
            processedFileNames.append(processedFile.getFilename());
        }
        return processedFileNames.toString();
    }

    protected void include(
            String path, RenderRequest renderRequest,
            RenderResponse renderResponse)
            throws IOException, PortletException {

        PortletRequestDispatcher portletRequestDispatcher
                = getPortletContext().getRequestDispatcher(path);

        if (portletRequestDispatcher == null) {
            log.log(Level.SEVERE, "{0} is not a valid include", path);
        } else {
            portletRequestDispatcher.include(renderRequest, renderResponse);
        }
    }
}
