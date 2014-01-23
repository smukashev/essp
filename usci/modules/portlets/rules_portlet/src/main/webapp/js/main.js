Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var metaTreeView = null;
var metaTreeViewStore = null;
var mainMetaEditorPanel = null;
var currentClassId = null;

function fillMetaClassTree(classId, className) {
    currentClassId = classId;
    if(metaTreeView == null) {
        createMetaClassTree(classId, className);
    } else {
        var root = {
            text: className,
            id: classId,
            expanded: true
        };
        metaTreeViewStore.setRootNode(root);
    }
}

function clearExtjsComponent(cmp) {
    var f;
    while(f = cmp.items.first()){
        cmp.remove(f, true);
    }
}

function createMetaClassTree(classId, className) {
    Ext.QuickTips.init();

    metaTreeViewStore = Ext.create('Ext.data.TreeStore', {
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_CLASS'}
        },
        root: {
            text: className,
            id: classId,
            expanded: true
        },
        folderSort: true,
        sorters: [{
            property: 'text',
            direction: 'ASC'
        }]
    });

    var metaTreeMenu = new Ext.menu.Menu({
        items: [
            {
                id: 'mtm-del',
                text: label_DEL
            },{
                id: 'mtm-edit',
                text: label_EDIT
            },
            ,{
                id: 'mtm-add',
                text: label_ADD
            }
        ],
        listeners: {
            click: function(menu, item, e, eOpts) {
                var tree = Ext.getCmp('metaTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();

                //alert(item.id + " " + selectedNode.data.id);

                parentNodeId = null;
                if (selectedNode.parentNode != null) {
                    parentNodeId = selectedNode.parentNode.data.id;
                } else {
                    parentNodeId = currentClassId;
                }

                switch (item.id) {
                    case 'mtm-del':
                        attrPath = selectedNode.data.id;
                        attrPathCode = null;
                        attrPathPart = null;

                        if (attrPath != null) {
                            pathArray = attrPath.split(".");

                            attrPathCode = pathArray[pathArray.length - 1];

                            attrPathPart = attrPath.substring(0, attrPath.length -(attrPathCode.length + 1));
                        } else {
                            attrPathPart = parentPath;
                        }

                        Ext.Ajax.request({
                            url: dataUrl,
                            waitMsg: label_ADDING,
                            params : {op : 'DEL_ATTR', attrPathPart: attrPathPart,
                                attrPathCode: attrPathCode},
                            actionMethods: {
                                read: 'POST'
                            },
                            success: function(response, opts) {
                                selectedNode.parentNode.removeChild(selectedNode);
                            },
                            failure: function(response, opts) {
                                data = JSON.parse(response.responseText);
                                alert(label_ERROR_ACC.format(data.errorMessage));
                            }
                        });

                        break;
                    case 'mtm-edit':
                        createMCAttrForm(currentClassId,
                            parentNodeId, selectedNode.data.id, null).show();
                        break;
                    case 'mtm-add':
                        createMCAttrForm(currentClassId,
                            parentNodeId, null,
                            function (id, text, leaf) {
                                var newNode = {id: id, text: text, leaf: leaf};
                                selectedNode.appendChild(newNode);
                            }).show();
                        break;
                }
            }
        }
    });

    metaTreeView = Ext.create('Ext.tree.Panel', {
        store: metaTreeViewStore,
        id: 'metaTreeView',
        viewConfig: {
            plugins: {
                ptype: 'treeviewdragdrop'
            }
        },
        height: '100%',
        //width: 250,
        autoHeight: true,
        //autoScroll: true,
        preventHeader: true,
        useArrows: true,
        listeners : {
            itemcontextmenu: function(view, record, item, index, event, eOpts) {
                metaTreeMenu.showAt(event.getXY());
                event.stopEvent();
            }
        }
    });

    metaclassTreeContainer = Ext.getCmp('metaclassTreeContainer');
    clearExtjsComponent(metaclassTreeContainer);
    metaclassTreeContainer.add(metaTreeView);
    metaclassTreeContainer.doLayout();
}

function createMetaClassTreeStub(classId, className) {
    return Ext.create('Ext.panel.Panel', {
        preventHeader: true,
        html  : label_CHOOSE
    });
}

