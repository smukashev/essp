<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<portlet:defineObjects />

<%
//    List<BaseEntity> baseEntityList = (List<BaseEntity>)renderRequest.getAttribute("entityList");
%>

<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<link rel="stylesheet" media="all" href="/static-usci/ext/resources/css/ext-all.css" />
<link rel="stylesheet" media="all" href="<%=request.getContextPath()%>/css/main.css" />

<script>
    var dataUrl = '<%=getDataURL%>';
    var givenEntityId = '<%=renderRequest.getAttribute("entityId")%>';
    var givenRepDate = '<%=renderRequest.getAttribute("repDate")%>';
    var contextPathUrl = '<%=request.getContextPath()%>';
</script>

<style>
    .node {
        border: 1px solid black;
        margin: 5px;
    }

    .leaf {
        margin: 5px;
    }

    .loading {
        display: none;
        background-image: url("<%=request.getContextPath()%>/pics/loading.gif");
        background-repeat: no-repeat;
        background-position: 0 center;
        text-decoration: none;
        width: 10px;
        cursor: default;
    }

    .not-filled{
        display: none;
        text-decoration: none;
        cursor: default;
        color: red;
        margin-left: 2px;
    }
</style>

<script src="/static-usci/ext/ext-all.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/dev.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/main.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/st_format.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/s_subject_doc.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/w_search_doc.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/s_credit_pc.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/s_person_fio.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/s_org_name.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/s_portfolio.js" type="text/javascript"></script>

<script src="<%=request.getContextPath()%>/js/lang/default.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/lang/<%= ((ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY)).getLocale() %>.js" type="text/javascript"></script>

<c:choose>
    <c:when test="${not empty error}">
        ${error}
    </c:when>
    <c:when test="${empty error}">
        <div id="entity-editor-content">
    </c:when>
</c:choose>

</div>