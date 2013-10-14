<%@ page import="com.liferay.portal.kernel.util.Constants" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
<liferay-theme:defineObjects />

<portlet:defineObjects />

<form action="<liferay-portlet:actionURL portletConfiguration='true' />" method="post" name="<portlet:namespace />fm"> 
    <input name="<portlet:namespace /><%=Constants.CMD%>" type="hidden" value="<%=Constants.UPDATE%>" /> 

    Type: <select name="<portlet:namespace />type"> <option value="REPORT">Report</option> 
        <option value="REFERENCE">Reference</option><option value="BANKS">Banks</option>
    <option value="TEST">Test</option></select> <br/>
        
        Default report date: <input name="<portlet:namespace />defaultreportdate" value="01.04.2012"/><br/>

    <input type="button" value="Save" onClick="submitForm(document.<portlet:namespace />fm);" /> 
</form>