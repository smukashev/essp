Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var currentClassId = null;
var grid;
var refStore;
var entityStore;
var subEntityStore;
var attrStore;
var newArrayElements = [];

var nextArrayIndex = 0;

var modalWindow;

function createXML(currentNode, rootFlag, offset, arrayEl, first, remove) {
    var xmlStr = "";

    var children = currentNode.childNodes;

    if(arrayEl) {
        xmlStr += offset + "<item>\n";
    } else {
        if(first) {
            xmlStr += offset + "<entity " +
            (rootFlag ? " class=\"" + currentNode.data.code + "\"" : "") +
            (remove ? " operation=\"DELETE\"" : "") + ">\n";
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

function fillAttrValuesFromTree(attributes, existingVals) {
    for (i = 0; i < attributes.length; i++) {
        for (j = 0; j < existingVals.length; j++) {
            if (attributes[i].data.code == existingVals[j].data.code) {
                attributes[i].data.value = existingVals[j].data.value;
                break;
            }
        }
    }
}

function createItemsGrid(itemId) {
    Ext.Ajax.request({
        url: dataUrl,
        params : {
            op: 'LIST_REF_COLUMNS',
            metaId: itemId
        },
        success: function (response) {
            var json = JSON.parse(response.responseText);
            var colsInfo = buildColumnsInfo(json);

            var refModel = Ext.define('refModel', {
                extend: 'Ext.data.Model',
                fields: json.names
            });

            refStore = Ext.create('Ext.data.Store', {
                model: 'refModel',
                proxy: {
                    type: 'ajax',
                    url: dataUrl,
                    extraParams: {
                        op : 'LIST_BY_CLASS',
                        metaId : itemId,
                        date: Ext.getCmp('edDate').value,
                        withHis: Ext.getCmp('checkboxHistory').pressed
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
                autoLoad: true
            });

            grid = Ext.create('Ext.grid.Panel', {
                id: "refsGrid",
                height: "100%",
                store: refStore,
                columns: colsInfo,
                title: label_ITEMS,
                listeners : {
                    itemclick: function(dv, record, item, index, e) {
                        entityId = Ext.getCmp("entityId");
                        entityId.setValue(record.get('ID'));
                        loadEntity(record.get('ID'), record.get('open_date'));
                    }
                }
            });

            var refsGridContainer = Ext.getCmp('refsGridContainer');
            refsGridContainer.removeAll();
            refsGridContainer.add(grid);
        }
    });
}

function loadEntity(entityId, date) {
    entityStore.load({
        params: {
            op : 'LIST_ENTITY',
            entityId: entityId,
            date: date,
            asRoot: true
        },
        callback: function(records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
            }
        }
    });
}

function buildColumnsInfo(json) {
    json.names.splice(0, 0, "ID");
    json.titles.splice(0, 0, "ID");

    json.names.push("open_date");
    json.names.push("close_date");

    json.titles.push("Дата начала");
    json.titles.push("Дата окончания");

    var colsInfo = [];

    for (i = 0; i < json.names.length; i++) {
        colsInfo.push({
            text: json.titles[i],
            dataIndex: json.names[i],
            flex: 1
        });
    }

    return colsInfo;
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

function addField(form, attr, isEdit, node) {
    idSuffix = isEdit ? "_edit" : "_add";

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
                fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                width: "100%",
                format: 'd.m.Y',
                value: new Date(
                    attr.value.
                        replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')),
                disabled: disabled,
                allowBlank: !attr.isRequired,
                blankText: label_REQUIRED_FIELD
            }));
    } else if (attr.ref) {
        form.add(Ext.create("Ext.form.field.ComboBox", {
            id: attr.code + "FromItem" + idSuffix,
            fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
            width: "100%",
            disabled: disabled,
            allowBlank: !attr.isRequired,
            blankText: label_REQUIRED_FIELD,
            store: Ext.create('Ext.data.Store', {
                model: 'refStoreModel',
                pageSize: 100,
                proxy: {
                    type: 'ajax',
                    url: dataUrl,
                    extraParams: {op : 'LIST_BY_CLASS_SHORT', metaId: attr.metaId},
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
            valueField: 'ID',
            value: attr.value,
            editable : false
        }));
    } else {
        form.add(Ext.create("Ext.form.field.Text",
            {
                id: attr.code + "FromItem" + idSuffix,
                fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                width: "100%",
                value: attr.value,
                disabled: disabled,
                allowBlank: !attr.isRequired,
                blankText: label_REQUIRED_FIELD
            }));
    }
}

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
            newArrayElements.push(element);
            addField(form, element, isEdit, selectedNode);
        }
    }));
}

