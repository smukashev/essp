<%@ page import="com.liferay.portal.kernel.util.WebKeys" %>
<%@ page import="com.liferay.portal.theme.ThemeDisplay" %>
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
    boolean readOnly = true;

    for(Role r : UserLocalServiceUtil.getUser(PortalUtil.getUserId(request)).getRoles()) {
        if(r.getName().equals("NationalBankEmployee") || r.getName().equals("Administrator"))
            readOnly = false;
    }
%>

<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<link rel="stylesheet" media="all" href="/static-usci/ext/resources/css/ext-all.css" />
<link rel="stylesheet" media="all" href="<%=request.getContextPath()%>/css/main.css" />

<script>
    var dataUrl = '<%=getDataURL%>';
    var contextPathUrl = '<%=request.getContextPath()%>';
    var readOnly = '<%=readOnly%>' == 'true';
</script>

<script src="<%=request.getContextPath()%>/js/require.js" type="text/javascript"></script>
<script src="/static-usci/ext/ext-all.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/rform.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/package_control.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/new_rule.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/history.js" type="text/javascript"></script>

<script>
    require.config({
        paths: {
            'ace' : '<%=request.getContextPath()%>/js/ace'
        },
        shim: {
            'ace/ace': ['<%=request.getContextPath()%>/js/main.js']
        }
    });

    var editor;
    var newRuleEditor;

    require(['ace/ace'],function(ace){
        console.log("ace code")
        editor = ace.edit('bkeditor');
        editor.setReadOnly(readOnly);
        editor.getSession().on('change', function(){
            Ext.getCmp('btnDel').setDisabled(false);
            if(editor.getSession().getValue() != editor.backup)
            {
                Ext.getCmp('btnCancel').setDisabled(false);
                Ext.getCmp('btnSave').setDisabled(false);
            }
            else {
                Ext.getCmp('btnCancel').setDisabled(true);
                Ext.getCmp('btnSave').setDisabled(true);

            }
        })
    });

</script>

<style type="text/css" media="screen">
    #bkeditor {
        position: relative;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        height: 100%;
    }
</style>


<%--<script src="<%=request.getContextPath()%>/js/ext/ext-all.js" type="text/javascript"></script>--%>
<%--<script src="<%=request.getContextPath()%>/js/create_mc_form.js" type="text/javascript"></script>--%>
<%--<script src="<%=request.getContextPath()%>/js/create_mc_attr_form.js" type="text/javascript"></script>--%>
<%--<script src="<%=request.getContextPath()%>/js/main.js" type="text/javascript"></script>--%>

<script src="<%=request.getContextPath()%>/js/lang/default.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/lang/<%= ((ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY)).getLocale() %>.js" type="text/javascript"></script>


<div id="rules-content"></div>

