package kz.bsbnb.usci.porltet.entity_editor;

import com.google.gson.Gson;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liferay.util.portlet.PortletProps;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.form.ISearcherFormService;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.*;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainPortlet extends MVCPortlet {
    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBatchEntryService batchEntryService;
    private ISearcherFormService searcherFormService;
    private PortalUserBeanRemoteBusiness portalUserBusiness;

    public void connectToServices() {
        try {
            RmiProxyFactoryBean metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
            metaFactoryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1098/metaFactoryService");
            metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);
            metaFactoryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            metaFactoryServiceFactoryBean.afterPropertiesSet();
            metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();

            RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
            entityServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1098/entityService");
            entityServiceFactoryBean.setServiceInterface(IEntityService.class);
            entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            entityServiceFactoryBean.afterPropertiesSet();
            entityService = (IEntityService) entityServiceFactoryBean.getObject();

            RmiProxyFactoryBean batchEntryServiceFactoryBean = new RmiProxyFactoryBean();
            batchEntryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() + ":1099/batchEntryService");
            batchEntryServiceFactoryBean.setServiceInterface(IBatchEntryService.class);
            batchEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            batchEntryServiceFactoryBean.afterPropertiesSet();
            batchEntryService = (IBatchEntryService) batchEntryServiceFactoryBean.getObject();

            RmiProxyFactoryBean searcherFormEntryServiceFactoryBean = new RmiProxyFactoryBean();
            searcherFormEntryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1098/searcherFormService");
            searcherFormEntryServiceFactoryBean.setServiceInterface(ISearcherFormService.class);
            searcherFormEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            searcherFormEntryServiceFactoryBean.afterPropertiesSet();
            searcherFormService = (ISearcherFormService) searcherFormEntryServiceFactoryBean.getObject();

            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
                    + ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        } catch (Exception e) {
            System.out.println("Can\"t initialise services: " + e.getMessage());
        }
    }

    private final Logger logger = LoggerFactory.getLogger(MainPortlet.class);
    private List<String> classesFilter;

    @Override
    public void init() throws PortletException {
        connectToServices();

        classesFilter = new LinkedList<>();

        for(String s : PortletProps.get("classes.filter").split(",")) {
            classesFilter.add(s);
        }

        super.init();
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {

        HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(
                PortalUtil.getHttpServletRequest(renderRequest));

        boolean hasRights = false;

        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if(user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                            || role.getName().equals("NationalBankEmployee"))
                        hasRights = true;
                }
            }
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }

        if(!hasRights)
            return;


        String entityId = httpReq.getParameter("entityId");
        String sRepDate = httpReq.getParameter("repDate");
        renderRequest.setAttribute("entityId", entityId);
        renderRequest.setAttribute("repDate", sRepDate);

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_CLASSES,
        LIST_ENTITY,
        SAVE_XML,
        FIND_ACTION,
        GET_FORM,
        LIST_ATTRIBUTES,
        LIST_BY_CLASS_SHORT,
        LIST_CREDITORS
    }

    private String testNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    private String clearSlashes(String str) {
        String outStr = str.replaceAll("\"", "\\\\\"");
        return outStr;
    }

    private String entityToJson(BaseEntity entity, String title, String code, IMetaAttribute attr,
                                boolean asRoot,
                                boolean isNb,
                                long creditorId) {

        MetaClass meta = entity.getMeta();

        //credit check
        if(meta.getClassName().equals("credit") && !isNb) {
            BaseEntity creditor = (BaseEntity) entity.getEl("creditor");
            if(creditor.getId() != creditorId)
                throw new RuntimeException("нет прав для просмотра");
        }

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
        str += "\"isRequired\": " + (attr != null ? attr.isRequired() : false) + ",";
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
                        meta.getMetaAttribute(innerClassesNames), false, isNb, creditorId);
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

                str +=  setToJson((BaseSet) (value.getValue()), attrTitle, innerClassesNames, value.getMetaAttribute(),
                        isNb, creditorId);
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
                    "\"isKey\":\""+meta.getMetaAttribute(innerClassesNames).isKey()+"\",\n" +
                    "\"isRequired\":\""+meta.getMetaAttribute(innerClassesNames).isRequired()+"\"\n" +
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
                            "\"isKey\":\""+meta.getMetaAttribute(innerClassesNames).isKey()+"\",\n" +
                            "\"isRequired\":\""+meta.getMetaAttribute(innerClassesNames).isRequired()+"\"\n" +
                            "}";
                }
            }
        }

        str += "]}";

        return str;
    }

    private String setToJson(BaseSet set, String title, String code, IMetaAttribute attr,
                             boolean isNb,
                             long creditorId) {
        IMetaType type = set.getMemberType();

        if (title == null) {
            title = code;
        }

        String str = "{";

        str += "\"title\": \"" + title + "\",";
        str += "\"code\": \"" + code + "\",";
        str += "\"value\": \"" + set.get().size() + "\",";
        str += "\"simple\": " + !attr.getMetaType().isComplex() + ",";
        str += "\"array\": true,";
        str += "\"isKey\": " + attr.isKey() + ",";
        str += "\"type\": \"META_SET\",";
        str += "\"iconCls\":\"folder\",";

        {
            StringBuilder result = new StringBuilder();
            IMetaType memberType = set.getMemberType();

            if (memberType.isComplex()) {
                result.append("\"childMetaId\":");
                result.append("\"");
                result.append(((IMetaClass) memberType).getId());
                result.append("\",");
            }

            result.append("\"childType\":");
            result.append("\"");
            result.append(getMetaTypeStr(memberType));
            result.append("\",");

            str += result.toString();
        }

        str += "\"children\":[";

        boolean first = true;

        int i = 0;

        if (type.isComplex()) {
            for (IBaseValue value : set.get()) {
                if (value != null && value.getValue() != null) {

                    //bank relation check
                    try {
                        if(!isNb) {
                            if ("bank_relations".equals(attr.getName())) {
                                BaseEntity relation = (BaseEntity) value.getValue();
                                if (((BaseEntity) relation.getEl("creditor")).getId() != creditorId)
                                    continue;
                            }
                        }
                    } catch(Exception e) {
                        logger.error(e.getMessage());
                        // strict mode
                        continue;
                    }

                    if (!first) {
                        str += ",";
                    } else {
                        first = false;
                    }

                    str +=  entityToJson((BaseEntity)(value.getValue()), "[" + i + "]", "[" + i + "]",
                            null, false, isNb, creditorId);
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

    boolean retry = false;

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException
    {

        if (metaFactoryService == null) {
            connectToServices();
            //todo: add error message here
            if (metaFactoryService == null)
                return;
        }

        OutputStream out = resourceResponse.getPortletOutputStream();

        try {
            OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));
            User currentUser = PortalUtil.getUser(resourceRequest);
            List<Creditor> creditors;

            Gson gson = new Gson();

            switch (operationType) {
                case LIST_CLASSES:
                    //List<Pair> classes = searcherFormService.getMetaClasses(currentUser.getUserId());
                    List<String[]> classes = searcherFormService.getMetaClasses(currentUser.getUserId());

                    if(classes.size() < 1)
                        throw new RuntimeException("no.any.rights");
                    //List<Pair> afterFilter = new LinkedList<>();
                    List<String[]> afterFilter = new LinkedList<>();
                    /*for(Pair c : classes)
                        if(classesFilter.contains(c.getName()))
                            afterFilter.add(c);*/
                    for(String[]  c: classes)
                        if(classesFilter.contains(c[1]))
                            afterFilter.add(c);
                    //for(String[] c : classes)

                    out.write(JsonMaker.getCaptionedArray(afterFilter,
                            new String[]{"searchName", "metaName", "title"}).getBytes());

                    //writer.write(JsonMaker.getJson(afterFilter));
                    break;
                case GET_FORM:
                    //Long metaId = Long.valueOf(resourceRequest.getParameter("metaId"));
                    String searchClassName = resourceRequest.getParameter("search");
                    String metaName = resourceRequest.getParameter("metaName");

                    //String generatedForm = searcherFormService.getDom(currentUser.getUserId(), metaFactoryService.getMetaClass(metaId));
                    String generatedForm = searcherFormService.getDom(currentUser.getUserId(),
                            searchClassName, metaFactoryService.getMetaClass(metaName),"");
                    out.write(generatedForm.getBytes());
                    break;
                case FIND_ACTION:
                    Enumeration<String> list = resourceRequest.getParameterNames();
                    creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());

                    long creditorId;

                    if(creditors.size() == 0)
                        throw new RuntimeException("нет доступа к кредиторам");
                    else
                    if(creditors.size() > 0) {
                        logger.warn("доступ к более одному банку");
                    }

                    creditorId = creditors.get(0).getId();
                    metaName = resourceRequest.getParameter("metaClass");
                    MetaClass metaClass = metaFactoryService.getMetaClass(metaName);
                    HashMap<String,String> parameters = new HashMap<>();
                    searchClassName = resourceRequest.getParameter("searchName");

                    while(list.hasMoreElements()) {
                        String attribute = list.nextElement();
                        if(attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
                            continue;
                        parameters.put(attribute, resourceRequest.getParameter(attribute));
                    }

                    ISearchResult searchResult = searcherFormService.search(searchClassName, parameters, metaClass, "", creditorId);
                    Iterator<BaseEntity> cursor = searchResult.iterator();

                    long ret = -1;

                    if(cursor.hasNext()) {
                        ret = cursor.next().getId();
                        ret = ret > 0 ? ret : -1;
                    }

                    out.write(("{\"success\": true, \"data\":\"" + ret + "\"}").getBytes());

                    break;
                case LIST_BY_CLASS_SHORT:
                    String metaId = resourceRequest.getParameter("metaId");
                    RefListResponse refListResponse = entityService.getRefListResponse(Long.parseLong(metaId), null, false);
                    refListResponse = refListToShort(refListResponse);
                    String sJson = gson.toJson(refListResponse);
                    out.write(sJson.getBytes());
                    break;
                case SAVE_XML:
                    String xml = resourceRequest.getParameter("xml_data");
                    String sDate = resourceRequest.getParameter("date");
                    Date date = (Date) DataTypes.fromString(DataTypes.DATE, sDate);

                    BatchEntry batchEntry = new BatchEntry();

                    batchEntry.setValue(xml);
                    batchEntry.setRepDate(date);
                    batchEntry.setUserId(currentUser.getUserId());

                    batchEntryService.save(batchEntry);

                    out.write(("{\"success\": true }").getBytes());

                    break;
                case LIST_ATTRIBUTES:
                    metaId = resourceRequest.getParameter("metaId");

                    if (StringUtils.isNotEmpty(metaId)) {
                        metaClass = metaFactoryService.getMetaClass(Long.valueOf(metaId));
                        sJson = getAttributesJson(metaClass);
                        out.write(sJson.getBytes());
                    }

                    break;
                case LIST_CREDITORS:
                    creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());
                    out.write(JsonMaker.getJson(creditors).getBytes());
                    break;
                case LIST_ENTITY:
                    String entityId = resourceRequest.getParameter("entityId");
                    String asRootStr = resourceRequest.getParameter("asRoot");
                    boolean isNb = false;
                    creditorId = -1;

                    for(Role r : currentUser.getRoles())
                        if("NationalBankEmployee".equals(r.getDescriptiveName()) ||
                           "Administrator".equals(r.getDescriptiveName())) {
                            isNb = true;
                            break;
                        }

                    creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());

                    if(!isNb) {
                        if(creditors.size() > 1)
                            throw new RuntimeException("доступ к более одному банку");

                        if(creditors.size() == 0)
                            throw new RuntimeException("нет доступа к кредиторам");

                        creditorId = creditors.get(0).getId();
                    }

                    boolean asRoot = StringUtils.isNotEmpty(asRootStr) ? Boolean.valueOf(asRootStr) : false;

                    if (entityId != null && entityId.trim().length() > 0) {
                        //search by single Id
                        date = null;
                        if(resourceRequest.getParameter("date") != null)
                            date = (Date) DataTypes.fromString(DataTypes.DATE, resourceRequest.getParameter("date"));

                        if(date == null)
                            date = new Date();

                        BaseEntity entity = entityService.load(Integer.parseInt(entityId), date);

                        sJson = "{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entity, entity.getMeta().getClassTitle(),
                                        entity.getMeta().getClassName(), null, asRoot, isNb, creditorId) +
                                "]}";

                        out.write(sJson.getBytes());
                    } else {
                        searchClassName = resourceRequest.getParameter("searchName");
                        metaName = resourceRequest.getParameter("metaClass");
                        metaClass = metaFactoryService.getMetaClass(metaName);
                        creditorId = Long.parseLong(resourceRequest.getParameter("creditorId"));

                        list = resourceRequest.getParameterNames();
                        parameters = new HashMap<>();
                        while(list.hasMoreElements()) {
                            String attribute = list.nextElement();
                            if(attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
                                continue;
                            parameters.put(attribute, resourceRequest.getParameter(attribute));
                        }


                        searchResult = searcherFormService.search(searchClassName, parameters, metaClass, "", creditorId);

                        StringBuilder sb = new StringBuilder("{\"text\":\".\",\"children\": [\n");
                        Iterator<BaseEntity> it = searchResult.iterator();
                        do {
                            if(!it.hasNext())
                                break;
                            BaseEntity currentEntity = it.next();
                            sb.append(entityToJson(currentEntity, currentEntity.getMeta().getClassTitle(),
                                    currentEntity.getMeta().getClassName(), null, true, isNb, creditorId));

                            if(it.hasNext()) sb.append(",");
                        } while(true);

                        sb.append("]}");
                        out.write(sb.toString().getBytes());

                        if(1==1)
                            return;

                        //search by parameters

                        list = resourceRequest.getParameterNames();
                        metaName = resourceRequest.getParameter("metaClass");
                        searchClassName = resourceRequest.getParameter("searchName");

                        metaClass = metaFactoryService.getMetaClass(metaName);
                        parameters = new HashMap<String,String>();

                        while(list.hasMoreElements()) {
                            String attribute = list.nextElement();
                            if(attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
                                continue;
                            parameters.put(attribute, resourceRequest.getParameter(attribute));
                        }

                        searchResult = searcherFormService.search(searchClassName, parameters, metaClass, "", creditorId);
                        if(searchResult.getData() == null)
                            throw new IllegalArgumentException("ошибка сериализации");
                        /*
                        StringBuilder sb = new StringBuilder("{\"text\":\".\",\"children\": [\n");
                        Iterator<BaseEntity> it = searchResult.iterator();
                        do {
                            if(!it.hasNext())
                                break;
                            BaseEntity currentEntity = it.next();
                            sb.append(entityToJson(currentEntity, currentEntity.getMeta().getClassTitle(),
                                    currentEntity.getMeta().getClassName(), null, true, isNb, creditorId));

                            if(it.hasNext()) sb.append(",");
                        } while(true);*/

                        sb.append("]}");
                        out.write(sb.toString().getBytes());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            String originalError = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            if(originalError.contains("connect") || originalError.contains("rmi"))
                if(!retry) {
                    retry = true;
                    logger.info("connect failed, reconnect triggered");
                    try {
                        init();
                        serveResource(resourceRequest, resourceResponse);
                    } catch (PortletException e1) {
                        //resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
                        out.write(("{ \"success\": false, \"errorMessage\": \""+ originalError + e1.getMessage()
                                .replaceAll("\"","").replaceAll("\n","")+"\"}").getBytes());
                    } finally {
                        retry = false;
                        return;
                    }
                }

            out.write(("{\"success\": false, \"errorMessage\": \"" + originalError + "\"}").getBytes());
        }
    }

    private RefListResponse refListToShort(RefListResponse refListResponse) {
        List<Map<String, Object>> shortRows = new ArrayList<>();

        String titleKey = null;

        if (!refListResponse.getData().isEmpty()) {
            Set<String> keys = refListResponse.getData().get(0).keySet();

            if (keys.contains("name_ru"))
                titleKey = "name_ru";
            else if (keys.contains("name_kz"))
                titleKey = "name_kz";
            else if (keys.contains("name"))
                titleKey = "name";
        }

        for (Map<String, Object> row : refListResponse.getData()) {
            Object id = row.get("ID");
            Object title = titleKey != null ? row.get(titleKey) : "------------------------";

            Map<String, Object> shortRow = new HashMap<>();
            shortRow.put("ID", id);
            shortRow.put("title", title);
            shortRows.add(shortRow);
        }

        return new RefListResponse(shortRows);
    }

    private String getAttributesJson(IMetaClass meta) {
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

            result.append("\"code\":");
            result.append("\"");
            result.append(attrName);
            result.append("\"");

            result.append(",\"title\":");
            result.append("\"");
            result.append(metaAttribute.getTitle());
            result.append("\"");

            result.append(",\"isKey\":");
            result.append("\"");
            result.append(metaAttribute.isKey());
            result.append("\"");

            result.append(",\"isRequired\":");
            result.append("\"");
            result.append(metaAttribute.isRequired());
            result.append("\"");

            result.append(",\"array\":");
            result.append("\"");
            result.append(metaAttribute.getMetaType().isSet());
            result.append("\"");

            result.append(",\"simple\":");
            result.append("\"");
            result.append(!metaAttribute.getMetaType().isComplex());
            result.append("\"");

            result.append(",\"ref\":");
            result.append("\"");
            result.append(metaAttribute.getMetaType().isReference());
            result.append("\"");

            if (metaAttribute.getMetaType().isComplex() && !metaAttribute.getMetaType().isSet()) {
                result.append(",\"metaId\":");
                result.append("\"");
                result.append(((IMetaClass)metaAttribute.getMetaType()).getId());
                result.append("\"");
            }

            result.append(",\"type\":");
            result.append("\"");
            result.append(getMetaTypeStr(metaAttribute.getMetaType()));
            result.append("\"");

            if (metaAttribute.getMetaType().isSet()) {
                IMetaType memberType = ((IMetaSet) metaAttribute.getMetaType()).getMemberType();

                if (memberType.isComplex()) {
                    result.append(",\"childMetaId\":");
                    result.append("\"");
                    result.append(((IMetaClass) memberType).getId());
                    result.append("\"");
                }

                result.append(",\"childType\":");
                result.append("\"");
                result.append(getMetaTypeStr(memberType));
                result.append("\"");
            }

            result.append("}");
        }

        result.append("]}");

        return result.toString();
    }

    private String getMetaTypeStr(IMetaType metaType) {
        if (metaType.isSet())
            return "META_SET";
        else if (metaType.isComplex()) {
            return "META_CLASS";
        } else {
            return ((IMetaValue)metaType).getTypeCode().name();
        }
    }

}
