<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<liferay-theme:defineObjects />

<portlet:defineObjects />

<form action="<liferay-portlet:actionURL portletConfiguration='true' />" method="post" name="<portlet:namespace />fm"> 
    <input name="<portlet:namespace /><%=Constants.CMD%>" type="hidden" value="<%=Constants.UPDATE%>" /> 

    Type: <select name="<portlet:namespace />type"> <option value="DEVELOPMENT">Development</option> 
        <option value="WORK">Work</option></select> <br/>
        Business rules URL: <input name="<portlet:namespace />business_rules_url"/>

    <input type="button" value="Save" onClick="submitForm(document.<portlet:namespace />fm);" /> 
</form>
