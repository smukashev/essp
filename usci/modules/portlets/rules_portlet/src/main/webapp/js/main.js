Ext.require([
    'Ext.Msg',
    'Ext.panel.*',
    'Ext.form.*',
    'Ext.selection.CellModel',
    'Ext.grid.*',
    'Ext.data.*'
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
        fields: ['id','name','isActive'],
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


    ruleListGrid = Ext.create('Ext.grid.Panel', {
        store: store,
        columns: [
            /*{
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
            },*/
            {
                text     : 'Название',
                dataIndex: 'name',
                width: 200,
                flex: 1,
                field: {
                    allowBlank: false
                }
            }/*,{
                xtype: 'checkcolumn',
                id: 'my-check',
                text: 'активность',
                dataIndex: 'isActive',
                listeners : {
                    beforecheckchange: function( a, rowIndex, newValue, eOpts ) {
                        if(readOnly) return false;
                        var ruleId = ruleListGrid.store.getAt(rowIndex).raw.id;
                        var ruleBody = editor.getSession().getValue();
                        var ruleEdited = false;
                        var t = false;

                        if(ruleId == ruleListGrid.getSelectionModel().getLastSelected().data.id) {
                            ruleEdited = true;
                        }

                        Ext.Ajax.request({
                            url: dataUrl,
                            waitMsg: 'adding',
                            async: false,
                            params : {
                                op : 'RULE_SWITCH',
                                ruleId: ruleId,
                                date: Ext.getCmp('elemPackageVersionCombo').value,
                                pkgName: Ext.getCmp('elemComboPackage').getRawValue(),
                                newValue: newValue,
                                ruleBody: ruleBody,
                                ruleEdited: ruleEdited
                            },
                            reader: {
                                type: 'json'
                            },
                            actionMethods: {
                                read: 'POST',
                                root: 'data'
                            },
                            success: function(response, opts) {
                                var r = Ext.decode(response.responseText);
                                if(r.success) {
                                    t = true;
                                } else {
                                    var c = Ext.getCmp('errorPanel');
                                    c.update(r.data);
                                    c.expand();
                                }
                            },
                            failure: function(response, opts) {
                                Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
                                return false;
                            }
                        });

                        return t;
                    }
                }
            }*/
            /* ,
            {
                text : 'Id6nik',
                dataIndex: 'id',
            }*/
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
        plugins: [
            Ext.create('Ext.grid.plugin.CellEditing', {
                clicksToEdit: 2
            })],
        dockedItems: [{
            xtype: 'toolbar',
            items: [{
                text: 'добавить',
                icon: contextPathUrl + '/pics/add.png',
                id: 'btnNewRule',
                hidden: readOnly,
                handler: function(e1,e2){
                    newRuleForm().show();

                    require(['ace/ace'],function(ace){
                        ace.edit('bknew-rule');
                    });

                    /*Ext.getCmp('txtTitle').show();
                    Ext.getCmp('txtTitle').focus(false,200);
                    Ext.getCmp('btnAddGreen').show();
                    Ext.EventObject.stopPropagation();*/
                }
            },{
                xtype: 'textfield',
                id: 'txtTitle',
                hidden: true,
                listeners: {
                    render: function(cmd){
                        cmd.getEl().on('click', function(){ Ext.EventObject.stopPropagation(); });
                    },
                    scope:this,
                    specialkey: function(f,e){
                        if(e.getKey()==e.ENTER)
                            Ext.getCmp('btnAddGreen').handler();
                        else if(e.getKey() == e.ESC)
                        {
                            Ext.getCmp('btnAddGreen').hide();
                            Ext.getCmp('txtTitle').hide();
                        }
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
            }/*,{
                text: 'запуск',
                id:    'btnRun',
                icon: contextPathUrl + '/pics/run.png',
                handler: function(){
                    Ext.MessageBox.prompt('Запуск','Введитие идшник baseEntity: ', function(btn,text){
                        if(btn == "ok"){
                            Ext.getCmp('btnRun').setIcon(contextPathUrl + "/pics/loading.gif");
                            this.lastValue = text;
                            Ext.Ajax.request({
                                url: dataUrl,
                                waitMsg: 'adding',
                                params : {
                                    op : 'RUN_RULE',
                                    date: Ext.Date.format(Ext.getCmp('elemDatePackage').value, 'd.m.Y'),
                                    batchName: Ext.getCmp('elemComboPackage').getRawValue(),
                                    baseEntityId: text
                                },
                                reader: {
                                    type: 'json'
                                },
                                actionMethods: {
                                    read: 'POST',
                                    root: 'data'
                                },
                                success: function(response, opts) {
                                    var errors = Ext.decode(response.responseText).data;
                                    alert(errors.join('\n'));
                                },
                                failure: function(response, opts) {
                                },
                                callback: function(){
                                    Ext.getCmp('btnRun').setIcon( contextPathUrl + "/pics/run.png");
                                }
                            });

                        }
                    }, this, false, this.lastValue);
                }
            }*/,{
                text: 'сброс',
                icon: contextPathUrl + '/pics/bin.png',
                hidden: readOnly,
                id: 'btnFlush',
                handler: function(){
                    Ext.Ajax.request({
                        url: dataUrl,
                        waitMsg: 'adding',
                        params : {
                            op : 'FLUSH'
                        },
                        reader: {
                            type: 'json'
                        },
                        actionMethods: {
                            read: 'POST',
                            root: 'data'
                        },
                        success: function(response, opts) {
                            Ext.Msg.alert("Информация","Кэш очищен");
                        },
                        failure: function(response, opts) {
                            Ext.Msg.alert("Информация","Кэш не очищен");
                        }
                    });
                }
            },{
                text: 'копировать',
                id: 'btnCopy',
                icon: contextPathUrl + '/pics/copy2.png',
                hidden: readOnly,
                //disabled: true,
                handler: function(){
                    createRuleForm().show();
                }
            },{
                text: 'обновить',
                id: 'btnRefresh',
                icon: contextPathUrl + '/pics/refresh.png',
                handler: function(){
                    updateRules();
                }
            },{
                text: 'пакеты',
                id: 'btnPackages',
                hidden: readOnly,
                handler: function(){
                    packageControlForm().show();
                }
            },{
                text: 'Поиск',
                id: 'btnSearch',
                icon: contextPathUrl + '/pics/search.png',
                handler: function(){
                    Ext.getCmp('txtSearch').show();
                    Ext.getCmp('txtSearch').focus(false,200);
                }
            },{
                xtype: 'textfield',
                id: 'txtSearch',
                hidden: true,
                listeners: {
                    render: function(cmd) {
                        cmd.getEl().on('click',function(){Ext.EventObject.stopPropagation();});
                    },
                    scope: this,
                    specialkey: function(f,e) {
                        if(e.getKey() == e.ENTER) {
                            updateRules(Ext.getCmp('txtSearch').value);
                        } else if(e.getKey() == e.ESC) {
                            Ext.getCmp('txtSearch').hide();
                        }
                    }
                }
            }]
        }],
        height: '75%',
        region: 'south'
    });

    ruleListGrid.on('edit', function(e,r){
        if(readOnly) return;
        Ext.Ajax.request({
            url: dataUrl,
            waitMsg: 'adding',
            params : {
                op : 'RENAME_RULE',
                ruleId: ruleListGrid.getSelectionModel().getLastSelected().data.id,
                title: r.value
            },
            reader: {
                type: 'json'
            },
            actionMethods: {
                read: 'POST',
                root: 'data'
            },
            success: function(response, opts) {
            },
            failure: function(response, opts) {
                Ext.Msg.alert("Ошибка", JSON.parse(response.responseText).errorMessage );
            }
        });
    }, this);

    return ruleListGrid;
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






function updateRules(searchText){

    if(Ext.getCmp('elemComboPackage').value == null || Ext.getCmp('elemPackageVersionCombo').value == null)
        return;
    reset();
    ruleListGrid.store.load(
        {
            params: {
                op: 'GET_RULE_TITLES',
                packageId:Ext.getCmp('elemComboPackage').value,
                date: Ext.getCmp('elemPackageVersionCombo').value,
                searchText: searchText
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
        fields: ['name']
    });

    var map1 = new Ext.util.KeyMap(document,{
            key: "s",
            ctrl: true,
            shift: true,
            fn: function(){
                if(editor.isFocused()){
                    Ext.getCmp('btnSave').handler();
                }
            }
        }
    );

    var map2 = new Ext.util.KeyMap(document,{
            key: "a",
            ctrl: true,
            shift: true,
            fn: function(){
                Ext.getCmp('btnNewRule').handler();
            }
        }
    );

    var map3 = new Ext.util.KeyMap(document,{
            key: "e",
            ctrl: true,
            shift: true,
            fn: function(){
                Ext.getCmp('btnRun').handler();
            }
        }
    );

    var map4 = new Ext.util.KeyMap(document,{
            key: "f",
            ctrl: true,
            shift: true,
            fn: function(){
                Ext.getCmp('btnFlush').handler();
            }
        }
    );

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

    packageVersionStore = Ext.create('Ext.data.Store',{
        id: 'packageVersionStore',
        model: 'packageListModel',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                op: 'PACKAGE_VERSIONS'
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        }
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
                            hidden: readOnly,
                            id: 'btnCancel',  icon: contextPathUrl + '/pics/undo.png', handler: function(){ editor.setValue(editor.backup, -1); }, disabled: true},
                        {
                            text: 'сохранить',
                            //scope: this,
                            id: 'btnSave',
                            icon: contextPathUrl + '/pics/save.png',
                            disabled: true,
                            hidden: readOnly,
                            handler: function(){

                                Ext.Ajax.request({
                                    url: dataUrl,
                                    waitMsg: 'adding',
                                    params : {
                                        op : 'UPDATE_RULE',
                                        ruleBody: editor.getSession().getValue(),
                                        ruleId: editor.ruleId,
                                        date: Ext.getCmp('elemPackageVersionCombo').value,
                                        pkgName: Ext.getCmp('elemComboPackage').getRawValue()
                                    },
                                    reader: {
                                        type: 'json'
                                    },
                                    actionMethods: {
                                        read: 'POST',
                                        root: 'data'
                                    },
                                    success: function(response, opts) {
                                        console.log(JSON.parse(response.responseText).success);
                                        var r = JSON.parse(response.responseText);
                                        //var obj = Ext.decode(response.responseText).data;
                                        //ruleListGrid.fireEvent('itemclick',ruleListGrid, ruleListGrid.getSelectionModel().getLastSelected());
                                        var c = Ext.getCmp('errorPanel');

                                        if(!r.success) {
                                            //console.log(r.errorMessage);
                                            c.update(r.errorMessage);
                                            c.expand();
                                        } else {
                                            c.update("");
                                            c.collapse();
                                            ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected()); //cellclick: function(grid, td, cellIndex, newValue, tr, rowIndex, e, eOpts){
                                        }
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
                            hidden: readOnly,
                            handler: function(){
                                //Ext.Msg.alert("Сообщение","Вы точно хотите удалить правило ?");
                                Ext.Msg.show({
                                    title: 'Потверждение',
                                    msg: 'Вы точно хотите удалить правило ?',
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
                                    fieldLabel: 'Выберите пакет',
                                    listeners: {
                                        change: function(){
                                            //updateRules();
                                            packageVersionStore.load({
                                                params: {
                                                    packageId: Ext.getCmp('elemComboPackage').value
                                                }});
                                        }
                                    }
                                }/*, {
                                    xtype: 'datefield',
                                    id: 'elemDatePackage',
                                    fieldLabel: 'дата',
                                    listeners: {
                                        change: function(){
                                            updateRules();
                                        }
                                    },
                                    format: 'd.m.Y'
                                }*/, {
                                    xtype: 'combobox',
                                    id: 'elemPackageVersionCombo',
                                    store: packageVersionStore,
                                    displayField: 'name',
                                    valueField: 'name',
                                    fieldLabel: 'дата',
                                    listeners: {
                                        change: function(){
                                            updateRules();
                                        }
                                    }
                                }
                            ]
                        },
                        initGrid()
                    ]
                },{
                    region: 'south',
                    html: 'ошибок нет',
                    id: 'errorPanel',
                    collapsible: true,
                    collapsed: true,
                    height: '10%'
                }
            ]
        }
    ); //end of panel
});