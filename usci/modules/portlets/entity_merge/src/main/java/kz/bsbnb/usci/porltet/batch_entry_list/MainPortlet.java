package kz.bsbnb.usci.porltet.batch_entry_list;

import com.google.gson.Gson;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBaseEntityMergeService;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.porltet.batch_entry_list.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.batch_entry_list.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
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
    private RmiProxyFactoryBean entityMergeServiceFactoryBean;

    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBaseEntityMergeService entityMergeService;

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

            entityMergeServiceFactoryBean = new RmiProxyFactoryBean();
            entityMergeServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/entityMergeService");
            entityMergeServiceFactoryBean.setServiceInterface(IBaseEntityMergeService.class);
            entityMergeServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityMergeServiceFactoryBean.afterPropertiesSet();
            entityMergeService = (IBaseEntityMergeService) entityMergeServiceFactoryBean.getObject();
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
        SAVE_JSON,
        LIST_BY_CLASS
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


    private String entityToJson(BaseEntity entityLeft, BaseEntity entityRight, String title, String code) {

        MetaClass meta = null;
        String idLeft = "";
        String idRight = "";
        if(entityLeft != null){
            meta = entityLeft.getMeta();
            idLeft = Long.toString(entityLeft.getId());
        }
        if(entityRight != null)
        {
            meta = entityRight.getMeta();
            idRight = Long.toString(entityRight.getId());
        }

        if (title == null) {
            title = code;
        }

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"valueLeft\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"valueRight\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"simple\": false,";
        str += "\"array\": false,";
        str += "\"id_left\":  \"" + idLeft + "\", ";
        str += "\"id_right\": \"" + idRight +"\", ";
        str += "\"type\": \"META_CLASS\",";
        str += "\"iconCls\":\"folder\",";
        str += "\"children\":[";

        boolean first = true;

        for (String innerClassesNames : meta.getComplexAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue valueLeft = null;
            IBaseValue valueRight = null;
            BaseEntity valueLeftSubEntity = null;
            BaseEntity valueRightSubEntity = null;

            if(entityLeft != null)
            {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if(entityRight != null)
            {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if(valueLeft != null)
            {
                valueLeftSubEntity =(BaseEntity) valueLeft.getValue();
            }

            if(valueRight != null)
            {
                valueRightSubEntity = (BaseEntity) valueRight.getValue();
            }

            if ((valueLeft != null && valueLeftSubEntity != null) ||
                    (valueRight != null && valueRightSubEntity != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str +=  entityToJson(valueLeftSubEntity, valueRightSubEntity,
                        attrTitle, innerClassesNames);
            }

        }

        for (String innerClassesNames : meta.getComplexArrayAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue valueLeft = null;
            IBaseValue valueRight = null;
            BaseSet valueLeftSubSet = null;
            BaseSet valueRightSubSet = null;

            if(entityLeft != null)
            {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if(entityRight != null)
            {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if(valueLeft != null)
            {
                valueLeftSubSet =(BaseSet) valueLeft.getValue();
            }

            if(valueRight != null)
            {
                valueRightSubSet = (BaseSet) valueRight.getValue();
            }

            if ((valueLeft != null && valueLeftSubSet != null) ||
                    (valueRight != null && valueRightSubSet != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str +=  setToJson(valueLeftSubSet, valueRightSubSet,
                        attrTitle, innerClassesNames);
            }
        }

        for (String innerClassesNames : meta.getSimpleAttributesNames()) {
            String attrTitle = innerClassesNames;
            if (meta.getMetaAttribute(innerClassesNames).getTitle() != null &&
                    meta.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                attrTitle = meta.getMetaAttribute(innerClassesNames).getTitle();

            IBaseValue valueLeft = null;
            IBaseValue valueRight = null;
            Object valueLeftSubEntity = null;
            Object valueRightSubEntity = null;

            if(entityLeft != null)
            {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if(entityRight != null)
            {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if(valueLeft != null)
            {
                valueLeftSubEntity = valueLeft.getValue();
            }

            if(valueRight != null)
            {
                valueRightSubEntity = valueRight.getValue();
            }


            if ((valueLeft != null && valueLeftSubEntity != null) ||
                    (valueRight != null && valueRightSubEntity != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                if(((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() != DataTypes.DATE) {
                    String leftValueString = null;
                    String rightValueString = null;
                    if(valueLeftSubEntity != null){
                        leftValueString = valueLeftSubEntity.toString();
                    }
                    if(valueRightSubEntity != null){
                        rightValueString = valueRightSubEntity.toString();
                    }
                    str +=  "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"valueLeft\":\"" + clearSlashes(testNull(leftValueString)) + "\",\n" +
                            "\"valueRight\":\"" + clearSlashes(testNull(rightValueString)) + "\",\n" +
                            "\"array\": false,\n" +
                            "\"simple\": true,\n" +
                            "\"type\": \"" + ((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
                            "\"leaf\":true,\n" +
                            "\"iconCls\":\"file\"\n" +
                            "}";
                } else {
                    String dtStrLeft = "";
                    String dtStrRight = "";
                    if (valueLeftSubEntity != null) {
                        dtStrLeft = new SimpleDateFormat("dd.MM.yyyy").format(valueLeftSubEntity);
                    }

                    if (valueRightSubEntity != null) {
                        dtStrRight = new SimpleDateFormat("dd.MM.yyyy").format(valueRightSubEntity);
                    }

                    str +=  "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"valueLeft\":\"" + dtStrLeft + "\",\n" +
                            "\"valueRight\":\"" + dtStrRight + "\",\n" +
                            "\"array\": false,\n" +
                            "\"simple\": true,\n" +
                            "\"type\": \"" + ((MetaValue)meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
                            "\"leaf\":true,\n" +
                            "\"iconCls\":\"file\"\n" +
                            "}";
                }
            }
        }

        str += "]}";

        return str;
    }



    private String setToJson(BaseSet setLeft, BaseSet setRight, String title, String code) {

        IMetaType type = null;
        int setLeftSize = 0;
        int setRightSize = 0;

        if(setLeft != null)
        {
            type = setLeft.getMemberType();
            setLeftSize = setLeft.get().size();
        }

        if(setRight != null)
        {
            type = setRight.getMemberType();
            setRightSize = setRight.get().size();
        }

        if (title == null) {
            title = code;
        }


        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"valueLeft\": \"" + setLeftSize + "\",";
        str += "\"valueRight\": \"" + setRightSize + "\",";
        str += "\"simple\": false,";
        str += "\"array\": true,";
        str += "\"type\": \"META_SET\",";
        str += "\"mergeable\": \"true\",";
        str += "\"iconCls\":\"folder\",";
        str += "\"children\":[";

        boolean first = true;

        int i = 0;

        if (type.isComplex()) {

            IBaseValue valueLeft = null;
            IBaseValue valueRight = null;
            Iterator<IBaseValue> iteratorLeft = null;
            Iterator<IBaseValue> iteratorRight = null;

            if(setLeft != null){
                iteratorLeft = setLeft.get().iterator();
            }
            if(setRight != null){
                iteratorRight = setRight.get().iterator();
            }

            while((iteratorLeft != null && iteratorLeft.hasNext()) ||
                    (iteratorRight != null && iteratorRight.hasNext()))
            {
                valueLeft = null;
                valueRight = null;
                BaseEntity valueLeftSubEntity = null;
                BaseEntity valueRightSubEntity = null;

                if(iteratorLeft != null && iteratorLeft.hasNext())
                {
                    valueLeft = iteratorLeft.next();
                }

                if(iteratorRight != null && iteratorRight.hasNext())
                {
                    valueRight = iteratorRight.next();
                }

                if(valueLeft != null)
                {
                    valueLeftSubEntity =(BaseEntity) valueLeft.getValue();
                }

                if(valueRight != null)
                {
                    valueRightSubEntity = (BaseEntity) valueRight.getValue();
                }

                if ((valueLeft != null && valueLeftSubEntity != null) ||
                        (valueRight != null && valueRightSubEntity != null)) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    str +=  entityToJson(valueLeftSubEntity, valueRightSubEntity, "[" + i + "]",
                            "[" + i + "]");
                    i++;
                }

            }

        } else {

            IBaseValue valueLeft = null;
            IBaseValue valueRight = null;
            Iterator<IBaseValue> iteratorLeft = null;
            Iterator<IBaseValue> iteratorRight = null;

            if(setLeft != null){
                iteratorLeft = setLeft.get().iterator();
            }
            if(setRight != null){
                iteratorRight = setRight.get().iterator();
            }

            while((iteratorLeft != null && iteratorLeft.hasNext()) ||
                    (iteratorRight != null && iteratorRight.hasNext()))
            {
                valueLeft = null;
                valueRight = null;
                Object valueLeftSubEntity = null;
                Object valueRightSubEntity = null;

                if(iteratorLeft != null && iteratorLeft.hasNext())
                {
                    valueLeft = iteratorLeft.next();
                }

                if(iteratorRight != null && iteratorRight.hasNext())
                {
                    valueRight = iteratorRight.next();
                }

                if(valueLeft != null)
                {
                    valueLeftSubEntity = valueLeft.getValue();
                }

                if(valueRight != null)
                {
                    valueRightSubEntity = valueRight.getValue();
                }

                if ((valueLeft != null && valueLeftSubEntity != null) ||
                        (valueRight != null && valueRightSubEntity != null)) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    if(((MetaValue)type).getTypeCode() != DataTypes.DATE)
                    {
                        String leftValueString = null;
                        String rightValueString = null;
                        if(valueLeftSubEntity != null){
                            leftValueString = valueLeftSubEntity.toString();
                        }
                        if(valueRightSubEntity != null){
                            rightValueString = valueRightSubEntity.toString();
                        }
                        str +=  "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"valueLeft\":\"" + clearSlashes(testNull(leftValueString)) + "\",\n" +
                                "\"valueRight\":\"" + clearSlashes(testNull(rightValueString)) + "\",\n" +
                                "\"simple\": true,\n" +
                                "\"array\": false,\n" +
                                "\"type\": \"" + ((MetaValue)type).getTypeCode() + "\",\n" +
                                "\"leaf\":true,\n" +
                                "\"iconCls\":\"file\"\n" +
                                "}";
                    } else {

                        String dtStrLeft = "";
                        String dtStrRight = "";
                        if (valueLeftSubEntity != null) {
                            dtStrLeft = new SimpleDateFormat("dd.MM.yyyy").format(valueLeftSubEntity);
                        }

                        if (valueRightSubEntity != null) {
                            dtStrRight = new SimpleDateFormat("dd.MM.yyyy").format(valueRightSubEntity);
                        }

                        str +=  "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"valueLeft\":\"" + dtStrLeft + "\",\n" +
                                "\"valueRight\":\"" + dtStrRight + "\",\n" +
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
            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));

            Gson gson = new Gson();

            switch (operationType) {
                case SAVE_JSON:
                    String json = resourceRequest.getParameter("json_data");
                    String leftEntity = resourceRequest.getParameter("leftEntityId");
                    String rightEntity = resourceRequest.getParameter("rightEntityId");
                    System.out.println(json);
                    System.out.println("\n THE LEFT ENTITY ID: "+leftEntity);
                    System.out.println("\n THE RIGHT ENTITY ID: "+rightEntity);
                    entityMergeService.mergeBaseEntities(Long.parseLong(leftEntity), Long.parseLong(rightEntity), json);

                    writer.write("{\"success\": true }");

                    break;
                case LIST_CLASSES:
                    MetaClassList classesListJson = new MetaClassList();
                    List<MetaClassName> metaClassesList = metaFactoryService.getMetaClassesNames();

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
                    String leftEntityId = resourceRequest.getParameter("leftEntityId");
                    String rightEntityId = resourceRequest.getParameter("rightEntityId");
                    System.out.println("\n >>>>>>>>>>>>>> RECEIVED: "+leftEntityId +"  "+rightEntityId);
                    if ((leftEntityId != null && leftEntityId.trim().length() > 0)&&
                            (rightEntityId != null && rightEntityId.trim().length() > 0)){
                        BaseEntity entityLeft = entityService.load(Integer.parseInt(leftEntityId));
                        BaseEntity entityRight = entityService.load(Integer.parseInt(rightEntityId));

                        writer.write("{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entityLeft, entityRight, entityLeft.getMeta().getClassTitle(),
                                        entityLeft.getMeta().getClassName()) +
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
