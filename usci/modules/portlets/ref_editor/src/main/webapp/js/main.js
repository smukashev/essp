Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var currentClassId = null;

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

var grid;
var store;
var subEntityStore;

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
                    entityId = Ext.getCmp("entityId");
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

function loadSubEntity(subNode) {
    var subEntityId = Ext.getCmp(subNode.data.code + "FromItem").getValue();

    subEntityStore.load({
        params: {
            op : 'LIST_ENTITY',
            entityId: subEntityId,
            date: Ext.getCmp('edDate').value,
            asRoot: false
        },
        callback: function(records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
            } else {
                subNode.data.value = records[0].data.value;
                subNode.data.children = records[0].data.children;
                subNode.childNodes = records[0].childNodes;
            }
        }
    });
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
            {name: 'value',     type: 'string'},
            {name: 'simple',     type: 'boolean'},
            {name: 'array',     type: 'boolean'},
            {name: 'ref',     type: 'boolean'},
            {name: 'type',     type: 'string'},
            {name: 'isKey',     type: 'boolean'},
            {name: 'metaId',     type: 'string'}
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

    subEntityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        storeId: 'subEntityStore',
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
            entityId = Ext.getCmp("entityId");

            entityStore.load({
                params: {
                    op : 'LIST_ENTITY',
                    entityId: entityId.getValue(),
                    date: Ext.getCmp('edDate').value,
                    asRoot: true
                },
                callback: function(records, operation, success) {
                    if (!success) {
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
                    }
                }
            });
        },
        maxWidth: 200
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
        maxWidth: 200
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
        maxWidth: 200
    });

    var modalWindow = Ext.create("Ext.Window",{
        title : 'Добавление записи',
        width : 400,
        modal : true,
        closable : false,
        items  : [
            {
                id: "ModalFormPannel",
                width: "100%",
                defaults: {
                    anchor: '100%'
                },
                autoScroll:true
            }],
        tbar : [{
            text : 'Сохранить новую запись' ,
            handler :function(){
                var tree = Ext.getCmp('entityTreeView');
                rootNode = tree.getRootNode();

                var selectedNode = tree.getSelectionModel().getLastSelected();

                var children = selectedNode.childNodes;

                for(var i = 0; i < children.length; i++){
                    if(children[i].data.simple) {
                        if(children[i].data.type == "DATE") {
                            children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem1")
                                .getSubmitValue();
                        } else {
                            children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem1")
                                .getValue();
                        }
                    }
                }

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

                this.up('.window').close();
            }
        }]
    });

    var buttonAdd = Ext.create('Ext.button.Button', {
        id: "entityEditorAddBtn",
        text: 'Добавить новую запись',
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            var selectionModel = tree.getSelectionModel();
            var selectedNode = selectionModel.getLastSelected();

            var children = selectedNode.childNodes;

            var form1 = Ext.getCmp('ModalFormPannel');

            // form.removeAll();

            for(var i = 0; i < children.length; i++){
                if(children[i].data.simple) {
                    if(children[i].data.type == "DATE") {
                        form1.add(Ext.create("Ext.form.field.Date",
                            {
                                id: children[i].data.code + "FromItem1",
                                fieldLabel: children[i].data.title,
                                width: "100%",
                                format: 'd.m.Y'/*,
                             value: new Date(
                             children[i].data.value.
                             replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1'))*/
                            }));
                    } else {
                        form1.add(Ext.create("Ext.form.field.Text",
                            {
                                id: children[i].data.code + "FromItem1",
                                fieldLabel: children[i].data.title,
                                width: "100%",
                                // value: children[i].data.value
                            }));
                    }
                }
            }

            form1.doLayout();

            modalWindow.show();
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
                            } else {
                                if(children[i].data.type == "META_CLASS") {
                                    loadSubEntity(children[i]);
                                }
                            }
                        }

                        console.log("selectedNode", selectedNode);

                        selectedNode.appendChild();

                        Ext.getCmp("entityTreeView").getView().refresh();
                    }
                });


                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPannel');
                form.removeAll();

                form.add(Ext.create("Ext.form.field.ComboBox", {
                    id: "attributesCombo",
                    //fieldLabel: "Attributes:",
                    width: "100%",
                    store: Ext.create('Ext.data.Store', {
                        model: 'attrsStoreModel',
                        pageSize: 100,
                        proxy: {
                            type: 'ajax',
                            url: dataUrl,
                            extraParams: {
                                op : 'LIST_ATTRIBUTES',
                                entityId: selectedNode.data.value,
                                date: Ext.getCmp('edDate').value
                            },
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
                    }),
                    displayField: 'title',
                    valueField: 'name'
                }));

                form.add(Ext.create('Ext.button.Button', {
                    id: "btnFormSave",
                    text: "Добавить поле",
                    handler : function () {
                        form.add(

                        );
                    }
                }));

                for(var i = 0; i < children.length; i++){
                    var disabled = children[i].data.isKey || children[i].data.array
                        || (!selectedNode.data.root && selectedNode.data.ref)
                        || (selectedNode.data.array && !children[i].data.simple && !children[i].data.ref);

                    if (children[i].data.type == "DATE") {
                        form.add(Ext.create("Ext.form.field.Date",
                            {
                                id: children[i].data.code + "FromItem",
                                fieldLabel: children[i].data.title,
                                width: "100%",
                                format: 'd.m.Y',
                                value: new Date(
                                    children[i].data.value.
                                        replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')),
                                disabled: disabled
                            }));
                    } else if (children[i].data.ref) {
                        form.add(Ext.create("Ext.form.field.ComboBox", {
                            id: children[i].data.code + "FromItem",
                            fieldLabel: children[i].data.title,
                            width: "100%",
                            disabled: disabled,
                            store: Ext.create('Ext.data.Store', {
                                model: 'refStoreModel',
                                pageSize: 100,
                                proxy: {
                                    type: 'ajax',
                                    url: dataUrl,
                                    extraParams: {op : 'LIST_BY_CLASS', metaId: children[i].data.metaId},
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
                            }),
                            displayField: 'title',
                            valueField: 'id',
                            value: children[i].data.value
                        }));
                    } else {
                        form.add(Ext.create("Ext.form.field.Text",
                            {
                                id: children[i].data.code + "FromItem",
                                fieldLabel: children[i].data.title,
                                width: "100%",
                                value: children[i].data.value,
                                disabled: disabled
                            }));
                    }
                }

                form.doLayout();
            }
        }
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
            },{
                id: "EntityEditorFormPannel",
                xtype : 'panel',
                region: 'east',
                width: "40%",
                collapsible: true,
                split:true,
                //preventHeader: true,
                title: label_INPUT_FORM,
                defaults: {
                    anchor: '100%'
                },
                bodyPadding: '5 5 0',
                autoScroll:true
            },/*{
             xtype : 'panel',
             region: 'south',
             preventHeader: true,
             width: "60%",
             height: 150,
             autoScroll:true,
             items: [ createItemsGrid()]
             }*/],
        dockedItems: [
            {
                fieldLabel: label_REF,
                id: 'entityEditorComplexTypeCombo',
                xtype: 'combobox',
                store: classesStore,
                valueField:'classId',
                displayField:'className',
                listeners: {
                    change: function (field, newValue, oldValue) {
                        currentClassId = newValue;

                        refStore.proxy.extraParams = {metaId: currentClassId, op: 'LIST_BY_CLASS'};
                        Ext.getCmp('entityEditorrefCombo').value = null;

                        refStore.reload();
                        createItemsGrid(currentClassId);
                    }
                },
                maxWidth: 400
            },{
                fieldLabel: label_ITEMS,
                id: 'entityEditorrefCombo',
                xtype: 'combobox',
                maxWidth: 400,
                store: refStore,
                valueField:'id',
                displayField:'title',
                listeners: {
                    change: function (field, newValue, oldValue) {
                        entityId = Ext.getCmp("entityId");
                        entityId.setValue(newValue);
                    }
                }
            },{
                fieldLabel: label_ENTITY_ID,
                id: 'entityId',
                name: 'entityId',
                xtype: 'textfield',
                maxWidth: 400,
                value: (givenEntityId == "null" ? "" : givenEntityId)
            }, {
                fieldLabel: label_Date,
                id: 'edDate',
                xtype: 'datefield',
                maxWidth: 400
            }, {
                xtype: 'tbseparator'
            }
        ],
        tbar: [
            buttonAdd, buttonShow, buttonXML, buttonShowXML
        ]
    });
});