Ext.require([
    'Ext.Msg',
    'Ext.panel.*',
    'Ext.form.*'
]);


var ruleListGrid = null;
var packageStore = null;


function deleteRule(rowIndex){
    Ext.Ajax.request({
        url: dataUrl,
        waitMsg: 'adding',
        params : {
            op : 'DEL_RULE',
            ruleId: editor.ruleId,
            batchVersionId: editor.batchVersionId //Ext.getCmp("elemCombokage").value
        },
        reader: {
            type: 'json'
        },
        actionMethods: {
            read: 'POST',
            root: 'data'
        },
        success: function(response, opts) {
            rowIndex = (typeof rowIndex === 'undefined') ? -1 : rowIndex;

            ruleListGrid.store.removeAt(rowIndex);

            if(ruleListGrid.store.data.length == 0)
                reset();
            else{
                ruleListGrid.getSelectionModel().select(0);
                ruleListGrid.fireEvent("cellclick",ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected());
            }
        },
        failure: function(response, opts) {
            Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
        }
    });
}



function initGrid(){

    var store = Ext.create('Ext.data.ArrayStore', {
        fields: ['id','name'],
        proxy: {
            type: 'ajax',
            url : dataUrl,
            reader: {
                type: 'json',
                root: 'data'
            }
        }
        //data: myData
    });


    return ruleListGrid = Ext.create('Ext.grid.Panel', {
        store: store,
        columns: [
            {
                header: '',
                xtype: 'actioncolumn',
                width: 30,
                sortable: false,
                items: [{
                    tooltip: 'удалить',
                    icon: contextPathUrl + '/pics/crop2.png',
                    handler: function(grid, rowIndex, colIndex){
                        Ext.MessageBox.show({
                            title: 'Потверждение',
                            msg: 'Вы уверены что хотите удалить правило?',
                            buttons: Ext.MessageBox.OKCANCEL,
                            fn: function(btn){
                                if(btn == 'ok')
                                    deleteRule(rowIndex);
                            }
                        });
                    }
                }]
            },
            {
                text     : 'Title',
                dataIndex: 'name',
                width: 200
            } ,
            {
                text : 'Id6nik',
                dataIndex: 'id'
            }
        ],
        listeners : {
            cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts){
                Ext.Ajax.request({
                    url: dataUrl,
                    waitMsg: 'adding',
                    params : {
                        op : 'GET_RULE',
                        ruleId: newValue.data.id
                    },
                    reader: {
                        type: 'json'
                    },
                    actionMethods: {
                        read: 'POST',
                        root: 'data'
                    },
                    success: function(response, opts) {
                        var obj = Ext.decode(response.responseText).data;
                        editor.backup = obj.rule;
                        editor.title = obj.title;
                        editor.ruleId = newValue.data.id;
                        editor.infIndex = newValue.index;
                        editor.setValue(obj.rule, -1);
                        editor.$readOnly = false;
                    },
                    failure: function(response, opts) {
                        Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
                    }
                });

            }
        },
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: 'add',
                icon: contextPathUrl + '/pics/add.png',
                id: 'btnNewRule',
                handler: function(e1,e2){
                    Ext.getCmp('txtTitle').show();
                    Ext.getCmp('txtTitle').focus(false,200);
                    Ext.getCmp('btnAddGreen').show();
                    Ext.EventObject.stopPropagation();
                }
            },{
                xtype: 'textfield',
                id: 'txtTitle',
                hidden: true,
                listeners: {
                    render: function(cmd){
                        cmd.getEl().on('click', function(){ Ext.EventObject.stopPropagation(); });
                    }
                }
            },{
                id: 'btnAddGreen',
                text: '',
                icon: contextPathUrl + '/pics/addgreen.png',
                hidden: true,
                handler: function(e1){

                    console.log("ok press green button");

                    Ext.Ajax.request({
                        disableCaching: false,
                        url: dataUrl,
                        params: {
                            op: 'NEW_RULE',
                            title: Ext.getCmp('txtTitle').value,
                            batchVersionId: editor.batchVersionId
                        },
                        success: function(response){
                            var ruleId = Ext.decode(response.responseText).data;
                            ruleListGrid.store.add({id: ruleId, name : Ext.getCmp('txtTitle').value });
                            ruleListGrid.getSelectionModel().select(ruleListGrid.store.indexOfId(ruleId));
                            ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected());
                            Ext.getCmp('btnAddGreen').hide();
                            Ext.getCmp('txtTitle').hide();
                            editor.focus();
                        },
                        failure: function(response){
                            Ext.Msg.alert('ошибка',Ext.decode(response.responseText).errorMessage);
                        }
                    });

                    Ext.EventObject.stopPropagation();
                }
            },{
                text: 'copy',
                id: 'btnCopy',
                icon: contextPathUrl + '/pics/copy2.png',
                //disabled: true,
                handler: function(){
                    createRuleForm().show();
                }
            }]
        }],
        height: '75%',
        region: 'south'
    });

}

function reset(){
    editor.setValue("",-1);
    editor.$readOnly = true;
    editor.batchVersionId = -1;
    editor.backup = "";
    Ext.getCmp('btnCancel').setDisabled(true);
    Ext.getCmp('btnSave').setDisabled(true);
    Ext.getCmp('btnDel').setDisabled(true);
    Ext.getCmp('btnCopy').setDisabled(true);
    ruleListGrid.store.loadData([],false);
}






