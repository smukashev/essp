package kz.bsbnb.usci.porltet.entity_portlet;
import com.google.gson.Gson;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author abukabayev
 */
public class MainPortlet extends MVCPortlet {

    private RmiProxyFactoryBean metaFactoryServiceFactoryBean;
    private RmiProxyFactoryBean batchServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;

    private IBatchService batchService;
    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;

    BaseEntity baseEntity2 = null;
    Batch batch = null;
    BaseEntity result = null;

    @Override
    public void init() throws PortletException {

//        ApplicationContext ctx = new ClassPathXmlApplicationContext("portletContext.xml");
//        ctx.getBean("remoteEntityService");

        metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();
        batchService = (IBatchService) batchServiceFactoryBean.getObject();
        entityService = (IEntityService) entityServiceFactoryBean.getObject();

        entityServiceFactoryBean = new RmiProxyFactoryBean();
        entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
        entityServiceFactoryBean.setServiceInterface(IEntityService.class);

        entityServiceFactoryBean.afterPropertiesSet();
        entityService = (IEntityService) entityServiceFactoryBean.getObject();

        batchServiceFactoryBean = new RmiProxyFactoryBean();
        batchServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/batchService");
        batchServiceFactoryBean.setServiceInterface(IBatchService.class);

        batchServiceFactoryBean.afterPropertiesSet();
        batchService = (IBatchService) batchServiceFactoryBean.getObject();

        metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
        metaFactoryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/metaFactoryService");
        metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);

        metaFactoryServiceFactoryBean.afterPropertiesSet();
        metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();

        super.init();
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {
       try{

           List<BaseEntity>  baseEntityList = metaFactoryService.getBaseEntities();

        renderRequest.setAttribute("entityList", baseEntityList);
       }catch (Exception e){
           e.printStackTrace();
       }
        super.doView(renderRequest, renderResponse);
    }


    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {

        PrintWriter writer = resourceResponse.getWriter();
        Gson gson = new Gson();
        String metaId = resourceRequest.getParameter("metaId");
        String json = resourceRequest.getParameter("json");
        JSONObject jjson;
        MetaClass metaClass = null;
        BaseEntity baseEntity = null;

        if (!metaId.equals("no")) {
            baseEntity  = metaFactoryService.getBaseEntity(metaId);
            metaClass = baseEntity.getMeta();
        }

        if (json.equals("addEntity")){
            try {
                JSONObject addJson = new JSONObject(resourceRequest.getParameter("obj"));
                int count = Integer.parseInt(resourceRequest.getParameter("count"));

                String className = (String) addJson.get("className");

                MetaClass meta = new MetaClass(className);


                for (int i=1;i<=count;i++){
                  if (addJson.get("type_"+i).equals("STRING")){
                      meta.setMetaAttribute((String) addJson.get("name_"+i),new MetaAttribute((Boolean) addJson.get("check_"+i),false,new MetaValue(DataTypes.STRING)));
                  }else
                  if (addJson.get("type_"+i).equals("DATE '01/01/1990'")){
                      meta.setMetaAttribute((String) addJson.get("name_"+i),new MetaAttribute((Boolean) addJson.get("check_"+i),false,new MetaValue(DataTypes.DATE)));
                  }else
                  if (addJson.get("type_"+i).equals("BOOLEAN")){
                      meta.setMetaAttribute((String) addJson.get("name_"+i),new MetaAttribute((Boolean) addJson.get("check_"+i),false,new MetaValue(DataTypes.BOOLEAN)));
                  }else
                  if (addJson.get("type_"+i).equals("INTEGER")){
                      meta.setMetaAttribute((String) addJson.get("name_"+i),new MetaAttribute((Boolean) addJson.get("check_"+i),false,new MetaValue(DataTypes.INTEGER)));
                  }else
                  if (addJson.get("type_"+i).equals("DOUBLE")){
                      meta.setMetaAttribute((String) addJson.get("name_"+i),new MetaAttribute((Boolean) addJson.get("check_"+i),false,new MetaValue(DataTypes.DOUBLE)));
                  }

                }

                BaseEntity baseEntityNew = new BaseEntity(meta,new Date());

                Long batchId = batchService.save(new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime())));
                batch = batchService.load(batchId);

                for (int i=1;i<=count;i++){
                    if (addJson.get("type_"+i).equals("STRING")){
                        baseEntityNew.put((String) addJson.get("name_"+i),new BaseValue(batch,1,addJson.get("value_"+i)));
                    }else
                    if (addJson.get("type_"+i).equals("DATE '01/01/1990'")){
                        try {

                            Date date = new SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH).parse((String) addJson.get("value_"+i));

                            baseEntityNew.put((String) addJson.get("name_"+i),new BaseValue(batch,1,date));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }else
                    if (addJson.get("type_"+i).equals("BOOLEAN")){
                        baseEntityNew.put((String) addJson.get("name_"+i),new BaseValue(batch,1,Boolean.parseBoolean((String) addJson.get("value_" + i))));
                    }else
                    if (addJson.get("type_"+i).equals("INTEGER")){
                        baseEntityNew.put((String) addJson.get("name_"+i),new BaseValue(batch,1,Integer.parseInt((String) addJson.get("value_" + i))));
                    }else
                    if (addJson.get("type_"+i).equals("DOUBLE")){
                        baseEntityNew.put((String) addJson.get("name_"+i),new BaseValue(batch,1,Double.parseDouble((String) addJson.get("value_" + i))));
                    }
                }

                System.out.println("new Meta : ");
                System.out.println(baseEntityNew.getMeta().getClassName());
                entityService.update(baseEntityNew,baseEntityNew);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else

        if (json.equals("makeTree"))//TODO:make tree of complex attributes
        {
            String keyAttributes = resourceRequest.getParameter("obj");
            try {
                jjson = new JSONObject(keyAttributes);


             Long batchId = batchService.save(new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime())));
             batch = batchService.load(batchId);
            BaseEntity baseEntity1 = new BaseEntity(metaClass,new Date());


                Iterator<?> keys = jjson.keys();

                while( keys.hasNext() ){
                    String key = (String)keys.next();
                    System.out.println(key+" "+jjson.get(key));

                    DataTypes dataTypes =    ((MetaValue) baseEntity1.getMeta().getMetaAttribute(key).getMetaType()).getTypeCode();



                    if (dataTypes.toString().equals("STRING")){
                        baseEntity1.put(key, new BaseValue(batch, 1, jjson.get(key)));
                    }else
                    if (dataTypes.toString().equals("DATE")){

                        try {

                            Date date = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse((String) jjson.get(key));
                            baseEntity1.put(key, new BaseValue(batch, 1, date));

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }else
                    if (dataTypes.toString().equals("BOOLEAN")){

                        baseEntity1.put(key, new BaseValue(batch, 1, Boolean.parseBoolean((String) jjson.get(key))));
                    }else
                    if (dataTypes.toString().equals("DOUBLE")){
                        baseEntity1.put(key, new BaseValue(batch, 1,Double.parseDouble((String) jjson.get(key))));
                    }else
                    if (dataTypes.toString().equals("INTEGER")){
                        baseEntity1.put(key, new BaseValue(batch, 1,Integer.parseInt((String) jjson.get(key))));
                    }



                }



                baseEntity2 = entityService.search(baseEntity1);

            BaseEntityJson baseEntityJson  = new BaseEntityJson();

            BatchJson batchJson = new BatchJson();

            baseEntityJson.setReportDate(baseEntity2.getReportDate());
            baseEntityJson.setId(baseEntity2.getId());
            for (String attr : metaClass.getAttributeNames()){
                BaseValueJson baseValueJson = new BaseValueJson();
                batchJson.setId(baseEntity2.getBaseValue(attr).getBatch().getId());
                batchJson.setRepDate(baseEntity2.getBaseValue(attr).getBatch().getRepDate());
                batchJson.setReceiptDate(baseEntity2.getBaseValue(attr).getBatch().getReceiptDate());
                baseValueJson.setBatch(batchJson);
                baseValueJson.setRepDate(baseEntity2.getBaseValue(attr).getRepDate());
                baseValueJson.setValue(baseEntity2.getBaseValue(attr).getValue());
                baseEntityJson.put(metaClass, attr, baseValueJson);
            }

              String base = gson.toJson(baseEntityJson);


              writer.write(base);
            } catch (Exception e) {
                e.printStackTrace();
                writer.write("error");
            }

        }

