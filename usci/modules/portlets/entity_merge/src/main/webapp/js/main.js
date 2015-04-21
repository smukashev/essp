Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*',
    'Ext.ux.CheckColumn'
]);

var tabs;
var grid;
var currentClass;
var currentClass2;
var regex = /^\S+-(\d+)-(\S+)-(\S+)$/;
var errors = [];

function createJSON(currentNode, offset, first){

    var JSONstr = "";
    var children = currentNode.childNodes;

    if(first){
        if(currentNode.data.keep_left){
            JSONstr += offset + "{" +"\n" +
                         '"action" : "keep_left", \n'+
                         '"childMap" : [ \n';
        }
        if(currentNode.data.keep_right){
            JSONstr += offset + "{" +"\n" +
                         '"action" : "keep_right", \n'+
                         '"childMap" : [ \n';
        }
        if(currentNode.data.keep_both){
            JSONstr += offset + "{" +"\n" +
                         '"action" : "keep_both", \n'+
                         '"childMap" : [ \n';
        }
        if(currentNode.data.merge){
            JSONstr += offset + "{" +"\n" +
                                     '"action" : "merge", \n'+
                                     '"childMap" : [ \n';
        }
    }

    for(var i = 0; i < children.length; i++){
        if(currentNode.data.array){
            if(children[i].data.simple){

                JSONstr += "";

            } else {
                if(children[i].data.keep_left)
                {
                    JSONstr += offset + '{ "id":{ "type":"long", "left": "'+ children[i].data.id_left +'", "right":"'+ children[i].data.id_right+'"},'+
                                ' "map": { "action" : "keep_left", "childMap" : ['+ createJSON(children[i], offset+" ", false, false)+'] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.keep_right)
                {
                    JSONstr += offset + '{ "id":{ "type":"long", "left":"'+ children[i].data.id_left +'", "right":"'+ children[i].data.id_right+'"},'+
                                ' "map": { "action" : "keep_right", "childMap" : ['+ createJSON(children[i], offset+" ", false, false)+'] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.merge)
                {
                    JSONstr += offset + '{"id":{ "type":"long", "left":"'+ children[i].data.id_left +'", "right":"'+ children[i].data.id_right+'"},'+
                                ' "map": { "action" : "merge", "childMap" : ['+ createJSON(children[i], offset+" ", false, false)+'] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.keep_both)
                {
                    JSONstr += offset + '{"id":{ "type":"long", "left":"'+ children[i].data.id_left +'", "right":"'+ children[i].data.id_right+'"},'+
                                ' "map": { "action" : "keep_both", "childMap" : ['+ createJSON(children[i], offset+" ", false, false)+'] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
            }

        }
        if(!currentNode.data.array)
        {
            if(children[i].data.simple){
                if(children[i].data.keep_left)
                {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                ' "map": { "action" : "keep_left", "childMap" : [] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.keep_right)
                {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                ' "map": { "action" : "keep_right", "childMap" : [] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.merge)
                {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                ' "map": { "action" : "merge", "childMap" : [] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }
                if(children[i].data.keep_both)
                {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                ' "map": { "action" : "keep_both", "childMap" : [] } }';
                    if(!(i + 1 == children.length)){
                        JSONstr +=",";
                    }
                }

            } else {
                var subNodeAction;
                if(currentNode.data.keep_left){
                    subNodeAction = "keep_left";
                }
                if(currentNode.data.keep_right){
                    subNodeAction = "keep_right";
                }
                if(currentNode.data.merge){
                    subNodeAction = "merge";
                }
                if(currentNode.data.keep_both){
                    subNodeAction = "keep_both";
                }
                if(children[i].data.keep_left)
                {

                    if (currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[")==-1){

                         JSONstr += offset + '{ "id":{ "type":"long", "left": "'+currentNode.data.id_left +'", "right":"'+ currentNode.data.id_right+'"},'+
                                    ' "map": { "action" : "'+subNodeAction+'", "childMap" : [{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_left", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    } else {
                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_left", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }
                    }

                }
                if(children[i].data.keep_right)
                {
                    if(currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[")==-1){

                         JSONstr += offset + '{ "id":{ "type":"long", "left": "'+currentNode.data.id_left +'", "right":"'+ currentNode.data.id_right+'"},'+
                                    ' "map": { "action" : "'+subNodeAction+'", "childMap" : [{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_right", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }
                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_right", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    }

                }
                if(children[i].data.merge)
                {
                    if(currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[")==-1){
                         JSONstr += offset + '{ "id":{ "type":"long", "left": "'+currentNode.data.id_left +'", "right":"'+ currentNode.data.id_right+'"},'+
                                    ' "map": { "action" : "'+subNodeAction+'", "childMap" : [{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "merge", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "merge", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    }
                }
                if(children[i].data.keep_both)
                {
                    if(currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[")==-1){
                         JSONstr += offset + '{ "id":{ "type":"long", "left": "'+currentNode.data.id_left +'", "right":"'+ currentNode.data.id_right+'"},'+
                                    ' "map": { "action" : "'+subNodeAction+'", "childMap" : [{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_both", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"'+ children[i].data.code +'"},'+
                                    ' "map": { "action" : "keep_both", "childMap" : ['+createJSON(children[i], offset+" ", false)+'] } }';
                        if(!(i + 1 == children.length)){
                            JSONstr +=",";
                        }

                    }
                }
            }
        }
    }
    if(JSONstr.indexOf(",", JSONstr.length - ",".length) !== -1){
        JSONstr = JSONstr.slice(0, - 1);
    }
    if(first){
        JSONstr += "]}"
    }

    return JSONstr;

}

function markEntityKeepLeft(){

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if(selectedNode.data.keep_left){
        selectedNode.data.keep_left = false;
    } else {
        selectedNode.data.keep_left = true;
        selectedNode.data.keep_right = false;
        selectedNode.data.merge = false;
        selectedNode.data.keep_both = false;
    }

     Ext.getCmp("entityTreeView").getView().refresh();
}


function markEntityKeepRight(){

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if(selectedNode.data.keep_right){
        selectedNode.data.keep_right = false;
    } else {
        selectedNode.data.keep_right = true;
        selectedNode.data.keep_left = false;
        selectedNode.data.merge = false;
        selectedNode.data.keep_both = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}


function markEntityMerge(){

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if(selectedNode.data.merge){
        selectedNode.data.merge = false;
    } else {
        selectedNode.data.merge = true;
        selectedNode.data.keep_right = false;
        selectedNode.data.keep_left = false;
        selectedNode.data.keep_both = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}

function markEntityKeepBoth(){

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if(selectedNode.data.keep_both){
        selectedNode.data.keep_both = false;
    } else {
        selectedNode.data.keep_both = true;
        selectedNode.data.keep_left = false;
        selectedNode.data.keep_right = false;
        selectedNode.data.merge = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}

function getForm(){
    currentClass = Ext.getCmp('edClass').value;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            meta: currentClass
        },
        success: function(data){
            document.getElementById('entity-editor-form').innerHTML = data.responseText;
        }
    });
}

function getForm2(){
    currentClass2 = Ext.getCmp('edClass2').value;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            meta: currentClass2
        },
        success: function(data){
            document.getElementById('entity-editor-form2').innerHTML = data.responseText;
        }
    });
}

function find(control){
    var nextDiv = control.parentNode.nextSibling;
    var inputDiv = control.previousSibling.previousSibling;

    var info = inputDiv.id.match(regex);


    var params = {op : 'FIND_ACTION', metaClass: info[2]};
    for(var i=0;i<errors.length;i++)
        errors[i].style.display = 'none';

    errors = [];

    for (var  i = 0; i < nextDiv.childNodes.length; i++) {
        var preKeyElem = nextDiv.childNodes[i];
        if(preKeyElem.className.indexOf('leaf') > -1) {
            filterLeaf(preKeyElem, params);
        } else {
            filterNode(preKeyElem, params);
        }
    }

    if(errors.length > 0) {
        for (var i = 0; i < errors.length; i++) {
            errors[i].style.display = 'inline';
        }
        return;
    } else {
        var loadDiv = control.nextSibling;
        loadDiv.style.display = 'inline';
    }


    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: params,
        success: function(response) {
            var data = JSON.parse(response.responseText);
            if(data.data > -1)
                inputDiv.value = data.data;
            else
                inputDiv.value = '';

            loadDiv.style.display = 'none';
        },
        failure: function() {
            console.log('woops');
        }
    });
}

function filterLeaf(control, queryObject){
    for(var i =0 ;i<control.childNodes.length;i++) {
        var childControl = control.childNodes[i];
        if(childControl.tagName == 'INPUT' || childControl.tagName=='SELECT') {
            var info =  childControl.id.match(regex);
            var id = info[1];

            if(childControl.value.length == 0) {
                errors.push(document.getElementById('err-' + id));
            }

            queryObject[info[3]] = childControl.value;
        }
    }
}

function filterNode(control, queryObject){
    for(var i =0; i<control.childNodes.length;i++) {
        var childControl = control.childNodes[i];
        if(childControl.className != undefined && childControl.className.indexOf('leaf') > -1) {
            filterLeaf(childControl, queryObject);
            break;
        }
    }
}

function getLeftEntityId() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("leftEntityId").getValue();
    } else {
        return document.getElementsByClassName('inp-1')[0].value;
    }
}

function getRightEntityId() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("rightEntityId").getValue();
    } else {
        return document.getElementsByClassName('inp-1')[1].value;
    }
}

function getLeftReportDate() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("leftReportDate").getValue();
    } else {
        return Ext.getCmp("edDate").getValue();
    }
}

function getRightReportDate() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("rightReportDate").getValue();
    } else {
        return Ext.getCmp("edDate2").getValue();
    }
}

