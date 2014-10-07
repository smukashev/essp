<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="kz.bsbnb.usci.eav.showcase.ShowCase" %>
<%@ page import="kz.bsbnb.usci.eav.showcase.ShowCaseField" %>

<portlet:defineObjects />

<% ShowCase showcase = (ShowCase)renderRequest.getAttribute("showcase");%>

<portlet:actionURL var="showcasesURL" name="listShowcases">
    <portlet:param name="mvcPath" value="/jsp/showcases.jsp" />
</portlet:actionURL>

<portlet:actionURL var="SCDataURL" name="showcaseData">
    <portlet:param name="mvcPath" value="/jsp/showcaseData.jsp" />
</portlet:actionURL>

<portlet:resourceURL var="ajaxURL">
    <portlet:param name="test" value="test" />
    <portlet:param name="showcaseId" value="<%= \"\" + showcase.getId()%>" />
</portlet:resourceURL>


<script type="text/javascript">
    var dataURL = "<%= ajaxURL %>";
    var namespace = "<portlet:namespace/>";
    var columns = [
        <% for(ShowCaseField field : showcase.getFieldsList()){ %>
            {"title":"<%= field.getName() %>", "field":"SC_<%= field.getColumnName().toUpperCase() %>", "visible":true},
        <% } %>
    {}]
    columns.pop();
</script>

<a href="<%= showcasesURL %>"><liferay-ui:message key="BACK"/></a><br/>

<h3><%= showcase.getTitle()%> (<%= showcase.getName()%>)</h3>

<div ng-app="showcaseApp" ng-controller="ShowcaseController">

<liferay-ui:message key="FIELDS"/>:
<table>
<tr>
<td style="padding-right:20px">
    <label class="checkbox" ng-repeat="column in columns | slice:0:5">
        <input type="checkbox" ng-model="column.visible" /> {{column.title}}
    </label>
</td>
<td style="padding-right:20px">
    <label class="checkbox" ng-repeat="column in columns | slice:5:10">
        <input type="checkbox" ng-model="column.visible" /> {{column.title}}
    </label>
</td>
<td style="padding-right:20px">
    <label class="checkbox" ng-repeat="column in columns | slice:10:15">
        <input type="checkbox" ng-model="column.visible" /> {{column.title}}
    </label>
</td>
<td style="padding-right:20px">
    <label class="checkbox" ng-repeat="column in columns | slice:15:columns.length ">
        <input type="checkbox" ng-model="column.visible" /> {{column.title}}
    </label>
</td>
</tr>
</table>

<div loading-container="tableParams.settings().$loading">
    <table ng-table="tableParams" show-filter="true" class="table">
        <thead>
        <tr>
            <th ng-repeat="column in columns" ng-show="column.visible"
                class="text-center">
                {{column.title}}
            </th>
        </tr>
        </thead>
        <tbody>
        <tr ng-repeat="item in $data">
            <td ng-repeat="column in columns" ng-show="column.visible" sortable="column.field">
                {{item[column.field]}}
            </td>
        </tr>
        </tbody>
    </table>
</div>
</div>