function updateRules(){

    if(Ext.getCmp('elemComboPackage').value == null || Ext.getCmp('elemDatePackage').value == null)
        return;
    reset();
    ruleListGrid.store.load(
        {
            params: {
                op: 'GET_RULE_TITLES',
                packageId:Ext.getCmp('elemComboPackage').value,
                date: Ext.Date.format(Ext.getCmp('elemDatePackage').value, 'd.m.Y')
            },
            callback: function(a,b,success){
                if(success == false){
                    Ext.Msg.alert('','Нет версии пакета');
                    reset();
                }else{
                    editor.batchVersionId = Ext.decode(b.response.responseText).batchVersionId;
                    Ext.getCmp('btnCopy').setDisabled(false);
                }
            },
            scope: this
        }
    );

    Ext.getCmp('btnCopy').setDisabled(false);
    ruleListGrid.getView().refresh();
}


Ext.getDoc().on('click',function(){
        Ext.getCmp('btnAddGreen').hide();
        Ext.getCmp('txtTitle').hide();
    }
);


Ext.onReady(function(){
    Ext.define('packageListModel',{
        extend: 'Ext.data.Model',
        fields: ['id','name']
    });

    var myData = [
        {
            id : '12',
            name: 'Alex',
            surname: 'Brown'
        },
        {
            id: '14',
            name: 'Bruce',
            surname : 'Gordon'
        }
    ];


    packageStore = Ext.create('Ext.data.Store',{
        id: 'packageStore',
        model: 'packageListModel',
        //data: myData,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                op: 'PACKAGE_ALL'
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        },
        autoLoad: true
    });



    var panel  = Ext.create('Ext.panel.Panel',{
            title : '',
            width : '100%',
            height : 600,
            renderTo: 'rules-content',
            layout : 'border',
            id: 'MainPanel',
            defaults : {
                split: true
            },
            items: [
                {
                    region: 'east',
                    id: 'elemRuleBody',
                    width: '75%',
                    //title: 'East',
                    html: "<div id='bkeditor'>function(){}</div>",
                    tbar: [
                        {
                            text : 'отменить',
                            id: 'btnCancel',  icon: contextPathUrl + '/pics/undo.png', handler: function(){ editor.setValue(editor.backup, -1); }, disabled: true},
                        {
                            text: 'сохранить',
                            scope: this,
                            id: 'btnSave',
                            icon: contextPathUrl + '/pics/save.png',
                            disabled: true,
                            handler: function(){

                                Ext.Ajax.request({
                                    url: dataUrl,
                                    waitMsg: 'adding',
                                    params : {
                                        op : 'UPDATE_RULE',
                                        ruleBody: editor.getSession().getValue(),
                                        ruleId: editor.ruleId
                                    },
                                    reader: {
                                        type: 'json'
                                    },
                                    actionMethods: {
                                        read: 'POST',
                                        root: 'data'
                                    },
                                    success: function(response, opts) {
                                        console.log(response.responseText.success);
                                        //var obj = Ext.decode(response.responseText).data;
                                        //ruleListGrid.fireEvent('itemclick',ruleListGrid, ruleListGrid.getSelectionModel().getLastSelected());
                                        ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected()); //cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts){
                                        //editor.backup = obj.rule;
                                        //editor.setValue(obj.rule, -1);


                                        /*editor.getSession().on('change', function(){
                                         //alert(editor.isDirty());
                                         Ext.getCmp('btnEdit').setDisabled(true);
                                         })*/
                                        //console.log(obj.data[0].rule);
                                        //selectedNode.parentNode.removeChild(selectedNode);
                                    },
                                    failure: function(response, opts) {
                                        data = JSON.parse(response.responseText);
                                        //alert(label_ERROR_ACC.format(data.errorMessage));
                                    }
                                });
                            }
                        },
                        {text: 'удалить',
                            id: 'btnDel',
                            icon: contextPathUrl + '/pics/crop2.png',
                            disabled: true,
                            handler: function(){
                                //Ext.Msg.alert("Сообщение","Вы точно хотите удалить правило ?");
                                Ext.Msg.show({
                                    title: 'Потверждение',
                                    msg: 'Вы точно хотите удалить правило?',
                                    buttons: Ext.Msg.OKCANCEL,
                                    fn: function(btn){
                                        if(btn=='ok')
                                            deleteRule(ruleListGrid.store.indexOf(ruleListGrid.getSelectionModel().getLastSelected()));
                                    }
                                });

                            }
                        }]
                },{
                    xtype: 'panel',
                    region: 'center',
                    layout: 'border',
                    defaults : {
                        split: true
                    },
                    items: [
                        {
                            xtype: 'panel',
                            region: 'center',
                            bodyStyle: 'padding: 15px',
                            items: [
                                {
                                    xtype : 'combobox',
                                    id: 'elemComboPackage',
                                    store: packageStore,
                                    valueField:'id',
                                    displayField:'name',
                                    fieldLabel: 'Choose',
                                    listeners: {
                                        change: function(){
                                            updateRules();
                                        }
                                    }
                                }, {
                                    xtype: 'datefield',
                                    id: 'elemDatePackage',
                                    fieldLabel: 'date',
                                    listeners: {
                                        change: function(){
                                            updateRules();
                                        }
                                    },
                                    format: 'd.m.Y'
                                }
                            ]
                        },
                        initGrid()
                    ]
                }

            ]
        }
    ); //end of panel
});