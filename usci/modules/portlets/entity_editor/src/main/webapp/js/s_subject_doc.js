Ext.onReady(function() {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.SubjectDocFormImpl';

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
            "value": "0",
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

    subjectTypeModalWindow = Ext.create("Ext.Window", {
        title: 'Тип кредитора',
        width: 400,
        modal: true,
        closable: true,
        closeAction: 'hide',
        items: [
            {
                xtype: 'form',
                bodyPadding: '5 5 0',
                width: "100%",
                defaults: {
                    anchor: '100%'
                },
                autoScroll: true,
                items: [
                    {
                        xtype: 'radiogroup',
                        columns: 3,
                        id: 'subjectTypeGroup',
                        vertical: true,
                        items: [
                            {boxLabel: 'Юр лицо', name: 'type', inputValue: 'isOrganization', checked: true},
                            {boxLabel: 'Физ лицо', name: 'type', inputValue: 'isPerson'},
                            {boxLabel: 'Банк', name: 'type', inputValue: 'isCreditor'}
                        ]
                    }
                ],
                dockedItems: [{
                    xtype: 'toolbar',
                    padding: '2 0 2 0',
                    dock: 'bottom',
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'tbfill'
                        },
                        {
                            text: 'Далее',
                            listeners: {
                                scope: this,
                                click: function (txtField, e) {
                                    subjectTypeModalWindow.close();
                                    var tree = Ext.getCmp('s_person_doc_tree');
                                    var node = tree.getRootNode().getChildAt(0);
                                    formBasic(node, function (form) {
                                        form.elem.data.title = '[' + node.childNodes.length + ']';
                                        node.data.value = node.childNodes.length + 1;
                                        node.appendChild(form.elem);
                                        editorAction.commitInsert();
                                        Ext.getCmp('entityTreeView').getView().refresh();
                                    });
                                }
                            }
                        },
                        {
                            text: 'Отмена',
                            listeners: {
                                scope: this,
                                click: function (txtField, e) {
                                    console.log('click cancel')
                                }
                            }
                        }
                    ]
                }]

            }]

    });


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
                            subjectTypeModalWindow.show();
                    }
                }, {
                    text: 'Очистить',
                    handler: function(){
                        Ext.getCmp('s_person_doc_tree').getRootNode().getChildAt(0).removeAll();
                    }
                }]
            }));

        panel.doSearch = function(){
            var subjectType =  Ext.getCmp('subjectTypeGroup').getValue();
            var docs = Ext.getCmp('s_person_doc_tree').getRootNode().getChildAt(0);
            var params = {
                op: 'LIST_ENTITY',
                metaClass: 'subject',
                searchName: serviceCode,
                timeout: 120000,
                date: Ext.getCmp('edDate').value,
                creditorId: Ext.getCmp('edCreditor').value,
                subjectType: subjectType
            };
            params.childCnt = docs.childNodes.length;
            for(var i = 0; i< params.childCnt; i++) {
                var doc = docs.getChildAt(i);
                for(var j=0;j<doc.childNodes.length;j++) {
                    var docElem = doc.getChildAt(j);
                    if(docElem.data.code == 'doc_type')
                        params['doc_type' + i] = docElem.data.value;
                      else
                        params['no' + i] = docElem.data.value;
                }
            }
            //loader
            var myMask = new Ext.LoadMask(Ext.getCmp("entityTreeView"), {msg: "Please wait..."});
            myMask.show();

            entityStore.load({

                params: params,
                callback: function (records, operation, success) {
                    myMask.hide();

                    if (!success) {
                        var error = '';
                        if(operation.error != undefined) {
                            if(operation.error.statusText in errors)
                                error = errors[operation.error.statusText];
                            else
                                error = operation.error.statusText;
                        }
                        else
                            error = operation.request.proxy.reader.rawData.errorMessage;
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(error));
                    }
                    if(records && records.length == 0)
                        Ext.MessageBox.alert(label_INFO, 'Поиск вернул 0 результатов');

                    Ext.getCmp('entityEditorShowBtn').enable();
                }});
        }
    };
});