Ext.onReady(function() {
    grid = null;

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['id','name']
    });

    var classesStore = Ext.create('Ext.data.Store', {
        model: 'classesStoreModel',
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_CLASSES'},
            actionMethods: {
                read: 'POST'
            },
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'total'
            }
        },
        autoLoad: true,
        remoteSort: true
    });

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['id','title']
    });

    Ext.define('entityModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title',     type: 'string'},
            {name: 'code',     type: 'string'},
            {name: 'valueLeft',     type: 'string'},
            {name: 'valueRight',     type: 'string'},
            {name: 'simple',     type: 'boolean'},
            {name: 'array',     type: 'boolean'},
            {name: 'type',     type: 'string'},
            {name: 'keep_left',    type: 'boolean', defaultValue: false},
            {name: 'keep_right',    type: 'boolean', defaultValue: false},
            {name: 'merge',    type: 'boolean', defaultValue: false},
            {name: 'keep_both',    type: 'boolean', defaultValue: false},
            {name: 'id_left',    type: 'string'},
            {name: 'id_right',    type: 'string'},
        ]
    });

    var entityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ENTITY'}
        },
        folderSort: true
    });

    var buttonShow = Ext.create('Ext.button.Button', {
        id: "entityEditorShowBtn",
        text: label_VIEW,
        handler : function (){
            leftReportDate = Ext.getCmp("leftReportDate");
            rightReportDate = Ext.getCmp("rightReportDate");

            entityStore.load({
                params: {
                    op : 'LIST_ENTITY',
                    leftEntityId: getLeftEntityId(),
                    leftReportDate: getLeftReportDate(),
                    rightEntityId: getRightEntityId(),
                    rightReportDate : getRightReportDate()
                },
                callback: function(records, operation, success) {
                    if (!success) {
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
                    }
                }
            });
        }
    });

    var buttonXML = Ext.create('Ext.button.Button', {
        id: "entityEditorXmlBtn",
        text: label_SAVE,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var JSONstr = createJSON(rootNode.childNodes[0], "", true)
            var leftReportDate = Ext.getCmp("leftReportDate");
            var rightReportDate = Ext.getCmp("rightReportDate");
            var deleteUnusedChecked = document.getElementById('deleteUnused').checked;

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    op: 'SAVE_JSON',
                    json_data: JSONstr,
                    leftEntityId: getLeftEntityId(),
                    rightEntityId: getRightEntityId(),
                    leftReportDate: getLeftReportDate(),
                    rightReportDate : getRightReportDate(),
                    deleteUnused : deleteUnusedChecked
                },
                success: function() {
                    Ext.MessageBox.alert(label_DB_SUCCESS_TITLE, label_DB_SUCCESS);
                },
                failure: function() {
                    Ext.MessageBox.alert(label_DB_FAILURE_TITLE, label_DB_FAILURE);
                }
            });
        }
    });

    var buttonShowXML = Ext.create('Ext.button.Button', {
        id: "entityEditorShowXmlBtn",
        text: 'JSON',
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var JSONstr = createJSON(rootNode.childNodes[0], "", true)

            var buttonClose = Ext.create('Ext.button.Button', {
             id: "itemFormCancel",
             text: label_CANCEL,
             handler : function (){
             Ext.getCmp('xmlFromWin').destroy();
             }
             });

             var xmlForm = Ext.create('Ext.form.Panel', {
             id: 'xmlForm',
             region: 'center',
             width: 615,
             fieldDefaults: {
             msgTarget: 'side'
             },
             defaults: {
             anchor: '100%'
             },

             bodyPadding: '5 5 0',
             items: [{
             fieldLabel: 'JSON',
             name: 'id',
             xtype: 'textarea',
             value: JSONstr,
             height: 615
             }],

             buttons: [buttonClose]
             });

             xmlFromWin = new Ext.Window({
             id: "xmlFromWin",
             layout: 'fit',
             title:'JSON',
             modal: true,
             maximizable: true,
             items:[xmlForm]
             });

             xmlFromWin.show();
        }
    });

    var entityGrid = Ext.create('Ext.tree.Panel', {
        //collapsible: true,
        id: 'entityTreeView',
        preventHeader: true,
        useArrows: true,
        rootVisible: false,
        store: entityStore,
        multiSelect: true,
        singleExpand: true,
        height: 300,
        autoScroll: true,
        columns: [{
            xtype: 'treecolumn',
            text: label_TITLE,
            flex: 4,
            sortable: true,
            dataIndex: 'title'
        },{
            text: label_CODE,
            flex: 2,
            dataIndex: 'code',
            sortable: true
        },{
            text: label_VALUE_1,
            flex: 3,
            dataIndex: 'valueLeft',
            sortable: true
        },{
            text: label_VALUE_2,
            flex: 3,
            dataIndex: 'valueRight',
            sortable: true
        },{
            text: label_TYPE,
            flex: 2,
            dataIndex: 'type',
            sortable: true
        },
        {
            text: label_KEEP_BOTH,
            flex: 3,
            dataIndex: 'keep_both',
            sortable: true,
                renderer: function (dataIndex) {
                            return '<center><input type="checkbox" onclick="markEntityKeepBoth()"'+ (dataIndex ? 'checked' : '') +' /></center>'
                        }
        },{
            text: label_KEEP_LEFT,
            flex: 3,
            dataIndex: 'keep_left',
            sortable: true,
                renderer: function (dataIndex) {
                            return '<center><input type="checkbox" onclick="markEntityKeepLeft()"'+ (dataIndex ? 'checked' : '') +' /></center>'
                        }
         },{
            text: label_KEEP_RIGHT,
            flex: 3,
            dataIndex: 'keep_right',
            sortable: true,
                renderer: function (dataIndex) {
                            return '<center><input type="checkbox" onclick="markEntityKeepRight()"'+ (dataIndex ? 'checked' : '') +' /></center>'
                        }
         },{
            text: label_MERGE,
            flex: 3,
            dataIndex: 'merge',
            sortable: true,
                renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                            if(record.get('array')){
                                return '<center><input type="checkbox" onclick="markEntityMerge()"'+ (record.get('merge') ? 'checked' : '') +' /></center>'
                            } else {
                                return "";
                            }
                     }
         }],
        listeners : {}
    });

    mainEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title : label_MERGE_PANEL_BY_ID,
        preventHeader: true,
        width : '100%',
        height: '100%',
        border: 0,
        defaults : {
            padding: '3'
        },
        dockedItems: [
            {
                xtype: 'panel',
                layout: 'hbox',
                border: 0,
                items: [
                    {
                        fieldLabel: label_LEFT_ENTITY_ID,
                        id: 'leftEntityId',
                        name: 'leftEntityId',
                        xtype: 'textfield',
                        margin: '10 10 10 10',
                        value: (givenEntityId == "null" ? "" : givenEntityId)
                    },
                    {
                        fieldLabel: label_LEFT_REPORT_DATE,
                        id: 'leftReportDate',
                        name: 'leftReportDate',
                        xtype: 'datefield',
                        format: 'd.m.Y',
                        margin: '10 10 10 10'
                    }
                ]
            },
            {
                xtype: 'panel',
                layout: 'hbox',
                border: 0,
                items: [
                    {
                        fieldLabel: label_RIGHT_ENTITY_ID,
                        id: 'rightEntityId',
                        name: 'rightEntityId',
                        xtype: 'textfield',
                        margin: '10 10 10 10',
                        value: (givenEntityId == "null" ? "" : givenEntityId)
                    },
                    {
                        fieldLabel: label_RIGHT_REPORT_DATE,
                        id: 'rightReportDate',
                        name: 'rightReportDate',
                        xtype: 'datefield',
                        format: 'd.m.Y',
                        margin: '10 10 10 10'
                    },
                ]
            }
        ]
    });

    var clientEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title : label_MERGE_PANEL_BY_ID,
        preventHeader: true,
        width : '100%',
        height: '100%',
        defaults : {
            padding: '3'
        },
        dockedItems: [
            {
                xtype: 'panel',
                layout: 'hbox',
                border: 0,
                items: [
                    {
                        xtype: 'panel',
                        layout: 'vbox',
                        padding: 15,
                        border: 0,
                        items: [
                            {
                                id: 'edClass',
                                xtype: 'combobox',
                                store: classesStore,
                                valueField:'id',
                                displayField:'name',
                                fieldLabel: label_CLASS
                            },
                            {
                                xtype: 'component',
                                html: "<a href='#' onclick='getForm();'>" +LABEL_UPDATE+ "</a>"
                            },
                            {
                                xtype: 'datefield',
                                id: 'edDate',
                                fieldLabel: label_date,
                                format: 'd.m.Y'
                            },
                            {
                                xtype: 'component',
                                html: '<div id="entity-editor-form" style="height: 350px;"></div>'
                            }
                        ]
                    },
                    {
                        xtype: 'tbfill'
                    },
                    {
                        xtype: 'panel',
                        layout: 'vbox',
                        padding: 15,
                        border: 0,
                        items: [
                            {
                                id: 'edClass2',
                                xtype: 'combobox',
                                store: classesStore,
                                valueField:'id',
                                displayField:'name',
                                fieldLabel: label_CLASS
                            },
                            {
                                xtype: 'component',
                                html: "<a href='#' onclick='getForm2();'>" +LABEL_UPDATE+ "</a>"
                            },
                            {
                                xtype: 'datefield',
                                id: 'edDate2',
                                fieldLabel: label_date,
                                format: 'd.m.Y'
                            },
                            {
                                xtype: 'component',
                                html: '<div id="entity-editor-form2" style="height: 350px;"></div>'
                            }
                        ]
                    }
                ]
            }
        ]
    });

    tabs = Ext.widget('tabpanel', {
        id:"tabs",
        width: '100%',
        height: '100%',
        layout: 'fit',
        activeTab: 0,
        border: 0,
        defaults :{
           bodyPadding: 0
        },
        items: [mainEntityEditorPanel, clientEntityEditorPanel]
    });

    var rootPanel = Ext.create('Ext.panel.Panel', {
        renderTo: 'merge-content',
        width : '100%',
        height: '700px',
        preventHeader: true,
        layout: 'border',
        items: [
            {
                region: 'north',
                height: '50%',
                split: true,
                items: [tabs]
            },
            {
                region: 'south',
                height: '50%',
                split: true,
                items: [
                    {
                        region: 'north',
                        height: '10%',
                        width: '100%',
                        layout: 'hbox',
                        split: true,
                        items: [
                            buttonShow, buttonShowXML, buttonXML,
                            {
                                fieldLabel: label_DELETE_UNUSED,
                                xtype: 'component',
                                html: "<div style='padding-left: 20px; padding-top: 5px;'><input type='checkbox' id='deleteUnused' name='deleteUnused' value='deleteUnused'/> " + label_DELETE_UNUSED + "</div>"
                            }
                        ]
                    },
                    {
                        region: 'north',
                        height: '90%',
                        split: true,
                        items: [entityGrid]
                    }
                ]
            }
        ]
    });
});
