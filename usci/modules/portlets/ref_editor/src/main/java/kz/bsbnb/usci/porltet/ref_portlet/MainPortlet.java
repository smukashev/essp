package kz.bsbnb.usci.porltet.ref_portlet;

import com.google.gson.Gson;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.write.*;
import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.porltet.ref_portlet.model.json.MetaClassList;
import kz.bsbnb.usci.porltet.ref_portlet.model.json.MetaClassListEntry;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Boolean;
import java.lang.Number;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainPortlet extends MVCPortlet {
    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBatchEntryService batchEntryService;
    private PortalUserBeanRemoteBusiness portalUserBusiness;
    public final Logger logger = Logger.getLogger(MainPortlet.class);
    private Exception currentException;

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

            RmiProxyFactoryBean portalUserBeanRemoteBusinessFactoryBean = new RmiProxyFactoryBean();
            portalUserBeanRemoteBusinessFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP() +
                    ":1099/portalUserBeanRemoteBusiness");
            portalUserBeanRemoteBusinessFactoryBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);

            portalUserBeanRemoteBusinessFactoryBean.afterPropertiesSet();
            portalUserBusiness = (PortalUserBeanRemoteBusiness) portalUserBeanRemoteBusinessFactoryBean.getObject();

        } catch (Exception e) {
            throw new RuntimeException(Errors.getError(Errors.E286));
        }
    }

    @Override
    public void doView(RenderRequest renderRequest,
                       RenderResponse renderResponse) throws IOException, PortletException {

        HttpServletRequest httpReq = PortalUtil.getOriginalServletRequest(
                PortalUtil.getHttpServletRequest(renderRequest));

        String entityId = httpReq.getParameter("entityId");
        renderRequest.setAttribute("entityId", entityId);

        boolean hasRights = false;

        try {
            User user = PortalUtil.getUser(PortalUtil.getHttpServletRequest(renderRequest));
            if (user != null) {
                for (Role role : user.getRoles()) {
                    if (role.getName().equals("Administrator") || role.getName().equals("BankUser")
                            || role.getName().equals("NationalBankEmployee"))
                        hasRights = true;
                }
            }

            if (!hasRights)
                throw new AccessControlException(Errors.compose(Errors.E238));

        } catch (Exception e) {
            currentException = e;
        }

        super.doView(renderRequest, renderResponse);
    }

    enum OperationTypes {
        LIST_CLASSES,
        LIST_ENTITY,
        SAVE_XML,
        LIST_BY_CLASS,
        LIST_BY_CLASS_SHORT,
        LIST_REF_COLUMNS,
        LIST_ATTRIBUTES,
        EXPORT_REF
    }

    private String testNull(String str) {
        if (str == null)
            return "";
        return str;
    }

    private String clearSlashes(String str) {
        String outStr = str.replaceAll("\"", "\\\\\"");
        logger.info(outStr);
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

                str +=  setToJson((BaseSet) (value.getValue()), attrTitle, innerClassesNames, value.getMetaAttribute());
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

    private String setToJson(BaseSet set, String title, String code, IMetaAttribute attr) {
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
        OutputStream out = resourceResponse.getPortletOutputStream();

        try {

            if(currentException !=null)
                throw currentException;

            if (metaFactoryService == null)
                connectToServices();

            OperationTypes operationType = OperationTypes.valueOf(getParam("op", resourceRequest));

            Gson gson = new Gson();

            User currentUser = PortalUtil.getUser(resourceRequest);

            switch (operationType) {
                case SAVE_XML:
                    String xml = getParam("xml_data", resourceRequest);
                    String sDate = getParam("date", resourceRequest);
                    Date date = (Date) DataTypes.getCastObject(DataTypes.DATE, sDate);

                    BatchEntry batchEntry = new BatchEntry();

                    batchEntry.setValue(xml);
                    batchEntry.setRepDate(date);

                    batchEntry.setUserId(currentUser.getUserId());

                    batchEntryService.save(batchEntry);

                    out.write("{\"success\": true }".getBytes());

                    break;
                case LIST_CLASSES:
                    MetaClassList classesListJson = new MetaClassList();
                    List<MetaClassName> metaClassesList = metaFactoryService.getRefNames();

                    classesListJson.setTotal(metaClassesList.size());

                    for (MetaClassName metaName : metaClassesList) {
                        MetaClassListEntry metaClassListEntry = new MetaClassListEntry();

                        metaClassListEntry.setClassId("" + metaName.getId());

                        if(metaName.getClassTitle() != null
                                && metaName.getClassTitle().trim().length() > 0) {
                            metaClassListEntry.setClassTitle(metaName.getClassTitle());
                        } else {
                            metaClassListEntry.setClassTitle(metaName.getClassName());
                        }

                        metaClassListEntry.setClassName(metaName.getClassName());

                        classesListJson.getData().add(metaClassListEntry);
                    }

                    out.write(gson.toJson(classesListJson).getBytes());

                    break;
                case LIST_BY_CLASS:
                    String metaId = getParam("metaId", resourceRequest);
                    sDate = resourceRequest.getParameter("date");
                    date = null;
                    if (StringUtils.isNotEmpty(sDate)) {
                        date = (Date) DataTypes.getCastObject(DataTypes.DATE, sDate);
                    }
                    String sWithHis = resourceRequest.getParameter("withHis");
                    boolean withHis = Boolean.valueOf(sWithHis);
                    RefListResponse refListResponse = entityService.getRefListResponse(Long.parseLong(metaId), date, withHis);
                    String sJson = gson.toJson(refListResponse);
                    out.write(sJson.getBytes());
                    break;
                case LIST_BY_CLASS_SHORT:
                    metaId = getParam("metaId", resourceRequest);
                    refListResponse = entityService.getRefListResponse(Long.parseLong(metaId), null, false);
                    refListResponse = refListToShort(refListResponse);
                    sJson = gson.toJson(refListResponse);
                    out.write(sJson.getBytes());
                    break;
                case LIST_REF_COLUMNS:
                    metaId = getParam("metaId", resourceRequest);
                    RefColumnsResponse refColumns = entityService.getRefColumns(Long.parseLong(metaId));
                    sJson = gson.toJson(refColumns);
                    out.write(sJson.getBytes());
                    break;
                case LIST_ATTRIBUTES:
                    metaId = getParam("metaId", resourceRequest);

                    if (StringUtils.isNotEmpty(metaId)) {
                        MetaClass metaClass = metaFactoryService.getMetaClass(Long.valueOf(metaId));
                        sJson = getAttributesJson(metaClass, currentUser);


                        out.write(sJson.getBytes());
                    }

                    break;
                case LIST_ENTITY:
                    String entityId = getParam("entityId", resourceRequest);
                    String asRootStr = getParam("asRoot", resourceRequest);

                    boolean asRoot = StringUtils.isNotEmpty(asRootStr) ? Boolean.valueOf(asRootStr) : false;

                    if (entityId != null && entityId.trim().length() > 0) {
                        date = null;

                        if(StringUtils.isNotEmpty(resourceRequest.getParameter("date")))
                            date = (Date) DataTypes.getCastObject(DataTypes.DATE, resourceRequest.getParameter("date"));

                        if(date == null)
                            date = new Date();

                        BaseEntity entity = entityService.load(Integer.parseInt(entityId), date);

                        sJson = "{\"text\":\".\",\"children\": [\n" +
                                entityToJson(entity, entity.getMeta().getClassTitle(),
                                        entity.getMeta().getClassName(), null, asRoot) +
                                "]}";

                        out.write(sJson.getBytes());
                    }
                    break;
                case EXPORT_REF:
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

                    metaId = getParam("metaId", resourceRequest);
                    sDate = resourceRequest.getParameter("date");
                    date = null;
                    if (StringUtils.isNotEmpty(sDate)) {
                        date = (Date) DataTypes.getCastObject(DataTypes.DATE, sDate);
                    }
                    sWithHis = resourceRequest.getParameter("withHis");
                    withHis = Boolean.valueOf(sWithHis);
                    refColumns = entityService.getRefColumns(Long.parseLong(metaId));
                    refListResponse = entityService.getRefListResponse(Long.parseLong(metaId), date, withHis);
                    MetaClass metaClass = metaFactoryService.getMetaClass(Long.valueOf(metaId));

                    String refName = metaClass.getClassName();
                    String refTitle = metaClass.getClassTitle() != null ? metaClass.getClassTitle() : metaClass.getClassName();
                    String fileName = refName + "_" + df.format(new Date()) + ".xls";

                    resourceResponse.setProperty("Content-Disposition", "attachment;filename=" + fileName);
                    resourceResponse.setContentType("application/vnd.ms-excel");
                    byte[] excelBytes = constructExcel(
                            refListResponse,
                            refColumns,
                            refTitle,
                            currentUser.getFullName(),
                            sDate,
                            withHis
                    );
                    out.write(excelBytes);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            currentException = null;
            String originalError = e.getMessage() != null ? e.getMessage().replaceAll("\"","&quot;").replace("\n","")
                    .replaceAll("\t", " ") : e.getClass().getName();
            originalError = Errors.decompose(originalError);
            out.write(("{\"success\": false, \"errorMessage\": \"" + originalError + "\"}").getBytes());
        }
    }

    private byte[] constructExcel(RefListResponse refListResponse, RefColumnsResponse refColumnsResponse, String refTitle, String userName, String sRepDate, boolean withHis) throws WriteException, IOException, ParseException {
        WritableWorkbook workbook = null;
        String formatString = "dd.MM.yyyy";
        DateFormat dfShort = new SimpleDateFormat(formatString);
        DateFormat dfFull = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        WritableFont times12font = new WritableFont(WritableFont.TIMES, 10);
        WritableFont times12fontBold = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD);

        WritableCellFormat infoFormat = new WritableCellFormat(times12font);

        WritableCellFormat headerFormat = new WritableCellFormat(times12fontBold);
        headerFormat.setAlignment(jxl.format.Alignment.CENTRE);
        headerFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

        WritableCellFormat dataFormat = new WritableCellFormat(times12font);
        dataFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        dataFormat.setWrap(true);

        WritableCellFormat dateFormat = new WritableCellFormat(new jxl.write.DateFormat(formatString));
        dateFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

        CellView cellView = new CellView();
        cellView.setSize(5000);

        ByteArrayOutputStream baos = null;
        baos = new ByteArrayOutputStream();
        workbook = Workbook.createWorkbook(baos);
        WritableSheet sheet = workbook.createSheet("Sheet1", 0);

        List<Map<String, Object>> rows = refListResponse.getData();
        List<String> headers = refColumnsResponse.getTitles();
        List<String> keys = refColumnsResponse.getNames();

        keys.add("open_date");
        keys.add("close_date");

        headers.add("Дата начала");
        headers.add("Дата окончания");

        int initialColIndex = 0;

        int rowCounter = 1;

        {
            String str = "Название справочника: " + refTitle;
            sheet.addCell(new Label(initialColIndex, rowCounter, str, infoFormat));
            rowCounter++;

            str = "По состоянию на: " + sRepDate;
            sheet.addCell(new Label(initialColIndex, rowCounter, str, infoFormat));
            rowCounter++;

            str = "С историей: " + (withHis ? "Да" : "Нет");
            sheet.addCell(new Label(initialColIndex, rowCounter, str, infoFormat));
            rowCounter++;

            str = "Дата формирования: " + dfFull.format(new Date());
            sheet.addCell(new Label(initialColIndex, rowCounter, str, infoFormat));
            rowCounter++;

            str = "Пользователь: " + userName   ;
            sheet.addCell(new Label(initialColIndex, rowCounter, str, infoFormat));
            rowCounter++;
        }

        rowCounter++;

        for (int columnIndex = initialColIndex; columnIndex < headers.size(); columnIndex++) {
            sheet.addCell(new Label(columnIndex, rowCounter, headers.get(columnIndex), headerFormat));
        }

        rowCounter++;

        for (Map<String, Object> row : rows) {
            for (int columnIndex = initialColIndex; columnIndex < keys.size(); columnIndex++) {
                String key = keys.get(columnIndex);
                Object value = row.get(key);

                if (value != null) {
                    if (value instanceof Number) {
                        jxl.write.Number number;
                        try {
                            number = new jxl.write.Number(columnIndex, rowCounter, Integer.parseInt(value.toString()), dataFormat);
                        } catch (NumberFormatException nfe) {
                            try {
                                number = new jxl.write.Number(columnIndex, rowCounter, ((Number) value).doubleValue(), dataFormat);
                            } catch (Exception e) {
                                logger.error("Excel export failed: number format not correct");
                                number = new jxl.write.Number(columnIndex, rowCounter, 0, dataFormat);
                            }
                        }
                        sheet.addCell(number);
                    } else if (value instanceof Date) {
                        sheet.addCell(new jxl.write.DateTime(columnIndex, rowCounter, (Date) value, dateFormat));
                    } else if (key.equals("open_date") || key.equals("close_date")) {
                        value = dfShort.parse(value.toString());
                        sheet.addCell(new jxl.write.DateTime(columnIndex, rowCounter, (Date) value, dateFormat));
                    } else {
                        sheet.addCell(new jxl.write.Label(columnIndex, rowCounter, value.toString(), dataFormat));
                    }
                } else {
                    sheet.addCell(new Blank(columnIndex, rowCounter, dataFormat));
                }
                sheet.setColumnView(columnIndex, cellView);
            }
            sheet.setRowView(rowCounter, 1000);

            rowCounter++;
        }

        workbook.write();

        workbook.close();

        return baos.toByteArray();
    }

    private RefListResponse refListToShort(RefListResponse refListResponse) {
        List<Map<String, Object>> shortRows = new ArrayList<Map<String, Object>>();

        String titleKey = null;

        if (!refListResponse.getData().isEmpty()) {
            Set<String> keys = refListResponse.getData().get(0).keySet();

            for (String key : keys) {
                if (key.startsWith("name")) {
                    titleKey = key;
                    break;
                }
            }
        }

        for (Map<String, Object> row : refListResponse.getData()) {
            Object id = row.get("ID");
            Object title = titleKey != null ? row.get(titleKey) : "------------------------";

            Map<String, Object> shortRow = new HashMap<String, Object>();
            shortRow.put("ID", id);
            shortRow.put("title", title);
            shortRows.add(shortRow);
        }

        return new RefListResponse(shortRows);
    }

    private String getAttributesJson(IMetaClass meta, User currentUser) {
        StringBuilder result = new StringBuilder();

        result.append("{\"success\" : true, \"total\":");
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

            if(meta.getClassName().equals("ref_portfolio")) {
                if (attrName.equals("creditor") || attrName.equals("code")) {
                    if (attrName.equals("creditor") ) {
                        List<Creditor> creditors = portalUserBusiness.getMainCreditorsInAlphabeticalOrder(currentUser.getUserId());
                        for (Creditor creditor : creditors) {

                            result.append(",\"value\":");
                            result.append("\"");
                            result.append(creditor.getId());
                            result.append("\"");
                        }
                    } else {
                            result.append(",\"metaId\":");
                            result.append("\"");
                            result.append(meta.getId());
                            result.append("\"");
                    }
                    result.append(",\"isHidden\":");
                    result.append("\"");
                    result.append("true");
                    result.append("\"");
                } else {
                    result.append(",\"isHidden\":");
                    result.append("\"");
                    result.append("false");
                    result.append("\"");
                }
            }

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

    private String getParam(String name, ResourceRequest resourceRequest) {
        return PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest)).getParameter(name);
    }
}
