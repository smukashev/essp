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
                var tree = Ext.getCmp('metaTreeView');
                var selectedNode = tree.getSelectionModel().getLastSelected();
                if(selectedNode.data.id==currentClassId)
                {
                    //metaTreeMenu.getComponent('mtm-del').disable(true);
                    metaTreeMenu.getComponent('mtm-edit').disable(true);
                }
                else
                {
                    //metaTreeMenu.getComponent('mtm-del').enable(true);
                    metaTreeMenu.getComponent('mtm-edit').enable(true);
                }
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
        fields: ['classId','className', 'disabled', 'isReference']
    });

    var metaClassListStore = Ext.create('Ext.data.Store', {
        id: "metaClassListStore",
        model: 'metaClassListModel',
        remoteGroup: true,
        buffered: true,
        autoLoad: true,
        autoSync: true,
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
                        createMCForm(record.get('classId'), record.get('className'), record.get('disabled'), record.get('isReference'), grid, record).show();
                    }}
                ]
            },
            /*{
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
                                Ext.getCmp('metaClassesGrid').getStore().load();
                            },
                            failure: function(response, opts) {
                                alert("error");
                            }
                        });
                    }}
                ]
            },*/
            {
                text     : label_TITLE,
                dataIndex: 'className',
                flex: 1
            },
            {
                text     : label_CODE,
                dataIndex: 'classId',
                flex: 1
            },
            {
                text     : label_REFERENCE,
                dataIndex: 'isReference',
                flex: 1,
                renderer: function(val)
                {
                    if(val==true)
                    {
                       return 'Да';
                    }
                    else
                    {
                        return 'Нет';
                    }

                }
            }
        ],
        viewConfig: {
            forceFit: true,
            getRowClass: function(record, index) {
                var rec = metaClassListStore.getAt(index);
                var c = rec.get('disabled');
                if (c == 1) {
                    return 'disable';
                } else if (c == 0) {
                    return 'enable';
                }
            }
        },
        xtype : 'panel',
        region: 'west',
        width: 250,
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
                createMCForm(record.get('classId'), record.get('className'), record.get('disabled'), record.get('isReference'), grid, record).show();
            }
        },
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: label_ADD,
                icon: contextPathUrl + '/pics/add.png',
                handler: function(){
                    grid = Ext.getCmp("metaClassesGrid");
                    createMCForm("", "", "", "",  grid, null).show();
                }
            }]
        }]
    });
}

Ext.onReady(function() {
    mainMetaEditorPanel = Ext.create('Ext.panel.Panel', {
        title : label_DATA_PANEL,
        preventHeader: true,
        width : '100%',
        height: '500px',
        renderTo : 'meta-editor-content',
        layout : 'border',
        defaults : {
            padding: '3'
        },
        items  : [createMetaClassesListView(),
            {
                xtype : 'panel',
                id: 'metaclassTreeContainer',
                region: 'center',
                title : label_CLASS_ST,
                scroll: 'both',
                autoScroll:true,
                items: [createMetaClassTreeStub()],
                dockedItems: [{
                    xtype: 'toolbar',
                    items: [{
                        text: label_UNFOLD,
                        handler: function(){
                            metaTreeView.expandAll();
                        }
                    }, {
                        text: label_FOLD,
                        handler: function(){
                            metaTreeView.collapseAll();
                        }
                    }]
                }]
            }]
    });
});
