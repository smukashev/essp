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
var maxId = 0;
var nextArrayIndex = 0;

var modalWindow;
var arrayElWindow;

var FORM_ADD = 0;
var FORM_EDIT = 1;
var FORM_ADD_ARRAY_EL = 2;

function createXML(currentNode, rootFlag, offset, arrayEl, first, operation) {
    var xmlStr = "";

    var children = currentNode.childNodes;

    if(arrayEl) {
        xmlStr += offset + "<item>\n";
    } else {
        if(first) {
            xmlStr += offset + "<" + currentNode.data.code +
                (operation ? " operation=\"" + operation + "\"" : "") + ">\n";
        } else {
            xmlStr += offset + "<" + currentNode.data.code + ">\n";
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
        xmlStr += offset + "</" + currentNode.data.code + ">\n";
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
                    },
                    listeners: {
                        exception : function(proxy, response, operation) {
                            handleError(operation);
                        }
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
            refStore.on('load', function (store, records, successful, eOpts){
                if (store.getCount() > 0)
                {
                    maxId = store.getAt(0).get('code'); // initialise to the first record's id value.
                    store.each(function(rec) // go through all the records
                    {
                        maxId = Math.max(maxId, rec.get('code'));
                    });
                    maxId = maxId+1;
                }
            });
        }
    });
}

