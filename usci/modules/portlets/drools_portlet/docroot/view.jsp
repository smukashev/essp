<%@ page import="Batch" %>
<%@ page import="java.util.List" %>
<%@ page import="Rule" %>
<%
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:resourceURL var="getDataURL">

    <%--<portlet:param name="metaId" value="testClass" />--%>

</portlet:resourceURL>

<%
    List<Batch> batchList = (List<Batch>)renderRequest.getAttribute("batchList");
    List<Rule> ruleList = (List<Rule>)renderRequest.getAttribute("ruleList");
%>

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>

<script>
    var packageId=null;
    var packageName=null;
    var packageVersionId=null;
    var packageVersionDate=null;
    var packageDesc=null

    $(function() {
        $( "#datepicker" ).datepicker();
        $( "#datepicker2" ).datepicker();
    });

    function showForm(){
        document.getElementById("formAddPackage").style.display="";
    }

    function addPackage(){
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"addPackage","name":document.getElementById("packageName").value,"date":document.getElementById("datepicker2").value},
            success: function(data){
                if (data!="error")
                {
                    $("#messageBox").removeClass().addClass("success").html("Successfully added").fadeIn(2000).fadeOut(4000);
                } else
                {
                    $("#messageBox").removeClass().addClass("error").html("Ooops , Package with such name alreade exists").fadeIn(2000).fadeOut(4000);
                }

            }
        });
        document.getElementById("packageName").value="";
        document.getElementById("datepicker2").value="";
    }

    function updateRule(id){
        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"updateRule","id":id,"name":document.getElementById("area_"+id).value},
            success: function(data){
                if (data!="error")
                {
                    $("#messageBox").removeClass().addClass("success").html("Rule was successfully updated").fadeIn(2000).fadeOut(4000);
                } else
                {
                    $("#messageBox").removeClass().addClass("error").html("Ooops , error").fadeIn(2000).fadeOut(4000);
                }
            }
        });

        document.getElementById("button_"+id).disabled=true;
        document.getElementById("button_"+id).style.display="none";
    }

    function enableButton(name){
//       alert(name);
        document.getElementById("button_"+name).disabled=false;
        document.getElementById("button_"+name).style.display="";
    }

    function displayRules(id){

        <%--$.ajax({--%>
            <%--type: "POST",--%>
            <%--url: "<%=getDataURL%>" ,--%>
            <%--data: {"type":"getRules","id":id},--%>
            <%--success: function(data){--%>
                <%--var json = jQuery.parseJSON(data);--%>
                <%--var div="";--%>
                <%--jQuery.each(json, function() {--%>
<%--//                    alert(this.rule);--%>
                    <%--div="<div><textarea name='"+this.id+"' id='area_"+this.id+"'onchange='javascript:enableButton(this.name)'>"+this.rule+"</textarea><input type='button' value='update' id='button_"+this.id+"' name='"+this.id+"' onclick='javascript:updateRule(this.name)' disabled=true></div>";--%>

                    <%--$(".rules").append(div);--%>

                <%--});--%>

            <%--}--%>
        <%--});--%>
    }


    function displayRules2(){


        document.getElementById("hideable").style.display="none";
        document.getElementById("rules_id").style.display="";

        packageId = document.getElementById("selectBatch").value;
        packageName = $("#selectBatch option:selected").text();
        packageVersionDate = document.getElementById("datepicker").value;
        jQuery('.rules').html('');
        jQuery('.packageInfo').html('');
        var divPackageInfo = "<div><label>Package title: </label><span>"+packageName+"</span><br><label>Description: </label><span>"+packageDesc+"</span><br><label>Version: </label><span>"+packageVersionDate+"</span><br><p></p></div>" ;
        $(".packageInfo").append(divPackageInfo);

         $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"getRules","id":document.getElementById("selectBatch").value,"versionDate":document.getElementById("datepicker").value},
            success: function(data){
                if (data!="noresult"){
                    var json = jQuery.parseJSON(data);

                    var div="";



                    jQuery.each(json, function() {


                        div="<div><label>Rule title:"+this.title+"</label><br><textarea name='"+this.id+"' id='area_"+this.id+"'onchange='javascript:enableButton(this.name)'>"+this.rule+"</textarea><input type='button' style='display: none' value='update' id='button_"+this.id+"' name='"+this.id+"' onclick='javascript:updateRule(this.name)' disabled=true></div>";

                        $(".rules").append(div);


                    });
                }else
                {
                    $(".rules").append("<span>No results.</span>");
                }

            }
        });
    }

    function addRule(){
        document.getElementById("hideable").style.display="";
        document.getElementById("rules_id").style.display="none";
    }

    function saveRule(){
//        alert("Package: "+packageId);
//        alert("Package version "+packageVersionDate);
//           alert(document.getElementById("selectRule").value);

        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"copyRule","ruleId":document.getElementById("selectRule").value,"packageId":packageId,"versionDate":packageVersionDate},
            success: function(data){
                if (data!="error")
                {
                    $("#messageBox").removeClass().addClass("success").html("Successfully added").fadeIn(2000).fadeOut(4000);
                } else
                {
                    $("#messageBox").removeClass().addClass("error").html("Ooops , error").fadeIn(2000).fadeOut(4000);
                }
            }
        });
    }

    function saveNewRule(){
//        alert(document.getElementById("newRuleTitle").value);

        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"addNewRule","packageId":packageId,"versionDate":packageVersionDate,"ruleTitle":document.getElementById("newRuleTitle").value,"rule":document.getElementById("area_new").value},
            success: function(data){
                if (data!="error")
                {
                    $("#messageBox").removeClass().addClass("success").html("Successfully saved").fadeIn(2000).fadeOut(4000);
                } else
                {
                    $("#messageBox").removeClass().addClass("error").html("Ooops , error").fadeIn(2000).fadeOut(4000);
                }
            }
        });
        document.getElementById("newRuleTitle").value="";
        document.getElementById("area_new").value="";
    }


    function addNewRule(){
        var div="";
        div="<label>Rule title: </label><input type='text' id='newRuleTitle'><label>Rule: </label><textarea name='newRuleText' id='area_new'></textarea><button type='button' id='buttonNewSave' onclick='javascript:saveNewRule()'>save</button>";
        $(".addRuleForm").append(div);
    }

    function runRules(){
        packageId = document.getElementById("selectBatch").value;
        packageName = $("#selectBatch option:selected").text();
        packageVersionDate = document.getElementById("datepicker").value;

        alert("Running");
        jQuery('.rules').html('');
        jQuery('.packageInfo').html('');
        var divPackageInfo = "<div><label>Package title: </label><span>"+packageName+"</span><br><label>Description: </label><span>"+packageDesc+"</span><br><label>Version: </label><span>"+packageVersionDate+"</span><br><p></p></div>" ;
        $(".packageInfo").append(divPackageInfo);

        $.ajax({
            type: "POST",
            url: "<%=getDataURL%>" ,
            data: {"type":"runRules","name":packageName,"versionDate":document.getElementById("datepicker").value,"entityId":document.getElementById("entity_id").value},
            success: function(data){
                $(".rules").append(data);

            }
        });
        document.getElementById("entity_id").value="";
    }


    </script>
