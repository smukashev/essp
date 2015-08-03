package kz.bsbnb.usci.porltet.meta_editor;

import com.google.gson.Gson;
import com.liferay.portal.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.porltet.meta_editor.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.meta_editor.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.log4j.helpers.OptionConverter;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MainPortlet extends MVCPortlet {
    private RmiProxyFactoryBean metaFactoryServiceFactoryBean;

    private IMetaFactoryService metaFactoryService;

    public void connectToServices() {
        try {
            metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
            metaFactoryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/metaFactoryService");
            metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);
            metaFactoryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

            metaFactoryServiceFactoryBean.afterPropertiesSet();
            metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();
        } catch (Exception e) {
            System.out.println("Can't initialise services: " + e.getMessage());
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
       //renderRequest.setAttribute("entityList", baseEntityList);

        boolean hasRights = false;

        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if(user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("NationalBankEmployee"))
                        hasRights = true;
                }
            }
        } catch (PortalException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (com.liferay.portal.kernel.exception.PortalException e) {
            e.printStackTrace();
        }

        if(!hasRights)
            return;

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_ALL,
        LIST_CLASS,
        SAVE_CLASS,
        DEL_CLASS,
        SAVE_ATTR,
        GET_ATTR,
        DEL_ATTR
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
                case LIST_ALL:
                    MetaClassList classesListJson = new MetaClassList();
                    List<MetaClassName> metaClassesList = metaFactoryService.getMetaClassesNames();

                    classesListJson.setTotal(metaClassesList.size());

                    for (MetaClassName metaName : metaClassesList) {
                        MetaClassListEntry metaClassListEntry = new MetaClassListEntry();
                        if(metaName.getIsDisabled()==1)
                            continue;
                        metaClassListEntry.setClassId(metaName.getClassName());
                        if(metaName.getClassTitle() != null
                                && metaName.getClassTitle().trim().length() > 0)
                            metaClassListEntry.setClassName(metaName.getClassTitle());
                        else
                            metaClassListEntry.setClassName(metaName.getClassName());

                        classesListJson.getData().add(metaClassListEntry);
                    }

                    writer.write(gson.toJson(classesListJson));

                    break;
                case LIST_CLASS:
                    String node = resourceRequest.getParameter("node");
                    if (node != null && node.trim().length() > 0) {
                        //writer.write("[{\"text\":\"ComponentLoader.js\",\"id\":\"src\\/ComponentLoader.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"grid\",\"id\":\"src\\/grid\",\"cls\":\"folder\"},{\"text\":\"ZIndexManager.js\",\"id\":\"src\\/ZIndexManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Img.js\",\"id\":\"src\\/Img.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"ComponentManager.js\",\"id\":\"src\\/ComponentManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"core\",\"id\":\"src\\/core\",\"cls\":\"folder\"},{\"text\":\"data\",\"id\":\"src\\/data\",\"cls\":\"folder\"},{\"text\":\"tip\",\"id\":\"src\\/tip\",\"cls\":\"folder\"},{\"text\":\"app\",\"id\":\"src\\/app\",\"cls\":\"folder\"},{\"text\":\"Shadow.js\",\"id\":\"src\\/Shadow.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Action.js\",\"id\":\"src\\/Action.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"button\",\"id\":\"src\\/button\",\"cls\":\"folder\"},{\"text\":\"util\",\"id\":\"src\\/util\",\"cls\":\"folder\"},{\"text\":\"draw\",\"id\":\"src\\/draw\",\"cls\":\"folder\"},{\"text\":\"slider\",\"id\":\"src\\/slider\",\"cls\":\"folder\"},{\"text\":\"PluginManager.js\",\"id\":\"src\\/PluginManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"FocusManager.js\",\"id\":\"src\\/FocusManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"AbstractPlugin.js\",\"id\":\"src\\/AbstractPlugin.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Template.js\",\"id\":\"src\\/Template.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"tab\",\"id\":\"src\\/tab\",\"cls\":\"folder\"},{\"text\":\"ComponentQuery.js\",\"id\":\"src\\/ComponentQuery.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"chart\",\"id\":\"src\\/chart\",\"cls\":\"folder\"},{\"text\":\"container\",\"id\":\"src\\/container\",\"cls\":\"folder\"},{\"text\":\"ModelManager.js\",\"id\":\"src\\/ModelManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"ElementLoader.js\",\"id\":\"src\\/ElementLoader.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"XTemplate.js\",\"id\":\"src\\/XTemplate.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"ShadowPool.js\",\"id\":\"src\\/ShadowPool.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Ajax.js\",\"id\":\"src\\/Ajax.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Layer.js\",\"id\":\"src\\/Layer.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"state\",\"id\":\"src\\/state\",\"cls\":\"folder\"},{\"text\":\"AbstractManager.js\",\"id\":\"src\\/AbstractManager.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"form\",\"id\":\"src\\/form\",\"cls\":\"folder\"},{\"text\":\"Component.js\",\"id\":\"src\\/Component.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"view\",\"id\":\"src\\/view\",\"cls\":\"folder\"},{\"text\":\"panel\",\"id\":\"src\\/panel\",\"cls\":\"folder\"},{\"text\":\"LoadMask.js\",\"id\":\"src\\/LoadMask.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"toolbar\",\"id\":\"src\\/toolbar\",\"cls\":\"folder\"},{\"text\":\"picker\",\"id\":\"src\\/picker\",\"cls\":\"folder\"},{\"text\":\"window\",\"id\":\"src\\/window\",\"cls\":\"folder\"},{\"text\":\"fx\",\"id\":\"src\\/fx\",\"cls\":\"folder\"},{\"text\":\"resizer\",\"id\":\"src\\/resizer\",\"cls\":\"folder\"},{\"text\":\"selection\",\"id\":\"src\\/selection\",\"cls\":\"folder\"},{\"text\":\"ProgressBar.js\",\"id\":\"src\\/ProgressBar.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"dd\",\"id\":\"src\\/dd\",\"cls\":\"folder\"},{\"text\":\"tree\",\"id\":\"src\\/tree\",\"cls\":\"folder\"},{\"text\":\"menu\",\"id\":\"src\\/menu\",\"cls\":\"folder\"},{\"text\":\"AbstractComponent.js\",\"id\":\"src\\/AbstractComponent.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"Editor.js\",\"id\":\"src\\/Editor.js\",\"leaf\":true,\"cls\":\"file\"},{\"text\":\"layout\",\"id\":\"src\\/layout\",\"cls\":\"folder\"},{\"text\":\"direct\",\"id\":\"src\\/direct\",\"cls\":\"folder\"},{\"text\":\"flash\",\"id\":\"src\\/flash\",\"cls\":\"folder\"}]");
                        int dotIndex = node.indexOf(".");
                        String className = "";
                        String attrName = "";
                        if (dotIndex < 0) {
                            className = node;
                        } else {
                            className = node.substring(0, dotIndex);
                            attrName = node.substring(dotIndex + 1);
                        }

                        MetaClass meta = metaFactoryService.getMetaClass(className);
                        IMetaType attribute = meta;

                        if (attrName.length() > 0) {
                            attribute = meta.getEl(attrName);
                        }

                        writer.write("[");
                        if (!attribute.isSet()) {
                            if (attribute.isComplex()) {
                                MetaClass attrMetaClass = (MetaClass)attribute;

                                boolean first = true;

                                for (String innerClassesNames : attrMetaClass.getComplexAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (attrMetaClass.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            attrMetaClass.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = attrMetaClass.getMetaAttribute(innerClassesNames).getTitle();

                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"cls\":\"folder\"}");

                                }

                                for (String innerClassesNames : attrMetaClass.getComplexArrayAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (attrMetaClass.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            attrMetaClass.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = attrMetaClass.getMetaAttribute(innerClassesNames).getTitle();

                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"cls\":\"folder\"}");

                                }

                                for (String innerClassesNames : attrMetaClass.getSimpleSetAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (attrMetaClass.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            attrMetaClass.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = attrMetaClass.getMetaAttribute(innerClassesNames).getTitle();


                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"leaf\":true,\"cls\":\"file\"}");
                                }

                                for (String innerClassesNames : attrMetaClass.getSimpleAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (attrMetaClass.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            attrMetaClass.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = attrMetaClass.getMetaAttribute(innerClassesNames).getTitle();

                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"leaf\":true,\"cls\":\"file\"}");
                                }
                            }
                        } else {
                            MetaSet attrMetaSet = (MetaSet)attribute;

                            if (attrMetaSet.getMemberType().isComplex()) {
                                MetaClass metaClassFromSet = (MetaClass)attrMetaSet.getMemberType();

                                boolean first = true;

                                for (String innerClassesNames : metaClassFromSet.getComplexAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle();

                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"cls\":\"folder\"}");

                                }

                                for (String innerClassesNames : metaClassFromSet.getSimpleAttributesNames()) {
                                    if (!first) {
                                        writer.write(",");
                                    } else {
                                        first = false;
                                    }

                                    String attrTitle = innerClassesNames;
                                    if (metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle() != null &&
                                            metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle().trim().length() > 0)
                                        attrTitle = metaClassFromSet.getMetaAttribute(innerClassesNames).getTitle();

                                    writer.write("{\"text\":\"" +
                                            attrTitle +
                                            "\",\"id\":\"" + node + "." + innerClassesNames +
                                            "\",\"leaf\":true,\"cls\":\"file\"}");
                                }
                            }
                        }
                        writer.write("]");
                    }
                    break;
                case SAVE_CLASS:
                    String classId = resourceRequest.getParameter("classId");
                    if (classId != null && classId.trim().length() > 0) {
                        String className = resourceRequest.getParameter("className");
                        MetaClass meta = null;
                        try {
                            meta = metaFactoryService.getMetaClass(classId);
                        } catch (IllegalArgumentException ex) {}


                        if (meta == null) {
                            meta = new MetaClass(classId);
                        }

                        meta.setClassTitle(className);
                       // meta.setClassName(classId);


                        metaFactoryService.saveMetaClass(meta);
                        writer.write("{\"success\": true, \"data\": {\"id\":\"" + classId + "\"," +
                                "\"name\":\"" + className + "\"}}");
                    } else {
                        writer.write("{\"success\": false, \"errorMessage\": \"Не задан класс\"}");
                    }
                    break;
                case SAVE_ATTR:
                    String attrPath = resourceRequest.getParameter("attrPathPart");
                    if (attrPath != null && attrPath.trim().length() > 0) {
                        int dotIndex = attrPath.indexOf(".");
                        String className = "";
                        String attrName = "";
                        Boolean is_key  = false;
                        Boolean is_required  = false;
                        Boolean is_nullable  = false;
                        if (dotIndex < 0) {
                            className = attrPath;
                        } else {
                            className = attrPath.substring(0, dotIndex);
                            attrName = attrPath.substring(dotIndex + 1);
                        }

                        MetaClass meta = metaFactoryService.getMetaClass(className);
                        IMetaType attribute = meta;

                        if (attrName.length() > 0) {
                            attribute = meta.getEl(attrName);
                        }

                        if(attribute.isComplex()) {
                            MetaClass metaParent = (MetaClass)attribute;

                            int attrType = Integer.parseInt(resourceRequest.getParameter("attrType"));
                            String attrPathCode = resourceRequest.getParameter("attrPathCode");

                            IMetaType typeToAdd = null;

                            switch (attrType) {
                                case 1:
                                    String attrSimpleType = resourceRequest.getParameter("attrSimpleType");
                                    typeToAdd = new MetaValue(DataTypes.valueOf(attrSimpleType));

                                    break;
                                case 2:
                                    String attrComplexType = resourceRequest.getParameter("attrComplexType");
                                    MetaClass metaOfNewAttr =
                                            metaFactoryService.getMetaClass(attrComplexType);

                                    typeToAdd = metaOfNewAttr;

                                    break;
                                case 3:
                                    attrSimpleType = resourceRequest.getParameter("attrSimpleType");
                                    typeToAdd = new MetaSet(
                                            new MetaValue(DataTypes.valueOf(attrSimpleType)));

                                    break;
                                case 4:
                                    attrComplexType = resourceRequest.getParameter("attrComplexType");
                                    metaOfNewAttr =
                                            metaFactoryService.getMetaClass(attrComplexType);

                                    typeToAdd = new MetaSet(metaOfNewAttr);

                                    break;
                                default:
                                    break;
                            }

                            if (typeToAdd != null) {
                                MetaAttribute attrToAdd = new MetaAttribute(false, false, typeToAdd);
                                attrToAdd.setTitle(resourceRequest.getParameter("attrTitle"));

                                is_key  = Boolean.parseBoolean(resourceRequest.getParameter("is_Key"));
                                is_required  = Boolean.parseBoolean(resourceRequest.getParameter("is_Required"));
                                is_nullable  = Boolean.parseBoolean(resourceRequest.getParameter("is_Nullable"));
                                attrToAdd.setKey(is_key);
                                attrToAdd.setRequired(is_required);
                                attrToAdd.setNullable(is_nullable);
                                metaParent.setMetaAttribute(attrPathCode, attrToAdd);

                            }

                            metaFactoryService.saveMetaClass(metaParent);

                            writer.write("{\"success\": true, \"data\": {}}");
                        } else {
                            writer.write("{\"success\": false, " +
                                    "\"errorMessage\": \"Путь не указывает на класс\"}");
                        }
                    } else {
                        writer.write("{\"success\": false, \"errorMessage\": \"Не задан аттрибут\"}");
                    }

                    break;
                case DEL_CLASS:
                    classId = resourceRequest.getParameter("classId");
                    if (classId != null && classId.trim().length() > 0) {
                        metaFactoryService.delMetaClass(classId);
                    }
                    writer.write("{\"success\": true, \"data\": {}}");
                    break;
                case GET_ATTR:
                    attrPath = resourceRequest.getParameter("attrPath");
                    if (attrPath != null && attrPath.trim().length() > 0) {
                        int dotIndex = attrPath.indexOf(".");
                        String className = "";
                        String attrName = "";
                        if (dotIndex < 0) {
                            className = attrPath;
                        } else {
                            className = attrPath.substring(0, dotIndex);
                            attrName = attrPath.substring(dotIndex + 1);
                        }

                        MetaClass meta = metaFactoryService.getMetaClass(className);
                        IMetaType attribute = meta;
                        String title = meta.getClassTitle();

                        if (attrName.length() > 0) {
                            attribute = meta.getEl(attrName);
                            title =  meta.getMetaAttribute(attrName).getTitle();
                        }

                        if (!attribute.isSet()) {
                            if (attribute.isComplex()) {
                                MetaClass value = (MetaClass)attribute;
                                writer.write("{\"success\": true, \"data\": {");

                                writer.write("\"type\": 2, ");
                                writer.write("\"title\": \"" +
                                        title + "\", ");
                                writer.write("\"complexType\": \"" + value.getClassName() + "\", ");
                                writer.write("\"is_key\": \"" + meta.getMetaAttribute(attrName).isKey() + "\", ");
                                writer.write("\"is_required\": \"" + meta.getMetaAttribute(attrName).isRequired() + "\", ");
                                writer.write("\"is_nullable\": \"" + meta.getMetaAttribute(attrName).isNullable() + "\"");
                            } else {
                                MetaValue value = (MetaValue)attribute;

                                writer.write("{\"success\": true, \"data\": {");

                                writer.write("\"type\": 1, ");
                                writer.write("\"title\": \"" +
                                        title + "\", ");
                                writer.write("\"simpleType\": \"" + value.getTypeCode() + "\", ");
                                writer.write("\"is_key\": \"" + meta.getMetaAttribute(attrName).isKey() + "\", ");
                                writer.write("\"is_required\": \"" + meta.getMetaAttribute(attrName).isRequired() + "\", ");
                                writer.write("\"is_nullable\": \"" + meta.getMetaAttribute(attrName).isNullable() + "\"");
                                writer.write("}}");
                            }
                        } else {
                            MetaSet attrMetaSet = (MetaSet)attribute;

                            if (attrMetaSet.getMemberType().isComplex()) {
                                writer.write("{\"success\": true, \"data\": {");

                                writer.write("\"type\": 4, ");
                                writer.write("\"title\": \"" +
                                        title + "\", ");
                                writer.write("\"complexType\": \"" +
                                        ((MetaClass)attrMetaSet.getMemberType()).getClassName() + "\", ");
                                writer.write("\"is_key\": \"" + meta.getMetaAttribute(attrName).isKey() + "\", ");
                                writer.write("\"is_required\": \"" + meta.getMetaAttribute(attrName).isRequired() + "\", ");
                                writer.write("\"is_nullable\": \"" + meta.getMetaAttribute(attrName).isNullable() + "\"");
                                writer.write("}}");
                            } else {
                                writer.write("{\"success\": true, \"data\": {");

                                writer.write("\"type\": 3, ");
                                writer.write("\"title\": \"" +
                                        title + "\", ");
                                writer.write("\"simpleType\": \"" + attrMetaSet.getTypeCode() + "\", ");
                                writer.write("\"is_key\": \"" + meta.getMetaAttribute(attrName).isKey() + "\", ");
                                writer.write("\"is_required\": \"" + meta.getMetaAttribute(attrName).isRequired() + "\", ");
                                writer.write("\"is_nullable\": \"" + meta.getMetaAttribute(attrName).isNullable() + "\"");
                                writer.write("}}");
                            }
                        }
                    } else {
                        writer.write("{\"success\": false, \"errorMessage\": \"Не задан аттрибут\"}");
                    }
                    break;
                case DEL_ATTR:
                    attrPath = resourceRequest.getParameter("attrPathPart");
                    if (attrPath != null && attrPath.trim().length() > 0) {
                        int dotIndex = attrPath.indexOf(".");
                        String className = "";
                        String attrName = "";
                        if (dotIndex < 0) {
                            className = attrPath;
                        } else {
                            className = attrPath.substring(0, dotIndex);
                            attrName = attrPath.substring(dotIndex + 1);
                        }

                        MetaClass meta = metaFactoryService.getMetaClass(className);
                        IMetaType attribute = meta;

                        if (attrName.length() > 0) {
                            attribute = meta.getEl(attrName);
                        }

                        String attrPathCode = resourceRequest.getParameter("attrPathCode");

                        if (attribute.isComplex()) {
                            MetaClass metaOfDel = (MetaClass)attribute;

                            metaOfDel.removeMemberType(attrPathCode);

                            metaFactoryService.saveMetaClass(metaOfDel);

                            writer.write("{\"success\": true, \"data\": {}}");
                        } else {
                            writer.write("{\"success\": false, " +
                                    "\"errorMessage\": \"Не верный путь аттрибута\"}");
                        }
                    } else {
                        writer.write("{\"success\": false, \"errorMessage\": \"Не задан аттрибут\"}");
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            writer.write("{\"success\": false, \"errorMessage\": \"" + e.getMessage() + "\"}");
        }
    }
}
