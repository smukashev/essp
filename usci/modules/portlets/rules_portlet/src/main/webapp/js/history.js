var historyEditor;

function historyForm(ruleId){

    var historyStore = Ext.create('Ext.data.ArrayStore', {
        fields: ['openDate','closeDate','rule'],
        proxy: {
            type: 'ajax',
            url : dataUrl,
            extraParams: {
                op: 'RULE_HISTORY',
                ruleId: ruleId
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        },
        autoLoad: true,
        listeners: {
          load: function(me, records, success, eOpts){
             historyEditor.setValue(records[0].data.rule, -1)
          }
        }
        //data: myData
    });

    ruleHistoryGrid = Ext.create('Ext.grid.Panel', {
        store: historyStore,
        columns: [
            {
                text: label_OPEN_DATE,
                width: '50%',
                dataIndex: 'openDate',
                renderer: Ext.util.Format.dateRenderer('d.m.Y')
            },
            {
                text: label_CLOSE_DATE,
                width: '50%',
                dataIndex: 'closeDate',
                renderer: Ext.util.Format.dateRenderer('d.m.Y')
            }],
         listeners: {
           cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts) {
              //console.log(newValue.data.rule);
              historyEditor.setValue(newValue.data.rule, -1);
           }
         }
    });

    return new Ext.Window({
        id: 'historyForm',
        modal: 'true',
        title: label_HISTORY,
        items: [
            Ext.create('Ext.form.Panel',{
                region: 'center',
                width: 1200,
                height: 700,
                layout: 'border',
                /*tbar: [{
                    text: 'text_001'
                }],*/
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