Ext.onReady(function() {
    var serviceCode = 's_person_doc';

    Ext.define('model', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title',     type: 'string'},
            {name: 'code',     type: 'string'},
            {name: 'value',     type: 'string'},
            {name: 'simple',     type: 'boolean'},
            {name: 'array',     type: 'boolean'},
            {name: 'ref',     type: 'boolean'},
            {name: 'type',     type: 'string'},
            {name: 'isKey',     type: 'boolean'},
            {name: 'isRequired',     type: 'boolean'},
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
        ]
    });

    store = Ext.create('Ext.data.TreeStore', {
        model: 'model',
        storeId: 's_person_doc_store',
        proxy: {
            type: 'memory'/*,
             url: dataUrl,
             extraParams: {op : 'LIST_ENTITY', metaId : 9, asRoot: true, entityId: '124'}*/
        },
        folderSort: true
    });

    var data = {
        "title" : ".",
        "children" : [{
            "title": "Документы",
            "code": "docs",
            "value": "2",
            "simple": false,
            "array": true,
            "isKey": true,
            "type": "META_SET",
            "iconCls":"folder",
            "childMetaId":"3",
            "childType":"META_CLASS"
        }]
    };

    var rootNode = store.setRootNode(data);

    forms[serviceCode] = function (panel) {
        panel.removeAll();
        panel.bodyPadding = 0;
        panel.add(
            Ext.create('Ext.tree.Panel',{
                id: 's_person_doc_tree',
                preventHeader: true,
                useArrows: true,
                rootVisible: false,
                store: store,
                multiSelect: true,
                singleExpand: true,
                folderSort: true,
                width: 800,
                //renderTo: 'us-search-area',
                columns: [{
                    xtype: 'treecolumn',
                    text: 'title',
                    flex: 2,
                    sortable: true,
                    dataIndex: 'title'
                },{
                    text: 'code',
                    flex: 1,
                    dataIndex: 'code',
                    sortable: true
                },{
                    text: 'value',
                    flex: 4,
                    dataIndex: 'value',
                    sortable: true
                },{
                    text: 'sample',
                    flex: 1,
                    dataIndex: 'simple',
                    sortable: true
                },{
                    text: 'array',
                    flex: 1,
                    dataIndex: 'array',
                    sortable: true
                },{
                    text: 'type',
                    flex: 1,
                    dataIndex: 'type',
                    sortable: true
                }],
                tbar: [{
                    text: 'добавить документ поиска',
                    handler: function(){
                        var tree = Ext.getCmp('s_person_doc_tree');
                        var docNode = tree.getRootNode().getChildAt(0);
                        var form = Ext.getCmp('modalDocSearchForm');
                        form.removeAll();
                        loadAttributes(form, docNode, true);
                        Ext.getCmp('modalDocSearchWindow').show();
                    }
                }, {
                    text: 'Очистить',
                    handler: function(){
                        Ext.getCmp('s_person_doc_tree').getRootNode().getChildAt(0).removeAll();
                    }
                }]
            }));
    };
});