<div class="messageBox">
<div class="success" id="messageBox" style="display: none"></div>
</div>
<br>
<div class="_20_documentLibraryContainer">
    <div class="aui-layout lfr-app-column-view">
        <div class="aui-layout-content lfr-app-column-view-content " style="background-color: #f8f8f8">

            <div class="aui-column aui-w20 navigation-pane aui-column-first">
                <div class="aui-column-content aui-column-content-first navigation-pane-content ">
                    <div class="lfr-header-row">
                        <div class="lfr-header-row-content">

                        </div>
                    </div>
                    <div class="body-row">
                        <div class="folder-display-style lfr-list-view-content yui3-widget aui-component aui-listview">
                            <div class="folder-display-style lfr-list-view-content folder-display-style lfr-list-view-content-content aui-listview-content" >
                                <div class="container">
                                  <div id="stylized" class="myform">
                                    <form id="formGetRules">
                                        <h1>Get Rules</h1>
                                        <p>Get rules of selected package and version</p>
                                        <label>Name
                                            <span class="small">Select package name</span>
                                        </label>
                                       <select id="selectBatch">
                                            <%for (Batch b:batchList) {%>
                                                <option value="<%=b.getId()%>"><%=b.getName()%></option>
                                            <%}%>
                                        </select>
                                        <label>Version
                                            <span class="small">Select version date</span>
                                        </label>
                                        <input type="text" id="datepicker" />
                                        <button type="button" onclick="javascript:displayRules2()">Show</button><br><br>
                                        <label>Entity
                                            <span class="small">Enter entity id</span>
                                        </label>
                                        <input type="text" id="entity_id" />
                                        <button type="button" onclick="javascript:runRules()">Run</button><br><br>
                                        <div class="spacer"></div>
                                    </form>



                                  </div>
                                   <br>
                                  <div class="stylized">

                                    <form id="formAddPackage">
                                        <h1>Add package</h1>
                                        <p>Add new package</p>

                                        <label>Name
                                            <span class="small">Enter package name</span>
                                        </label>
                                        <input id="packageName" type="text" name="Name">
                                        <label>Date
                                            <span class="small">Enter package version date</span>
                                        </label>
                                        <input type="text" id="datepicker2" />
                                        <button type="button" onclick="javascript:addPackage()">Add</button>
                                        <div class="spacer"></div>
                                    </form>
                                  </div>

                                  </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>

            <div class="aui-column aui-w80 context-pane aui-column-last">

                <div class="lfr-header-row">
                    <div class="toolbar">
                        <input id="addRuleButton" type="button" value="Add Rule" onclick="javascript:addRule()">
                    </div>
                </div>
                <div class="aui-form  yui3-widget aui-form-validator aui-form-validator-content">
                    <div class="document-container">

                        <div class="_20_entries" id="_20_entries">
                        <div class="container">
                            <div class="packageInfo">
                                <%--<p></p>--%>
                            </div>
                          <div id="hideable" style="display: none">
                            <div class="addRules" id="addrules_id">

                                <form id="addRuleForm" class="addRuleForm" >
                                    <label>Select Rule:</label>
                                    <select id="selectRule" >
                                    <%for (Rule r:ruleList) {%>
                                    <option value="<%=r.getId()%>"><%=r.getTitle()%></option>
                                    <%}%>
                                     </select>
                                    <button type="button" onclick="javascript:saveRule()">Add</button>
                                    <button type="button" onclick="javascript:addNewRule()">Add new</button>
                                    <%--<span class="filler">&nbsp;&nbsp;&nbsp;</span>--%>

                                </form>
                            </div>
                           </div>
                            <label></label>
                            <div class="rules" id="rules_id">

                            </div>

                        </div>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </div>
</div>