function loadEntity(entityId, date) {
    entityStore.load({
        params: {
            op : 'LIST_ENTITY',
            entityId: entityId,
            date: date,
            asRoot: true,
            timeout:120000
        },
        callback: function(records, operation, success) {
            if (!success) {
                handleError(operation);
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

function loadSubEntity(subNode, idSuffix) {
    subNode.removeAll();

    var subEntityId = Ext.getCmp(subNode.data.code + "FromItem" + idSuffix).getValue();

    subEntityStore.load({
        params: {
            op : 'LIST_ENTITY',
            entityId: subEntityId,
            date: Ext.getCmp('edDate').value,
            asRoot: false,
            timeout: 120000
        },
        callback: function(records, operation, success) {
            if (!success) {
                handleError(operation);
            } else {
                subNode.data.value = records[0].data.value;

                while (records[0].childNodes.length > 0) {
                    subNode.appendChild(records[0].childNodes[0]);
                }
            }
        }
    });
}

function addField(form, attr, idSuffix, node) {
    if (node && node.array) {
        nextArrayIndex++;
    }

    var readOnly = (node && node.value && attr.isKey)
        || (attr.array && attr.isKey)
        || (node && !node.root && node.ref)
        || (node && node.array && !attr.simple && !attr.ref)
        || (!attr.simple && !attr.array && !attr.ref);

    var allowBlank = !(attr.isRequired || attr.isKey);

    if (attr.array) {
        form.add(Ext.create("MyCheckboxField",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD,
                    checked: (attr.isKey || attr.value)
                })
        );
    } else if (attr.type == "DATE") {
        form.add(Ext.create("Ext.form.field.Date",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    format: 'd.m.Y',
                    value: new Date(
                        attr.value.
                            replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')),
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );
    } else if (attr.type == "INTEGER" || attr.type == "DOUBLE") {
        form.add(Ext.create("Ext.form.field.Number",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    value: attr.value,
                    allowDecimals: attr.type == "DOUBLE",
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );
    } else if (attr.type == "BOOLEAN") {
        form.add(Ext.create("Ext.form.field.ComboBox",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD,
                    editable : false,
                    store: Ext.create('Ext.data.Store', {
                        fields: ['value', 'title'],
                        data: [
                            {value: 'true', title: 'Да'},
                            {value: 'false', title: 'Нет'}
                        ]
                    }),
                    displayField: 'title',
                    valueField: 'value',
                    value: attr.value
                })
        );
    } else if (attr.ref) {
        if(attr.code=="creditor") {
            form.add(Ext.create("Ext.form.field.Text",
                    {
                        id: attr.code + "FromItem" + idSuffix,
                        fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                        labelWidth: "60%",
                        width: "40%",
                        value: attr.value,
                        readOnly: readOnly,
                        allowBlank: allowBlank,
                        blankText: label_REQUIRED_FIELD,
                        hidden: attr.isHidden
                    })
            );
        } else {
        form.add(Ext.create("Ext.form.field.ComboBox", {
            id: attr.code + "FromItem" + idSuffix,
            fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
            labelWidth: "60%",
            width: "40%",
            readOnly: readOnly,
            allowBlank: allowBlank,
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
                    },
                    listeners: {
                        exception : function(proxy, response, operation) {
                            handleError(operation);
                        }
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
        }
    } else {
        //ref_portfolio auto insert code
        if(attr.code=="code" && attr.metaId=="36"){
            //refStore.load();
            form.add(Ext.create("Ext.form.field.Text",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    value: function(){
                        Ext.Ajax.request({
                            url: dataUrl,
                            async: false,
                            params: {
                                op: 'LIST_BY_CLASS',
                                metaId: attr.metaId,
                                date: new Date(),
                                withHis: true
                            },
                            success: function (result) {
                                var json = JSON.parse(result.responseText);
                                maxId = 0;
                                for (i = 0; i< json.data.length;i++) {
                                    maxId = Math.max(maxId, parseInt(json.data[i].code) + 1);
                                }
                            }
                        });

                        return maxId;
                    }(),
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD,
                    hidden: attr.isHidden
                })
            );
        } else {
            form.add(Ext.create("Ext.form.field.Text",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: "60%",
                    width: "40%",
                    value: attr.value,
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );}
    }
}

function addArrayElementButton(form) {
    form.add(Ext.create('Ext.button.Button', {
        id: "btnFormAddArrayElement",
        text: "Добавить элемент",
        margin: '0 0 5 0',

        handler : function () {
            var tree = Ext.getCmp('entityTreeView');
            var selectedNode = tree.getSelectionModel().getLastSelected();

            if (selectedNode.data.simple) {
                var element = {
                    title: "[" + nextArrayIndex + "]",
                    code: "[" + nextArrayIndex + "]",
                    metaId: selectedNode.childMetaId,
                    type: selectedNode.childType
                };
                newArrayElements.push(element);
                addField(form, element, "_edit", selectedNode);
            } else {
                var arrayElForm= Ext.getCmp('ArrayElFormPannel');
                arrayElForm.removeAll();
                loadAttributes(arrayElForm, selectedNode, true);
                arrayElWindow.show();
            }
        }
    }));
}

function loadAttributes(form, selectedNode, arrayElAddition,callback) {
    var children;
    var metaId;
    var selectedNodeData;
    var idSuffix;

    if (selectedNode && arrayElAddition) {
        children = [];
        metaId = selectedNode.data.childMetaId;
        selectedNodeData = null;
        idSuffix = '_add';
    } else if (selectedNode) {
        children = selectedNode.childNodes;
        metaId = selectedNode.data.metaId;
        selectedNodeData = selectedNode.data;
        idSuffix = '_edit';
    } else {
        children = [];
        metaId = currentClassId;
        selectedNodeData = null;
        idSuffix = '_add';
    }

    Ext.Ajax.request({
        url: dataUrl,
        params: {
            op: 'LIST_ATTRIBUTES',
            metaId: metaId
        },
        success: function (result) {
            var json = JSON.parse(result.responseText);

            if(!json.success) {
                handleErrorByMessage(json.errorMessage);
                return;
            }

            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            fillAttrValuesFromTree(attributes, children);

            for(var i = 0; i < attributes.length; i++) {
                addField(form, attributes[i].data, idSuffix, selectedNodeData);
            }

            if(callback) {
                callback();
            }
        }
    });
}

function saveFormValues(formKind) {
    var idSuffix = formKind == FORM_EDIT ? "_edit" : "_add";
    var tree = Ext.getCmp('entityTreeView');
    var selectedNode = tree.getSelectionModel().getLastSelected();

    var rootNode = tree.getRootNode();
    var classesCombo = Ext.getCmp('entityEditorComplexTypeCombo');

    if (formKind == FORM_ADD) {
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
    } else if (formKind == FORM_ADD_ARRAY_EL) {
        var form = Ext.getCmp('EntityEditorFormPannel');
        var arrayIndex = selectedNode.childNodes.length;
        var element = {
            leaf: false,
            title: "[" + arrayIndex + "]",
            code: "[" + arrayIndex + "]",
            type: selectedNode.data.childType,
            metaId: selectedNode.data.childMetaId
        };
        selectedNode.appendChild(element);
        selectedNode.data.value = selectedNode.childNodes.length;
        selectedNode = selectedNode.getChildAt(arrayIndex);
        addField(form, element, "_edit", selectedNode);
    }

    if (selectedNode.data.array && selectedNode.data.simple) {
        selectedNode.removeAll();

        for (var i = 0; i < newArrayElements.length; i++) {
            var el = newArrayElements[i];
            var field = Ext.getCmp(el.code + "FromItem" + idSuffix);
            el.value = el.type == "DATE" ? field.getSubmitValue() : field.getValue();
            selectedNode.appendChild(el);
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

            if (fieldValue) {
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
                        loadSubEntity(subNode, idSuffix);
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

    if (formKind != FORM_EDIT) {
        modalWindow.hide();
        arrayElWindow.hide();
    }
}

function hasEmptyKeySet(mainNode) {
    for(var i = 0; i < mainNode.childNodes.length; i++) {
        var currentNode = mainNode.childNodes[i];

        if (currentNode.data.array && currentNode.data.isKey && currentNode.childNodes.length == 0) {
            Ext.MessageBox.alert(label_ERROR, "Не заполнен ключевой массив: " + currentNode.data.title);
            return true;
        } else if (!currentNode.data.simple) {
            if (hasEmptyKeySet(currentNode)) {
                return true;
            }
        }
    }

    return false;
}


function handleError(operation) {
    var error = '';
    if(operation.error != undefined) {
        error = operation.error.statusText;
    } else {
        error = operation.request.proxy.reader.rawData.errorMessage;
    }

    if(error) {
        if(error.toLowerCase().indexOf('connect') > -1) {
            error = 'Сбой при подключении к сервисам, возможно идут технические работы';
        }
    } else {
        error = 'Неизвестная ошибка';
    }

    Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(error));
}

function handleErrorByMessage(errorMessage){
    if(!errorMessage) {
        errorMessage = 'Неизвестная ошибка';
    } else {
        if(errorMessage.toLowerCase().indexOf('connect') > -1) {
            errorMessage = 'Сбой при подключении к сервисам, возможно идут технические работы';
        }
    }

    Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(errorMessage));
}


Ext.onReady(function() {
    Ext.override(Ext.data.proxy.Ajax, {timeout: 120000});

    grid = null;

    Ext.define('MyCheckboxField', {
        extend: 'Ext.form.field.Checkbox',

        initComponent: function () {
            this.fieldSubTpl[9] = '<input type="checkbox" id="{id}" {checked} {inputAttrTpl}';
            this.callParent();
        },

        getSubTplData: function() {
            var me = this;
            return Ext.apply(me.callParent(), {
                checked: (me.checked ? 'checked' : '')
            });
        }
    });

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
            },
            listeners: {
                exception : function(proxy, response, operation) {
                    handleError(operation);
                }
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
            {name: 'isHidden',     type: 'boolean'},
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
            {name: 'isHidden',     type: 'boolean'},
        ]
    });

    entityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        storeId: 'entityStore',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ENTITY', timeout: 120000},
            listeners: {
                exception : function(proxy, response, operation) {
                    handleError(operation);
                }
            }
        },
        folderSort: true,
        root : {
            loaded: true
        }
    });

    subEntityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        storeId: 'subEntityStore',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ENTITY', timeout: 120000},
            listeners: {
                exception : function(proxy, response, operation) {
                    handleError(operation);
                }
            }
        },
        folderSort: true
    });

    var buttonXML = Ext.create('Ext.button.Button', {
        id: "entityEditorXmlBtn",
        text: label_SAVE,
        hidden:!isNb,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            if (hasEmptyKeySet(rootNode.childNodes[0])) {
                return;
            }

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
                    var json = JSON.parse(response.responseText);

                    if(!json.success) {
                        handleErrorByMessage(json.errorMessage);
                    } else {
                        Ext.MessageBox.alert("", "Сохранено успешно");
                    }
                }
            });
        },
        maxWidth: 200
    });

    var buttonShowXML = Ext.create('Ext.button.Button', {
        id: "entityEditorShowXmlBtn",
        text: 'XML',
        maxWidth: 200,
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
        }
    });

    var buttonDelete = Ext.create('Ext.button.Button', {
        id: "buttonDelete",
        text: label_DEL,
        maxWidth: 200,
        hidden:!isNb,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true, "DELETE");

            var selected = grid.getSelectionModel().getLastSelected();

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    xml_data: xmlStr,
                    date: Ext.getCmp('edDate').value,
                    op: 'SAVE_XML'
                },
                success: function(response) {
                    var json = JSON.parse(response.responseText);

                    if(!json.success) {
                        handleErrorByMessage(json.errorMessage);
                    } else {
                        Ext.MessageBox.alert("", "Операция выполнена успешно");
                    }
                }
            });
        }
    });

    var buttonOpen = Ext.create('Ext.button.Button', {
        id: "buttonOpen",
        text: label_OPEN,
        maxWidth: 200,
        hidden:!isNb,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true, "OPEN");

            var selected = grid.getSelectionModel().getLastSelected();

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    xml_data: xmlStr,
                    /*date: selected.data.open_date,*/
                    date: Ext.getCmp('edDate').value,
                    op: 'SAVE_XML'
                },
                success: function(response) {
                    Ext.MessageBox.alert("", "Операция на открытия отправлено успешно.");
                }
            });
        }
    });

    var buttonClose = Ext.create('Ext.button.Button', {
        id: "buttonClose",
        text: label_CLOSE,
        maxWidth: 200,
        hidden:!isNb,
        handler : function (){
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var xmlStr = createXML(rootNode.childNodes[0], true, "", false, true, "CLOSE");

            var selected = grid.getSelectionModel().getLastSelected();

            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    xml_data: xmlStr,
                    /*date: selected.data.open_date,*/
                    date: Ext.getCmp('edDate').value,
                    op: 'SAVE_XML'
                },
                success: function(response) {
                    Ext.MessageBox.alert("", "Операция выполнена успешно");
                }
            });
        }
    });

    var buttonExport = Ext.create('Ext.button.Button', {
        id: 'buttonExport',
        text: label_EXPORT,
        maxWidth: 200,
        handler: function () {

            var hiddenForm = Ext.create('Ext.form.Panel', {
                title:'hiddenForm',
                standardSubmit: true,
                url: dataUrl,
                timeout: 120000,
                height:0,
                width: 0,
                hidden:true,
                items:[
                    {xtype:'textfield', name:'op', value:'EXPORT_REF'},
                    {xtype:'textfield', name:'metaId', value: currentClassId},
                    {xtype:'textfield', name:'date', value: Ext.getCmp('edDate').getSubmitValue()},
                    {xtype:'textfield', name:'withHis', value: Ext.getCmp('checkboxHistory').pressed}
                ]
            });

            hiddenForm.getForm().submit();
        }
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
                    saveFormValues(FORM_ADD);
                }
            }
        }]
    });

    arrayElWindow = Ext.create("Ext.Window",{
        title : 'Добавление элемента массива',
        width : 400,
        modal : true,
        closable : true,
        closeAction: 'hide',
        items  : [
            {
                id: "ArrayElFormPannel",
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
            handler :function() {
                var form = Ext.getCmp('ArrayElFormPannel');
                if (form.isValid()) {
                    saveFormValues(FORM_ADD_ARRAY_EL);
                }
            }
        }]
    });

    var buttonAdd = Ext.create('Ext.button.Button', {
        id: "entityEditorAddBtn",
        text: 'Добавить',
        hidden:!isNb,
        handler : function (){
            nextArrayIndex = 0;
            var form = Ext.getCmp('ModalFormPannel');
            form.removeAll();
            var classesCombo = Ext.getCmp('entityEditorComplexTypeCombo');
            var metaId = classesCombo.getValue();

            if (!metaId) {
                Ext.MessageBox.alert(label_ERROR, "Выберите справочник");
                return;
            }

            var loadMask = new Ext.LoadMask(Ext.getBody(), {msg:label_LOADING});
            loadMask.show();
            loadAttributes(form, null,null, function() { loadMask.hide()} );

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
                Ext.getCmp('btnConfirmChanges').show();
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPannel');
                form.removeAll();

                if (!selectedNode.data.simple) {
                    if (!selectedNode.data.array) {
                        loadAttributes(form, selectedNode);
                    } else {
                        Ext.getCmp('btnConfirmChanges').hide();

                        addArrayElementButton(form);

                        for(var i = 0; i < children.length; i++){
                            addField(form, children[i].data, true, selectedNode.data);
                        }
                    }
                }

                form.doLayout();
            }
        }
    });

    /*why extra query ???
    entityGrid.getStore().load({
        callback: function (records, operation, success) {
            if (!success) {
                handleError(operation);
            }
        }
    });*/

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

    today = dd+'.'+mm+'.'+yyyy;
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
                        id: "btnConfirmChanges",
                        text: label_CONFIRM_CHANGES,
                        handler : function () {
                            var form = Ext.getCmp('EntityEditorFormPannel');
                            if (form.isValid()) {
                                saveFormValues(FORM_EDIT);
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
                        var selectedRef = field.findRecordByValue(newValue);
                        if(!isNb){
                            if(selectedRef.data['className']  == 'ref_portfolio'){
                                buttonAdd.show();
                                buttonXML.show();
                                //buttonDelete.show();
                                //buttonClose.show();
                                //buttonOpen.show();
                            }else{
                                buttonAdd.hide();
                                buttonXML.hide();
                                buttonDelete.hide();
                                buttonClose.hide();
                                buttonOpen.hide();
                            }
                        }

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
                fieldLabel: 'Поиск элементов справочника:',
                id: 'findCodName',
                name: 'findCodName',
                xtype: 'textfield',
                listeners: {
                    'change': function() {
                            var val = this.getRawValue();
                            refStore.clearFilter();
                            refStore.filter(function (me) {
                                val = val.toLowerCase();
                                return (me.data.name    == null ? "" : me.data.name.toLowerCase().indexOf(val) > -1)
                                    || (me.data.name_ru == null ? "" : me.data.name_ru.toLowerCase().indexOf(val) > -1)
                                    || (me.data.name_kz == null ? "" : me.data.name_kz.toLowerCase().indexOf(val) > -1)
                                    || (me.data.code    == null ? "" : me.data.code.indexOf(val) > -1)
                                    || (me.data.no_     == null ? "" : me.data.no_.indexOf(val) > -1);
                            });
                    }
                }
            },
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        fieldLabel: label_Date,
                        id: 'edDate',
                        xtype: 'datefield',
                        format: 'd.m.Y',
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
            buttonAdd, buttonXML, buttonShowXML, buttonDelete, buttonOpen, buttonClose, buttonExport
        ]
    });
});