package kz.bsbnb.usci.portlets.drules_portlet;

import com.google.gson.Gson;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.brms.rulesingleton.RulesSingleton;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Batch;
import kz.bsbnb.usci.brms.rulesvr.model.impl.BatchVersion;
import kz.bsbnb.usci.brms.rulesvr.model.impl.Rule;
import kz.bsbnb.usci.brms.rulesvr.service.*;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.service.IEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author abukabayev
 */
public class MainPortlet extends MVCPortlet {
    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean batchVersionServiceFactoryBean;
    private RmiProxyFactoryBean ruleServiceFactoryBean;
    private RmiProxyFactoryBean listenerServiceFactoryBean;

    private RmiProxyFactoryBean entityServiceFactoryBean;

    private IEntityService entityService;

    private IBatchService batchService;
    private IRuleService ruleService;
    private IBatchVersionService batchVersionService;

    @Autowired
    private RulesSingleton rulesSingleton;


    @Override
    public void init() throws PortletException {

        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextPortlet.xml");
        rulesSingleton = context.getBean("rulesSingleton",RulesSingleton.class);
        rulesSingleton.reloadCache();

        entityServiceFactoryBean = new RmiProxyFactoryBean();
        entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
        entityServiceFactoryBean.setServiceInterface(IEntityService.class);

        entityServiceFactoryBean.afterPropertiesSet();
        entityService = (IEntityService) entityServiceFactoryBean.getObject();


        batchServiceFactoryBean = new RmiProxyFactoryBean();
        batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchService");
        batchServiceFactoryBean.setServiceInterface(IBatchService.class);

        batchServiceFactoryBean.afterPropertiesSet();
        batchService = (IBatchService) batchServiceFactoryBean.getObject();

        batchVersionServiceFactoryBean = new RmiProxyFactoryBean();
        batchVersionServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/batchVersionService");
        batchVersionServiceFactoryBean.setServiceInterface(IBatchVersionService.class);

        batchVersionServiceFactoryBean.afterPropertiesSet();
        batchVersionService = (IBatchVersionService) batchVersionServiceFactoryBean.getObject();

        ruleServiceFactoryBean = new RmiProxyFactoryBean();
        ruleServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1097/ruleService");
        ruleServiceFactoryBean.setServiceInterface(IRuleService.class);

        ruleServiceFactoryBean.afterPropertiesSet();
        ruleService = (IRuleService) ruleServiceFactoryBean.getObject();

        super.init();
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
        try{

            List<Batch> batchList = batchService.getAllBatches();
//            for (Batch s : batchList){
//                System.out.println(s.getName()+" "+s.getId());
//            }
//            System.out.println(batchList.size());
            List<Rule> ruleList = ruleService.getAllRules();

            renderRequest.setAttribute("batchList",batchList);
            renderRequest.setAttribute("ruleList",ruleList);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.doView(renderRequest, renderResponse);
    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
        PrintWriter writer = resourceResponse.getWriter();
        Gson gson = new Gson();
        String type = resourceRequest.getParameter("type");

        if (type.equals("addPackage")){

            String name = resourceRequest.getParameter("name");

            try {

                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(resourceRequest.getParameter("date"));

                Batch batch = new Batch(name,date);

                Long id = batchService.save(batch);
                System.out.println("BATCH ID : ");
                System.out.println(id);
                if (id != null)writer.write("success");else writer.write("error");
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("error");
            }


        }else
        if (type.equals("getRules")){
            String id = resourceRequest.getParameter("id");

            try {
             Date date = new SimpleDateFormat("MM/dd/yyyy").parse(resourceRequest.getParameter("versionDate"));

                Batch batch = batchService.load(Long.parseLong(id, 10));
                BatchVersion batchVersion = batchVersionService.load(batch,date);
                if (batchVersion!=null){
                    List<Rule> ruleList = ruleService.load(batchVersion);
                    String json = gson.toJson(ruleList);
                    System.out.println("RULE JSON :");
                    System.out.println(json);
                    writer.write(json);
                } else{
                    writer.write("noresult");
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }else
        if (type.equals("updateRule")){
            String id = resourceRequest.getParameter("id");
            String value = resourceRequest.getParameter("name");
            Rule rule = new Rule();
            rule.setId(Long.parseLong(id,10));
            rule.setRule(value);
            try{
                ruleService.update(rule);
                writer.write("success");
            }catch(Exception e){
                e.printStackTrace();
                writer.write("error");
            }
        }else
        if (type.equals("copyRule")){
            String ruleId = resourceRequest.getParameter("ruleId");
            String packageId = resourceRequest.getParameter("packageId");
            String data = resourceRequest.getParameter("versionDate");


            Date date = null;
            try {
                date = new SimpleDateFormat("MM/dd/yyyy").parse(data);
                batchVersionService.copyRule(Long.parseLong(ruleId,10),batchService.load(Long.parseLong(packageId,10)),date);
                writer.write("success");
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("error");
            }
//            System.out.println(ruleId);
//            System.out.println(packageId);
//            System.out.println(date);

        }else
         if (type.equals("addNewRule")){
             String rule = resourceRequest.getParameter("rule");
             String ruleTitle = resourceRequest.getParameter("ruleTitle");
             String packageId = resourceRequest.getParameter("packageId");
             String data = resourceRequest.getParameter("versionDate");

             Rule ruleSave = new Rule();
             ruleSave.setTitle(ruleTitle);
             ruleSave.setRule(rule);
             Date date = null;
             try {
                 date = new SimpleDateFormat("MM/dd/yyyy").parse(data);
                 Long ruleId = ruleService.save(ruleSave,new BatchVersion());

                 batchVersionService.copyRule(ruleId,batchService.load(Long.parseLong(packageId,10)),date);
                 if (ruleId != null)writer.write("success");else writer.write("error");
             } catch (Exception e) {
                 e.printStackTrace();
                 writer.write("error");
             }
         } else
          if (type.equals("runRules")){
          try{
              String packageName = resourceRequest.getParameter("name");
              String versionDate = resourceRequest.getParameter("versionDate");
              String entityId = resourceRequest.getParameter("entityId");

              System.out.println("########### ID:");
              System.out.println(entityId);
              System.out.println(Long.parseLong(entityId, 10));
              System.out.println(versionDate);
              Date date = new SimpleDateFormat("MM/dd/yyyy").parse(versionDate);

              BaseEntity baseEntity = entityService.load(Long.parseLong(entityId, 10));

              //TODO: refactor
              rulesSingleton.reloadCache();

              rulesSingleton.runRules(baseEntity,packageName,date);
              System.out.print("SIZEL:: ");
              System.out.println(baseEntity.getValidationErrors().size());
              String ss="";
              for (String s: baseEntity.getValidationErrors()){
                System.out.println(s);
                  ss+= s + "<br>";
              }

              writer.write(ss);
          }catch(Exception e){
              e.printStackTrace();
          }
          }
    }
}