function createMetaClassesListView() {
    Ext.define('metaClassListModel', {
        extend: 'Ext.data.Model',
        fields: ['classId','className']
    });

    var metaClassListStore = Ext.create('Ext.data.Store', {
        id: "metaClassListStore",
        model: 'metaClassListModel',
        remoteGroup: true,
        buffered: true,
        leadingBufferZone: 300,
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ALL'},
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

    return Ext.create('Ext.grid.Panel', {
        store: metaClassListStore,
        id: "metaClassesGrid",
        columns: [
            {
                header: '',
                xtype: 'actioncolumn',
                width: 26,
                sortable: false,
                items: [{
                    icon: contextPathUrl + '/pics/edit.png',
                    tooltip: label_EDIT,
                    handler: function (grid, rowIndex, colIndex) {
                        var record = metaClassListStore.getAt(rowIndex);
                        createMCForm(record.get('classId'), record.get('className'), grid, record).show();
                    }}
                ]
            },
            {
                header: '',
                xtype: 'actioncolumn',
                width: 26,
                sortable: false,
                items: [{
                    icon: contextPathUrl + '/pics/delete.png',
                    tooltip: label_DEL,
                    handler: function (grid, rowIndex, colIndex) {
                        var rec = metaClassListStore.getAt(rowIndex);
                        id_field = rec.get('classId');
                        Ext.Ajax.request({
                            url: dataUrl,
                            waitMsg:label_ADDING,
                            params : {op : 'DEL_CLASS', classId: id_field},
                            actionMethods: {
                                read: 'POST'
                            },
                            success: function(response, opts) {
                                //reloadInfinitStore(store);
                            },
                            failure: function(response, opts) {
                                alert("error");
                            }
                        });
                    }}
                ]
            },
            {
                text     : label_TITLE,
                dataIndex: 'className',
                flex: 1
            }
        ],
        xtype : 'panel',
        region: 'west',
        width: 150,
        collapsible: true,
        split:true,
        minSize:50,
        title: label_CLASSES,
        listeners : {
            cellclick: function(grid, td, cellIndex, record, tr, rowIndex, e, eOpts) {
                if(cellIndex == 2) {
                    fillMetaClassTree(record.get('classId'), record.get('className'));
                }
            },
            itemdblclick: function(dv, record, item, index, e) {
                grid = Ext.getCmp("metaClassesGrid");
                createMCForm(record.get('classId'), record.get('className'), grid, record).show();
            }
        },
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: label_ADD,
                icon: contextPathUrl + '/pics/add.png',
                handler: function(){
                    grid = Ext.getCmp("metaClassesGrid");
                    createMCForm("", "", grid, null).show();
                }
            }]
        }]
    });
}

// ===============================================================================

var ruleListGrid = null;

function deleteRule(rowIndex){
    rowIndex = (typeof rowIndex === 'undefined') ? -1 : rowIndex;

    ruleListGrid.store.removeAt(rowIndex);

    if(ruleListGrid.store.data.length == 0)
        editor.setValue("",-1);
    else{
        ruleListGrid.getSelectionModel().select(0);
        ruleListGrid.fireEvent("cellclick",ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected());
    }

    Ext.Ajax.request({
        url: dataUrl,
        waitMsg: 'adding',
        params : {
            op : 'DEL_RULE',
            ruleId: newValue.data.id,
            packageId: Ext.getCmp("elemComboPackage").value
        },
        reader: {
            type: 'json'
        },
        actionMethods: {
            read: 'POST',
            root: 'data'
        },
        success: function(response, opts) {
            /*var obj = Ext.decode(response.responseText).data;
             editor.backup = obj.rule;
             editor.ruleId = newValue.data.id;
             editor.infIndex = newValue.index;
             editor.setValue(obj.rule, -1);*/
            //ruleListGrid.fireEvent("itemclick",)
        },
        failure: function(response, opts) {
            Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
        }
    });
}


