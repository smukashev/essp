
Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*',
    'Ext.ux.CheckColumn'
]);

var currentClassId = null;


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

var grid;
var store;

function createItemsGrid(itemId) {
    if(grid == null) {
        Ext.define('myModel', {
            extend: 'Ext.data.Model',
            fields: ['id','code','title']
        });

        store = Ext.create('Ext.data.Store', {
            model: 'myModel',
            remoteGroup: true,
            buffered: true,
            leadingBufferZone: 300,
            pageSize: 100,
            proxy: {
                type: 'ajax',
                url: dataUrl,
                extraParams: {op : 'LIST_BY_CLASS', metaId : itemId},
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

        grid = Ext.create('Ext.grid.Panel', {
            id: "itemsGrid",
            height: "100%",
            store: store,

            columns: [
                {
                    text     : 'ID',
                    dataIndex: 'id',
                    flex:1
                },
                {
                    text     : label_CODE,
                    dataIndex: 'code',
                    flex:1
                },
                {
                    text     : label_TITLE,
                    dataIndex: 'title',
                    flex:3
                }
            ],
            title: label_ITEMS,
            listeners : {
                itemdblclick: function(dv, record, item, index, e) {
                    entityId = Ext.getCmp("leftEntityId");
                    entityId.setValue(record.get('id'));
                }
            }
        });

        return grid;
    } else {
        store.load({
            params: {
                metaId: itemId,
                op : 'LIST_BY_CLASS'
            },
            callback: function(records, operation, success) {
                if (!success) {
                    Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA);
                }
            }
        });
    }
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


Ext.onReady(function() {
    grid = null;

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['classId','className']
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

    var refStore = Ext.create('Ext.data.Store', {
        model: 'refStoreModel',
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_BY_CLASS'},
            actionMethods: {
                read: 'POST'
            },
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'total'
            }
        },
        autoLoad: false,
        remoteSort: true
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
            leftEntityId = Ext.getCmp("leftEntityId");
            rightEntityId = Ext.getCmp("rightEntityId");

            entityStore.load({
                params: {
                    op : 'LIST_ENTITY',
                    leftEntityId: leftEntityId.getValue(),
                    rightEntityId: rightEntityId.getValue()
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
            leftEntityId = Ext.getCmp("leftEntityId");
            rightEntityId = Ext.getCmp("rightEntityId");

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    op: 'SAVE_JSON',
                    json_data: JSONstr,
                    leftEntityId: leftEntityId.getValue(),
                    rightEntityId: rightEntityId.getValue()
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
        title : 'Панель данных',
        preventHeader: true,
        width : '100%',
        height: '500px',
        renderTo : 'entity-editor-content',
        layout : 'border',
        defaults : {
            padding: '3'
        },
        items  : [
            {
                xtype : 'panel',
                region: 'center',
                preventHeader: true,
                width: "60%",
                autoScroll:true,
                items: [entityGrid]
            }],
        dockedItems: [
            {
                fieldLabel: label_LEFT_ENTITY_ID,
                id: 'leftEntityId',
                name: 'leftEntityId',
                xtype: 'textfield',
                value: (givenEntityId == "null" ? "" : givenEntityId)
            },
            {
                fieldLabel: label_RIGHT_ENTITY_ID,
                id: 'rightEntityId',
                name: 'rightEntityId',
                xtype: 'textfield',
                value: (givenEntityId == "null" ? "" : givenEntityId)
            },
            buttonShow, buttonShowXML, buttonXML
        ]
    });
});
