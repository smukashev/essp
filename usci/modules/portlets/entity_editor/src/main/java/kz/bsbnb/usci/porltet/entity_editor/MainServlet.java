package kz.bsbnb.usci.porltet.entity_editor;

import com.google.gson.Gson;
import com.liferay.portal.model.User;
//import com.liferay.portal.util.PortalUtil;
//import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.ISearcherFormService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Pair;
import kz.bsbnb.usci.porltet.entity_editor.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.entity_editor.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.jooq.Meta;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

//public class MainPortlet extends MVCPortlet {
public class MainServlet extends HttpServlet {
    private RmiProxyFactoryBean metaFactoryServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;
    private RmiProxyFactoryBean batchEntryServiceFactoryBean;
    private RmiProxyFactoryBean searcherFormEntryServiceFactoryBean;

    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBatchEntryService batchEntryService;
    private ISearcherFormService searcherFormService;

    public void connectToServices() {
        try {
            metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
            metaFactoryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/metaFactoryService");
            metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);
            metaFactoryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            metaFactoryServiceFactoryBean.afterPropertiesSet();
            metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();

            entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IEntityService.class);
            entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityServiceFactoryBean.afterPropertiesSet();
            entityService = (IEntityService) entityServiceFactoryBean.getObject();

            batchEntryServiceFactoryBean = new RmiProxyFactoryBean();
            batchEntryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/batchEntryService");
            batchEntryServiceFactoryBean.setServiceInterface(IBatchEntryService.class);
            batchEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchEntryServiceFactoryBean.afterPropertiesSet();
            batchEntryService = (IBatchEntryService) batchEntryServiceFactoryBean.getObject();

