<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<portlet:defineObjects />

<portlet:actionURL var="showcasesURL" name="listShowcases">
    <portlet:param name="mvcPath" value="/jsp/showcases.jsp" />
</portlet:actionURL>


<br/><a href="<%= showcasesURL%>"> <liferay-ui:message key="SHOWCASE"/> </a>