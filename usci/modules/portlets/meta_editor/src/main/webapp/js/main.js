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
                text: 'Удалить'
            },{
                id: 'mtm-edit',
                text: 'Редактировать'
            },
            ,{
                id: 'mtm-add',
                text: 'Добавить'
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
                            waitMsg:'Идет удаление...',
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
                                alert("Произошла ошибка: " + data.errorMessage);
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
        html  : 'Выберите класс для просмотра'
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
                    tooltip: 'Редактировать',
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
                    tooltip: 'Удалить',
                    handler: function (grid, rowIndex, colIndex) {
                        var rec = metaClassListStore.getAt(rowIndex);
                        id_field = rec.get('classId');
                        Ext.Ajax.request({
                            url: dataUrl,
                            waitMsg:'Идет удаление...',
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
                text     : 'Наименование',
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
        title: "Классы",
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
                text: 'Добавить',
                icon: contextPathUrl + '/pics/add.png',
                handler: function(){
                    grid = Ext.getCmp("metaClassesGrid");
                    createMCForm("", "", grid, null).show();
                }
            }]
        }]
    });
}

Ext.onReady(function() {
    mainMetaEditorPanel = Ext.create('Ext.panel.Panel', {
        title : 'Панель метаданных',
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
                title : 'Структура класса',
                scroll: 'both',
                autoScroll:true,
                items: [createMetaClassTreeStub()],
                dockedItems: [{
                    xtype: 'toolbar',
                    items: [{
                        text: 'Раскрыть всё',
                        handler: function(){
                            metaTreeView.expandAll();
                        }
                    }, {
                        text: 'Свернуть всё',
                        handler: function(){
                            metaTreeView.collapseAll();
                        }
                    }]
                }]
            }]
    });
});
