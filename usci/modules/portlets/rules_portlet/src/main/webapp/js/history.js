var historyEditor;

function historyForm(){
    var cars = [
        [10, '01.02.2001', ''],
        [15, '01.01.2001', '01.02.2001']
    ];

    var carsStore = Ext.create('Ext.data.ArrayStore', {
        fields: ['id','open_date','close_date'],
        data: cars
    });
    /*
    var store = Ext.create('Ext.data.ArrayStore', {
        fields: ['id','open_date','close_date'],
        proxy: {
            type: 'ajax',
            url : dataUrl,
            reader: {
                type: 'json',
                root: 'data'
            }
        }
        //data: myData
    });*/

    ruleHistoryGrid = Ext.create('Ext.grid.Panel', {
        store: carsStore,
        columns: [
            {
                text: 'Дата открытия',
                width: '50%',
                dataIndex: 'open_date'
            },
            {
                text: 'Дата закрытия',
                width: '50%',
                dataIndex: 'close_date'
            }]
    });

    return new Ext.Window({
        id: 'historyForm',
        modal: 'true',
        title: 'История',
        items: [
            Ext.create('Ext.form.Panel',{
                region: 'center',
                width: 1200,
                height: 700,
                layout: 'border',
                tbar: [{
                    text: 'text_001'
                }],
                items: [
                    {
                        region: 'west',
                        width: '20%',
                        items: [ruleHistoryGrid]
                    },{
                        xtype: 'panel',
                        region: 'center',
                        defaults : {
                            split: true
                        },
                        items: [{
                            region: 'center',
                            html: "<div id='bkhistory' style='height: 700px;'>function(){}</div>",
                        }]
                    }

                ]
            })
        ],
        listeners: {
            show: function(panel) {
                require(['ace/ace'],function(ace){
                    historyEditor = ace.edit('bkhistory');
                });
            }
        }
    });


    return ret;
}