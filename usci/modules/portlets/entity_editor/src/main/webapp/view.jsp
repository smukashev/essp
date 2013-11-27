<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui" %>

<portlet:defineObjects />

<%
//    List<BaseEntity> baseEntityList = (List<BaseEntity>)renderRequest.getAttribute("entityList");
%>

<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<link rel="stylesheet" media="all" href="<%=request.getContextPath()%>/js/ext/resources/css/ext-all.css" />
<link rel="stylesheet" media="all" href="<%=request.getContextPath()%>/css/main.css" />

<script>
    var dataUrl = '<%=getDataURL%>';
    var givenEntityId = '<%=renderRequest.getAttribute("entityId")%>';
    var contextPathUrl = '<%=request.getContextPath()%>';
</script>

<script src="<%=request.getContextPath()%>/js/ext/ext-all.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/main.js" type="text/javascript"></script>

<div id="entity-editor-content">

</div>