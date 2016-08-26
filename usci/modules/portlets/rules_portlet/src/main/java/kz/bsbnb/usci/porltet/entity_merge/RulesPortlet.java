package kz.bsbnb.usci.porltet.entity_merge;

import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.core.service.IPackageService;
import kz.bsbnb.usci.core.service.IRuleService;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.rule.PackageVersion;
import kz.bsbnb.usci.eav.rule.Rule;
import kz.bsbnb.usci.eav.rule.RulePackage;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.porltet.entity_merge.model.json.JsonMaker;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RulesPortlet extends MVCPortlet{
    private IRuleService ruleService;
    private IPackageService batchService;
    private IEntityService entityService;
    private boolean retry;
    private static final Logger logger = Logger.getLogger(RulesPortlet.class);

    public void connectToServices() throws PortletException {
        try {
            //ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextPortlet.xml");

            RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IEntityService.class);
            entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityServiceFactoryBean.afterPropertiesSet();
            entityService = (IEntityService) entityServiceFactoryBean.getObject();


            RmiProxyFactoryBean ruleServiceFactoryBean = new RmiProxyFactoryBean();
            ruleServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/ruleService");
            ruleServiceFactoryBean.setServiceInterface(IRuleService.class);

            ruleServiceFactoryBean.afterPropertiesSet();
            ruleService = (IRuleService) ruleServiceFactoryBean.getObject();

            RmiProxyFactoryBean batchServiceFactoryBean = new RmiProxyFactoryBean();
            batchServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/packageService");
            batchServiceFactoryBean.setServiceInterface(IPackageService.class);

            batchServiceFactoryBean.afterPropertiesSet();

            batchService = (IPackageService) batchServiceFactoryBean.getObject();

        } catch (Exception e) {
            throw new RuntimeException(Errors.compose(Errors.E286));
        }
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
        //renderRequest.setAttribute("entityList", baseEntityList);

        try {
            boolean hasRights = false;
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if(user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("NationalBankEmployee")
                            || role.getName().equals("BankUser"))
                        hasRights = true;
                }
            }

            if (!hasRights)
                throw new AccessControlException(Errors.compose(Errors.E238));

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            renderRequest.setAttribute("error", "Нет прав для просмотра");
        }

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        PACKAGE_ALL,
        PACKAGE_VERSIONS,
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
        RULE_SWITCH,
        NEW_PACKAGE_VERSION,

        LIST_ALL,
        LIST_CLASS,
        SAVE_CLASS,
        DEL_CLASS,
        SAVE_ATTR,
        GET_ATTR,
        DEL_ATTR,
        NEW_RULE_HISTORY, RULE_HISTORY;
    }

    public void getWriteAccess(ResourceRequest resourceRequest){
        boolean writeAccessGranted = false;
        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(resourceRequest));
            if (user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("NationalBankEmployee"))
                        writeAccessGranted = true;
                }
            }
        } catch (Exception e) {}

        if(!writeAccessGranted){
            logger.error(Errors.getError(Errors.E238));
            throw new RuntimeException(Errors.compose(Errors.E238));
        }

    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException {

        PrintWriter writer = resourceResponse.getWriter();

        try {

            if(batchService == null ||ruleService ==null || entityService == null)
                connectToServices();

            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));
            long ruleId, batchVersionId, batchId;
            String title;
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            long baseEntityId;

            if(resourceRequest.getParameterMap().containsKey("fail"))
               throw new RuntimeException(Errors.compose(Errors.E258));

            switch(operationType){
                case PACKAGE_ALL:
                    List<RulePackage> allPackages = batchService.getAllPackages();
                    Iterator<RulePackage> iterator = allPackages.iterator();

                    if(resourceRequest.getParameter("dev") == null || !resourceRequest.getParameter("dev").equals("true")) {
                        while (iterator.hasNext()) {
                            if (iterator.next().getName().startsWith("dev"))
                                iterator.remove();
                        }
                    }
                    writer.write(JsonMaker.getJson(allPackages));
                    break;
                case PACKAGE_VERSIONS:
                    batchId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    //writer.write(JsonMaker.getJson(batchService.getBatchVersions(batchId)));
                    RulePackage r = new RulePackage();
                    r.setId(batchId);
                    writer.write(JsonMaker.getJson(ruleService.getPackageVersions(r)));
                    break;
                case GET_RULE_TITLES:
                    long packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    Date date =  df.parse(resourceRequest.getParameter("date"));
                    String searchText = resourceRequest.getParameter("searchText");
                    if(searchText != null && searchText.length() > 0)
                        writer.write(JsonMaker.getJson(ruleService.getRuleTitles(packageId, date, searchText)));
                    else
                        writer.write(JsonMaker.getJson(ruleService.getRuleTitles(packageId,date)));
                    break;
                case GET_RULE:
                    date = df.parse(resourceRequest.getParameter("date"));
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    Rule rule = new Rule();
                    rule.setId(ruleId);
                    rule.setOpenDate(date);
                    writer.write(JsonMaker.getJson(ruleService.getRule(rule)));
                    break;
                case UPDATE_RULE:
                    getWriteAccess(resourceRequest);
                    String ruleBody = resourceRequest.getParameter("ruleBody");
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    String pkgName = resourceRequest.getParameter("pkgName");
                    packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    date =  df.parse(resourceRequest.getParameter("date"));
                    PackageVersion packageVersion = new PackageVersion(new RulePackage(packageId, pkgName), date);
                    rule = new Rule("", ruleBody, date);
                    rule.setId(ruleId);
                    String errors = ruleService.getPackageErrorsOnRuleUpdate(rule, packageVersion);
                    if(errors != null)
                        throw new RuntimeException(errors);
                    ruleService.updateBody(rule);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case DEL_RULE:
                    getWriteAccess(resourceRequest);
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    pkgName = resourceRequest.getParameter("pkgName");
                    packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    //long packageVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    date = df.parse(resourceRequest.getParameter("date"));
                    errors = ruleService.getPackageErrorsOnRuleDelete(new Rule(ruleId, date));
                    if(errors != null)
                        throw new RuntimeException(errors);
                    ruleService.deleteRule(ruleId, new RulePackage(packageId, pkgName));
                    writer.write(JsonMaker.getJson(true));
                    //writer.write(JsonMaker.getNegativeJson("Настройки отключены для удаления"));
                    break;
                case NEW_RULE:
                    getWriteAccess(resourceRequest);
                    packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    pkgName = resourceRequest.getParameter("pkgName");
                    title = resourceRequest.getParameter("title");
                    date = (Date) DataTypes.getCastObject(DataTypes.DATE, resourceRequest.getParameter("date"));
                    ruleBody = resourceRequest.getParameter("ruleBody");
                    errors = ruleService.getPackageErrorsOnRuleInsert(
                            new PackageVersion(new RulePackage(packageId, pkgName), date),
                            title,
                            ruleBody);
                    if(errors != null)
                        throw new RuntimeException(errors);
                    writer.write(JsonMaker.getJson(ruleService.createNewRuleInPackage(
                            new Rule(title, ruleBody),
                            new PackageVersion(new RulePackage(packageId, pkgName), date))
                    ));
                    break;
                case COPY_EXISTING_RULE:
                    getWriteAccess(resourceRequest);
                    batchVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    ruleService.copyExistingRule(ruleId, batchVersionId);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case COPY_RULE:
                    getWriteAccess(resourceRequest);
                    batchVersionId = Long.parseLong(resourceRequest.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    title = resourceRequest.getParameter("title");
                    ruleId = ruleService.createCopy(ruleId, title, batchVersionId);
                    writer.write(JsonMaker.getJson(ruleId));
                    break;
                case RUN_RULE:
                    getWriteAccess(resourceRequest);
                    String batchName = resourceRequest.getParameter("batchName");
                    date = df.parse(resourceRequest.getParameter("date"));
                    baseEntityId = Long.parseLong(resourceRequest.getParameter("baseEntityId"));
                    BaseEntity be = (BaseEntity) entityService.load(baseEntityId);
                    ruleService.runRules(be,batchName,date);
                    writer.write(JsonMaker.getJson(be.getValidationErrors()));
                    break;
                case FLUSH:
                    getWriteAccess(resourceRequest);
                    ruleService.reloadCache();
                    break;
                case RENAME_RULE:
                    getWriteAccess(resourceRequest);
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    title = resourceRequest.getParameter("title");
                    ruleService.renameRule(ruleId, title);
                    break;
                case RULE_SWITCH:
                    getWriteAccess(resourceRequest);
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    pkgName = resourceRequest.getParameter("pkgName");
                    date = df.parse(resourceRequest.getParameter("date"));
                    boolean makeActive = resourceRequest.getParameter("newValue").equals("true");
                    boolean ruleEdited = resourceRequest.getParameter("ruleEdited").equals("true");
                    String error = null;
                    ruleBody = resourceRequest.getParameter("ruleBody");

                    boolean success = false;

                    if(makeActive) {
                        error = ruleService.getPackageErrorsOnRuleActivate(ruleBody, ruleId, pkgName, date, ruleEdited);
                        if(error == null) {
                            if(ruleEdited)
                                success |= ruleService.activateRule(ruleBody, ruleId);
                            else
                                success |= ruleService.activateRule(ruleId);
                        }
                    } else {
                        error = ruleService.getPackageErrorsOnRuleDisable(ruleId, pkgName, date);
                        if(error == null)
                            success |= ruleService.disableRule(ruleId);
                    }

                    if(error != null) {
                        //writer.write("{ \"success\": false, \"errorMessage\": \""+ error.replaceAll("\n", "")+"\"}");
                        writer.write(JsonMaker.getNegativeJson(error));
                    } else {
                        if(!success)
                            writer.write(JsonMaker.getNegativeJson("Ошибка при обновлении в базе"));
                        else
                            writer.write(JsonMaker.getJson(true));
                    }
                    break;
                case NEW_PACKAGE_VERSION:
                    getWriteAccess(resourceRequest);
                    packageId = Long.parseLong(resourceRequest.getParameter("packageId"));
                    date = (Date) DataTypes.getCastObject(DataTypes.DATE, resourceRequest.getParameter("date"));
                    ruleService.insertBatchVersion(packageId, date);
                    break;
                case NEW_RULE_HISTORY:
                    getWriteAccess(resourceRequest);
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    date = (Date) DataTypes.getCastObject(DataTypes.DATE, resourceRequest.getParameter("date"));
                    ruleBody = resourceRequest.getParameter("ruleBody");
                    rule = new Rule("", ruleBody, date);
                    rule.setId(ruleId);
                    ruleService.insertHistory(rule);
                    break;
                case RULE_HISTORY:
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    writer.write(JsonMaker.getJson(ruleService.getRuleHistory(ruleId)));
                    break;
                default:
                    logger.error(Errors.compose(Errors.E118, operationType));
                    throw new UnsupportedOperationException(Errors.compose(Errors.E118, operationType));
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

    private String castJsonString(Exception e) {
        return e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;")
                        .replaceAll("\n","").replaceAll("\t"," ") : e.getClass().getName();
    }
}


