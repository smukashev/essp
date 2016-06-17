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
            var sendXml = function() {
                Ext.Ajax.request({
                    url: dataUrl,
                    method: 'POST',
                    params: {
                        op: 'SEND_XML'
                    },
                    success: function(info) {
                        var response = JSON.parse(info.responseText);
                        if(response.success) {
                            Ext.MessageBox.alert(LABEL_SUCCESS, LABEL_SEND_APPROVAL);
                            store.load();
                        } else {
                            Ext.MessageBox.alert("Ошибка",response.errorMessage);
                        }

                    },
                    failure: function() {
                        console.log('woops');
                    }
                });
            };

            Ext.Ajax.request({
                url: dataUrl,
                method: 'GET',
                params: {
                    op: 'GET_REPORT_DATE'
                },
                success: function(info) {
                    var response = JSON.parse(info.responseText);
                    var reportDate;

                    if(response.success) {
                        reportDate = response.data;
                        var hasNotReportingChanges = false;
                        for(var i=0 ;i<store.getCount();i++)
                            if( store.getAt(i).get('rep_date') != reportDate) {
                                hasNotReportingChanges = true;
                            }

                        if(hasNotReportingChanges && !isNb) {
                            Ext.MessageBox.alert({
                                title: '',
                                msg: 'Обнаружены изменения за период, отличные от отчитвываемого, ' +
                                'они попадут в отдельную очередь и требуют согласования пользователя НБ',
                                buttons: Ext.MessageBox.YESNO,
                                buttonText:{
                                    yes: "Да",
                                    no: "Нет"
                                },
                                fn: function(val){
                                    if(val == 'yes') {
                                        sendXml();
                                    }
                                }
                            });

                        } else {
                            sendXml();
                        }

                    } else {
                        Ext.MessageBox.alert("",response.errorMessage);
                    }
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

    grid.getStore().load({
        callback: function (records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert(label_ERROR, operation.request.proxy.reader.rawData.errorMessage);
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
