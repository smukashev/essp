Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var regex = /^\S+-(\d+)-(\S+)-(\S+)$/;
var currentClass;


function getForm(){
    currentClass = Ext.getCmp('edClass').value;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            metaId: currentClass
        },
        success: function(data){
            document.getElementById('entity-editor-form').innerHTML = data.responseText;
        }
    });
}

var errors = [];

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

function createXML(currentNode, rootFlag, offset, arrayEl, first) {
    var xmlStr = "";

    var children = currentNode.childNodes;

    if(arrayEl) {
        xmlStr += offset + "<item>\n";
    } else {
        if(first) {
            xmlStr += offset + "<entity " +
            (rootFlag ? " class=\"" + currentNode.data.code + "\"" : "") + ">\n";
        } else {
            xmlStr += offset + "<" + currentNode.data.code +
            (rootFlag ? " class=\"" + currentNode.data.code + "\"" : "") + ">\n";
        }
    }

    for(var i = 0; i < children.length; i++){
        if(children[i].data.simple) {
            if(currentNode.data.array) {
                xmlStr += offset + "  " + "<item>";
                xmlStr += children[i].data.value;
                xmlStr += "</item>\n";
            } else {
                xmlStr += offset + "  " + "<" + children[i].data.code + ">";
                xmlStr += children[i].data.value;
                xmlStr += "</" + children[i].data.code + ">\n";
            }
        } else {
            xmlStr += createXML(children[i], false, offset + "    ", currentNode.data.array, false);
        }
    }

    if(arrayEl) {
        xmlStr += offset + "</item>\n";
    } else {
        if(first) {
            xmlStr += offset + "</entity>\n";
        } else {
            xmlStr += offset + "</" + currentNode.data.code + ">\n";
        }
    }

    return xmlStr;
}

