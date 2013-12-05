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
                    text     : 'Код',
                    dataIndex: 'code',
                    flex:1
                },
                {
                    text     : 'Наименоваие',
                    dataIndex: 'title',
                    flex:3
                }
            ],
            title: 'Элементы',
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
                    Ext.MessageBox.alert('Ошибка', 'Не возможно получить данные');
                }
            }
        });
    }
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
            {name: 'type',     type: 'string'}
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
        text: 'Просмотр',
        handler : function (){
            entityId = Ext.getCmp("entityId");

            entityStore.load({
                params: {
                    op : 'LIST_ENTITY',
                    entityId: entityId.getValue()
                },
                callback: function(records, operation, success) {
                    if (!success) {
                        Ext.MessageBox.alert('Ошибка', 'Не возможно получить данные: ' + operation.error);
                    }
                }
            });
        }
    });

    var buttonXML = Ext.create('Ext.button.Button', {
        id: "entityEditorXmlBtn",
        text: 'Сохранить',
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

            /*var buttonClose = Ext.create('Ext.button.Button', {
                id: "itemFormCancel",
                text: 'Отмена',
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

            xmlFromWin.show();*/
        }
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
             text: 'Отмена',
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
            text: 'Наименование',
            flex: 2,
            sortable: true,
            dataIndex: 'title'
        },{
            text: 'Код',
            flex: 1,
            dataIndex: 'code',
            sortable: true
        },{
            text: 'Значение',
            flex: 4,
            dataIndex: 'value',
            sortable: true
        },{
            text: 'Простой',
            flex: 1,
            dataIndex: 'simple',
            sortable: true
        },{
            text: 'Массив',
            flex: 1,
            dataIndex: 'array',
            sortable: true
        },{
            text: 'Тип',
            flex: 1,
            dataIndex: 'type',
            sortable: true
        }],
        listeners : {
            itemclick: function(view, record, item, index, e, eOpts) {
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();

                var buttonSaveAttributes = Ext.create('Ext.button.Button', {
                    id: "entityEditorShowBtn",
                    text: 'Сохранить',
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

                var form = Ext.getCmp('EntityEditorFormPannel');
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
                                            replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1'))
                                }));
                        } else {
                            form.add(Ext.create("Ext.form.field.Text",
                                {
                                    id: children[i].data.code + "FromItem",
                                    fieldLabel: children[i].data.title,
                                    width: "100%",
                                    value: children[i].data.value
                                }));
                        }
                    }
                }

                form.add(buttonSaveAttributes);

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
                title: "Форма ввода",
                defaults: {
                    anchor: '100%'
                },
                bodyPadding: '5 5 0',
                autoScroll:true
            },{
                xtype : 'panel',
                region: 'south',
                preventHeader: true,
                width: "60%",
                height: 150,
                autoScroll:true,
                items: [ createItemsGrid()]
            }],
        dockedItems: [
            {
                fieldLabel: 'Справочник',
                id: 'entityEditorComplexTypeCombo',
                xtype: 'combobox',
                store: classesStore,
                valueField:'classId',
                displayField:'className',
                listeners: {
                    change: function (field, newValue, oldValue) {
                            currentClassId = newValue;

                            refStore.reload(
                                {
                                    params:
                                    {
                                        metaId : currentClassId
                                    }
                                });

                            createItemsGrid(currentClassId);
                        }
                }
            },{
                fieldLabel: 'Элементы',
                id: 'entityEditorrefCombo',
                xtype: 'combobox',
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
                fieldLabel: 'Идентификатор сущности',
                id: 'entityId',
                name: 'entityId',
                xtype: 'textfield',
                value: (givenEntityId == "null" ? "" : givenEntityId)
            },
            buttonShow, buttonXML, buttonShowXML
        ]
    });
});
