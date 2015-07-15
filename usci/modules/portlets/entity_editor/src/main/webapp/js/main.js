Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var regex = /^\S+-(\d+)-(\S+)-(\S+)$/;
var currentSearch;
var currentMeta;

var attrStore;
var entityStore;
var subEntityStore;
var newArrayElements = [];
var nextArrayIndex = 0;

var FORM_ADD = 0;
var FORM_EDIT = 1;
var FORM_ADD_ARRAY_EL = 2;

var ADDITION_META_ID = 59;
var ADDITION_META_NAME = "credit";

var modalWindow;
var arrayElWindow;

function getForm(){
    currentSearch = Ext.getCmp('edSearch').value;
    currentMeta = Ext.getCmp('edSearch').displayTplData[0].metaName;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            search: currentSearch,
            metaName: currentMeta
        },
        success: function(data){
            document.getElementById('entity-editor-form').innerHTML = data.responseText;
            var all = document.getElementsByClassName("usci-date");
            for(var i = 0; i < all.length;i++) {
                var info =  all[i].id.match(regex);
                Ext.create('Ext.form.DateField', {
                    renderTo: all[i].id,
                    fieldLabel: 'дата',
                    labelWidth: 27,
                    id: 'inp-' + info[1],
                    format: 'd.m.Y',
                });
            }
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
        } else if(childControl.tagName == 'DIV') {
            var info = childControl.id.match(regex);
            if(info == null)
                continue;
            var id = info[1];
            queryObject[info[3]] = Ext.getCmp('inp-' + id).value;
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


    var params = {op : 'FIND_ACTION', metaClass: info[2], searchName: currentSearch};
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

function loadAttributes(form, selectedNode, arrayElAddition) {
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
        metaId = ADDITION_META_ID; // hard code
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
            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            fillAttrValuesFromTree(attributes, children);

            for(var i = 0; i < attributes.length; i++) {
                addField(form, attributes[i].data, idSuffix, selectedNodeData);
            }
        }
    });
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

function loadSubEntity(subNode, idSuffix) {
    subNode.removeAll();

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

                for (i = 0; i < records[0].childNodes.length; i++) {
                    subNode.appendChild(records[0].data.children[i]);
                }
            }
        }
    });
}