function loadAttributes(form, selectedNode) {
    var children;
    var metaId;
    var selectedNodeData;
    var isEdit;

    if (selectedNode) {
        children = selectedNode.childNodes;
        metaId = selectedNode.data.metaId;
        selectedNodeData = selectedNode.data;
        isEdit = true;
    } else {
        children = [];
        metaId = currentClassId;
        selectedNodeData = null;
        isEdit = false;
    }

    Ext.Ajax.request({
        url: dataUrl,
        params: {
            op: 'LIST_ATTRIBUTES',
            metaId: metaId
        },
        success: function (result) {
            var json = JSON.parse(result.responseText);
            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            fillAttrValuesFromTree(attributes, children);

            for(var i = 0; i < attributes.length; i++) {
                addField(form, attributes[i].data, isEdit, selectedNodeData);
            }
        }
    });
}

function saveFormValues(isEdit) {
    var idSuffix = isEdit ? "_edit" : "_add";
    var tree = Ext.getCmp('entityTreeView');
    var selectedNode = tree.getSelectionModel().getLastSelected();

    if (!isEdit) {
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
        selectedNode = rootNode.getChildAt(0);
    }
    if (selectedNode.data.array) {
        for (var i = 0; i < newArrayElements.length; i++) {
            selectedNode.appendChild(newArrayElements[i]);
        }
        selectedNode.data.value = selectedNode.childNodes.length;
    } else {
        var attributes = attrStore.getRange();

        for (var i = 0; i < attributes.length; i++) {
            var attr = attributes[i].data;

            var field = Ext.getCmp(attr.code + "FromItem" + idSuffix);

            var fieldValue;

            if (attr.type == "DATE") {
                fieldValue = field.getSubmitValue();
            } else {
                fieldValue = field.getValue();
            }

            var existingAttrNode = selectedNode.findChild('code', attr.code);

            if (attr.array) {
                if (!existingAttrNode) {
                    selectedNode.appendChild(attr);
                    subNode = selectedNode.getChildAt(selectedNode.childNodes.length - 1);
                }
            } else if (fieldValue) {
                var subNode;

                if (existingAttrNode) {
                    subNode = existingAttrNode;
                } else {
                    selectedNode.appendChild(attr);
                    subNode = selectedNode.getChildAt(selectedNode.childNodes.length - 1);
                }

                subNode.data.value = fieldValue;

                if (attr.simple) {
                    subNode.data.leaf = true;
                    subNode.data.iconCls = 'file';
                } else {
                    subNode.data.leaf = false;
                    subNode.data.iconCls = 'folder';

                    if (attr.ref && attr.type == "META_CLASS") {
                        loadSubEntity(subNode, isEdit);
                    }
                }
            } else {
                if (existingAttrNode) {
                    selectedNode.removeChild(existingAttrNode);
                }
            }
        }
    }

    tree.getView().refresh();

    if (!isEdit) {
        modalWindow.hide();
    }
}

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
        remoteSort: true
    });

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'ID', type: 'string'},
            {name: 'title', type: 'string'}
        ]
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
            {name: 'isRequired',     type: 'boolean'},
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
        ]
    });

    attrStore = Ext.create('Ext.data.Store', {
        storeId: 'attrsStore',
        model: 'attrsStoreModel'
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
            {name: 'isRequired',     type: 'boolean'},
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
        ]
    });

    entityStore = Ext.create('Ext.data.TreeStore', {
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
                    date: Ext.getCmp('edDate').value,
                    op: 'SAVE_XML'
                },
                success: function(response) {
                    alert("Сохранено успешно");
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

    var buttonDelete = Ext.create('Ext.button.Button', {
        id: "buttonDelete",
        text: label_DEL,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true, true);

            var selected = grid.getSelectionModel().getLastSelected();

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    xml_data: xmlStr,
                    date: selected.data.open_date,
                    op: 'SAVE_XML'
                },
                success: function(response) {
                    alert("Операция выполнена успешно");
                }
            });
        },
        maxWidth: 200
    });

    modalWindow = Ext.create("Ext.Window",{
        title : 'Добавление записи',
        width : 400,
        modal : true,
        closable : true,
        closeAction: 'hide',
        items  : [
            {
                id: "ModalFormPannel",
                xtype: 'form',
                bodyPadding: '5 5 0',
                width: "100%",
                defaults: {
                    anchor: '100%'
                },
                autoScroll:true
            }],
        tbar : [{
            text : 'Сохранить новую запись' ,
            handler :function(){
                var form = Ext.getCmp('ModalFormPannel');
                if (form.isValid()) {
                    saveFormValues(false);
                }
            }
        }]
    });

    var buttonAdd = Ext.create('Ext.button.Button', {
        id: "entityEditorAddBtn",
        text: 'Добавить',
        handler : function (){
            newAddFormItems = [];
            nextArrayIndex = 0;
            var form = Ext.getCmp('ModalFormPannel');
            form.removeAll();
            var classesCombo = Ext.getCmp('entityEditorComplexTypeCombo');
            var metaId = classesCombo.getValue();

            if (!metaId) {
                alert("Выберите справочник");
                return;
            }

            loadAttributes(form)

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
                newArrayElements = [];
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPannel');
                form.removeAll();

                if (!selectedNode.data.simple) {
                    if (!selectedNode.data.array) {
                        loadAttributes(form, selectedNode);
                    } else {
                        addArrayElementButton(form, selectedNode.data, true);

                        for(var i = 0; i < children.length; i++){
                            addField(form, children[i].data, true, selectedNode.data);
                        }
                    }
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

    today = dd+'/'+mm+'/'+yyyy;
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
                xtype : 'form',
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
                        text: label_CONFIRM_CHANGES,
                        handler : function () {
                            var form = Ext.getCmp('EntityEditorFormPannel');
                            if (form.isValid()) {
                                saveFormValues(true);
                            }
                        }
                    })
                ]
            },{
                xtype : 'panel',
                id: 'refsGridContainer',
                region: 'north',
                preventHeader: true,
                width: "60%",
                height: 250,
                layout: 'fit',
                items: []
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
                        createItemsGrid(currentClassId);
                    }
                },
                editable : false
            },
            {
                fieldLabel: label_ENTITY_ID,
                id: 'entityId',
                name: 'entityId',
                xtype: 'textfield',
                disabled : true,
                /*maxWidth: 400,*/
                value: (givenEntityId == "null" ? "" : givenEntityId)
            },
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        fieldLabel: label_Date,
                        id: 'edDate',
                        xtype: 'datefield',
                        format: 'd/m/Y',
                        maxWidth: 400,
                        value : today
                    },
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        id: "checkboxHistory",
                        text: 'Отображать историю',
                        pressed: false,
                        enableToggle: true,
                        listeners: {
                            toggle: function (obj, pressed) {
                                if (refStore) {
                                    refStore.load({params: {
                                        withHis: pressed,
                                        date: Ext.getCmp('edDate').value
                                    }});
                                }
                            }
                        }
                    },
                ]
            },
            {
                xtype: 'tbseparator',
                height: 10
            }
        ],
        tbar: [
            buttonAdd, buttonXML, buttonShowXML, buttonDelete
        ]
    });
});