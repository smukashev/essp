<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>
<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.liferay.portal.util.PortalUtil" %>
<%@ page import="com.liferay.portal.service.UserLocalServiceUtil" %>
<%@ page import="com.liferay.portal.model.Role" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui" %>

<portlet:defineObjects />

<%
//    List<BaseEntity> baseEntityList = (List<BaseEntity>)renderRequest.getAttribute("entityList");
%>
<%
    boolean isNb = false;

    for(Role r : UserLocalServiceUtil.getUser(PortalUtil.getUserId(request)).getRoles()) {
        if(r.getName().equals("NationalBankEmployee") || r.getName().equals("Administrator"))
            isNb = true;
    }
%>
<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<link rel="stylesheet" media="all" href="/static-usci/ext/resources/css/ext-all.css" />
<link rel="stylesheet" media="all" href="<%=request.getContextPath()%>/css/main.css" />

<script>
    var dataUrl = '<%=getDataURL%>';
    var givenEntityId = '<%=renderRequest.getAttribute("entityId")%>';
    var contextPathUrl = '<%=request.getContextPath()%>';
    var isNb = '<%=isNb%>' == 'true';
</script>

<script src="/static-usci/ext/ext-all.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/main.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/st_format.js" type="text/javascript"></script>

<script src="<%=request.getContextPath()%>/js/lang/default.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/lang/<%= ((ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY)).getLocale() %>.js" type="text/javascript"></script>

<div id="entity-editor-content">

</div>