        else
        if (json.equals("makeTreeClass")){
            String metaClassJson = gson.toJson(baseEntity.getMeta());
            writer.write(metaClassJson);
        }
        else
        {
            BaseEntityJson obj = gson.fromJson(json, BaseEntityJson.class);
            System.out.println("Number of objects : "+obj.getValues().size());

            Iterator iterator = obj.getValues().entrySet().iterator();
            result = baseEntity2;
            result = new BaseEntity(baseEntity2.getMeta(),baseEntity2.getReportDate());

            Long batchId = batchService.save(new Batch(new Timestamp(new Date().getTime()), new java.sql.Date(new Date().getTime())));
            Batch batchNew = batchService.load(batchId);

            while (iterator.hasNext()){
                Map.Entry<String, BaseValueJson> e = (Map.Entry<String, BaseValueJson>) iterator.next();

                if (!result.getMeta().getAttributeNames().contains(e.getKey())){

                    result.getMeta().setMetaAttribute(e.getKey(),new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
                    result.put(e.getKey(), new BaseValue(batchNew, 1, e.getValue().getValue().toString()));
                    System.out.println(e.getKey());
                    for (String s : result.getMeta().getAttributeNames()){
                        System.out.println("ADDED : "+s);
                    }
                }
                else{
                     DataTypes dataTypes =    ((MetaValue) baseEntity2.getMeta().getMetaAttribute(e.getKey()).getMetaType()).getTypeCode();

//                        result.getMeta().setMetaAttribute(e.getKey(),new MetaAttribute(false, false, new MetaValue(dataTypes)));

                    if (dataTypes.toString().equals("STRING")){
                        result.put(e.getKey(), new BaseValue(batchNew, 1, e.getValue().getValue()));
                        System.out.println("UPDATING: "+e.getKey()+" value: "+e.getValue().getValue());

                    }else
                    if (dataTypes.toString().equals("DATE")){

                        try {

                            Date date = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).parse(String.valueOf(e.getValue().getValue()));
                            result.put(e.getKey(), new BaseValue(batchNew, 1, date));

                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }else
                    if (dataTypes.toString().equals("BOOLEAN")){
                        result.put(e.getKey(), new BaseValue(batchNew, 1,Boolean.parseBoolean(String.valueOf(e.getValue().getValue()))));
                    }else
                    if (dataTypes.toString().equals("DOUBLE")){
                        result.put(e.getKey(), new BaseValue(batchNew, 1,Double.parseDouble(String.valueOf(e.getValue().getValue()))));
                    }else
                    if (dataTypes.toString().equals("INTEGER")){
                        result.put(e.getKey(), new BaseValue(batchNew, 1,(int)Double.parseDouble(String.valueOf(e.getValue().getValue()))));
                    }

                }


            }
            try{
                entityService.update(result, result);
            } catch(Exception e){
               e.printStackTrace();
            }

        }
    }
}
