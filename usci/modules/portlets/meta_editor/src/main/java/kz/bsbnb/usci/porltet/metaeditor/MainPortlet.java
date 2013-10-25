package kz.bsbnb.usci.porltet.metaeditor;

import com.google.gson.Gson;
import com.liferay.util.bridges.mvc.MVCPortlet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.porltet.metaeditor.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.metaeditor.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author abukabayev
 */
public class MainPortlet extends MVCPortlet {
    private RmiProxyFactoryBean metaFactoryServiceFactoryBean;

    private IMetaFactoryService metaFactoryService;

    public void connectToServices() {
        try {
            metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
            metaFactoryServiceFactoryBean.setServiceUrl("rmi://127.0.0.1:1098/metaFactoryService");
            metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);

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
        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_ALL,
        LIST_CLASS,
        SAVE_CLASS,
        DEL_CLASS
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
        OperationTypes operationType = OperationTypes.valueOf(resourceRequest.getParameter("op"));

        Gson gson = new Gson();

        switch (operationType) {
            case LIST_ALL:
                MetaClassList classesListJson = new MetaClassList();
                List<MetaClassName> metaClassesList = metaFactoryService.getMetaClassesNames();

                classesListJson.setTotal(metaClassesList.size());

                for (MetaClassName metaName : metaClassesList) {
                    MetaClassListEntry metaClassListEntry = new MetaClassListEntry();

                    metaClassListEntry.setClassId(metaName.getClassName());
                    metaClassListEntry.setClassName(metaName.getClassName() + " имя");

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
                                writer.write("{\"text\":\"" + innerClassesNames +
                                        "\",\"id\":\"" + node + "." + innerClassesNames +
                                        "\",\"cls\":\"folder\"}");

                            }

                            for (String innerClassesNames : attrMetaClass.getComplexArrayAttributesNames()) {
                                if (!first) {
                                    writer.write(",");
                                } else {
                                    first = false;
                                }
                                writer.write("{\"text\":\"" + innerClassesNames +
                                        "\",\"id\":\"" + node + "." + innerClassesNames +
                                        "\",\"cls\":\"folder\"}");

                            }

                            for (String innerClassesNames : attrMetaClass.getSimpleSetAttributesNames()) {
                                if (!first) {
                                    writer.write(",");
                                } else {
                                    first = false;
                                }
                                writer.write("{\"text\":\"" + innerClassesNames +
                                        "\",\"id\":\"" + node + "." + innerClassesNames +
                                        "\",\"leaf\":true,\"cls\":\"file\"}");
                            }

                            for (String innerClassesNames : attrMetaClass.getSimpleAttributesNames()) {
                                if (!first) {
                                    writer.write(",");
                                } else {
                                    first = false;
                                }
                                writer.write("{\"text\":\"" + innerClassesNames +
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
                                writer.write("{\"text\":\"" + innerClassesNames +
                                        "\",\"id\":\"" + node + "." + innerClassesNames +
                                        "\",\"cls\":\"folder\"}");

                            }

                            for (String innerClassesNames : metaClassFromSet.getSimpleAttributesNames()) {
                                if (!first) {
                                    writer.write(",");
                                } else {
                                    first = false;
                                }
                                writer.write("{\"text\":\"" + innerClassesNames +
                                        "\",\"id\":\"" + node + "." + innerClassesNames +
                                        "\",\"leaf\":true,\"cls\":\"file\"}");
                            }
                        }
                    }
                    writer.write("]");
                }
                break;
            case SAVE_CLASS:
                String classId = resourceRequest.getParameter("id");
                if (classId != null && classId.trim().length() > 0) {
                    String className = resourceRequest.getParameter("className");
                    MetaClass meta = metaFactoryService.getMetaClass(classId);

                    if (meta == null) {
                        meta = new MetaClass(classId);
                    }

                    metaFactoryService.saveMetaClass(meta);
                }
                break;
            case DEL_CLASS:
                classId = resourceRequest.getParameter("id");
                if (classId != null && classId.trim().length() > 0) {
                    metaFactoryService.delMetaClass(classId);
                }
                break;
            default:
                break;
        }
    }
}
