Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

Ext.onReady(function() {
    var buttonSend = Ext.create('Ext.button.Button', {
        id: "entityEditorShowBtn",
        text: label_SEND,
        handler : function (){
            Ext.Ajax.request({
                url: dataUrl,
                method: 'POST',
                params: {
                    op: 'SEND_XML'
                },
                success: function() {
                    console.log('success');
                },
                failure: function() {
                    console.log('woops');
                }
            });
        }
    });

    Ext.define('myModel', {
        extend: 'Ext.data.Model',
        fields: ['id','u_date']
    });

    var store = Ext.create('Ext.data.Store', {
        model: 'myModel',
        remoteGroup: true,
        buffered: true,
        leadingBufferZone: 300,
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ENTRIES'},
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

    var grid = Ext.create('Ext.grid.Panel', {
        id: "itemsGrid",
        store: store,
        anchor: '100% 100%',
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
                        /*var rec = store.getAt(rowIndex);
                        id_field = rec.get('id');
                        Ext.Ajax.request({
                            url: 'item.php',
                            waitMsg:'Идет удаление...',
                            params : {op : 4, id: id_field},
                            actionMethods: {
                                read: 'POST'
                            },
                            success: function(response, opts) {
                                reloadInfinitStore(store);
                            },
                            failure: function(response, opts) {
                                alert("error");
                            }
                        });*/
                    }}
                ]
            },
            {
                text     : label_CODE,
                dataIndex: 'id',
                flex:1
            },
            {
                text     : label_DATE,
                dataIndex: 'u_date',
                flex:1
            }
        ],
        title: 'Записи',
        listeners : {
            itemdblclick: function(dv, record, item, index, e) {

            }
        }
    });

    mainEntityEditorPanel = Ext.create('Ext.panel.Panel', {
        title : 'Панель данных',
        preventHeader: true,
        width : '100%',
        height: '500px',
        layout: 'anchor',
        renderTo : 'entry-list-content',
        //layout : 'border',
        defaults : {
            padding: '3'
        },
        items  : [grid],
        dockedItems: [
            buttonSend
        ]
    });
});
