<%@page import="kz.bsbnb.usci.portlets.query.QuerySettings"%>
<%@page import="com.liferay.portlet.PortletPreferencesFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@ page import="com.liferay.portlet.PortletPreferencesFactory"%>
<%@ page import="javax.portlet.PortletPreferences"%>
<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<portlet:defineObjects/>

<form action="<liferay-portlet:actionURL portletConfiguration='true'/>" method="post" name="<portlet:namespace />fm"> 
    <input name="<portlet:namespace /><%=Constants.CMD%>" type="hidden" value="<%=Constants.UPDATE%>" /> 

    <%
        String portletResource = ParamUtil.getString(request, "portletResource");
        PortletPreferences preferences = PortletPreferencesFactoryUtil.getPortletSetup(request, portletResource);
        QuerySettings settings = new QuerySettings(preferences);
    %>

    JDBC Pool name: <input name="<portlet:namespace />PoolName" value="<%=settings.getPoolName()%>"/> <br/>
    Output date format: <input name="<portlet:namespace />OutputDateFormat" value="<%=settings.getOutputDateFormat()%>"/> <br/>
    <input type="button" value="Save" onClick="submitForm(document.<portlet:namespace />fm);" /> 
</form>