Ext.onReady(function() {

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['id','name']
    });

    Ext.define('entityModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title',     type: 'string'},
            {name: 'code',     type: 'string'},
            {name: 'value',     type: 'string'},
            {name: 'simple',     type: 'boolean'},
            {name: 'array',     type: 'boolean'},
            {name: 'type',     type: 'string'},
            {name: 'isKey',     type: 'boolean'}
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
            //entityId = Ext.getCmp("entityId");
            var entityId = document.getElementById('inp-1-' + currentClass + '-null').value;

            entityStore.load({
                params: {
                    op : 'LIST_ENTITY',
                    entityId: entityId,
                    date: Ext.getCmp('edDate').value
                },
                callback: function(records, operation, success) {
                    if (!success) {
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.request.proxy.reader.rawData.errorMessage));
                    }
                }
            });
        },
        maxWidth: 70,
        shadow: true
    });

    var buttonXML = Ext.create('Ext.button.Button', {
        id: "entityEditorXmlBtn",
        text: label_SAVE,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true);

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    xml_data: xmlStr,
                    op: 'SAVE_XML'
                },
                success: function() {
                    console.log('success');
                },
                failure: function() {
                    console.log('woops');
                }
            });
        },
        maxWidth: 70
    });

    var buttonShowXML = Ext.create('Ext.button.Button', {
        id: "entityEditorShowXmlBtn",
        text: 'XML',
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true);

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
                    fieldLabel: 'XML',
                    name: 'id',
                    xtype: 'textarea',
                    value: xmlStr,
                    height: 615
                }],

                buttons: [buttonClose]
            });

            xmlFromWin = new Ext.Window({
                id: "xmlFromWin",
                layout: 'fit',
                title:'XML',
                modal: true,
                maximizable: true,
                items:[xmlForm]
            });

            xmlFromWin.show();
        },
        maxWidth: 50
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
        columns: [{
            xtype: 'treecolumn',
            text: label_TITLE,
            flex: 2,
            sortable: true,
            dataIndex: 'title'
        },{
            text: label_CODE,
            flex: 1,
            dataIndex: 'code',
            sortable: true
        },{
            text: label_VALUE,
            flex: 4,
            dataIndex: 'value',
            sortable: true
        },{
            text: label_SIMPLE,
            flex: 1,
            dataIndex: 'simple',
            sortable: true
        },{
            text: label_ARRAY,
            flex: 1,
            dataIndex: 'array',
            sortable: true
        },{
            text: label_TYPE,
            flex: 1,
            dataIndex: 'type',
            sortable: true
        }],
        listeners : {
            itemclick: function(view, record, item, index, e, eOpts) {
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();

                var buttonSaveAttributes = Ext.create('Ext.button.Button', {
                    id: "btnFormSave",
                    text: label_SAVE,
                    handler : function () {
                        var tree = Ext.getCmp('entityTreeView');
                        var selectedNode = tree.getSelectionModel().getLastSelected();

                        var children = selectedNode.childNodes;

                        for(var i = 0; i < children.length; i++){
                            if(children[i].data.simple) {
                                if(children[i].data.type == "DATE") {
                                    children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem")
                                        .getSubmitValue();
                                } else {
                                    children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem")
                                        .getValue();
                                }
                            }
                        }

                        Ext.getCmp("entityTreeView").getView().refresh();
                    }
                });


                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPanel');
                form.removeAll();
                for(var i = 0; i < children.length; i++){
                    if(children[i].data.simple) {
                        if(children[i].data.type == "DATE") {
                            form.add(Ext.create("Ext.form.field.Date",
                                {
                                    id: children[i].data.code + "FromItem",
                                    fieldLabel: children[i].data.title,
                                    width: "100%",
                                    format: 'd.m.Y',
                                    value: new Date(
                                        children[i].data.value.
                                            replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')),
                                    disabled: children[i].data.isKey
                                }));
                        } else {
                            form.add(Ext.create("Ext.form.field.Text",
                                {
                                    id: children[i].data.code + "FromItem",
                                    fieldLabel: children[i].data.title,
                                    width: "100%",
                                    value: children[i].data.value,
                                    disabled: children[i].data.isKey
                                }));
                        }
                    }
                }

                form.add(buttonSaveAttributes);

                form.doLayout();
            }
        }
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

    var mainPanel = Ext.create('Ext.panel.Panel',{
        height: 500,
        renderTo: 'entity-editor-content',
        title: '&nbsp',
        //preventHeader: true,
        layout: 'border',
        items: [{
            region: 'west',
            width: '20%',
            split: true,
            layout: 'border',
            items: [{
                region: 'north',
                height: '20%',
                split: true,
                layout: {
                    type: 'vbox',
                    padding: 5,
                    align: 'stretch'
                },
                items: [{
                    id: 'edClass',
                    xtype: 'combobox',
                    store: classesStore,
                    labelWidth: 70,
                    valueField:'id',
                    displayField:'name',
                    fieldLabel: label_CLASS
                }, {
                    xtype: 'component',
                    html: "<a href='#' onclick='getForm();'>" +LABEL_UPDATE+ "</a>"
                },{
                    xtype: 'datefield',
                    id: 'edDate',
                    fieldLabel: label_date,
                    listeners: {
                        change: function(){
                            console.log('datefield changed');
                        }
                    },
                    format: 'd.m.Y'
                }]
            },{
                region: 'center',
                height: '80%',
                split: true,
                html: '<div id="entity-editor-form"></div>',
                tbar: [buttonShow, buttonXML, buttonShowXML]
            }]
        },{
            region: 'center',
            split: true,
            items: [entityGrid],
            autoScroll:true
        },{
            id: "EntityEditorFormPanel",
            xtype : 'panel',
            region: 'east',
            width: "20%",
            collapsible: true,
            split:true,
            //preventHeader: true,
            title: label_INPUT_FORM,
            defaults: {
                anchor: '100%'
            },
            bodyPadding: '5 5 0',
            autoScroll:true
        }]

    });
});