function addField(form, attr, idSuffix, node) {
    var labelWidth = "60%";
    var width = "40%";

    if (node && node.array) {
        nextArrayIndex++;
    }

    var readOnly = (node && node.value && attr.isKey)
        || (attr.isKey && (attr.array || (!attr.simple && !attr.ref)))
        || (node && !node.root && node.ref)
        || (node && node.array && !attr.simple && !attr.ref);
    //|| (!attr.simple && !attr.array && !attr.ref);

    var allowBlank = !(attr.isRequired || attr.isKey);

    if (attr.array || (!attr.simple && !attr.ref)) {
        form.add(Ext.create("MyCheckboxField",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: labelWidth,
                    width: width,
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
                    labelWidth: labelWidth,
                    width: width,
                    format: 'd.m.Y',
                    value: attr.value ? new Date(attr.value.replace(/(\d{2})\.(\d{2})\.(\d{4})/,'$3-$2-$1')) : null,
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
                    labelWidth: labelWidth,
                    width: width,
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
                    labelWidth: labelWidth,
                    width: width,
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
        form.add(Ext.create("Ext.form.field.ComboBox", {
            id: attr.code + "FromItem" + idSuffix,
            fieldLabel: (attr.isRequired ? "<b style='color:red'>*</b> " : "") + attr.title,
            labelWidth: labelWidth,
            width: width,
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
                    labelWidth: labelWidth,
                    width: width,
                    value: attr.value,
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );
    }
}

function loadEntity(entityId, date, currentSearch) {
    entityStore.load({
        params: {
            op : 'LIST_ENTITY',
            entityId: entityId,
            date: date,
            searchName: currentSearch,
            asRoot: true
        },
        callback: function(records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
            }
        }
    });
}

function saveFormValues(formKind) {
    var idSuffix = formKind == FORM_EDIT ? "_edit" : "_add";
    var tree = Ext.getCmp('entityTreeView');
    var selectedNode = tree.getSelectionModel().getLastSelected();

    var rootNode = tree.getRootNode();

    if (formKind == FORM_ADD) {
        rootNode.removeAll();
        rootNode.appendChild({
            leaf: false,
            title: ADDITION_META_NAME,
            code: ADDITION_META_NAME,
            type: "META_CLASS",
            metaId: ADDITION_META_ID
        });
        selectedNode = rootNode.getChildAt(0);
    } else if (formKind == FORM_ADD_ARRAY_EL) {
        var form = Ext.getCmp('EntityEditorFormPanel');
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

Ext.onReady(function() {

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

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'ID', type: 'string'},
            {name: 'title', type: 'string'}
        ]
    });

    Ext.define('searchStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['searchName','metaName', 'title']
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

    var buttonAdd = Ext.create('Ext.button.Button', {
        id: "entityEditorAddBtn",
        text: 'Добавить',
        handler : function (){
            nextArrayIndex = 0;
            var form = Ext.getCmp('ModalFormPannel');
            form.removeAll();
            loadAttributes(form)
            modalWindow.show();
        }
    });

    var buttonShow = Ext.create('Ext.button.Button', {
        id: "entityEditorShowBtn",
        text: label_VIEW,
        handler : function (){
            //entityId = Ext.getCmp("entityId");
            var keySearchComponent = document.getElementById('inp-1-' + currentMeta + '-null');

            if(keySearchComponent != null) {
                var entityId = document.getElementById('inp-1-' + currentMeta + '-null').value;
                loadEntity(entityId, Ext.getCmp('edDate').value, currentSearch);
            } else {
                //for custom implementations
                var params = {op : 'LIST_ENTITY',
                    metaClass: currentMeta,
                    searchName: currentSearch,
                    date: Ext.getCmp('edDate').value
                };

                var inputs = document.getElementById("entity-editor-form").childNodes;
                for(i=0;i<inputs.length;i++) {
                    if(inputs[i].tagName == 'INPUT') {
                        params[inputs[i].name] = inputs[i].value;
                    }
                }

                console.log(params);

                entityStore.load({
                    params: params,
                    callback: function (records, operation, success) {
                        if (!success) {
                            Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.request.proxy.reader.rawData.errorMessage));
                        }
                    }});
            }
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
                    date: Ext.getCmp('edDate').value,
                    op: 'SAVE_XML'
                },
                success: function() {
                    Ext.MessageBox.alert("", "Сохранено успешно");
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
        items : [
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
                nextArrayIndex = 0;
                newArrayElements = [];
                Ext.getCmp('btnConfirmChanges').show();
                var tree = Ext.getCmp('entityTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                var children = selectedNode.childNodes;

                var form = Ext.getCmp('EntityEditorFormPanel');
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

    var classesStore = Ext.create('Ext.data.Store', {
        model: 'searchStoreModel',
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
                    id: 'edSearch',
                    xtype: 'combobox',
                    displayField:'title',
                    store: classesStore,
                    labelWidth: 70,
                    valueField:'searchName',
                    fieldLabel: label_CLASS,
                    editable: false
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
                    format: 'd.m.Y',
                    value : today
                }]
            },{
                region: 'center',
                height: '80%',
                split: true,
                html: '<div id="entity-editor-form"></div>',
                tbar: [buttonShow, buttonXML, buttonShowXML, buttonAdd]
            }]
        },{
            region: 'center',
            width: "40%",
            split: true,
            items: [entityGrid],
            autoScroll:true
        },{
            id: "EntityEditorFormPanel",
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
            autoScroll: true,
            bbar: [
                Ext.create('Ext.button.Button', {
                    id: "btnConfirmChanges",
                    text: label_CONFIRM_CHANGES,
                    handler : function () {
                        var form = Ext.getCmp('EntityEditorFormPanel');
                        if (form.isValid()) {
                            saveFormValues(FORM_EDIT);
                        }
                    }
                })
            ]
        }]

    });

    if (givenEntityId && givenRepDate && givenEntityId.length && givenRepDate.length
        && givenEntityId != "null" && givenRepDate != "null") {
        var edDate = Ext.getCmp("edDate");
        edDate.setValue(givenRepDate);

        loadEntity(givenEntityId, givenRepDate);
    }

});