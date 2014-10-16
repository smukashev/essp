<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:actionURL var="createActionURL" name="createShowcase">
    <portlet:param name="mvcPath" value="/jsp/showcases.jsp" />
</portlet:actionURL>

<portlet:actionURL var="showcasesURL" name="listShowcases">
    <portlet:param name="mvcPath" value="/jsp/showcases.jsp" />
</portlet:actionURL>
<a href="<%= showcasesURL %>">Back</a><br/>

<div ng-app="showcaseApp">
<div ng-controller="ShowcaseController">

<form action="<%= createActionURL %>" method="post">
    <label>Name: </label> <input type="text" name="scName" />
    <label>Table name: </label> <input type="text" name="scTableName" />
    <label>Title: </label> <input type="text" name="scTitle" />


    <table>
        <tr><th>Path</th><th>Name</th></tr>
        <tr ng-repeat="field in fields">
            <td>{{field.path}}</td><td>{{field.name}}</td><td><a href="" ng-click="remove(field)">Remove</a></td>
        </tr>
    </table>

    <div ng-repeat="field in fields">
        <input type="hidden" name="nameField{{fields.indexOf(field)}}" ng-model="field.name" />
        <input type="hidden" name="pathField{{fields.indexOf(field)}}" ng-model="field.path" />
    </div>

    <input type="text" ng-model="fieldPath" />
    <input type="text" ng-model="fieldName" />
    <input type="button" ng-click="addField()" value="Add" />

    <input type="submit" value="Save" />

</form>

</div>
</div>