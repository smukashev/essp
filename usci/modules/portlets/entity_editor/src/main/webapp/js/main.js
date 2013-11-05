Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var currentClassId = null;

Ext.onReady(function() {
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

    Ext.define('entityModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title',     type: 'string'},
            {name: 'code',     type: 'string'},
            {name: 'value',     type: 'string'}
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

    var entityGrid = Ext.create('Ext.tree.Panel', {
        //collapsible: true,
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
        }]
    });

    mainEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title : 'Панель данных',
        preventHeader: true,
        width : '100%',
        height: '500px',
        renderTo : 'meta-editor-content',
        layout : 'border',
        defaults : {
            padding: '3'
        },
        items  : [
            {
                xtype : 'panel',
                region: 'center',
                preventHeader: true,
                autoScroll:true,
                items: [entityGrid]
            }],
        dockedItems: [
            {
                fieldLabel: 'Класс',
                id: 'entityEditorComplexTypeCombo',
                xtype: 'combobox',
                store: classesStore,
                valueField:'classId',
                displayField:'className',
                listeners: {
                    change: function (field, newValue, oldValue) {
                        currentClassId = newValue;
                    }
                }
            },{
                fieldLabel: 'Идентификатор сущности',
                id: 'entityId',
                name: 'entityId',
                xtype: 'textfield'
            },
            buttonShow
        ]
    });
});
