Ext.require([
    'Ext.tab.*',
    'Ext.tree.*',
    'Ext.data.*',
    'Ext.tip.*'
]);

var store;

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
                    Ext.MessageBox.alert(LABEL_SUCCESS, LABEL_SEND_APPROVAL);
                    store.load();
                },
                failure: function() {
                    console.log('woops');
                }
            });
        }
    });

    Ext.define('myModel', {
        extend: 'Ext.data.Model',
        fields: ['id', 'rep_date', 'u_date']
    });

    store = Ext.create('Ext.data.Store', {
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
                    tooltip: label_VIEW,
                    handler: function (grid, rowIndex, colIndex) {
                        var rec = store.getAt(rowIndex);
                        id_field = rec.get('id');

                        Ext.Ajax.request({
                            url: dataUrl,
                            method: 'POST',
                            params: {
                                op: 'GET_ENTRY',
                                id: id_field
                            },
                            success: function(response) {
                                var xmlStr = response.responseText;

                                var buttonClose = Ext.create('Ext.button.Button', {
                                    id: "itemFormCancel",
                                    text: label_CLOSE,
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
                        var rec = store.getAt(rowIndex);
                        id_field = rec.get('id');

                        Ext.Ajax.request({
                            url: dataUrl,
                            method: 'POST',
                            params: {
                                op: 'DELETE_ENTRY',
                                id: id_field
                            },
                            success: function() {
                                store.load();
                            }
                        });
                    }}
                ]
            },
            {
                text     : label_CODE,
                dataIndex: 'id',
                flex:1
            },
            {
                text     : label_REP_DATE,
                dataIndex: 'rep_date',
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