function initGrid(){
    var store = Ext.create('Ext.data.ArrayStore', {
        fields: ['id','name'],
        proxy: {
         type: 'ajax',
         url: dataUrl,
         reader: {
             type: 'json',
             root: 'data'
         }
        }
    });

    return ruleListGrid = Ext.create('Ext.grid.Panel', {
        store: store,
        columns: [
            {
                header: '',
                xtype: 'actioncolumn',
                width: 30,
                sortable: false,
                items: [{
                    tooltip: 'удалить',
                    icon: contextPathUrl+'/pics/crop2.png',
                    handler: function(grid, rowIndex, colIndex){
                        Ext.MessageBox.show({
                            title: 'sdf',
                            msg: 'sdf',
                            buttons: Ext.MessageBox.OKCANCEL,
                            fn: function(btn){
                                editor.preventDefault = true;
                                deleteRule(rowIndex);
                            }
                        });
                    }
                }]
            },
            {
                text     : 'Title',
                dataIndex: 'name',
                width: 200
            } ,
            {
                text : 'Id6nik',
                dataIndex: 'id'
            }
        ],
        listeners : {
            cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts){
                Ext.Ajax.request({
                    url: dataUrl,
                    waitMsg: 'adding',
                    params : {
                        op : 'GET_RULE',
                        ruleId: newValue.data.id
                    },
                    reader: {
                        type: 'json'
                    },
                    actionMethods: {
                        read: 'POST',
                        root: 'data'
                    },
                    success: function(response, opts) {
                        console.log("grid click at " + newValue.index);
                        var obj = Ext.decode(response.responseText).data;
                        editor.backup = obj.rule;
                        editor.ruleId = newValue.data.id;
                        editor.infIndex = newValue.index;
                        editor.setValue(obj.rule, -1);
                    },
                    failure: function(response, opts) {
                        Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
                    }
                });

            }
        },
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: 'add',
                icon: contextPathUrl + '/pics/add.png',
                handler: function(){
                    alert("add");
                }
            },{
                text: 'copy',
                icon: contextPathUrl + '/pics/copy2.png',
                handler: function(){
                    alert("copy");
                }
            }]
        }],
        height: 200,
        region: 'south'
    });

    /*return ruleListGrid = Ext.create('Ext.grid.Panel', {
        store: store,
        columns: [
            {
                text     : 'title',
                dataIndex: 'name'
            }
        ],
        height: 200,
        region: 'south'
    });*/
}

function updateRules(){
    if(Ext.getCmp('elemComboPackage').value == null || Ext.getCmp('elemDatePackage').value == null)
        return;

    ruleListGrid.store.load({
        params: {
          op: 'GET_RULE_TITLES',
          packageId: Ext.getCmp('elemComboPackage').value,
          date: Ext.Date.format(Ext.getCmp('elemDatePackage').value, 'd.m.Y')
        }
    });

    ruleListGrid.getView().refresh();
}


Ext.onReady(function(){

    Ext.define('packageListModel',{
        extend: 'Ext.data.Model',
        fields: ['id','name']
    });

    var packageStore = Ext.create('Ext.data.Store',{
        id: 'packageStore',
        model: 'packageListModel',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            reader: {
                type: 'json',
                root: 'data'
            },
            extraParams: {
                op: 'PACKAGE_ALL'
            }
        },
        autoLoad: true
    });

    var panel  = Ext.create('Ext.panel.Panel',{
            title : '',
            width : 600,
            height : 300,
            renderTo: 'rules-content',
            layout : 'border',
            id: 'MainPanel',
            defaults : {
                split: true
            },
            items: [
                {
                    region: 'east',
                    id: 'elemRuleBody',
                    width: 300,
                    title: 'East',
                    html: "<div id='bkeditor'>function(){}</div>",
                    tbar: [
                        {
                            text : 'отменить',
                            id: 'btnCancel',  icon: contextPathUrl + '/pics/undo.png', handler: function(){ editor.setValue(editor.backup, -1); }, disabled: true},
                        {
                            text: 'сохранить',
                            scope: this,
                            id: 'btnSave',
                            icon: contextPathUrl + '/pics/save.png',
                            disabled: true,
                            handler: function(){

                                Ext.Ajax.request({
                                    url: dataUrl,
                                    waitMsg: 'adding',
                                    params : {
                                        op : 'UPDATE_RULE',
                                        ruleBody: editor.getSession().getValue(),
                                        ruleId: editor.ruleId
                                    },
                                    reader: {
                                        type: 'json'
                                    },
                                    actionMethods: {
                                        read: 'POST',
                                        root: 'data'
                                    },
                                    success: function(response, opts) {
                                        console.log(response.responseText.success);
                                        //var obj = Ext.decode(response.responseText).data;
                                        //ruleListGrid.fireEvent('itemclick',ruleListGrid, ruleListGrid.getSelectionModel().getLastSelected());
                                        ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected()); //cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts){
                                        //editor.backup = obj.rule;
                                        //editor.setValue(obj.rule, -1);


                                        /*editor.getSession().on('change', function(){
                                         //alert(editor.isDirty());
                                         Ext.getCmp('btnEdit').setDisabled(true);
                                         })*/
                                        //console.log(obj.data[0].rule);
                                        //selectedNode.parentNode.removeChild(selectedNode);
                                    },
                                    failure: function(response, opts) {
                                        data = JSON.parse(response.responseText);
                                        //alert(label_ERROR_ACC.format(data.errorMessage));
                                    }
                                });
                            }
                        },
                        {text: 'удалить',
                            id: 'btnDel',
                            icon: contextPathUrl + '/pics/crop2.png',
                            disabled: true,
                            handler: function(){
                                //Ext.Msg.alert("Сообщение","Вы точно хотите удалить правило ?");
                                Ext.Msg.show({
                                    title: 'zapors',
                                    msg: 'mess',
                                    buttons: Ext.Msg.YESNO,
                                    fn: function(btn){
                                        deleteRule(ruleListGrid.store.indexOf(ruleListGrid.getSelectionModel().getLastSelected()));
                                    }
                                });

                            }
                        }]
                },{
                    xtype: 'panel',
                    region: 'center',
                    layout: 'border',
                    defaults : {
                        split: true
                    },
                    items: [
                        {
                            xtype: 'panel',
                            region: 'center',
                            bodyStyle: 'padding: 15px',
                            items: [
                                {
                                    xtype : 'combobox',
                                    id: 'elemComboPackage',
                                    store: packageStore,
                                    valueField:'id',
                                    displayField:'name',
                                    fieldLabel: 'Choose'
                                }, {
                                    xtype: 'datefield',
                                    id: 'elemDatePackage',
                                    fieldLabel: 'date',
                                    listeners: {
                                        change: function(){

                                            var packageId = Ext.getCmp('elemComboPackage').value;
                                            //alert(this.value);
                                            //alert(Ext.Date.format(this.value, 'd.m.Y'));
                                            updateRules();
                                            return;

                                            if(packageId != null && false){
                                                Ext.Ajax.request({
                                                    disableCaching: false,
                                                    url: dataUrl,
                                                    params: {op: 'LIST_RULES', packageId : packageId, date: Ext.Date.format(this.value,'d.m.Y')},
                                                    success: function(response){
                                                        //alert(response.status);
                                                    },
                                                    failure: function(){
                                                        alert("false");
                                                    }
                                                });

                                            }

                                        }
                                    },
                                    format: 'd.m.Y'
                                }
                            ]
                        },
                        initGrid()
                        //{
                        /*xtype: 'panel',
                         title: 'south',
                         region: 'south',
                         height: 200*/
                        //myFunctionName()
                        //}

                    ]
                }

            ]
        }
    ); //end of panel



