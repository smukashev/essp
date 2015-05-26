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
            //buffered: true,
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
                itemclick: function(dv, record, item, index, e) {
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

function loadSubEntity(subNode, isEdit) {
    subNode.removeAll();

    idSuffix = isEdit ? "_edit" : "_add";

    var subEntityId = Ext.getCmp(subNode.data.code + "FromItem" + idSuffix).getValue();

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
                //subNode.childNodes = records[0].childNodes;

                for (i = 0; i < records[0].childNodes.length; i++) {
                    subNode.appendChild(records[0].childNodes[i]);
                }
            }
        }
    });
}

function addField(form, attr, isEdit, isNew, node) {
    if (isEdit) {
        idSuffix = "_edit";
        newItems = newEditFormItems;
    } else {
        idSuffix = "_add";
        newItems = newAddFormItems;
    }

    if (isNew) {
        newItems.push(attr);
    }

    if (node && node.array) {
        nextArrayIndex++;
    }

    var disabled = (node && node.value && attr.isKey) || attr.array
        || (node && !node.root && node.ref)
        || (node && node.array && !attr.simple && !attr.ref);

    if (attr.type == "DATE") {
        form.add(Ext.create("Ext.form.field.Date",
            {
                id: attr.code + "FromItem" + idSuffix,
                fieldLabel: attr.title,
                width: "100%",
                format: 'd.m.Y',
                value: new Date(
                    attr.value.
                        replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')),
                disabled: disabled
            }));
    } else if (attr.ref) {
        form.add(Ext.create("Ext.form.field.ComboBox", {
            id: attr.code + "FromItem" + idSuffix,
            fieldLabel: attr.title,
            width: "100%",
            disabled: disabled,
            store: Ext.create('Ext.data.Store', {
                model: 'refStoreModel',
                pageSize: 100,
                proxy: {
                    type: 'ajax',
                    url: dataUrl,
                    extraParams: {op : 'LIST_BY_CLASS', metaId: attr.metaId},
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
            value: attr.value
        }));
    } else {
        form.add(Ext.create("Ext.form.field.Text",
            {
                id: attr.code + "FromItem" + idSuffix,
                fieldLabel: attr.title,
                width: "100%",
                value: attr.value,
                disabled: disabled
            }));
    }
}

function addAttributesCombo(form, metaId, isEdit) {
    idSuffix = isEdit ? "_edit" : "_add";

    var store = Ext.create('Ext.data.Store', {
        storeId: 'attrsStore' + idSuffix,
        model: 'attrsStoreModel',
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                op : 'LIST_ATTRIBUTES',
                metaId: metaId
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
        remoteSort: true,
        listeners : {
            load : function (obj, records) {
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                var localStore = Ext.StoreMgr.lookup('attrsStore' + idSuffix);

                if (isEdit) {
                    var count = 0;

                    for (i = 0; i < records.length; i++) {
                        var rec = records[i].data;

                        for (j = 0; j < selectedNode.childNodes.length; j++) {
                            if (rec.code == selectedNode.childNodes[j].data.code) {
                                localStore.removeAt(i - count);
                                count++;
                            }
                        }
                    }

                    if (count == records.length) {
                        var combo = Ext.getCmp("attributesCombo" + idSuffix);
                        var btn = Ext.getCmp("btnFormAdd");
                        form.remove(combo);
                        form.remove(btn);
                    }
                }
            }
        }
    });

    form.add(Ext.create("Ext.form.field.ComboBox", {
        id: "attributesCombo" + idSuffix,
        fieldLabel: "Атрибут:",
        width: "100%",
        store: store,
        editable: false,
        displayField: 'title',
        valueField: 'code'
        //hidden: !isEdit
    }));

    var combo = Ext.getCmp("attributesCombo" + idSuffix);
    combo.on('click', function () {
        combo.expand();
    });

    combo.on('select', function () {
        combo.expand();
    });

    form.add(Ext.create('Ext.button.Button', {
        id: "btnFormAdd",
        text: "Добавить",
        //hidden: !isEdit,
        handler : function () {
            var combo = Ext.getCmp('attributesCombo' + idSuffix);
            var selectedAttrName = combo.getValue();
            if (!selectedAttrName) return;

            var store = Ext.StoreMgr.lookup("attrsStore" + idSuffix);
            var index = store.findExact('code', selectedAttrName);
            var rec = store.getAt(index);
            store.removeAt(index);
            combo.setValue(null);

            if (!Ext.getCmp(rec.data.code + "FromItem" + idSuffix)) {
                addField(form, rec.data, isEdit, true);
            }
        }
    }));
}

var nextArrayIndex = 0;

function addArrayElementButton(form, selectedNode, isEdit) {
    form.add(Ext.create('Ext.button.Button', {
        id: "btnFormAddArrayElement",
        text: "Добавить элемент",
        handler : function () {
            var element = {
                title: "[" + nextArrayIndex + "]",
                code: "[" + nextArrayIndex + "]",
                metaId: selectedNode.childMetaId,
                type: selectedNode.childType
            };
            addField(form, element, isEdit, true, selectedNode);
        }
    }));
}

var newEditFormItems = [];
var newAddFormItems = [];

Ext.onReady(function() {
    grid = null;

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['classId', 'className', 'classTitle']
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
        remoteSort: true,
    });

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['id','title']
    });

    Ext.define('attrsStoreModel', {
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
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
        ]
    });

    /*var refStore = Ext.create('Ext.data.Store', {
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
     });*/

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
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
        ]
    });

    var entityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        storeId: 'entityStore',
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
                    var tree = Ext.getCmp('entityTreeView');
                    var rootNode = tree.getRootNode();
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
        closable : true,
        closeAction: 'hide',
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

                var classesCombo = Ext.getCmp('entityEditorComplexTypeCombo');
                var value = classesCombo.getValue();
                var rec = classesCombo.findRecordByValue(value);

                rootNode.removeAll();

                rootNode.appendChild({
                    leaf: false,
                    title: rec.data.className,
                    code: rec.data.className,
                    type: "META_CLASS",
                    metaId: rec.data.classId
                });

                var mainNode = rootNode.getChildAt(0);

                for (i = 0; i < newAddFormItems.length; i++) {
                    mainNode.appendChild(newAddFormItems[i]);
                    var currentNode = mainNode.getChildAt(i);

                    if(newAddFormItems[i].simple) {
                        currentNode.data.leaf = true;
                        currentNode.data.iconCls = "file";

                        if(newAddFormItems[i].type == "DATE") {
                            currentNode.data.value = Ext.getCmp(newAddFormItems[i].code + "FromItem_add")
                                .getSubmitValue();
                        } else {
                            currentNode.data.value = Ext.getCmp(newAddFormItems[i].code + "FromItem_add")
                                .getValue();
                        }

                    } else {
                        if(newAddFormItems[i].ref && newAddFormItems[i].type == "META_CLASS") {
                            currentNode.data.leaf = false;
                            currentNode.data.iconCls = "folder";

                            loadSubEntity(currentNode, false);
                        }
                    }
                }

                tree.getView().refresh();

                this.up('.window').hide();
            }
        }]
    });

    var buttonAdd = Ext.create('Ext.button.Button', {
        id: "entityEditorAddBtn",
        text: 'Добавить новую запись',
        handler : function (){
            newAddFormItems = [];
            nextArrayIndex = 0;
            var form = Ext.getCmp('ModalFormPannel');
            form.removeAll();
            var classesCombo = Ext.getCmp('entityEditorComplexTypeCombo');
            var metaId = classesCombo.getValue();
            addAttributesCombo(form, metaId, false);
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
        folderSort: true,
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
                nextArrayIndex = 0;
                newEditFormItems = [];
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPannel');
                form.removeAll();

                console.log("selectedNode = ", selectedNode);

                if (!selectedNode.data.simple) {
                    if (!selectedNode.data.array) {
                        if (selectedNode.data.root || !selectedNode.data.ref) {
                            addAttributesCombo(form, selectedNode.data.metaId, true);
                        }
                    } else {
                        addArrayElementButton(form, selectedNode.data, true);
                    }
                }

                for(var i = 0; i < children.length; i++){
                    addField(form, children[i].data, true, false, selectedNode.data);
                }

                form.doLayout();
            }
        }
    });

    // --------------------------------------------
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth()+1; //January is 0!
    var yyyy = today.getFullYear();

    if(dd<10) {
        dd='0'+dd
    }

    if(mm<10) {
        mm='0'+mm
    }

    today = mm+'/'+dd+'/'+yyyy;
    // ------------------------------------------------

    mainEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title : 'Панель данных',
        preventHeader: true,
        width : '100%',
        height: '700px',
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
                autoScroll:true,
                bbar: [
                    Ext.create('Ext.button.Button', {
                        id: "btnFormSave",
                        text: label_SAVE,
                        handler : function () {
                            var tree = Ext.getCmp('entityTreeView');
                            var selectedNode = tree.getSelectionModel().getLastSelected();

                            var children = selectedNode.childNodes;

                            for(var i = 0; i < children.length; i++){
                                if(children[i].data.simple) {
                                    if(children[i].data.type == "DATE") {
                                        children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem_edit")
                                            .getSubmitValue();
                                    } else {
                                        children[i].data.value = Ext.getCmp(children[i].data.code + "FromItem_edit")
                                            .getValue();
                                    }
                                } else {
                                    if(children[i].data.ref && children[i].data.type == "META_CLASS") {
                                        loadSubEntity(children[i], true);
                                    }
                                }
                            }

                            for (i = 0; i < newEditFormItems.length; i++) {
                                if(newEditFormItems[i].simple) {
                                    newEditFormItems[i].leaf = true;

                                    if(newEditFormItems[i].type == "DATE") {
                                        newEditFormItems[i].value = Ext.getCmp(newEditFormItems[i].code + "FromItem_edit")
                                            .getSubmitValue();
                                    } else {
                                        newEditFormItems[i].value = Ext.getCmp(newEditFormItems[i].code + "FromItem_edit")
                                            .getValue();
                                    }
                                    selectedNode.appendChild(newEditFormItems[i]);

                                } else {
                                    selectedNode.appendChild(newEditFormItems[i]);
                                    var subNode = selectedNode.getChildAt(children.length + i - 1);

                                    if(newEditFormItems[i].ref && newEditFormItems[i].type == "META_CLASS") {
                                        loadSubEntity(subNode, true);
                                    }
                                }
                            }

                            Ext.getCmp("entityTreeView").getView().refresh();
                            newEditFormItems = [];
                        }
                    })
                ]
            },{
                xtype : 'panel',
                region: 'north',
                preventHeader: true,
                width: "60%",
                height: 250,
                autoScroll:true,
                items: [ createItemsGrid()]
            }],
        dockedItems: [
            {
                fieldLabel: label_REF,
                id: 'entityEditorComplexTypeCombo',
                xtype: 'combobox',
                store: classesStore,
                valueField:'classId',
                displayField:'classTitle',
                listeners: {
                    change: function (field, newValue, oldValue) {
                        currentClassId = newValue;

                        /*
                         refStore.proxy.extraParams = {metaId: currentClassId, op: 'LIST_BY_CLASS'};
                         Ext.getCmp('entityEditorrefCombo').value = null;

                         refStore.reload();
                         */
                        createItemsGrid(currentClassId);
                    }
                },
                editable : false
            },
            /*{
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
             },*/
            {
                fieldLabel: label_ENTITY_ID,
                id: 'entityId',
                name: 'entityId',
                xtype: 'textfield',
                disabled : true,
                /*maxWidth: 400,*/
                value: (givenEntityId == "null" ? "" : givenEntityId)
            }, {
                fieldLabel: label_Date,
                id: 'edDate',
                xtype: 'datefield',
                maxWidth: 400,
                value : today
            },
            {
                xtype: 'tbseparator',
                height: 10
            }
        ],
        tbar: [
            buttonAdd, buttonShow, buttonXML, buttonShowXML
        ]
    });
});