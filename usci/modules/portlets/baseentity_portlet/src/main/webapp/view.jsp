<%@ page import="kz.bsbnb.usci.eav.model.base.impl.BaseEntity" %>
<%@ page import="java.util.List" %>
<%@ page import="kz.bsbnb.usci.porltet.entity_portlet.TestClass" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.hibernate.mapping.MetaAttributable" %>
<%@ page import="kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute" %>
<%@ page import="kz.bsbnb.usci.eav.model.meta.impl.MetaClass" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui" %>

<portlet:defineObjects />

<%
    List<BaseEntity> baseEntityList = (List<BaseEntity>)renderRequest.getAttribute("entityList");
//    List<TestClass> baseEntityList = (List<TestClass>)renderRequest.getAttribute("entityList");
%>

<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>


<script>

    var str = {meta:{"name" : "JSON Editor","powered by" : "jsLinb", "version" : "1.0"}};
    var metaId;
    var count=0;
//    $(document).ready(function(){
//        JSONeditor.start('tree','jform',"",false);
//    });

    function ajaxGetMeta(metaid)
    {
        var obj = {};
        var container = document.getElementById('formSend');
        var inputs = container.getElementsByTagName('input');
        for (var index = 0; index < inputs.length; ++index) {
            if (inputs[index].name!=""){
            obj[inputs[index].name] = inputs[index].value;}
        }
//        alert(JSON.stringify(obj));
        metaId = metaid;
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"metaId":metaId,"json":"makeTree","obj":JSON.stringify(obj)},
            success: function(data){
                if (data=="error"){
                    alert("Key attribute is wrong");
                }
                else{
                document.getElementById("formSend").style.display="none";
                var str = jQuery.parseJSON(data);
                    document.getElementById("treeName").innerHTML="<p>BaseEntity:</p>";
                JSONeditor.start('tree','jform',str,false);
                }
            }
        });
    }

    function getKeyAttributes(metaid)
    {
        metaId = metaid;
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"metaId":metaId,"json":"getAttributes"},
            success: function(data){


                var str = jQuery.parseJSON(data);
                console.log(str);
//
//                JSONeditor.start('tree','jform',str,false);

            }
        });
    }

    function sendJson(json){
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"metaId":metaId, "json":JSON.stringify(json)},
            dataType: "json",
            success: function(data){

            }
        });
    }

    function go(id){
        document.getElementById("addEntity").style.display="none";
        document.getElementById("formSend").style.display="";
        document.getElementById("formSend").innerHTML =document.getElementById(id).innerHTML;
        metaId = id;
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"metaId":metaId,"json":"makeTreeClass"},
            success: function(data){


                    var str = jQuery.parseJSON(data);
                    document.getElementById("treeName").innerHTML="<p>MetaClass:</p>";
                    JSONeditor.start('tree','jform',str,false);

            }
        });
    }

    function addEntity(){
        document.getElementById("addEntity").style.display="";
        document.getElementById("formSend").style.display="none";
        document.getElementById("tree").innerHTML="";
        document.getElementById("treeName").innerHTML="";
        document.getElementById("jform").innerHTML="";
        count=0;
    }

    function doAddEntity(){
        var container = document.getElementById('inForm');
        var obj = {};
        obj["className"] = document.getElementById('className').value;

        var inputs = container.getElementsByTagName('input');
        for (var index = 0; index < inputs.length; ++index) {
            if (inputs[index].type!="checkbox"){
                obj[inputs[index].name] = inputs[index].value;
            }else{
                obj[inputs[index].name] = inputs[index].checked;
            }
        }

        var selects = container.getElementsByTagName('select');
        for (var index1 = 0; index1 < selects.length; ++index1) {
            if (selects[index1].name!=""){
                obj[selects[index1].name] = selects[index1].value;
            }
        }

//         alert(JSON.stringify(obj));
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"metaId":"no","json":"addEntity","obj":JSON.stringify(obj),"count":count},
            success: function(data){
                if (data=="error"){
                    alert("Key attribute is wrong");
                }
                else{
                    alert("Successfully added !");
                    location.reload();
//                    document.getElementById("formSend").style.display="none";
//                    var str = jQuery.parseJSON(data);
//                    document.getElementById("treeName").innerHTML="<p>BaseEntity:</p>";
//                    JSONeditor.start('tree','jform',str,false);
                }
            }
        });
    }

    function addAttribute(){
        count++;
        var dd = document.getElementById('inForm').innerHTML;
        dd = dd + "<label><span>Attr_"+count+" name:</span><input type='text' name='name_"+count+"'></label><label><span>Attr_"+count+" value:</span><input type='text' name='value_"+count+"'></label>";
        dd = dd + "<label><span>Type:</span><select name='type_"+count+"'><option>STRING</option><option>BOOLEAN</option><option>DATE '01/01/1990'</option><option>INTEGER</option><option>DOUBLE</option></select></label>";
        dd = dd + "<label><span>isKey:</span><input type='checkbox' name='check_"+count+"' value='key'></label>";
        document.getElementById('inForm').innerHTML=dd;
    }

