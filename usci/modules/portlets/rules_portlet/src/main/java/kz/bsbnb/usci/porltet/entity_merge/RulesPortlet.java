package kz.bsbnb.usci.porltet.entity_merge;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.brms.rulesvr.service.IBatchService;
import kz.bsbnb.usci.brms.rulesvr.service.IRuleService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.porltet.entity_merge.model.json.JsonMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RulesPortlet extends MVCPortlet{
    private RmiProxyFactoryBean ruleServiceFactoryBean;
    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;

    private IRuleService ruleService;
    private IBatchService batchService;
    private IEntityService entityService;
    private boolean retry;

    @Override
    public void init() throws PortletException {

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextPortlet.xml");

        entityServiceFactoryBean = new RmiProxyFactoryBean();
        entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
        entityServiceFactoryBean.setServiceInterface(IEntityService.class);
        entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

        entityServiceFactoryBean.afterPropertiesSet();
        entityService = (IEntityService) entityServiceFactoryBean.getObject();


        ruleServiceFactoryBean = new RmiProxyFactoryBean();
        ruleServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/ruleService");
        ruleServiceFactoryBean.setServiceInterface(IRuleService.class);

        ruleServiceFactoryBean.afterPropertiesSet();
        ruleService = (IRuleService) ruleServiceFactoryBean.getObject();

        batchServiceFactoryBean = new RmiProxyFactoryBean();
        batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchService");
        batchServiceFactoryBean.setServiceInterface(IBatchService.class);

        batchServiceFactoryBean.afterPropertiesSet();

        batchService = (IBatchService) batchServiceFactoryBean.getObject();
        super.init();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
        //renderRequest.setAttribute("entityList", baseEntityList);

        boolean isAdmin = false;

        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if(user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getDescriptiveName().equals("Administrator"))
                        isAdmin = true;
                }
            }
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }

        if(!isAdmin)
            return;

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        PACKAGE_ALL,
        GET_RULE_TITLES,
        GET_RULE,
        UPDATE_RULE,
        DEL_RULE,
        NEW_RULE,
        COPY_EXISTING_RULE,
        COPY_RULE,
        RUN_RULE,
        FLUSH,
        RENAME_RULE,

        LIST_ALL,
        LIST_CLASS,
        SAVE_CLASS,
        DEL_CLASS,
        SAVE_ATTR,
        GET_ATTR,
        DEL_ATTR
    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException {

        PrintWriter writer = resourceResponse.getWriter();

        try {
            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));
            long ruleId, batchVersionId;
            String title;
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            long baseEntityId;

            if(resourceRequest.getParameterMap().containsKey("fail"))
               throw new RuntimeException("some error Message");

            switch(operationType){
                case PACKAGE_ALL:
                       writer.write(JsonMaker.getJson(batchService.getAllBatches()));
                    break;
                case GET_RULE_TITLES:
                    long packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    Date date =  df.parse(resourceRequest.getParameter("date"));
                    writer.write(JsonMaker.getJson(ruleService.getRuleTitles(packageId,date)));
                    break;
                case GET_RULE:
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    writer.write(JsonMaker.getJson(ruleService.getRule(ruleId)));
                    break;
                case UPDATE_RULE:
                    String ruleBody = resourceRequest.getParameter("ruleBody");
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    String pkgName = resourceRequest.getParameter("pkgName");
                    date =  df.parse(resourceRequest.getParameter("date"));
                    String errors = ruleService.getRuleErrorsInPackage(ruleBody, ruleId, pkgName, date);
                    if(errors != null)
                        throw new RuntimeException(errors);
                    ruleService.updateBody(ruleId, ruleBody);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case DEL_RULE:
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    long packageVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    ruleService.deleteRule(ruleId, packageVersionId);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case NEW_RULE:
                    batchVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    title = resourceRequest.getParameter("title");
                    writer.write(JsonMaker.getJson(ruleService.saveEmptyRule(title, batchVersionId)));
                    break;
                case COPY_EXISTING_RULE:
                    batchVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    ruleService.copyExistingRule(ruleId, batchVersionId);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case COPY_RULE:
                    batchVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    title = resourceRequest.getParameter("title");
                    ruleId = ruleService.createCopy(ruleId, title, batchVersionId);
                    writer.write(JsonMaker.getJson(ruleId));
                    break;
                case RUN_RULE:
                    String batchName = resourceRequest.getParameter("batchName");
                    date = df.parse(resourceRequest.getParameter("date"));
                    baseEntityId = Long.parseLong(resourceRequest.getParameter("baseEntityId"));
                    BaseEntity be = (BaseEntity) entityService.load(baseEntityId);
                    ruleService.runRules(be,batchName,date);
                    writer.write(JsonMaker.getJson(be.getValidationErrors()));
                    break;
                case FLUSH:
                    ruleService.reloadCache();
                    break;
                case RENAME_RULE:
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    title = resourceRequest.getParameter("title");
                    ruleService.renameRule(ruleId, title);
                    break;
            }

        } catch (Exception e) {

            if(!retry) {
                retry = true;
                try {
                    init();
                    serveResource(resourceRequest, resourceResponse);
                } catch (PortletException e1) {
                    //resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
                    writer.write("{ \"success\": false, \"errorMessage\": \""+ e1.getMessage()
                            .replaceAll("\"","").replaceAll("\n","")+"\"}");
                } finally {
                    retry = false;
                    return;
                }
            }

            //resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            writer.write("{ \"success\": false, \"errorMessage\": \""+ e.getMessage().replaceAll("\"","").replaceAll("\n","")+"\"}");
        }

    }
}