            searcherFormEntryServiceFactoryBean = new RmiProxyFactoryBean();
            searcherFormEntryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/searcherFormService");
            searcherFormEntryServiceFactoryBean.setServiceInterface(ISearcherFormService.class);
            searcherFormEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            searcherFormEntryServiceFactoryBean.afterPropertiesSet();
            searcherFormService = (ISearcherFormService) searcherFormEntryServiceFactoryBean.getObject();

        } catch (Exception e) {
            System.out.println("Can\"t initialise services: " + e.getMessage());
        }
    }

    /*
    @Override
    public void init() throws PortletException {
        connectToServices();

        super.init();
    }
    */

    //@Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {

        /*
        HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(
                PortalUtil.getHttpServletRequest(renderRequest));

        String entityId = httpReq.getParameter("entityId");
        renderRequest.setAttribute("entityId", entityId);

        super.doView(renderRequest, renderResponse);
        */
    }

    enum OperationTypes {
        LIST_CLASSES,
        LIST_ENTITY,
        SAVE_XML,
        LIST_BY_CLASS,
        FIND_ACTION
    }

    private String testNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    private String clearSlashes(String str) {
        //TODO: str.replaceAll("\"","\\\""); does not work! Fix needed.
        //String outStr = str.replaceAll("\\\\"," ");
        String outStr = str.replaceAll("\""," ");
        return outStr;
    }

    private String entityToJson(BaseEntity entity, String title, String code) {
        MetaClass meta = entity.getMeta();

        if (title == null) {
            title = code;
        }

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"value\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"simple\": false,";
        str += "\"array\": false,";
        str += "\"type\": \"META_CLASS\",";
        str += "\"iconCls\":\"folder\",";
        str += "\"children\":[";

        boolean first = true;

        for (String innerClassesNames : meta.getComplexAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue value = entity.getBaseValue(innerClassesNames);

            if (value != null && value.getValue() != null) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str +=  entityToJson((BaseEntity)(value.getValue()), attrTitle, innerClassesNames);
            }

        }

        for (String innerClassesNames : meta.getComplexArrayAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue value = entity.getBaseValue(innerClassesNames);

            if (value != null && value.getValue() != null) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str +=  setToJson((BaseSet) (value.getValue()), attrTitle, innerClassesNames);
            }
        }

        for (String innerClassesNames : meta.getSimpleAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue value = entity.getBaseValue(innerClassesNames);

            if (value != null && value.getValue() != null) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                if(((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() != DataTypes.DATE) {
                    str +=  "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"value\":\"" + clearSlashes(testNull(value.getValue().toString())) + "\",\n" +
                            "\"simple\": true,\n" +
                            "\"array\": false,\n" +
                            "\"type\": \"" + ((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
                            "\"leaf\":true,\n" +
                            "\"iconCls\":\"file\",\n" +
                            "\"isKey\":\""+meta.getMetaAttribute(innerClassesNames).isKey()+"\"\n" +
                            "}";
                } else {
                    Object dtVal = value.getValue();
                    String dtStr = "";
                    if (dtVal != null) {
                        dtStr = new SimpleDateFormat("dd.MM.yyyy").format(dtVal);
                    }

                    str +=  "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"value\":\"" + dtStr + "\",\n" +
                            "\"simple\": true,\n" +
                            "\"array\": false,\n" +
                            "\"type\": \"" + ((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
                            "\"leaf\":true,\n" +
                            "\"iconCls\":\"file\",\n" +
                            "\"isKey\":\""+meta.getMetaAttribute(innerClassesNames).isKey()+"\"\n" +
                            "}";
                }
            }
        }

        str += "]}";

        return str;
    }

    private String setToJson(BaseSet set, String title, String code) {
        IMetaType type = set.getMemberType();

        if (title == null) {
            title = code;
        }

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"value\": \"" + set.get().size() + "\",";
        str += "\"simple\": false,";
        str += "\"array\": true,";
        str += "\"type\": \"META_SET\",";
        str += "\"iconCls\":\"folder\",";
        str += "\"children\":[";

        boolean first = true;

        int i = 0;

        if (type.isComplex()) {
            for (IBaseValue value : set.get()) {
                if (value != null && value.getValue() != null) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    str +=  entityToJson((BaseEntity)(value.getValue()), "[" + i + "]",
                            "[" + i + "]");
                    i++;
                }

            }
        } else {
            for (IBaseValue value : set.get()) {
                if (value != null && value.getValue() != null) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    if(((MetaValue)type).getTypeCode() != DataTypes.DATE)
                    {
                        str +=  "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"value\":\"" + clearSlashes(testNull(value.getValue().toString())) + "\",\n" +
                                "\"simple\": true,\n" +
                                "\"array\": false,\n" +
                                "\"type\": \"" + ((MetaValue)type).getTypeCode() + "\",\n" +
                                "\"leaf\":true,\n" +
                                "\"iconCls\":\"file\"\n" +
                                "}";
                    } else {
                        Object dtVal = value.getValue();
                        String dtStr = "";
                        if (dtVal != null) {
                            dtStr = new SimpleDateFormat("dd.MM.yyyy").format(dtVal);
                        }

                        str +=  "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"value\":\"" + dtStr + "\",\n" +
                                "\"simple\": true,\n" +
                                "\"array\": false,\n" +
                                "\"type\": \"" + ((MetaValue)type).getTypeCode() + "\",\n" +
                                "\"leaf\":true,\n" +
                                "\"iconCls\":\"file\"\n" +
                                "}";
                    }
                }
            }
        }

        str += "]}";

        return str;
    }

    @Override
    //public void serveResource(HttpRequest resourceRequest, HttpResponse resourceResponse) throws IOException
    public void service(HttpServletRequest resourceRequest, HttpServletResponse resourceResponse) throws IOException
    {
        resourceResponse.setCharacterEncoding("utf-8");

        if (metaFactoryService == null) {
            connectToServices();
            //todo: add error message here
            if (metaFactoryService == null)
                return;
        }
        PrintWriter writer = resourceResponse.getWriter();

        try {
            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));

            Gson gson = new Gson();

            switch (operationType) {
                case FIND_ACTION:
                    Enumeration<String> list = resourceRequest.getParameterNames();

                    String metaString = resourceRequest.getParameter("metaClass");

                    MetaClass metaClass = metaFactoryService.getMetaClass(metaString);
                    BaseEntity baseEntity = new BaseEntity(metaClass, new Date());

                    while(list.hasMoreElements()) {
                        //System.out.println(list.nextElement());



                        String attribute = list.nextElement();

                        if(attribute.equals("op") || attribute.equals("metaClass"))
                            continue;
                        //String classInfo = resourceRequest.getParameter(attribute);

                        //boolean complex = true;
                        Object value;
                        String parameterValue = resourceRequest.getParameter(attribute);

                        //op: LIST_CLASSES
                        //primary_contract : simple_ldflsdf or complex19123
                        //class: credit

                        /*if(classInfo.startsWith("simple")) {
                            complex = false;
                        }*/
                        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
                        IMetaType metaType = metaAttribute.getMetaType();

                        if(metaType.isSet() || metaType.isSetOfSets())
                            throw new UnsupportedOperationException("Not yet implemented");

                        if(metaType.isComplex()) {
                            BaseEntity childBaseEntity = new BaseEntity((MetaClass) metaType, new Date());
                            childBaseEntity.setId(Long.valueOf(parameterValue));
                            value = childBaseEntity;

                        } else {
                            MetaValue metaValue = (MetaValue) metaType;
                            value = metaValue.getTypeCode().fromString(parameterValue);
                        }

                        Batch b = new Batch(new Date(), 100500L);
                        b.setId(777L);

                        baseEntity.put(attribute, BaseValueFactory.create(
                                BaseContainerType.BASE_ENTITY,
                                metaAttribute.getMetaType(),
                                b, 1, value));
                    }

                    baseEntity = entityService.search(baseEntity);

                    long ret = -1;

                    if(baseEntity.getId() > 0)
                        ret = baseEntity.getId();

                    writer.write("{\"success\": true, \"data\":\""+ ret +"\"}");

                    break;
                case SAVE_XML:
                    String xml = resourceRequest.getParameter("xml_data");

                    BatchEntry batchEntry = new BatchEntry();

                    batchEntry.setValue(xml);

                    //User currentUser = PortalUtil.getUser(resourceRequest);

                    //batchEntry.setUserId(currentUser.getUserId());
                    batchEntry.setUserId(2L);

                    batchEntryService.save(batchEntry);

                    writer.write("{\"success\": true }");

                    break;
                case LIST_CLASSES:
                    /*MetaClassList classesListJson = new MetaClassList();
                    List<MetaClassName> metaClassesList = metaFactoryService.getRefNames();

                    classesListJson.setTotal(metaClassesList.size());

                    for (MetaClassName metaName : metaClassesList) {
                        MetaClassListEntry metaClassListEntry = new MetaClassListEntry();

                        metaClassListEntry.setClassId("" + metaName.getId());
                        if(metaName.getClassTitle() != null
                                && metaName.getClassTitle().trim().length() > 0)
                            metaClassListEntry.setClassName(metaName.getClassTitle());
                        else
                            metaClassListEntry.setClassName(metaName.getClassName());

                        classesListJson.getData().add(metaClassListEntry);
                    }

                    writer.write(gson.toJson(classesListJson));
                    */


                    List<Pair> classes = searcherFormService.getMetaClasses(100500L);
                    //writer.write(gson.toJson(classes));
                    String str = searcherFormService.getDom(10500L, metaFactoryService.getMetaClass("credit"), "null");
                    //writer.write(clearSlashes(str));
                    writer.write(str);
                    //writer.write(str);
                    break;
                case LIST_BY_CLASS:
                    String metaId = resourceRequest.getParameter("metaId");
                    if (metaId != null && metaId.trim().length() > 0) {
                        List<RefListItem> ids = entityService.getRefsByMetaclass(Long.parseLong(metaId));

                        writer.write("{\"total\":" + ids.size());
                        writer.write(",\"data\":[");

                        boolean first = true;

                        for (RefListItem id : ids) {
                            if (first) {
                                first = false;
                            } else {
                                writer.write(",");
                            }

                            writer.write("{");

                            writer.write("\"id\":\"" + id.getId() + "\",");
                            writer.write("\"code\":\"" + id.getCode() + "\",");

                            for (String key : id.getKeys()) {
                                writer.write("\"" + key + "\":\"" + id.getValue(key) + "\",");
                            }

                            writer.write("\"title\":\"" + id.getTitle() + "\"");
                            writer.write("}");
                        }

                        writer.write("]}");
                    }

                    break;
                case LIST_ENTITY:
                    String entityId = resourceRequest.getParameter("entityId");
                    if (entityId != null && entityId.trim().length() > 0) {
                        BaseEntity entity = entityService.load(Integer.parseInt(entityId));

                        writer.write("{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entity, entity.getMeta().getClassTitle(),
                                        entity.getMeta().getClassName()) +
                                "]}");
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("{\"success\": false, \"errorMessage\": \"" + e.getMessage() + "\"}");
        }
    }
}
