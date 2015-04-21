package kz.bsbnb.usci.porltet.entity_merge;

import com.google.gson.Gson;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.core.service.IBaseEntityMergeService;
import kz.bsbnb.usci.core.service.ISearcherFormService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseContainerType;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Pair;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class MainPortlet extends MVCPortlet {
    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBaseEntityMergeService entityMergeService;

    private RmiProxyFactoryBean searcherFormEntryServiceFactoryBean;
    private ISearcherFormService searcherFormService;

    void connectToServices() {
        try {
            RmiProxyFactoryBean metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
            metaFactoryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/metaFactoryService");
            metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);
            metaFactoryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            metaFactoryServiceFactoryBean.afterPropertiesSet();
            metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();

            RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IEntityService.class);
            entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityServiceFactoryBean.afterPropertiesSet();
            entityService = (IEntityService) entityServiceFactoryBean.getObject();

            RmiProxyFactoryBean entityMergeServiceFactoryBean = new RmiProxyFactoryBean();
            entityMergeServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1099/entityMergeService");
            entityMergeServiceFactoryBean.setServiceInterface(IBaseEntityMergeService.class);
            entityMergeServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityMergeServiceFactoryBean.afterPropertiesSet();
            entityMergeService = (IBaseEntityMergeService) entityMergeServiceFactoryBean.getObject();

            searcherFormEntryServiceFactoryBean = new RmiProxyFactoryBean();
            searcherFormEntryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/searcherFormService");
            searcherFormEntryServiceFactoryBean.setServiceInterface(ISearcherFormService.class);
            searcherFormEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            searcherFormEntryServiceFactoryBean.afterPropertiesSet();
            searcherFormService = (ISearcherFormService) searcherFormEntryServiceFactoryBean.getObject();

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
    public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
            throws IOException, PortletException {
        String entityId = getParam("entityId", renderRequest);

        renderRequest.setAttribute("entityId", entityId);

        super.doView(renderRequest, renderResponse);
    }

    private String testNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    private String clearSlashes(String str) {
        //TODO: str.replaceAll("\"","\\\""); does not work! Fix needed.
        String outStr = str.replaceAll("\"", " ");
        System.out.println(outStr);
        return outStr;
    }

    private String entityToJson(BaseEntity entityLeft, BaseEntity entityRight, String title, String code) {
        MetaClass meta = null;
        String idLeft = "";
        String idRight = "";
        if (entityLeft != null) {
            meta = entityLeft.getMeta();
            idLeft = Long.toString(entityLeft.getId());
        }

        if (entityRight != null) {
            meta = entityRight.getMeta();
            idRight = Long.toString(entityRight.getId());
        }

        if (title == null)
            title = code;

        if(meta == null)
            throw new NullPointerException("Meta is null");

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"valueLeft\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"valueRight\": \"" + clearSlashes(testNull(meta.getClassTitle())) + "\",";
        str += "\"simple\": false,";
        str += "\"array\": false,";
        str += "\"id_left\":  \"" + idLeft + "\", ";
        str += "\"id_right\": \"" + idRight + "\", ";
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

            if (entityLeft != null) {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if (entityRight != null) {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if (valueLeft != null) {
                valueLeftSubEntity = (BaseEntity) valueLeft.getValue();
            }

            if (valueRight != null) {
                valueRightSubEntity = (BaseEntity) valueRight.getValue();
            }

            if ((valueLeft != null && valueLeftSubEntity != null) ||
                    (valueRight != null && valueRightSubEntity != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str += entityToJson(valueLeftSubEntity, valueRightSubEntity,
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

            if (entityLeft != null) {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if (entityRight != null) {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if (valueLeft != null) {
                valueLeftSubSet = (BaseSet) valueLeft.getValue();
            }

            if (valueRight != null) {
                valueRightSubSet = (BaseSet) valueRight.getValue();
            }

            if ((valueLeft != null && valueLeftSubSet != null) ||
                    (valueRight != null && valueRightSubSet != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                str += setToJson(valueLeftSubSet, valueRightSubSet,
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

            if (entityLeft != null) {
                valueLeft = entityLeft.getBaseValue(innerClassesNames);
            }

            if (entityRight != null) {
                valueRight = entityRight.getBaseValue(innerClassesNames);
            }

            if (valueLeft != null) {
                valueLeftSubEntity = valueLeft.getValue();
            }

            if (valueRight != null) {
                valueRightSubEntity = valueRight.getValue();
            }


            if ((valueLeft != null && valueLeftSubEntity != null) ||
                    (valueRight != null && valueRightSubEntity != null)) {
                if (!first) {
                    str += ",";
                } else {
                    first = false;
                }

                if (((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() != DataTypes.DATE) {
                    String leftValueString = null;
                    String rightValueString = null;
                    if (valueLeftSubEntity != null) {
                        leftValueString = valueLeftSubEntity.toString();
                    }
                    if (valueRightSubEntity != null) {
                        rightValueString = valueRightSubEntity.toString();
                    }
                    str += "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"valueLeft\":\"" + clearSlashes(testNull(leftValueString)) + "\",\n" +
                            "\"valueRight\":\"" + clearSlashes(testNull(rightValueString)) + "\",\n" +
                            "\"array\": false,\n" +
                            "\"simple\": true,\n" +
                            "\"type\": \"" + ((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
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

                    str += "{" +
                            "\"title\":\"" + attrTitle + "\",\n" +
                            "\"code\":\"" + innerClassesNames + "\",\n" +
                            "\"valueLeft\":\"" + dtStrLeft + "\",\n" +
                            "\"valueRight\":\"" + dtStrRight + "\",\n" +
                            "\"array\": false,\n" +
                            "\"simple\": true,\n" +
                            "\"type\": \"" + ((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
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

        if (setLeft != null) {
            type = setLeft.getMemberType();
            setLeftSize = setLeft.get().size();
        }

        if (setRight != null) {
            type = setRight.getMemberType();
            setRightSize = setRight.get().size();
        }

        if (title == null)
            title = code;

        if(type == null)
            throw new NullPointerException("Type is null");

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

            IBaseValue valueLeft;
            IBaseValue valueRight;
            Iterator<IBaseValue> iteratorLeft = null;
            Iterator<IBaseValue> iteratorRight = null;

            if (setLeft != null) {
                iteratorLeft = setLeft.get().iterator();
            }
            if (setRight != null) {
                iteratorRight = setRight.get().iterator();
            }

            while ((iteratorLeft != null && iteratorLeft.hasNext()) ||
                    (iteratorRight != null && iteratorRight.hasNext())) {
                valueLeft = null;
                valueRight = null;
                BaseEntity valueLeftSubEntity = null;
                BaseEntity valueRightSubEntity = null;

                if (iteratorLeft != null && iteratorLeft.hasNext()) {
                    valueLeft = iteratorLeft.next();
                }

                if (iteratorRight != null && iteratorRight.hasNext()) {
                    valueRight = iteratorRight.next();
                }

                if (valueLeft != null) {
                    valueLeftSubEntity = (BaseEntity) valueLeft.getValue();
                }

                if (valueRight != null) {
                    valueRightSubEntity = (BaseEntity) valueRight.getValue();
                }

                if ((valueLeft != null && valueLeftSubEntity != null) ||
                        (valueRight != null && valueRightSubEntity != null)) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    str += entityToJson(valueLeftSubEntity, valueRightSubEntity, "[" + i + "]",
                            "[" + i + "]");
                    i++;
                }

            }

        } else {

            IBaseValue valueLeft;
            IBaseValue valueRight;
            Iterator<IBaseValue> iteratorLeft = null;
            Iterator<IBaseValue> iteratorRight = null;

            if (setLeft != null) {
                iteratorLeft = setLeft.get().iterator();
            }
            if (setRight != null) {
                iteratorRight = setRight.get().iterator();
            }

            while ((iteratorLeft != null && iteratorLeft.hasNext()) ||
                    (iteratorRight != null && iteratorRight.hasNext())) {
                valueLeft = null;
                valueRight = null;
                Object valueLeftSubEntity = null;
                Object valueRightSubEntity = null;

                if (iteratorLeft != null && iteratorLeft.hasNext()) {
                    valueLeft = iteratorLeft.next();
                }

                if (iteratorRight != null && iteratorRight.hasNext()) {
                    valueRight = iteratorRight.next();
                }

                if (valueLeft != null) {
                    valueLeftSubEntity = valueLeft.getValue();
                }

                if (valueRight != null) {
                    valueRightSubEntity = valueRight.getValue();
                }

                if ((valueLeft != null && valueLeftSubEntity != null) ||
                        (valueRight != null && valueRightSubEntity != null)) {
                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    if (((MetaValue) type).getTypeCode() != DataTypes.DATE) {
                        String leftValueString = null;
                        String rightValueString = null;
                        if (valueLeftSubEntity != null) {
                            leftValueString = valueLeftSubEntity.toString();
                        }
                        if (valueRightSubEntity != null) {
                            rightValueString = valueRightSubEntity.toString();
                        }
                        str += "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"valueLeft\":\"" + clearSlashes(testNull(leftValueString)) + "\",\n" +
                                "\"valueRight\":\"" + clearSlashes(testNull(rightValueString)) + "\",\n" +
                                "\"simple\": true,\n" +
                                "\"array\": false,\n" +
                                "\"type\": \"" + ((MetaValue) type).getTypeCode() + "\",\n" +
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

                        str += "{" +
                                "\"title\":\"" + "[" + i + "]" + "\",\n" +
                                "\"code\":\"" + "[" + i + "]" + "\",\n" +
                                "\"valueLeft\":\"" + dtStrLeft + "\",\n" +
                                "\"valueRight\":\"" + dtStrRight + "\",\n" +
                                "\"simple\": true,\n" +
                                "\"array\": false,\n" +
                                "\"type\": \"" + ((MetaValue) type).getTypeCode() + "\",\n" +
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
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {

        if (metaFactoryService == null || entityService == null || entityMergeService == null)
            throw new NullPointerException("Services are null");


        PrintWriter writer = resourceResponse.getWriter();

        try {
            OperationTypes operationType = OperationTypes.valueOf(getParam("op", resourceRequest));
            User currentUser = PortalUtil.getUser(resourceRequest);

            Gson gson = new Gson();

            switch (operationType) {
                case SAVE_JSON: {
                    String json = resourceRequest.getParameter("json_data");
                    String leftEntity = resourceRequest.getParameter("leftEntityId");
                    String leftReportDt = resourceRequest.getParameter("leftReportDate");
                    String rightEntity = resourceRequest.getParameter("rightEntityId");
                    String rightReportDt = resourceRequest.getParameter("rightReportDate");
                    String deleteUnused = resourceRequest.getParameter("deleteUnused");

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                    Date leftRD = df.parse(leftReportDt);
                    Date rightRD = df.parse(rightReportDt);

                    System.out.println(json);
                    System.out.println("\n THE LEFT ENTITY ID: " + leftEntity);
                    System.out.println("\n THE RIGHT ENTITY ID: " + rightEntity);
                    System.out.println("\n THE LEFT ENTITY REPORT DATE: " + leftReportDt);
                    System.out.println("\n THE RIGHT ENTITY REPORT DATE: " + rightReportDt);
                    System.out.println("\n DELETE UNUSED: " + deleteUnused);

                    entityMergeService.mergeBaseEntities(Long.parseLong(leftEntity), Long.parseLong(rightEntity),
                            leftRD, rightRD, json, "true".equals(deleteUnused));

                    writer.write("{\"success\": true }");

                    break;
                }
                case GET_FORM:
                    String metaName = resourceRequest.getParameter("meta");

                    String generatedForm = searcherFormService.getDom(currentUser.getUserId(),
                            metaFactoryService.getMetaClass(metaName));

                    writer.write(generatedForm);
                    break;
                case FIND_ACTION:
                    Enumeration<String> list = resourceRequest.getParameterNames();

                    String metaString = resourceRequest.getParameter("metaClass");

                    MetaClass metaClass = metaFactoryService.getMetaClass(metaString);
                    BaseEntity baseEntity = new BaseEntity(metaClass, new Date());

                    while(list.hasMoreElements()) {
                        String attribute = list.nextElement();

                        if(attribute.equals("op") || attribute.equals("metaClass"))
                            continue;

                        Object value;
                        String parameterValue = resourceRequest.getParameter(attribute);

                        IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
                        if(metaAttribute == null)
                            continue;
                        IMetaType metaType = metaAttribute.getMetaType();

                        if(metaType.isSet() || metaType.isSetOfSets())
                            throw new UnsupportedOperationException("Not yet implemented");

                        if(metaType.isComplex()) {
                            BaseEntity childBaseEntity = new BaseEntity((MetaClass) metaType, new Date());
                            childBaseEntity.setId(Long.valueOf(parameterValue));
                            value = childBaseEntity;

                        } else {
                            MetaValue metaValue = (MetaValue) metaType;
                            value = DataTypes.fromString(metaValue.getTypeCode(), parameterValue);
                        }

                        Batch b = new Batch(new Date(), currentUser.getUserId());
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
                case LIST_CLASSES:
                    Long userId = 0L;

                    if(currentUser != null)
                        userId = currentUser.getUserId();

                    List<Pair> classes = searcherFormService.getMetaClasses(userId);

                    if(classes.size() < 1)
                        throw new RuntimeException("no.any.rights");

                    writer.write("{\"success\":\"true\", \"data\": " + gson.toJson(classes) + "}");
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
                case LIST_ENTITY: {
                    String leftEntityId = resourceRequest.getParameter("leftEntityId");
                    String leftReportDate = resourceRequest.getParameter("leftReportDate");
                    String rightEntityId = resourceRequest.getParameter("rightEntityId");
                    String rightReportDate = resourceRequest.getParameter("rightReportDate");

                    System.out.println("\n THE LEFT ENTITY ID: " + leftEntityId);
                    System.out.println("\n THE RIGHT ENTITY ID: " + rightEntityId);
                    System.out.println("\n THE LEFT ENTITY REPORT DATE: " + leftReportDate);
                    System.out.println("\n THE RIGHT ENTITY REPORT DATE: " + rightReportDate);

                    if ((leftEntityId != null && leftEntityId.trim().length() > 0) &&
                            (rightEntityId != null && rightEntityId.trim().length() > 0) &&
                            StringUtils.isNotEmpty(leftReportDate) && StringUtils.isNotEmpty(rightReportDate)) {

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date leftRD = df.   parse(leftReportDate);
                        Date rightRD = df.parse(rightReportDate);

                        BaseEntity entityLeft = entityService.load(Integer.parseInt(leftEntityId), leftRD);
                        BaseEntity entityRight = entityService.load(Integer.parseInt(rightEntityId), rightRD);

                        writer.write("{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entityLeft, entityRight, entityLeft.getMeta().getClassTitle(),
                                        entityLeft.getMeta().getClassName()) +
                                "]}");
                    }
                    break;
                }
                case GET_CANDIDATES:
                    // DUMMY DATA
                    writer.write("[{'type': 'credit', 'name_1':'0128', 'name_2': '02121', 'id_1':'5214', 'id_2':'36598'}, " +
                            "{'type': 'person', 'name_1':'Захар Петрович Басов', 'name_2': 'Петр Васильевич Франкенштейн', 'id_1':'333', 'id_2':'222'}," +
                            "{'type': 'doc', 'name_1':'важный документ', 'name_2': 'неважный документ', 'id_1':'123', 'id_2':'321'}]");
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("{\"success\": false, \"errorMessage\": \"" + e.getMessage() + "\"}");
        }
    }

    public String getParam(String name, RenderRequest request) {
        if (request.getParameter(name) != null)
            return request.getParameter(name);

        return PortalUtil.getOriginalServletRequest(PortalUtil.
                getHttpServletRequest(request)).getParameter(name);
    }

    String getParam(String name, ResourceRequest request) {
        if (request.getParameter(name) != null)
            return request.getParameter(name);

        return PortalUtil.getOriginalServletRequest(PortalUtil.
                getHttpServletRequest(request)).getParameter(name);
    }

    enum OperationTypes {
        LIST_CLASSES,
        LIST_ENTITY,
        SAVE_JSON,
        GET_FORM,
        FIND_ACTION,
        LIST_BY_CLASS,
        GET_CANDIDATES,
        NULL
    }
}
