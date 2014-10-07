<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="kz.bsbnb.usci.showcase.ShowcaseHolder" %>
<%@ page import="java.util.List" %>

<portlet:defineObjects />

<portlet:renderURL var="createSCPageURL">
    <portlet:param name="mvcPath" value="/jsp/createShowcase.jsp" />
</portlet:renderURL>

<h3><liferay-ui:message key="SHOWCASE"/></h3>

<table>
    <% for(ShowcaseHolder holder : (List<ShowcaseHolder>)renderRequest.getAttribute("showcases")){ %>
        <portlet:actionURL var="deleteSCURL" name="deleteShowcase">
            <portlet:param name="mvcPath" value="/jsp/showcases.jsp" />
            <portlet:param name="showcaseId" value="<%= String.valueOf(holder.getShowCaseMeta().getId())%>" />
        </portlet:actionURL>
        <portlet:actionURL var="viewSCURL" name="viewShowcase">
            <portlet:param name="mvcPath" value="/jsp/showcase.jsp" />
            <portlet:param name="showcaseId" value="<%= String.valueOf(holder.getShowCaseMeta().getId())%>" />
        </portlet:actionURL>

        <tr>
            <td style="border:1px black solid;padding:2px" ><a href="<%= viewSCURL %>"><%= holder.getShowCaseMeta().getName()%></a> </td><!--td><a href="<%= deleteSCURL%>">X</a></td-->
        </tr>
    <% } %>
</table>
<!--a href="<%= createSCPageURL %>">Create new</a-->