</script>

<div class="_20_documentLibraryContainer">
    <div class="aui-layout lfr-app-column-view">
        <div class="aui-layout-content lfr-app-column-view-content " style="background-color: #f8f8f8">

            <div class="aui-column aui-w20 navigation-pane aui-column-first">
            <div class="aui-column-content aui-column-content-first navigation-pane-content ">
                <div class="lfr-header-row">
                    <input id="save" type="button" value="Add baseentity" onclick="javascript:addEntity()">
                </div>
                <div class="body-row">
                    <div class="folder-display-style lfr-list-view-content yui3-widget aui-component aui-listview">
                         <div class="folder-display-style lfr-list-view-content folder-display-style lfr-list-view-content-content aui-listview-content" >

                             <ul class="lfr-component" style="height:600px; overflow-y: scroll;">
                                 <%  if (baseEntityList.size()!=0){
                                     for (BaseEntity entity : baseEntityList) {%>
                                 <li class="folder file-entry-type " style="padding:5px;">
                                     <a class="browse-folder" name="<%=entity.getMeta().getClassName()%>" onclick="javascript:go(this.name)">
                                         <span>
                                                <img src="/baseentity_portlet-0.0.1-SNAPSHOT/iconn.png">
                                         </span>
                                         <span class="entry-title" id="metaClassName"> <%= entity.getMeta().getClassName() %> </span>
                                     </a>

                                 </li>
                                 <form id="<%=entity.getMeta().getClassName()%>" style="display: none">
                                  <div>
                                     <p>Enter the key attributes :</p>
                                 <% for (String attr: entity.getMeta().getAttributeNames()){%>

                                    <%if (entity.getMeta().getMetaAttribute(attr).isKey()){%>
                                      <label><span><%=attr%></span>:<input type="text" name="<%=attr%>"> </label>
                                     <%}%>

                                 <%}%>
                                     <button type="button" onclick='javascript:ajaxGetMeta("<%=entity.getMeta().getClassName()%>")'>Submit</button>
                                    </div>
                                 </form>
                                 <%}}%>
                               </ul>

                         </div>
                    </div>

                </div>
            </div>
            </div>

            <div class="aui-column aui-w80 context-pane aui-column-last">

                <div class="lfr-header-row">
                    <div class="lfr-header-row-content">
                        <div class="toolbar">

                        </div>
                    </div>
                </div>
                <div class="aui-form  yui3-widget aui-form-validator aui-form-validator-content">
                    <div class="document-container">
                        <div class="_20_entries" id="_20_entries">
                           <div class="attributes_form">
                            <form id="formSend" style="display: none">
                            </form>
                            <form id="addEntity" style="display: none">
                                <label>
                                    <span>Class Name:</span><input type="text" name="className" id="className">
                                </label>
                                <div id="inForm">
                                </div>

                                <button type="button" onclick='javascript:addAttribute()'>Add Entity</button>
                                <button type="button" onclick='javascript:addAttribute()'>Add Set</button>
                                <button type="button" onclick='javascript:addAttribute()'>Add Complex</button>
                                <button type="button" onclick='javascript:addAttribute()'>Add attribute</button>

                                <button type="button" onclick='javascript:addAttribute()'>Add attribute</button>
                                <button type="button" onclick='javascript:doAddEntity()'>Save</button>
                            </form>
                           </div>
                            <div id="treeName"></div>
                            <%--<div class="XMLHolder" id="XMLHolder">--%>
                                <div style="position:relative;left:10px;width:500px;height: 600px;overflow: scroll;" id="tree"></div>
                                <div style="position:absolute;top:40px;left:850px" id="jform"></div>

                            <%--</div>--%>
                        </div>

                    </div>

                </div>
            </div>

        </div>
    </div>
</div>