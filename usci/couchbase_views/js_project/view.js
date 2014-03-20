Ext.require([
    'Ext.grid.*',
    'Ext.data.*'
]);


function createViewGrid()
{
	Ext.define('myModel', {
        extend: 'Ext.data.Model',
        fields: ['id',

            'r_queue_size',
            'r_batches_in_progress',
            'r_batches_completed',

            's_queue_size',
            's_threads_count',
            's_max_threads_count',
            's_avg_time',

            'c_avg_processed',
            'c_avg_inserts',
            'c_avg_selects',
            'c_total_processed',

            'cr_date']
     });   
     
     var store = Ext.create('Ext.data.Store', {
        id: 'statsViewStore',
        model: 'myModel',
        remoteGroup: true,
        buffered: true,
        leadingBufferZone: 300,
        pageSize: 100,
        proxy: {
             type: 'ajax',
             url: 'view.php',
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
        id: "viewGrid",
        store: store,

        columns: [
        {
            text     : 'Код',
            dataIndex: 'id',
            flex:1
        },
/////////////////////////
        {
            text     : 'r_queue_size',
            dataIndex: 'r_queue_size',
            flex:1
        },
        {
            text     : 'r_batches_in_progress',
            dataIndex: 'r_batches_in_progress',
            flex:1
        },
        {
            text     : 'r_batches_completed',
            dataIndex: 'r_batches_completed',
            flex:1
        },
/////////////////////////
        {
            text     : 's_queue_size',
            dataIndex: 's_queue_size',
            flex:1
        },
        {
            text     : 's_threads_count',
            dataIndex: 's_threads_count',
            flex:1
        },
        {
            text     : 's_max_threads_count',
            dataIndex: 's_max_threads_count',
            flex:1
        },
        {
            text     : 's_avg_time',
            dataIndex: 's_avg_time',
            flex:1
        },
/////////////////////////
        {
            text     : 'c_avg_processed',
            dataIndex: 'c_avg_processed',
            flex:1
        },
        {
            text     : 'c_avg_inserts',
            dataIndex: 'c_avg_inserts',
            flex:1
        },
        {
            text     : 'c_avg_selects',
            dataIndex: 'c_avg_selects',
            flex:1
        },
        {
            text     : 'c_total_processed',
            dataIndex: 'c_total_processed',
            flex:1
        },
//////////////////////////                
        {
            text     : 'Обновлено',
            dataIndex: 'cr_date',
            flex:1
        }
        ],
        title: 'Статистика сервера',
        tbar:[{
                    text:'Очистить',
                    handler:function(){
                        Ext.Ajax.request({
                        url: 'clear_stats.php',
                        waitMsg:'Идет удаление...',
                        actionMethods: {
                            read: 'POST'
                        },
                        success: function(response, opts) {
                           reloadInfinitStore(store);
                        },
                        failure: function(response, opts) {
                           alert("error");
                        }
                    });
                    }
            },{
                    text:'Обновить',
                    handler:function(){
                        reloadInfinitStore(store);
                    }
                },{
                    text:'Отправить комманду',
                    handler:function(){
                        createCommandForm().show();
                    }
                }
            ]
    });

    return grid;
}
