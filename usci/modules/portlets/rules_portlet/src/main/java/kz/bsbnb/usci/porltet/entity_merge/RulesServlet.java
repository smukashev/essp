package kz.bsbnb.usci.porltet.entity_merge;

import com.liferay.portal.kernel.json.JSON;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.brms.rulesvr.rulesingleton.RulesSingleton;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RulesServlet extends HttpServlet {
    private RmiProxyFactoryBean ruleServiceFactoryBean;
    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;

    //@Autowired
    //private RulesSingleton rulesSingleton;

    private IRuleService ruleService;
    private IBatchService batchService;
    private IEntityService entityService;
    boolean retry;



    public class Pair{
        int id;
        String name;

        public Pair(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }


    public class TempClass{
        public List<Pair> data;
        public TempClass(){
            data = new ArrayList<Pair> ();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        PrintWriter writer = resp.getWriter();

        try {
            OperationTypes operationType = OperationTypes.valueOf(req.getParameter("op"));
            long ruleId, batchVersionId;
            String title;
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            long baseEntityId;

            if(req.getParameterMap().containsKey("fail"))
                throw new RuntimeException("some error Message");

            switch(operationType){
                case PACKAGE_ALL:
                    writer.write(JsonMaker.getJson(batchService.getAllBatches()));
                    break;
                case GET_RULE_TITLES:
                    long packageId = Long.parseLong(req.getParameter("packageId"));
                    Date date =  df.parse(req.getParameter("date"));
                    writer.write(JsonMaker.getJson(ruleService.getRuleTitles(packageId,date)));
                    break;
                case GET_RULE :
                    ruleId = Long.parseLong(req.getParameter("ruleId"));
                    writer.write(JsonMaker.getJson(ruleService.getRule(ruleId)));
                    break;
                case UPDATE_RULE:
                    String ruleBody = req.getParameter("ruleBody");
                    ruleId = Long.parseLong(req.getParameter("ruleId"));

                    String errors = ruleService.getRuleErrors(ruleBody);
                    if(errors != null)
                        throw new RuntimeException(errors);

                    ruleService.updateBody(ruleId, ruleBody);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case DEL_RULE:
                    ruleId = Long.parseLong(req.getParameter("ruleId"));
                    long packageVersionId = Long.parseLong(req.getParameter("batchVersionId"));
                    ruleService.deleteRule(ruleId, packageVersionId);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case NEW_RULE:
                    batchVersionId = Long.parseLong(req.getParameter("batchVersionId"));
                    title = req.getParameter("title");
                    writer.write(JsonMaker.getJson(ruleService.saveEmptyRule(title, batchVersionId)));
                    break;
                case COPY_EXISTING_RULE:
                    batchVersionId = Long.parseLong(req.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(req.getParameter("ruleId"));
                    ruleService.copyExistingRule(ruleId, batchVersionId);
                    writer.write(JsonMaker.getJson(true));
                    break;
                case COPY_RULE:
                    batchVersionId = Long.parseLong(req.getParameter("batchVersionId"));
                    ruleId = Long.parseLong(req.getParameter("ruleId"));
                    title = req.getParameter("title");
                    ruleId = ruleService.createCopy(ruleId,title,batchVersionId);
                    writer.write(JsonMaker.getJson(ruleId));
                    break;
                case RUN_RULE:
                    //String batchName = req.getParameter("batchName");
                    //date =  df.parse(req.getParameter("date"));
                    //baseEntityId = Long.parseLong(req.getParameter("baseEntityId"));
                    //BaseEntity be = (BaseEntity) entityService.load(baseEntityId);
                    //rulesSingleton.runRules(be,batchName,date);
                    //ruleService.runRules(be,batchName,date);
                    //writer.write(JsonMaker.getJson(be.getValidationErrors()));
                    String rule = req.getParameter("body");
                    String entityBody = req.getParameter("entity");
                    //writer.write(JsonMaker.getJson(ruleService.runRule(entityBody, rule)));
                    break;
                case FLUSH:
                    ruleService.reloadCache();
                    break;
                case RENAME_RULE:
                    ruleId = Long.parseLong(req.getParameter("ruleId"));
                    title = req.getParameter("title");
                    ruleService.renameRule(ruleId,title);
                    break;
                /*case LIST_TEST_ENTITY:
                    //writer.write(JsonMaker.getJson(ruleService.listTestEntity(100500L)));
                    break;
                case INSERT_TEST_ENTITY:
                    TestEntity testEntity = new TestEntity();
                    testEntity.setBody(req.getParameter("body"));
                    testEntity.setTitle(req.getParameter("title"));
                    //writer.write(JsonMaker.getJson(ruleService.insertTestEntity(100500L, testEntity)));
                    break;
                case DELETE_TEST_ENTITY:
                    testEntity = new TestEntity();
                    testEntity.setId(Long.parseLong(req.getParameter("id")));
                    //writer.write(JsonMaker.getJson(ruleService.deleteTestEntity(testEntity)));
                    break;
                case UPDATE_TEST_ENTITY:
                    testEntity = new TestEntity();
                    testEntity.setTitle(req.getParameter("body"));
                    testEntity.setBody(req.getParameter("title"));
                    testEntity.setId(Long.parseLong(req.getParameter("id")));
                    //writer.write(JsonMaker.getJson(ruleService.updateTestEntity(testEntity)));
                    break;*/
            }
        } catch (Exception e) {

            e.printStackTrace();

            if(!retry) {
                retry = true;
                try {
                    init();
                    service(req, resp);
                } catch (Exception e1) {
                    resp.setHeader(ResourceResponse.HTTP_STATUS_CODE, "400");
                    writer.write("{ \"success\": false, \"errorMessage\": \""+ e1.getMessage()
                            .replaceAll("\"","").replaceAll("\n","")+"\"}");
                } finally {
                    retry = false;
                    return;
                }
            }

            resp.setHeader(ResourceResponse.HTTP_STATUS_CODE, "400");
            writer.write("{ \"success\": false, \"errorMessage\": \""+ e.getMessage().replaceAll("\"","").replaceAll("\n","")+"\"}");
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException{

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextPortlet.xml");
        //rulesSingleton = context.getBean("rulesSingleton",RulesSingleton.class);
        //rulesSingleton.reloadCache();

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

        //rulesSingleton = ruleService.getRulesSingleton();

        batchServiceFactoryBean = new RmiProxyFactoryBean();
        batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchService");
        batchServiceFactoryBean.setServiceInterface(IBatchService.class);

        batchServiceFactoryBean.afterPropertiesSet();

        batchService = (IBatchService) batchServiceFactoryBean.getObject();
        super.init();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*
    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
        //renderRequest.setAttribute("entityList", baseEntityList);
        super.doView(renderRequest, renderResponse);
    }*/

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
        LIST_TEST_ENTITY,

        LIST_ALL,
        LIST_CLASS,
        SAVE_CLASS,
        DEL_CLASS,
        SAVE_ATTR,
        GET_ATTR,
        INSERT_TEST_ENTITY, DELETE_TEST_ENTITY, UPDATE_TEST_ENTITY, DEL_ATTR
    }

    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException {

        //resourceResponse.setContentType("application/json; charset=UTF-8");
        resourceResponse.setCharacterEncoding("UTF-8");
        //resourceResponse.setLocale(Locale.ENGLISH);
        //resourceResponse.setProperty("sdf","sdf");
        //System.setProperty("file.encoding", "UTF-8");
        //Locale.setDefault(new Locale("en"));

        PrintWriter writer = resourceResponse.getWriter();

        writer.write("У лукоморья дуб зеленый");
        if(1==1) return;

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
                case GET_RULE :
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    writer.write(JsonMaker.getJson(ruleService.getRule(ruleId)));
                    break;
                case UPDATE_RULE:
                    String ruleBody = resourceRequest.getParameter("ruleBody");
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
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
                    ruleId = ruleService.createCopy(ruleId,title,batchVersionId);
                    writer.write(JsonMaker.getJson(ruleId));
                    break;
                case RUN_RULE:
                    String batchName = resourceRequest.getParameter("batchName");
                    date =  df.parse(resourceRequest.getParameter("date"));
                    baseEntityId = Long.parseLong(resourceRequest.getParameter("baseEntityId"));
                    BaseEntity be = (BaseEntity) entityService.load(baseEntityId);
                    //rulesSingleton.runRules(be,batchName,date);
                    ruleService.runRules(be,batchName,date);
                    writer.write(JsonMaker.getJson(be.getValidationErrors()));
                    break;
                case FLUSH:
                    //rulesSingleton.reloadCache();
                    ruleService.reloadCache();
                    break;
                case RENAME_RULE:
                    ruleId = Long.parseLong(resourceRequest.getParameter("ruleId"));
                    title = resourceRequest.getParameter("title");
                    ruleService.renameRule(ruleId,title);
                    break;
            }

        } catch (Exception e) {
            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            writer.write("{ \"success\": false, \"errorMessage\": \""+ e.getMessage().replaceAll("\"","").replaceAll("\n","")+"\"}");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
