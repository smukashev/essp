package kz.bsbnb.usci.porltet.ref_portlet;

import com.google.gson.Gson;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.porltet.ref_portlet.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.ref_portlet.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainPortlet extends MVCPortlet {
    private RmiProxyFactoryBean metaFactoryServiceFactoryBean;
    private RmiProxyFactoryBean entityServiceFactoryBean;
    private RmiProxyFactoryBean batchEntryServiceFactoryBean;

    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBatchEntryService batchEntryService;

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

        String entityId = httpReq.getParameter("entityId");
        renderRequest.setAttribute("entityId", entityId);

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_CLASSES,
        LIST_ENTITY,
        SAVE_XML,
        LIST_BY_CLASS,
        LIST_ATTRIBUTES
    }

    private String testNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    private String clearSlashes(String str) {
        //TODO: str.replaceAll("\"","\\\""); does not work! Fix needed.
        String outStr = str.replaceAll("\""," ");
        System.out.println(outStr);
        return outStr;
    }

    private String entityToJson(BaseEntity entity, String title, String code, IMetaAttribute attr, boolean asRoot) {
        MetaClass meta = entity.getMeta();

        if (title == null) {
            title = code;
        }

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
//        str += "\"value\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"value\": \"" + entity.getId() + "\",";
        str += "\"simple\": false,";
        str += "\"array\": false,";
        str += "\"ref\": " + entity.getMeta().isReference() + ",";
        str += "\"isKey\": " + (attr != null ? attr.isKey() : false) + ",";
        str += "\"root\": " + asRoot + ",";
        str += "\"type\": \"META_CLASS\",";
        str += "\"metaId\": \"" + entity.getMeta().getId() + "\",";
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

                str +=  entityToJson((BaseEntity)(value.getValue()), attrTitle, innerClassesNames,
                        meta.getMetaAttribute(innerClassesNames), false);
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

                    str +=  entityToJson((BaseEntity)(value.getValue()), "[" + i + "]", "[" + i + "]", null, false);
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
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException
    {

        if (metaFactoryService == null) {
            connectToServices();
            //todo: add error message here
            if (metaFactoryService == null)
                return;
        }
        PrintWriter writer = resourceResponse.getWriter();

        try {
            OperationTypes operationType = OperationTypes.valueOf(getParam("op", resourceRequest));

            Gson gson = new Gson();

            switch (operationType) {
                case SAVE_XML:
                    String xml = getParam("xml_data", resourceRequest);

                    BatchEntry batchEntry = new BatchEntry();

                    batchEntry.setValue(xml);

                    User currentUser = PortalUtil.getUser(resourceRequest);

                    batchEntry.setUserId(currentUser.getUserId());

                    batchEntryService.save(batchEntry);

                    writer.write("{\"success\": true }");

                    break;
                case LIST_CLASSES:
                    MetaClassList classesListJson = new MetaClassList();
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

                    break;
                case LIST_BY_CLASS:
                    String metaId = getParam("metaId", resourceRequest);
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

                            String title = id.getTitle() != null ? id.getTitle() : (String)id.getValue("VALUE");

                            writer.write("\"title\":\"" + title + "\"");
                            writer.write("}");
                        }

                        writer.write("]}");
                    }
                    break;
                case LIST_ATTRIBUTES:
                    String entityId = getParam("entityId", resourceRequest);

                    if (StringUtils.isNotEmpty(entityId)) {
                        Date date = null;

                        if(StringUtils.isNotEmpty(resourceRequest.getParameter("date")))
                            date = (Date) DataTypes.fromString(DataTypes.DATE, resourceRequest.getParameter("date"));

                        if(date == null)
                            date = new Date();

                        BaseEntity entity = entityService.load(Integer.parseInt(entityId), date);

                        writer.write(getAttributesJson(entity));
                    }

                    break;
                case LIST_ENTITY:
                    entityId = getParam("entityId", resourceRequest);
                    String asRootStr = getParam("asRoot", resourceRequest);

                    boolean asRoot = StringUtils.isNotEmpty(asRootStr) ? Boolean.valueOf(asRootStr) : false;

                    if (entityId != null && entityId.trim().length() > 0) {
                        Date date = null;

                        if(StringUtils.isNotEmpty(resourceRequest.getParameter("date")))
                            date = (Date) DataTypes.fromString(DataTypes.DATE, resourceRequest.getParameter("date"));

                        if(date == null)
                            date = new Date();

                        BaseEntity entity = entityService.load(Integer.parseInt(entityId), date);

                        writer.write("{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entity, entity.getMeta().getClassTitle(),
                                        entity.getMeta().getClassName(), null, asRoot) +
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

    private String getAttributesJson(BaseEntity entity) {
        IMetaClass meta = entity.getMeta();

        StringBuilder result = new StringBuilder();

        result.append("{\"total\":");
        result.append(meta.getAttributeNames().size());
        result.append(",\"data\":[");

        boolean first = true;

        for (String attrName : meta.getAttributeNames()) {
            IMetaAttribute metaAttribute = meta.getMetaAttribute(attrName);

            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append("{");

            result.append("\"name\":");
            result.append("\"");
            result.append(attrName);
            result.append("\"");

            result.append(",\"title\":");
            result.append("\"");
            result.append(metaAttribute.getTitle());
            result.append("\"");

            result.append("}");
        }

        result.append("]}");

        // TODO

        return result.toString();
    }

    private String getParam(String name, ResourceRequest resourceRequest) {
        return PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest)).getParameter(name);
    }
}
