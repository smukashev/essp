package kz.bsbnb.usci.porltet.entity_merge;

import com.google.gson.Gson;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.liferay.util.portlet.PortletProps;
import kz.bsbnb.usci.core.service.IBaseEntityMergeService;
import kz.bsbnb.usci.core.service.PortalUserBeanRemoteBusiness;
import kz.bsbnb.usci.core.service.form.ISearcherFormService;
import kz.bsbnb.usci.cr.model.Creditor;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.*;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.sync.service.IMetaFactoryService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainPortlet extends MVCPortlet {
    private IMetaFactoryService metaFactoryService;
    private IEntityService entityService;
    private IBaseEntityMergeService entityMergeService;
    private PortalUserBeanRemoteBusiness portalUserBeanRemoteBusiness;
    private ISearcherFormService searcherFormService;
    private Logger logger = Logger.getLogger(MainPortlet.class);
	private IMetaFactoryService metaFactoryService;
	private IEntityService entityService;
	private IBaseEntityMergeService entityMergeService;
	private PortalUserBeanRemoteBusiness portalUserBeanRemoteBusiness;
	private ISearcherFormService searcherFormService;
	private PortalUserBeanRemoteBusiness portalUserBusiness;


	void connectToServices() {
		try {
			RmiProxyFactoryBean metaFactoryServiceFactoryBean = new RmiProxyFactoryBean();
			metaFactoryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
					+ ":1098/metaFactoryService");
			metaFactoryServiceFactoryBean.setServiceInterface(IMetaFactoryService.class);
			metaFactoryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

			metaFactoryServiceFactoryBean.afterPropertiesSet();
			metaFactoryService = (IMetaFactoryService) metaFactoryServiceFactoryBean.getObject();

			RmiProxyFactoryBean entityServiceFactoryBean = new RmiProxyFactoryBean();
			entityServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
					+ ":1098/entityService");
			entityServiceFactoryBean.setServiceInterface(IEntityService.class);
			entityServiceFactoryBean.setRefreshStubOnConnectFailure(true);

			entityServiceFactoryBean.afterPropertiesSet();
			entityService = (IEntityService) entityServiceFactoryBean.getObject();

			RmiProxyFactoryBean entityMergeServiceFactoryBean = new RmiProxyFactoryBean();
			entityMergeServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
					+ ":1099/entityMergeService");
			entityMergeServiceFactoryBean.setServiceInterface(IBaseEntityMergeService.class);
			entityMergeServiceFactoryBean.setRefreshStubOnConnectFailure(true);

			entityMergeServiceFactoryBean.afterPropertiesSet();
			entityMergeService = (IBaseEntityMergeService) entityMergeServiceFactoryBean.getObject();

			RmiProxyFactoryBean searcherFormEntryServiceFactoryBean = new RmiProxyFactoryBean();
			searcherFormEntryServiceFactoryBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
					+ ":1098/searcherFormService");
			searcherFormEntryServiceFactoryBean.setServiceInterface(ISearcherFormService.class);
			searcherFormEntryServiceFactoryBean.setRefreshStubOnConnectFailure(true);

			searcherFormEntryServiceFactoryBean.afterPropertiesSet();
			searcherFormService = (ISearcherFormService) searcherFormEntryServiceFactoryBean.getObject();

			RmiProxyFactoryBean portalUserBean = new RmiProxyFactoryBean();
			portalUserBean.setServiceUrl("rmi://" + StaticRouter.getAsIP()
					+ ":1099/portalUserBeanRemoteBusiness");
			portalUserBean.setServiceInterface(PortalUserBeanRemoteBusiness.class);
			portalUserBean.setRefreshStubOnConnectFailure(true);
			portalUserBean.afterPropertiesSet();

			portalUserBeanRemoteBusiness = (PortalUserBeanRemoteBusiness) portalUserBean.getObject();

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

	private List<String> classesFilter;

	@Override
	public void init() throws PortletException {
		connectToServices();

		classesFilter = new LinkedList<>();

		for (String s : PortletProps.get("classes.filter").split(",")) {
			classesFilter.add(s);
		}

		super.init();
	}

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		String entityId = getParam("entityId", renderRequest);

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
		} catch (PortalException e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		}

		if (!hasRights)
			return;

		super.doView(renderRequest, renderResponse);
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

	private String getAttributesJsonSelect(IMetaClass meta) {
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
				result.append(((IMetaClass) metaAttribute.getMetaType()).getId());
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

	private String getMetaTypeStr(IMetaType metaType) {
		if (metaType.isSet())
			return "META_SET";
		else if (metaType.isComplex()) {
			return "META_CLASS";
		} else {
			return ((IMetaValue) metaType).getTypeCode().name();
		}
	}

	private String setToJsonSelect(BaseSet set, String title, String code, IMetaAttribute attr,
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
						if (!isNb) {
							if ("bank_relations".equals(attr.getName())) {
								BaseEntity relation = (BaseEntity) value.getValue();
								if (((BaseEntity) relation.getEl("creditor")).getId() != creditorId)
									continue;
							}
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						// strict mode
						continue;
					}

					if (!first) {
						str += ",";
					} else {
						first = false;
					}

					str += entityToJsonSelect((BaseEntity) (value.getValue()), "[" + i + "]", "[" + i + "]",
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

					if (((MetaValue) type).getTypeCode() != DataTypes.DATE) {
						str += "{" +
								"\"title\":\"" + "[" + i + "]" + "\",\n" +
								"\"code\":\"" + "[" + i + "]" + "\",\n" +
								"\"value\":\"" + clearSlashes(testNull(value.getValue().toString())) + "\",\n" +
								"\"simple\": true,\n" +
								"\"array\": false,\n" +
								"\"type\": \"" + ((MetaValue) type).getTypeCode() + "\",\n" +
								"\"leaf\":true,\n" +
								"\"iconCls\":\"file\"\n" +
								"}";
					} else {
						Object dtVal = value.getValue();
						String dtStr = "";
						if (dtVal != null) {
							dtStr = new SimpleDateFormat("dd.MM.yyyy").format(dtVal);
						}

						str += "{" +
								"\"title\":\"" + "[" + i + "]" + "\",\n" +
								"\"code\":\"" + "[" + i + "]" + "\",\n" +
								"\"value\":\"" + dtStr + "\",\n" +
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

	private String entityToJsonSelect(BaseEntity entity, String title, String code, IMetaAttribute attr,
									  boolean asRoot,
									  boolean isNb,
									  long creditorId) {

		MetaClass meta = entity.getMeta();

		//credit check
		if (meta.getClassName().equals("credit") && !isNb) {
			BaseEntity creditor = (BaseEntity) entity.getEl("creditor");
			if (creditor.getId() != creditorId)
				throw new RuntimeException(Errors.getMessage(Errors.E238));
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

				str += entityToJsonSelect((BaseEntity) (value.getValue()), attrTitle, innerClassesNames,
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

				str += setToJsonSelect((BaseSet) (value.getValue()), attrTitle, innerClassesNames, value.getMetaAttribute(),
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

				if (((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() != DataTypes.DATE) {
					str += "{" +
							"\"title\":\"" + attrTitle + "\",\n" +
							"\"code\":\"" + innerClassesNames + "\",\n" +
							"\"value\":\"" + clearSlashes(testNull(value.getValue().toString())) + "\",\n" +
							"\"simple\": true,\n" +
							"\"array\": false,\n" +
							"\"type\": \"" + ((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
							"\"leaf\":true,\n" +
							"\"iconCls\":\"file\",\n" +
							"\"isKey\":\"" + meta.getMetaAttribute(innerClassesNames).isKey() + "\",\n" +
							"\"isRequired\":\"" + meta.getMetaAttribute(innerClassesNames).isRequired() + "\"\n" +
							"}";
				} else {
					Object dtVal = value.getValue();
					String dtStr = "";
					if (dtVal != null) {
						dtStr = new SimpleDateFormat("dd.MM.yyyy").format(dtVal);
					}

					str += "{" +
							"\"title\":\"" + attrTitle + "\",\n" +
							"\"code\":\"" + innerClassesNames + "\",\n" +
							"\"value\":\"" + dtStr + "\",\n" +
							"\"simple\": true,\n" +
							"\"array\": false,\n" +
							"\"type\": \"" + ((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
							"\"leaf\":true,\n" +
							"\"iconCls\":\"file\",\n" +
							"\"isKey\":\"" + meta.getMetaAttribute(innerClassesNames).isKey() + "\",\n" +
							"\"isRequired\":\"" + meta.getMetaAttribute(innerClassesNames).isRequired() + "\"\n" +
							"}";
				}
			}
		}

		str += "]}";

		return str;
	}

	private String entityToJson(BaseEntity entityLeft, BaseEntity entityRight, String title, String code, Boolean isKey, MetaClass parentMeta) {
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

		if (meta == null)
			throw new NullPointerException(Errors.getMessage(Errors.E243));

		Boolean isSearchable = false;
		if (meta.isComplex())
			isSearchable = meta.isSearchable();

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
		str += "\"is_searchable\": " + isSearchable + ",";
		str += "\"is_key\": " + isKey + ",";
		str += "\"is_parent_ref\": " + ((parentMeta == null) ? "false" : parentMeta.isReference()) + ",";
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
						attrTitle, innerClassesNames, meta.getMetaAttribute(innerClassesNames).isKey(), meta);
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
						attrTitle, innerClassesNames, meta.getMetaAttribute(innerClassesNames).isKey());
			}
		}

		for (String innerClassesNames : meta.getSimpleArrayAttributesNames()) {
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
						attrTitle, innerClassesNames, meta.getMetaAttribute(innerClassesNames).isKey());
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
							"\"is_searchable\": false,\n" +
							"\"is_parent_searchable\": " + meta.isSearchable() + ",\n" +
							"\"is_key\": " + meta.getMetaAttribute(innerClassesNames).isKey() + ",\n" +
							"\"type\": \"" + ((MetaValue) meta.getMemberType(innerClassesNames)).getTypeCode() + "\",\n" +
							"\"is_parent_ref\": " + meta.isReference() + ",\n" +
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
							"\"is_searchable\": false,\n" +
							"\"is_key\": " + meta.getMetaAttribute(innerClassesNames).isKey() + ",\n" +
							"\"is_parent_ref\": " + meta.isReference() + ",\n" +
							"\"is_parent_searchable\": " + meta.isSearchable() + ",\n" +
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

	private String setToJson(BaseSet setLeft, BaseSet setRight, String title, String code, Boolean isKey) {

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

		if (type == null)
			throw new NullPointerException(Errors.getMessage(Errors.E245));

		Boolean isSearchable = false;
		if (type.isComplex()) {
			MetaClass metaClass = (MetaClass) type;
			isSearchable = metaClass.isSearchable();
		}

		String str = "{";

		str += "\"title\": \"" + title + "\",";
		str += "\"code\": \"" + code + "\",";
		str += "\"valueLeft\": \"" + setLeftSize + "\",";
		str += "\"valueRight\": \"" + setRightSize + "\",";
		str += "\"simple\": false,";
		str += "\"array\": true,";
		str += "\"is_searchable\": " + isSearchable + ",";
		str += "\"is_key\": " + isKey + ",";
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
							"[" + i + "]", isKey, null);
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
			throw new NullPointerException(Errors.getMessage(Errors.E244));


		PrintWriter writer = resourceResponse.getWriter();
		List<Creditor> creditors;
		try {
			OperationTypes operationType = OperationTypes.valueOf(getParam("op", resourceRequest));
			User currentUser = PortalUtil.getUser(resourceRequest);

			Gson gson = new Gson();
			String sJson;

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
					//Long metaId = Long.valueOf(resourceRequest.getParameter("meta"));
					String searchClassName = resourceRequest.getParameter("search");
					String prefix = resourceRequest.getParameter("prefix");

					String generatedForm = searcherFormService.getDom(currentUser.getUserId(),
							searchClassName, metaFactoryService.getMetaClass(resourceRequest.getParameter("metaName")),
							prefix);

					writer.write(generatedForm);
					break;
				case FIND_ACTION:
					List<Creditor> creditorList =
							portalUserBeanRemoteBusiness.getPortalUserCreditorList(currentUser.getUserId());

					Long creditorId = 0L;

                    if (creditorList.size() == 1) {
                        creditorId = creditorList.get(0).getId();
                    } else {
                        System.err.println("Not correct creditors number(" + creditorList.size() + ")");
                    }

					Enumeration<String> list = resourceRequest.getParameterNames();

					//metaId = Long.valueOf(resourceRequest.getParameter("metaClass"));

					MetaClass metaClass = metaFactoryService.getMetaClass(resourceRequest.getParameter("metaClass"));
					searchClassName = resourceRequest.getParameter("searchName");
					prefix = resourceRequest.getParameter("prefix");
					HashMap<String, String> parameters = new HashMap<String, String>();

					while (list.hasMoreElements()) {
						String attribute = list.nextElement();
						if (attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
							continue;
						parameters.put(attribute, resourceRequest.getParameter(attribute));
					}

					ISearchResult searchResult = searcherFormService.search(searchClassName, parameters, metaClass, prefix, creditorId);

					if (searchResult.getData() == null)
						throw new RuntimeException(Errors.getMessage(Errors.E242));

					Iterator<BaseEntity> cursor = searchResult.iterator();

					long ret = -1;


					if (cursor.hasNext()) {
						ret = cursor.next().getId();
						ret = ret > 0 ? ret : -1;
					}

					writer.write("{\"success\": true, \"data\":\"" + ret + "\"}");

					break;
				case LIST_CLASSES:
					Long userId = 0L;

					if (currentUser != null)
						userId = currentUser.getUserId();

					List<String[]> classes = searcherFormService.getMetaClasses(userId);

					if (classes.size() < 1)
						throw new RuntimeException(Errors.getMessage(Errors.E239));

					List<String[]> afterFilter = new LinkedList<>();

					//portlet props + remove cr implementations
					for (String[] c : classes)
//                        if (classesFilter.contains(c[1]) && !c[0].contains("cr"))
						afterFilter.add(c);

					writer.write(JsonMaker.getCaptionedArray(afterFilter,
							new String[]{"searchName", "metaName", "title"}));

					//writer.write("{\"success\":\"true\", \"data\": " + gson.toJson(classes) + "}");
					break;
				case LIST_CREDITORS:
					creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());
					writer.write(JsonMaker.getJson(creditors));
					break;
				case GET_ENTITY_ID: {
					String entityId = resourceRequest.getParameter("entityId");
					String asRootStr = resourceRequest.getParameter("asRoot");
					boolean isNb = false;
					creditorId = new Long(-1);

					for (Role r : currentUser.getRoles())
						if ("NationalBankEmployee".equals(r.getDescriptiveName()) ||
								"Administrator".equals(r.getDescriptiveName())) {
							isNb = true;
							break;
						}

					creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());

					if (!isNb) {
						if (creditors.size() > 1)
							throw new RuntimeException(Errors.getMessage(Errors.E240));

						if (creditors.size() == 0)
							throw new RuntimeException(Errors.getMessage(Errors.E241));

						creditorId = creditors.get(0).getId();
					}

					boolean asRoot = StringUtils.isNotEmpty(asRootStr) ? Boolean.valueOf(asRootStr) : false;

					searchClassName = resourceRequest.getParameter("searchName");
					String metaName = resourceRequest.getParameter("metaClass");
					metaClass = metaFactoryService.getMetaClass(metaName);
					try {
						creditorId = Long.parseLong(resourceRequest.getParameter("creditorId"));
					} catch (Exception e) {
						creditorId = new Long(-1);
					}
					list = resourceRequest.getParameterNames();
					parameters = new HashMap<>();
					while (list.hasMoreElements()) {
						String attribute = list.nextElement();
						if (attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
							continue;
						parameters.put(attribute, resourceRequest.getParameter(attribute));
					}


					searchResult = searcherFormService.search(searchClassName, parameters, metaClass, "", creditorId);

					StringBuilder sb = new StringBuilder("{\"entityIds\":[\n");
					Iterator<BaseEntity> it = searchResult.getData().iterator();
					do {
						if (!it.hasNext())
							break;
						BaseEntity currentEntity = it.next();
						sb.append(currentEntity.getId() + "");
						if (it.hasNext()) sb.append(",");
					} while (true);
					sb.append("]}");
					writer.write(sb.toString());

					break;
				}
				case LIST_BY_CLASS_SHORT_SELECT:
					String metaId = resourceRequest.getParameter("metaId");
					RefListResponse refListResponse = entityService.getRefListResponse(Long.parseLong(metaId), null, false);
					refListResponse = refListToShort(refListResponse);
					sJson = gson.toJson(refListResponse);
					writer.write(sJson);

					break;
				case LIST_ATTRIBUTES_SELECT:
					metaId = resourceRequest.getParameter("metaId");

					if (StringUtils.isNotEmpty(metaId)) {
						metaClass = metaFactoryService.getMetaClass(Long.valueOf(metaId));
						sJson = getAttributesJsonSelect(metaClass);
						writer.write(sJson);
					}

					break;
				case LIST_ENTITY_SELECT:
					String entityId = resourceRequest.getParameter("entityId");
					String asRootStr = resourceRequest.getParameter("asRoot");
					boolean isNb = false;
					creditorId = new Long(-1);

					for (Role r : currentUser.getRoles())
						if ("NationalBankEmployee".equals(r.getDescriptiveName()) ||
								"Administrator".equals(r.getDescriptiveName())) {
							isNb = true;
							break;
						}

					creditors = portalUserBusiness.getPortalUserCreditorList(currentUser.getUserId());

					if (!isNb) {
						if (creditors.size() > 1)
							throw new RuntimeException(Errors.getMessage(Errors.E240));

						if (creditors.size() == 0)
							throw new RuntimeException(Errors.getMessage(Errors.E241));

						creditorId = creditors.get(0).getId();
					}

					boolean asRoot = StringUtils.isNotEmpty(asRootStr) ? Boolean.valueOf(asRootStr) : false;

					if (entityId != null && entityId.trim().length() > 0) {
						//search by single Id
						Date date = null;
						if (resourceRequest.getParameter("date") != null)
							date = (Date) DataTypes.fromString(DataTypes.DATE, resourceRequest.getParameter("date"));

						if (date == null)
							date = new Date();

						BaseEntity entity = entityService.load(Integer.parseInt(entityId), date);

						sJson = "{\"text\":\".\",\"children\": [\n" +
								entityToJsonSelect(entity, entity.getMeta().getClassTitle(),
										entity.getMeta().getClassName(), null, asRoot, isNb, creditorId) +
								"]}";

						writer.write(sJson);
					} else {
						searchClassName = resourceRequest.getParameter("searchName");
						String metaName = resourceRequest.getParameter("metaClass");
						metaClass = metaFactoryService.getMetaClass(metaName);
						try {
							creditorId = Long.parseLong(resourceRequest.getParameter("creditorId"));
						} catch (Exception e) {
							creditorId = new Long(-1);
						}
						list = resourceRequest.getParameterNames();
						parameters = new HashMap<>();
						while (list.hasMoreElements()) {
							String attribute = list.nextElement();
							if (attribute.equals("op") || attribute.equals("metaClass") || attribute.equals("searchName"))
								continue;
							parameters.put(attribute, resourceRequest.getParameter(attribute));
						}


						searchResult = searcherFormService.search(searchClassName, parameters, metaClass, "", creditorId);

						StringBuilder sb = new StringBuilder("{\"text\":\".\"");
						if (searchResult.hasPagination())
							sb = sb.append(",\"totalCount\":" + searchResult.getTotalCount());
						sb = sb.append(",\"children\":[\n");

						Iterator<BaseEntity> it = searchResult.getData().iterator();
						do {
							if (!it.hasNext())
								break;
							BaseEntity currentEntity = it.next();
							sb.append(entityToJsonSelect(currentEntity, currentEntity.getMeta().getClassTitle(),
									currentEntity.getMeta().getClassName(), null, true, isNb, creditorId));

							if (it.hasNext()) sb.append(",");
						} while (true);

						sb.append("]}");
						writer.write(sb.toString());
					}
					break;
				case LIST_ENTITY: {
					String leftEntityId = resourceRequest.getParameter("leftEntityId");
					String leftReportDate = resourceRequest.getParameter("leftReportDate");
					String rightEntityId = resourceRequest.getParameter("rightEntityId");
					String rightReportDate = resourceRequest.getParameter("rightReportDate");
					String CreditorId = resourceRequest.getParameter("creditorId");

					System.out.println("\n THE LEFT ENTITY ID: " + leftEntityId);
					System.out.println("\n THE RIGHT ENTITY ID: " + rightEntityId);
					System.out.println("\n THE LEFT ENTITY REPORT DATE: " + leftReportDate);
					System.out.println("\n THE RIGHT ENTITY REPORT DATE: " + rightReportDate);

					if ((leftEntityId != null && leftEntityId.trim().length() > 0) &&
							(rightEntityId != null && rightEntityId.trim().length() > 0) &&
							StringUtils.isNotEmpty(leftReportDate) && StringUtils.isNotEmpty(rightReportDate)) {

						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						Date leftRD = df.parse(leftReportDate);
						Date rightRD = df.parse(rightReportDate);

						BaseEntity entityLeft = entityService.load(Integer.parseInt(leftEntityId), leftRD);
						BaseEntity entityRight = entityService.load(Integer.parseInt(rightEntityId), rightRD);

						writer.write("{\"text\":\".\",\"children\": [\n" +
								entityToJson(entityLeft, entityRight, entityLeft.getMeta().getClassTitle(),
										entityLeft.getMeta().getClassName(), false, null) +
								"]}");
					}
					break;
				}
				case LIST_CREDITOR: {
					Map m = new HashMap();
					List<Map> l = new LinkedList<>();
					creditors = portalUserBeanRemoteBusiness.getPortalUserCreditorList(currentUser.getUserId());

					for (Creditor creditor : creditors) {
						Map creditorMap = new HashMap();
						creditorMap.put("id", creditor.getId());
						creditorMap.put("title", creditor.getName());
						l.add(creditorMap);
					}
					m.put("data", l);
					JsonMaker.getJson(m);
					writer.write(JsonMaker.getJson(m));
					break;
				}
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
		LIST_CREDITORS,
		GET_ENTITY_ID,
		LIST_ENTITY_SELECT,
		LIST_BY_CLASS_SHORT_SELECT,
		LIST_ATTRIBUTES_SELECT,
		LIST_ENTITY,
		LIST_CREDITOR,
		SAVE_JSON,
		GET_FORM,
		FIND_ACTION,
		LIST_BY_CLASS,
		GET_CANDIDATES,
		NULL
	}
}