//    var panel  = Ext.create('Ext.panel.Panel',{
//            title : '',
//            width : 600,
//            height : 300,
//            renderTo: 'rules-content',
//            layout : 'border',
//            id: 'MainPanel',
//            defaults : {
//                split: true
//            },
//            items: [
//                {
//                    region: 'east',
//                    width: 300,
//                    title: 'East',
//                    html: "<div id='bkeditor'>function(){}</div>"
//                },{
//                    xtype: 'panel',
//                    region: 'center',
//                    layout: 'border',
//                    defaults : {
//                        split: true
//                    },
//                    items: [
//                        {
//                            xtype: 'panel',
//                            region: 'center',
//                            bodyStyle: 'padding: 15px',
//                            items: [
//                                {
//                                    xtype : 'combobox',
//                                    id: 'elemComboPackage',
//                                    store: packageStore,
//                                    valueField:'id',
//                                    displayField:'name',
//                                    fieldLabel: 'Choose'
//                                }, {
//                                    xtype: 'datefield',
//                                    id: 'elemDatePackage',
//                                    fieldLabel: 'date',
//                                    listeners: {
//                                        change: function(){updateRules();}
//                                    },
//                                    format: 'd.m.Y'
//                                }//,
//                            ]
//                        },
//                        initGrid()
//                    ]
//                }
//
//            ]
//        }
//    );

})

//Ext.onReady(function() {
//    mainMetaEditorPanel = Ext.create('Ext.panel.Panel', {
//        title : label_DATA_PANEL,
//        preventHeader: true,
//        width : '100%',
//        height: '500px',
//        renderTo : 'meta-editor-content',
//        layout : 'border',
//        defaults : {
//            padding: '3'
//        },
//        items  : [createMetaClassesListView(),
//            {
//                xtype : 'panel',
//                id: 'metaclassTreeContainer',
//                region: 'center',
//                title : label_CLASS_ST,
//                scroll: 'both',
//                autoScroll:true,
//                items: [createMetaClassTreeStub()],
//                dockedItems: [{
//                    xtype: 'toolbar',
//                    items: [{
//                        text: label_UNFOLD,
//                        handler: function(){
//                            metaTreeView.expandAll();
//                        }
//                    }, {
//                        text: label_FOLD,
//                        handler: function(){
//                            metaTreeView.collapseAll();
//                        }
//                    }]
//                }]
//            }]
//    });
//});
