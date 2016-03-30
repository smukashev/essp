Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*',
    'Ext.ux.CheckColumn'
]);

var attrStore;

var currentEntityId;
var leftEntityId;
var rightEntityId;
var currentEntityDate;
var leftEntityDate;
var rightEntityDate;

var tabs;
var grid;
var currentSearch;
var currentMeta;

var regex = /^\S+-(\d+)-(\S+)-(\S+)$/;
var errors = [];
var forms = [];
var entityStoreSelect;
var fetchSize = 50;

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
            op: 'LIST_ATTRIBUTES_SELECT',
            metaId: metaId
        },
        success: function (result) {
            var json = JSON.parse(result.responseText);
            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            fillAttrValuesFromTree(attributes, children);

            for (var i = 0; i < attributes.length; i++) {
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

function addField(form, attr, idSuffix, node) {
    var labelWidth = "60%";
    var width = "40%";

    if (node && node.array) {
        nextArrayIndex++;
    }

    var readOnly = (node && node.value && node.value != true && attr.isKey)
        || (attr.isKey && (attr.array || (!attr.simple && !attr.ref)))
        || (node && !node.root && node.ref)
        || (node && node.array && !attr.simple && !attr.ref);

    var allowBlank = !(attr.isRequired || attr.isKey);

    if (attr.array || (!attr.simple && !attr.ref)) {
        form.add(Ext.create("MyCheckboxField",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
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
                    fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: labelWidth,
                    width: width,
                    format: 'd.m.Y',
                    value: attr.value ? new Date(attr.value.replace(/(\d{2})\.(\d{2})\.(\d{4})/, '$3-$2-$1')) : null,
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );
    } else if (attr.type == "INTEGER" || attr.type == "DOUBLE") {
        form.add(Ext.create(Ext.form.NumberField,
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: labelWidth,
                    width: width,
                    value: attr.value,
                    minValue: 0,
                    allowDecimals: attr.type == "DOUBLE",
                    forcePrecision: attr.type == "DOUBLE",
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD
                })
        );
    } else if (attr.type == "BOOLEAN") {
        form.add(Ext.create("Ext.form.field.ComboBox",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                    labelWidth: labelWidth,
                    width: width,
                    readOnly: readOnly,
                    allowBlank: allowBlank,
                    blankText: label_REQUIRED_FIELD,
                    editable: false,
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
            fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
            labelWidth: labelWidth,
            width: width,
            readOnly: readOnly,
            allowBlank: allowBlank,
            blankText: label_REQUIRED_FIELD,
            store: Ext.create('Ext.data.Store', {
                model: 'refStoreModelDocType',
                pageSize: 100,
                proxy: {
                    type: 'ajax',
                    url: dataUrl,
                    extraParams: {op: 'LIST_BY_CLASS_SHORT_SELECT', metaId: attr.metaId},
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
            editable: false
        }));
    } else {
        form.add(Ext.create("Ext.form.field.Text",
                {
                    id: attr.code + "FromItem" + idSuffix,
                    fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
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

function loadSubEntity(subNode, idSuffix) {
    subNode.removeAll();

    var subEntityId = Ext.getCmp(subNode.data.code + "FromItem" + idSuffix).getValue();

    subEntityStoreSelect.load({
        params: {
            op: 'LIST_ENTITY_SELECT',
            entityId: subEntityId,
            date: Ext.getCmp('edDate').value,
            asRoot: false
        },
        callback: function (records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.error));
            } else {
                subNode.data.value = records[0].data.value;

                while (records[0].childNodes.length > 0) {
                    subNode.appendChild(records[0].childNodes[0]);
                }
            }
        }
    });
}

function getCurrentDate() {
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth() + 1; //January is 0!

    var yyyy = today.getFullYear();
    if (dd < 10) {
        dd = '0' + dd
    }
    if (mm < 10) {
        mm = '0' + mm
    }
    var today = dd + '.' + mm + '.' + yyyy;

    return today;
}

var userNavHistory = {
    currentPage: 1,
    nextPage: 1,
    getNextPage: function () {
        return this.nextPage;
    },
    setNextPage: function (nextPage) {
        this.nextPage = nextPage;
        console.log(this.nextPage);
    },
    init: function () {
        this.nextPage = 1;
    },
    success: function (totalCount) {
        this.currentPage = this.nextPage;
        if (totalCount) {
            Ext.getCmp('totalCount').setText(totalCount);
            Ext.getCmp('totalPageNo').setText(Math.floor((fetchSize - 1 + totalCount) / fetchSize));
            Ext.getCmp('currentPageNo').setText(this.currentPage);
        }
    }
};

function createJSON(currentNode, offset, first) {

    var JSONstr = "";
    var children = currentNode.childNodes;

    if (first) {
        if (currentNode.data.keep_left) {
            JSONstr += offset + "{" + "\n" +
            '"action" : "keep_left", \n' +
            '"childMap" : [ \n';
        }
        if (currentNode.data.keep_right) {
            JSONstr += offset + "{" + "\n" +
            '"action" : "keep_right", \n' +
            '"childMap" : [ \n';
        }
        if (currentNode.data.keep_both) {
            JSONstr += offset + "{" + "\n" +
            '"action" : "keep_both", \n' +
            '"childMap" : [ \n';
        }
        if (currentNode.data.merge) {
            JSONstr += offset + "{" + "\n" +
            '"action" : "merge", \n' +
            '"childMap" : [ \n';
        }
    }

    for (var i = 0; i < children.length; i++) {
        if (currentNode.data.array) {
            if (children[i].data.simple) {

                JSONstr += "";

            } else {
                if (children[i].data.keep_left) {
                    JSONstr += offset + '{ "id":{ "type":"long", "left": "' + children[i].data.id_left + '", "right":"' + children[i].data.id_right + '"},' +
                    ' "map": { "action" : "keep_left", "childMap" : [' + createJSON(children[i], offset + " ", false, false) + '] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.keep_right) {
                    JSONstr += offset + '{ "id":{ "type":"long", "left":"' + children[i].data.id_left + '", "right":"' + children[i].data.id_right + '"},' +
                    ' "map": { "action" : "keep_right", "childMap" : [' + createJSON(children[i], offset + " ", false, false) + '] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.merge) {
                    JSONstr += offset + '{"id":{ "type":"long", "left":"' + children[i].data.id_left + '", "right":"' + children[i].data.id_right + '"},' +
                    ' "map": { "action" : "merge", "childMap" : [' + createJSON(children[i], offset + " ", false, false) + '] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.keep_both) {
                    JSONstr += offset + '{"id":{ "type":"long", "left":"' + children[i].data.id_left + '", "right":"' + children[i].data.id_right + '"},' +
                    ' "map": { "action" : "keep_both", "childMap" : [' + createJSON(children[i], offset + " ", false, false) + '] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
            }

        }
        if (!currentNode.data.array) {
            if (children[i].data.simple) {
                if (children[i].data.keep_left) {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                    ' "map": { "action" : "keep_left", "childMap" : [] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.keep_right) {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                    ' "map": { "action" : "keep_right", "childMap" : [] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.merge) {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                    ' "map": { "action" : "merge", "childMap" : [] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }
                if (children[i].data.keep_both) {
                    JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                    ' "map": { "action" : "keep_both", "childMap" : [] } }';
                    if (!(i + 1 == children.length)) {
                        JSONstr += ",";
                    }
                }

            } else {
                var subNodeAction;
                if (currentNode.data.keep_left) {
                    subNodeAction = "keep_left";
                }
                if (currentNode.data.keep_right) {
                    subNodeAction = "keep_right";
                }
                if (currentNode.data.merge) {
                    subNodeAction = "merge";
                }
                if (currentNode.data.keep_both) {
                    subNodeAction = "keep_both";
                }
                if (children[i].data.keep_left) {

                    if (currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[") == -1) {

                        JSONstr += offset + '{ "id":{ "type":"long", "left": "' + currentNode.data.id_left + '", "right":"' + currentNode.data.id_right + '"},' +
                        ' "map": { "action" : "' + subNodeAction + '", "childMap" : [{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_left", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    } else {
                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_left", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }
                    }

                }
                if (children[i].data.keep_right) {
                    if (currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[") == -1) {

                        JSONstr += offset + '{ "id":{ "type":"long", "left": "' + currentNode.data.id_left + '", "right":"' + currentNode.data.id_right + '"},' +
                        ' "map": { "action" : "' + subNodeAction + '", "childMap" : [{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_right", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }
                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_right", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    }

                }
                if (children[i].data.merge) {
                    if (currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[") == -1) {
                        JSONstr += offset + '{ "id":{ "type":"long", "left": "' + currentNode.data.id_left + '", "right":"' + currentNode.data.id_right + '"},' +
                        ' "map": { "action" : "' + subNodeAction + '", "childMap" : [{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "merge", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "merge", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    }
                }
                if (children[i].data.keep_both) {
                    if (currentNode.data.id_left != "" && currentNode.data.id_right != "" && !first && currentNode.data.code.indexOf("[") == -1) {
                        JSONstr += offset + '{ "id":{ "type":"long", "left": "' + currentNode.data.id_left + '", "right":"' + currentNode.data.id_right + '"},' +
                        ' "map": { "action" : "' + subNodeAction + '", "childMap" : [{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_both", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    } else {

                        JSONstr += offset + '{ "id":{ "type":"attribute", "attr":"' + children[i].data.code + '"},' +
                        ' "map": { "action" : "keep_both", "childMap" : [' + createJSON(children[i], offset + " ", false) + '] } }';
                        if (!(i + 1 == children.length)) {
                            JSONstr += ",";
                        }

                    }
                }
            }
        }
    }
    if (JSONstr.indexOf(",", JSONstr.length - ",".length) !== -1) {
        JSONstr = JSONstr.slice(0, -1);
    }
    if (first) {
        JSONstr += "]}"
    }

    return JSONstr;

}

function markParent(selectedNode) {
    if (selectedNode == null || selectedNode.parentNode == null) {
        return;
    }

    if (selectedNode.parentNode.data.keep_left == false && selectedNode.parentNode.data.keep_right == false && selectedNode.parentNode.data.merge == false) {
        selectedNode.parentNode.data.keep_both = true;
    }

    markParent(selectedNode.parentNode);
}

function markEntityKeepLeft() {

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if (selectedNode.data.keep_left) {
        selectedNode.data.keep_left = false;
    } else {
        selectedNode.data.keep_left = true;
        selectedNode.data.keep_right = false;
        selectedNode.data.merge = false;
        selectedNode.data.keep_both = false;

        //markParent(selectedNode);
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}


function markEntityKeepRight() {

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if (selectedNode.data.keep_right) {
        selectedNode.data.keep_right = false;
    } else {
        selectedNode.data.keep_right = true;
        selectedNode.data.keep_left = false;
        selectedNode.data.merge = false;
        selectedNode.data.keep_both = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}


function markEntityMerge() {

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if (selectedNode.data.merge) {
        selectedNode.data.merge = false;
    } else {
        selectedNode.data.merge = true;
        selectedNode.data.keep_right = false;
        selectedNode.data.keep_left = false;
        selectedNode.data.keep_both = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}

function markEntityKeepBoth() {

    var grid = Ext.getCmp('entityTreeView');
    var store = grid.store;

    var selectedNode = grid.getSelectionModel().getLastSelected();

    if (selectedNode.data.keep_both) {
        selectedNode.data.keep_both = false;
    } else {
        selectedNode.data.keep_both = true;
        selectedNode.data.keep_left = false;
        selectedNode.data.keep_right = false;
        selectedNode.data.merge = false;
    }

    Ext.getCmp("entityTreeView").getView().refresh();
}

function getForm() {
    currentSearch = Ext.getCmp('edSearch').value;
    currentMeta = Ext.getCmp('edSearch').displayTplData[0].metaName;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            search: currentSearch,
            metaName: currentMeta,
            prefix: 'f1_'
        },
        success: function (data) {
            var form = document.getElementById('f1_entity-editor-form');
            form.innerHTML = data.responseText;
            var all = form.getElementsByClassName("usci-date");
            for (var i = 0; i < all.length; i++) {
                var info = all[i].id.match(regex);
                Ext.create('Ext.form.DateField', {
                    renderTo: all[i].id,
                    fieldLabel: 'дата',
                    labelWidth: 27,
                    id: 'f1_inp-' + info[1] + '-1',
                    format: 'd.m.Y'
                });
            }
        }
    });
}

function getForm2() {
    currentSearch2 = Ext.getCmp('edSearch2').value;
    currentMeta2 = Ext.getCmp('edSearch2').displayTplData[0].metaName;
    Ext.Ajax.request({
        url: dataUrl,
        method: 'POST',
        params: {
            op: 'GET_FORM',
            search: currentSearch2,
            metaName: currentMeta2,
            prefix: 'f2_'
        },
        success: function (data) {
            var form = document.getElementById('f2_entity-editor-form2');
            form.innerHTML = data.responseText;
            var all = form.getElementsByClassName("usci-date");
            for (var i = 0; i < all.length; i++) {
                var info = all[i].id.match(regex);
                Ext.create('Ext.form.DateField', {
                    renderTo: all[i].id,
                    fieldLabel: 'дата',
                    labelWidth: 27,
                    id: 'f2_inp-' + info[1] + '-2',
                    format: 'd.m.Y'
                });
            }
        }
    });
}

function find(control) {
    var nextDiv = control.parentNode.nextSibling;
    var inputDiv = control.previousSibling.previousSibling;

    var first = true;

    for (var i = control.parentNode; i && i != document.body; i = i.parentNode) {
        if (i.id.indexOf('f2') > -1)
            first = false;

        if (i.id.indexOf('f1') > -1)
            break;
    }

    var info = inputDiv.id.match(regex);

    var params = {op: 'FIND_ACTION', metaClass: info[2], searchName: currentSearch};
    for (var i = 0; i < errors.length; i++)
        errors[i].style.display = 'none';

    errors = [];

    for (var i = 0; i < nextDiv.childNodes.length; i++) {
        var preKeyElem = nextDiv.childNodes[i];
        if (preKeyElem.className.indexOf('leaf') > -1) {
            filterLeaf(preKeyElem, params, first);
        } else {
            filterNode(preKeyElem, params, first);
        }
    }

    if (errors.length > 0) {
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
        success: function (response) {
            var data = JSON.parse(response.responseText);
            if (data.data > -1)
                inputDiv.value = data.data;
            else
                inputDiv.value = '';

            loadDiv.style.display = 'none';
        },
        failure: function () {
            console.log('woops');
        }
    });
}

function filterLeaf(control, queryObject, first) {
    for (var i = 0; i < control.childNodes.length; i++) {
        var childControl = control.childNodes[i];
        var form = document.getElementById((first ? 'f1_' : 'f2_') + 'entity-editor-form' + (first ? '' : '2'));
        if (childControl.tagName == 'INPUT' || childControl.tagName == 'SELECT') {
            var info = childControl.id.match(regex);
            var id = info[1];

            if (childControl.value.length == 0) {
                errors.push(document.getElementById((first ? 'f1_' : 'f2_') + 'err-' + id));
            }

            queryObject[info[3]] = childControl.value;
        }
        else if (childControl.className == 'usci-date') {
            //var all = control.getElementsByClassName("usci-date");
            var info = childControl.id.match(regex);
            var value = Ext.getCmp((first ? 'f1_inp-' : 'f2_inp-') + info[1] + (first ? '-1' : '-2')).getRawValue();
            if (value.length == 0) {
                errors.push(document.getElementById((first ? 'f1_' : 'f2_') + 'err-' + id));
            }
            queryObject[info[3]] = value;
        }
    }
}

function filterNode(control, queryObject, first) {
    for (var i = 0; i < control.childNodes.length; i++) {
        var childControl = control.childNodes[i];
        if (childControl.className != undefined && childControl.className.indexOf('leaf') > -1) {
            filterLeaf(childControl, queryObject, first);
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
        return leftEntityId;
    }
}

function getCreditorId() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("creditor").getValue();
    } else {
        return Ext.getCmp('edCreditor').value;
    }
}

function getRightEntityId() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("rightEntityId").getValue();
    } else {
        return rightEntityId;
    }
}

function getLeftReportDate() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("leftReportDate").getValue();
    } else {
        return leftEntityDate;
    }
}

function getRightReportDate() {
    var currentTab = tabs.getActiveTab();
    var currentTabIndex = tabs.items.indexOf(currentTab);

    if (currentTabIndex == 0) {
        return Ext.getCmp("rightReportDate").getValue();
    } else {
        return rightEntityDate;
    }
}

Ext.onReady(function () {
    grid = null;

    Ext.override(Ext.data.proxy.Ajax, {timeout: 120000});

    Ext.define('MyCheckboxField', {
        extend: 'Ext.form.field.Checkbox',

        initComponent: function () {
            this.fieldSubTpl[9] = '<input type="checkbox" id="{id}" {checked} {inputAttrTpl}';
            this.callParent();
        },

        getSubTplData: function () {
            var me = this;
            return Ext.apply(me.callParent(), {
                checked: (me.checked ? 'checked' : '')
            });
        }
    });

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['searchName', 'metaName', 'title']
    });

    var classesStore = Ext.create('Ext.data.Store', {
        model: 'classesStoreModel',
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_CLASSES'},
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

    Ext.define('refStoreModelDocType', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'ID', type: 'string'},
            {name: 'title', type: 'string'}
        ]
    });

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['id', 'title']
    });

    Ext.define('entityModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title', type: 'string'},
            {name: 'code', type: 'string'},
            {name: 'valueLeft', type: 'string'},
            {name: 'valueRight', type: 'string'},
            {name: 'simple', type: 'boolean'},
            {name: 'array', type: 'boolean'},
            {name: 'type', type: 'string'},
            {name: 'keep_left', type: 'boolean', defaultValue: false},
            {name: 'keep_right', type: 'boolean', defaultValue: false},
            {name: 'merge', type: 'boolean', defaultValue: false},
            {name: 'keep_both', type: 'boolean', defaultValue: false},
            {name: 'id_left', type: 'string'},
            {name: 'id_right', type: 'string'},
            {name: 'is_parent_searchable', type: 'boolean'},
            {name: 'is_key', type: 'boolean'},
            {name: 'is_parent_ref', type: 'boolean'}
        ]
    });

    Ext.define('entityModelSelect', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title', type: 'string'},
            {name: 'code', type: 'string'},
            {name: 'value', type: 'string'},
            {name: 'simple', type: 'boolean'},
            {name: 'array', type: 'boolean'},
            {name: 'ref', type: 'boolean'},
            {name: 'type', type: 'string'},
            {name: 'isKey', type: 'boolean'},
            {name: 'isRequired', type: 'boolean'},
            {name: 'metaId', type: 'string'},
            {name: 'childMetaId', type: 'string'},
            {name: 'childType', type: 'string'},
        ]
    });

    Ext.define('attrsStoreModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title', type: 'string'},
            {name: 'code', type: 'string'},
            {name: 'value', type: 'string'},
            {name: 'simple', type: 'boolean'},
            {name: 'array', type: 'boolean'},
            {name: 'ref', type: 'boolean'},
            {name: 'type', type: 'string'},
            {name: 'isKey', type: 'boolean'},
            {name: 'isRequired', type: 'boolean'},
            {name: 'metaId', type: 'string'},
            {name: 'childMetaId', type: 'string'},
            {name: 'childType', type: 'string'},
        ]
    });

    attrStore = Ext.create('Ext.data.Store', {
        storeId: 'attrsStore',
        model: 'attrsStoreModel'
    });

    var types = Ext.create('Ext.data.Store', {
        fields: ['searchName', 'title'],
        /*data : [
         {"id":"s_credit_pc", "name":"Договор по номеру и дате договора"},
         {"id":"s_person_doc", "name":"Физ лицо по документу"},
         {"id":"s_org_doc", "name":"Юр лицо по документу"}
         ]*/
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_CLASSES'},
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'total'
            }
        }
    });

    var creditors = Ext.create('Ext.data.Store', {
        fields: ['id', 'name'],
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_CREDITORS'},
            reader: {
                type: 'json',
                root: 'data',
                totalProperty: 'total'
            }
        }
    });

    entityStoreSelect = Ext.create('Ext.data.TreeStore', {
        model: 'entityModelSelect',
        storeId: 'entityStoreSelect',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_ENTITY_SELECT'}
        },
        folderSort: true
    });

    subEntityStoreSelect = Ext.create('Ext.data.TreeStore', {
        model: 'entityModelSelect',
        storeId: 'subEntityStoreSelect',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_ENTITY_SELECT'}
        },
        folderSort: true
    });

    var CreditorStore = Ext.create('Ext.data.Store',
        {
            model: 'refStoreModel',
            proxy: {
                type: 'ajax',
                url: dataUrl,
                extraParams: {op: 'LIST_CREDITOR'},
                actionMethods: {
                    read: 'POST'
                },
                reader: {
                    type: 'json',
                    root: 'data'
                }/*,
                 listeners: {
                 load: function (obj, records) {
                 Ext.each(records, function (rec) {
                 console.log(rec.get('title'));
                 });
                 }
                 }*/
            },
            autoLoad: true,
            remoteSort: true
        });
    var entityStore = Ext.create('Ext.data.TreeStore', {
        model: 'entityModel',
        storeId: 'entityStore',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op: 'LIST_ENTITY'}
        },
        folderSort: true
    });

    var buttonSaveEntity = Ext.create('Ext.button.Button', {
        id: "entityEditorXmlBtn",
        text: label_SAVE,
        handler: function () {
            if (document.getElementById("entity_select").selectedIndex == 1) {
                leftEntityId = currentEntityId;
                leftEntityDate = currentEntityDate;
            } else {
                rightEntityId = currentEntityId;
                rightEntityDate = currentEntityDate;
            }
        },
        maxWidth: 70
    });

    var buttonShowEntity = Ext.create('Ext.button.Button', {
        id: "entityEditorShowBtn",
        text: label_VIEW,
        handler: function () {
            //entityId = Ext.getCmp("entityId");
            userNavHistory.init();
            Ext.getCmp('form-area').doSearch();

            return;
        },
        maxWidth: 70,
        shadow: true
    });

    var buttonShow = Ext.create('Ext.button.Button', {
        id: "entityEditorShowBtn",
        text: label_VIEW,
        handler: function () {
            leftReportDate = Ext.getCmp("leftReportDate");
            rightReportDate = Ext.getCmp("rightReportDate");

            entityStore.load({
                params: {
                    op: 'LIST_ENTITY',
                    leftEntityId: getLeftEntityId(),
                    leftReportDate: getLeftReportDate(),
                    rightEntityId: getRightEntityId(),
                    rightReportDate: getRightReportDate(),
                    creditorId: getCreditorId()
                },
                callback: function (records, operation, success) {
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
        handler: function () {
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
                    rightReportDate: getRightReportDate(),
                    deleteUnused: deleteUnusedChecked
                },
                success: function () {
                    Ext.MessageBox.alert(label_DB_SUCCESS_TITLE, label_DB_SUCCESS);
                },
                failure: function () {
                    Ext.MessageBox.alert(label_DB_FAILURE_TITLE, label_DB_FAILURE);
                }
            });
        }
    });

    var buttonShowXML = Ext.create('Ext.button.Button', {
        id: "entityEditorShowXmlBtn",
        text: 'JSON',
        handler: function () {
            var tree = Ext.getCmp('entityTreeView');
            rootNode = tree.getRootNode();

            var JSONstr = createJSON(rootNode.childNodes[0], "", true)

            var buttonClose = Ext.create('Ext.button.Button', {
                id: "itemFormCancel",
                text: label_CANCEL,
                handler: function () {
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
                title: 'JSON',
                modal: true,
                maximizable: true,
                items: [xmlForm]
            });

            xmlFromWin.show();
        }
    });

    var entityGrid = Ext.create('Ext.tree.Panel', {
        viewConfig: {
            //Return CSS class to apply to rows depending upon data values
            getRowClass: function (record, index) {
                if (record.get('is_parent_searchable')) {
                    return 'searchable-row'
                } else {
                    return 'unsearchable-row';
                }
            }
        },
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
        }, {
            text: label_CODE,
            flex: 2,
            dataIndex: 'code',
            sortable: true
        }, {
            text: label_VALUE_1,
            flex: 3,
            dataIndex: 'valueLeft',
            sortable: true
        }, {
            text: label_VALUE_2,
            flex: 3,
            dataIndex: 'valueRight',
            sortable: true
        }, {
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
                    return '<center><input type="checkbox" onclick="markEntityKeepBoth()"' + (dataIndex ? 'checked' : '') + ' /></center>'
                }
            }, {
                text: label_KEEP_LEFT,
                flex: 3,
                dataIndex: 'keep_left',
                sortable: true,
                renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                    if (record.get('is_parent_searchable')) {
                        return "";
                    } else {
                        return '<center><input type="checkbox" onclick="markEntityKeepLeft()"' + (value ? 'checked' : '') + ' /></center>';
                    }
                }
            }, {
                text: label_KEEP_RIGHT,
                flex: 3,
                dataIndex: 'keep_right',
                sortable: true,
                renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                    if (record.get('is_parent_searchable')) {
                        return "";
                    } else {
                        return '<center><input type="checkbox" onclick="markEntityKeepRight()' + (value ? 'checked' : '') + '" /></center>';
                    }
                }
            }, {
                text: label_MERGE,
                flex: 3,
                dataIndex: 'merge',
                sortable: true,
                renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                    if (record.get('is_parent_searchable')) {
                        return "";
                    } else if (record.get('array')) {
                        return '<center><input type="checkbox" onclick="markEntityMerge()"' + (record.get('merge') ? 'checked' : '') + ' /></center>'
                    } else {
                        return "";
                    }
                }
                /*
                 }, {
                 text: label_MERGE,
                 flex: 3,
                 dataIndex: 'is_searchable',
                 sortable: true
                 */
            }],
        listeners: {}
    });

    mainEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title: label_MERGE_PANEL_BY_ID,
        preventHeader: true,
        width: '100%',
        height: '100%',
        border: 0,
        defaults: {
            padding: '3'
        },
        dockedItems: [
            {
                xtype: 'panel',
                layout: 'hbox',
                border: 0,
                items: [
                    {
                        fieldLabel: 'Кредитор',
                        id: 'creditor',
                        name: 'creditor',
                        xtype: 'combobox',
                        valueField: 'id',
                        displayField: 'title',
                        store: CreditorStore,
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
                        margin: '10 10 10 10',
                        value: getCurrentDate()
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
                        margin: '10 10 10 10',
                        value: getCurrentDate()
                    }
                ]
            }
        ]
    });

    var clientEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title: label_MERGE_PANEL,
        preventHeader: true,
        width: '100%',
        height: '100%',
        defaults: {
            padding: '3'
        },
        dockedItems: [
            {
                xtype: 'panel',
                layout: 'hbox',
                border: 0,
                items: [
                    {
                        border: 1,
                        padding: 10,
                        items: [
                            {
                                xtype: 'panel',
                                layout: 'vbox',
                                padding: 15,
                                border: 0,
                                items: [
                                    {
                                        id: 'edSearch',
                                        xtype: 'combobox',
                                        labelWidth: 350,
                                        store: classesStore,
                                        valueField: 'searchName',
                                        displayField: 'title',
                                        fieldLabel: label_CLASS,
                                        editable: false
                                    },

                                    {
                                        xtype: 'component',
                                        html: "<a href='#' onclick='getForm();'>" + LABEL_UPDATE + "</a>"
                                    },
                                    {
                                        xtype: 'datefield',
                                        id: 'edDate',
                                        labelWidth: 350,
                                        fieldLabel: label_date,
                                        format: 'd.m.Y',
                                        value: new Date()
                                    },
                                    {
                                        xtype: 'component',
                                        html: '<div id="f1_entity-editor-form" style="height: 350px;"></div>'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'tbfill'
                    },
                    {
                        border: 1,
                        padding: 10,
                        items: [
                            {
                                xtype: 'panel',
                                layout: 'vbox',
                                padding: 15,
                                border: 0,
                                items: [
                                    {
                                        id: 'edSearch2',
                                        xtype: 'combobox',
                                        store: classesStore,
                                        labelWidth: 350,
                                        valueField: 'searchName',
                                        displayField: 'title',
                                        fieldLabel: label_CLASS,
                                        editable: false
                                    },
                                    {
                                        xtype: 'component',
                                        html: "<a href='#' onclick='getForm2();'>" + LABEL_UPDATE + "</a>"
                                    },
                                    {
                                        xtype: 'datefield',
                                        id: 'edDate2',
                                        labelWidth: 350,
                                        fieldLabel: label_date,
                                        format: 'd.m.Y',
                                        value: new Date()
                                    },
                                    {
                                        xtype: 'component',
                                        html: '<div id="f2_entity-editor-form2" style="height: 350px;"></div>'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    });

    var entityGridSelect = Ext.create('Ext.tree.Panel', {
        //collapsible: true,
        id: 'entityTreeViewSelect',
        preventHeader: true,
        useArrows: true,
        rootVisible: false,
        store: entityStoreSelect,
        multiSelect: true,
        singleExpand: true,
        columns: [{
            xtype: 'treecolumn',
            text: label_TITLE,
            flex: 4,
            sortable: true,
            dataIndex: 'title'
        }, {
            text: label_VALUE,
            flex: 2,
            dataIndex: 'value',
            sortable: true
        }, {
            text: label_SUBJECT_NAME,
            flex: 4,
            dataIndex: 'code',
            sortable: true,
            renderer: function (val, meta, record) {
                if (val == 'subject') {
                    var subjectName;
                    record.eachChild(function (n) {
                        if (n.get('code') == 'person_info') {
                            var lastname;
                            var firstname;
                            var middlename;
                            n.eachChild(function (n) {
                                if (n.get('code') == 'names') {
                                    if (n.firstChild) {
                                        n.firstChild.eachChild(function (n) {
                                            if (n.get('code') == 'lastname') {
                                                lastname = n.get('value')
                                            } else if (n.get('code') == 'firstname') {
                                                firstname = n.get('value')
                                            } else if (n.get('code') == 'middlename') {
                                                middlename = n.get('value')
                                            }
                                        });
                                    }
                                }
                            });
                            subjectName = "{0} {1} {2}".format(lastname ? lastname : '', firstname ? firstname : '', middlename ? middlename : '')
                        } else if (n.get('code') == 'organization_info') {
                            n.eachChild(function (n) {
                                if (n.get('code') == 'names') {
                                    if (n.firstChild) {
                                        n.firstChild.eachChild(function (n) {
                                            if (n.get('code') == 'name') {
                                                subjectName = n.get('value')
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });

                }
                return subjectName;
            }
        }],
        listeners: {
            itemclick: function (view, record, item, index, e, eOpts) {
                currentEntityId = record.get('value');
                currentEntityDate = Ext.getCmp('edDate').value;
            }
        }
    });

    var mainPanel = Ext.create('Ext.panel.Panel', {
        title: label_MERGE_PANEL,
        preventHeader: true,
        width: '100%',
        height: 500,
        //renderTo: 'merge-content',
        //preventHeader: true,
        layout: 'border',
        items: [{
            region: 'west',
            width: '50%',
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
                    displayField: 'title',
                    store: types,
                    labelWidth: 70,
                    valueField: 'searchName',
                    fieldLabel: 'Вид поиска',
                    editable: false,
                    listeners: {
                        change: function (a, key, prev) {
                            for (p in forms)
                                if (p == key)
                                    forms[p](Ext.getCmp('form-area'));
                        }
                    }
                }, {
                    id: 'edCreditor',
                    xtype: 'combobox',
                    displayField: 'name',
                    store: creditors,
                    labelWidth: 70,
                    valueField: 'id',
                    fieldLabel: 'Кредитор',
                    editable: false
                }, {
                    xtype: 'datefield',
                    id: 'edDate',
                    fieldLabel: 'Отчетная дата',
                    listeners: {
                        change: function () {
                            console.log('datefield changed');
                        }
                    },
                    format: 'd.m.Y',
                    value: getCurrentDate()
                }]
            }, {
                region: 'center',
                id: 'form-area',
                height: '80%',
                split: true,
                html: '<div id="entity-editor-form"></div>' +
                '</br>Выберите сущность: <select id="entity_select"><option value="left">Первая сущность</option><option value="right">Вторая сущность</option></select>',
                tbar: [buttonShowEntity, buttonSaveEntity]
            }]
        }, {
            region: 'east',
            width: "50%",
            split: true,
            items: [entityGridSelect],
            autoScroll: true,
            bbar: [
                {
                    text: '<<',
                    id: 'previousNav',
                    handler: function () {
                        userNavHistory.setNextPage(userNavHistory.currentPage - 1);
                        Ext.getCmp('form-area').doSearch();
                    }
                },
                {xtype: 'label', text: '1', id: 'currentPageNo'},
                {xtype: 'label', text: '/'},
                {xtype: 'label', text: '1', id: 'totalPageNo'},
                {
                    text: '>>', id: 'nextNav',
                    handler: function () {
                        userNavHistory.setNextPage(userNavHistory.currentPage + 1);
                        Ext.getCmp('form-area').doSearch();
                    }
                },
                {xtype: 'label', text: 'Всего результатов:'},
                {xtype: 'label', text: '0', id: 'totalCount'}
            ]
        }]

    });

    tabs = Ext.widget('tabpanel', {
        id: "tabs",
        width: '100%',
        height: '100%',
        layout: 'fit',
        activeTab: 0,
        border: 0,
        defaults: {
            bodyPadding: 0
        },
        items: [mainEntityEditorPanel, mainPanel]
    });

    var rootPanel = Ext.create('Ext.panel.Panel', {
        renderTo: 'merge-content',
        width: '100%',
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
