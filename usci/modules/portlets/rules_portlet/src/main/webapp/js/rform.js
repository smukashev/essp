var newRuleTextField;
var ruleInfo = {};


function createRuleForm(){

    var rf_store = Ext.create('Ext.data.ArrayStore', {
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


    var rf_ruleListGrid = Ext.create('Ext.grid.Panel', {
        store: rf_store,
        height: 200,
        columns: [
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
                ruleInfo.ruleId = newValue.data.id;
                ruleInfo.ruleTitle = newValue.data.name;

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
                        Ext.getCmp('rf_ruleBody').setValue(obj.rule);
                    },
                    failure: function(response, opts) {
                        Ext.Msg.alert("ошибка",Ext.decode(response.responseText).errorMessage);
                    }
                });

            }
        }

    });


    var buttonSave = Ext.create('Ext.button.Button', {
        id: "createMCFormSave",
        text: 'Скопировать',
        handler : function (){

            console.log(Ext.getCmp('rf_elemRuleStatus').value);

            if(Ext.getCmp('rf_elemRuleStatus').value){
                var ruleName = Ext.getCmp('rf_elemRuleName');
                if(ruleName.changed){
                    Ext.Ajax.request({
                        disableCaching: false,
                        url: dataUrl,
                        params: {
                            op: 'COPY_RULE',
                            ruleId: ruleInfo.ruleId,
                            batchVersionId: editor.batchVersionId,
                            title: ruleName.value
                        },
                        success: function(response){
                            var ruleId = Ext.decode(response.responseText).data;
                            ruleListGrid.store.add({id: ruleId, name : ruleName.value });
                            ruleListGrid.getSelectionModel().select(ruleListGrid.store.indexOfId(ruleId));
                            ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected());
                            Ext.getCmp('btnAddGreen').hide();
                            Ext.getCmp('txtTitle').hide();
                            editor.focus();
                        },
                        failure: function(response){
                            Ext.Msg.alert('ошибка',Ext.decode(response.responseText).errorMessage);
                            return;
                        }
                    });

                }else{
                    Ext.Msg.alert('','введите название для нового правила');
                }
            }else{
                Ext.Ajax.request({
                    disableCaching: false,
                    url: dataUrl,
                    params: {
                        op: 'COPY_EXISTING_RULE',
                        ruleId: ruleInfo.ruleId,
                        batchVersionId: editor.batchVersionId
                    },
                    success: function(response){
                        var ruleId = ruleInfo.ruleId;
                        ruleListGrid.store.add({id: ruleId, name : ruleInfo.ruleTitle });
                        ruleListGrid.getSelectionModel().select(ruleListGrid.store.indexOfId(ruleId));
                        ruleListGrid.fireEvent('cellclick', ruleListGrid, null, 1, ruleListGrid.getSelectionModel().getLastSelected());
                        Ext.getCmp('btnAddGreen').hide();
                        Ext.getCmp('txtTitle').hide();
                        editor.focus();
                    },
                    failure: function(response){
                        Ext.Msg.alert('ошибка',Ext.decode(response.responseText).errorMessage);
                        return;
                    }
                });

            }

            Ext.getCmp('createRuleForm').destroy();
        }
    });

    var buttonClose = Ext.create('Ext.button.Button', {
        id: "createMCFormCancel",
        text: 'Отмена',
        handler : function (){
            Ext.getCmp('createRuleForm').destroy();
        }
    });


    newRuleTextField = Ext.create('Ext.form.TextField',{
        id: 'rf_elemRuleName',
        changed: false,
        defaultValue: 'введите новое название',
        defaultFont: 'font: italic 90% Tahoma',
        workingFont: 'font: normal 100% serif',
        value: 'введите новое название',
        fieldStyle: 'font: italic 90% Tahoma',
        width: 300,
        listeners: {
            focus: function(){
                if(!this.changed)
                    this.setValue("");
                newRuleTextField.setFieldStyle(this.workingFont);
            },
            blur: function(){
                if(this.value == "" || this.value == this.defaultValue)
                {
                    this.changed = false;
                    this.setValue(this.defaultValue);
                    this.setFieldStyle(this.defaultFont);
                }
                else{
                    this.changed = true;
                }
            }
        }
    });


    return new Ext.Window({
        id: 'createRuleForm',
        layout: 'fit',
        modal: 'true',
        title: 'Скопировать правило',
        items: [
            {
                xtype: 'panel',
                layout: 'border',
                height: 300,
                width: 600,
                defaults: {
                    split: true
                },
                items: [
                    {
                        xtype: 'panel',
                        region: 'center',
                        title: 'incent',
                        bodyStyle: 'padding: 10 0 0 0',
                        items: [
                            {
                                xtype: 'combobox',
                                fieldLabel: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Пакет',
                                name: 'packageId',
                                id: 'rf_elemComboPackage',
                                store: packageStore,
                                valueField:'id',
                                displayField:'name',
                            }, {
                                xtype: 'datefield',
                                fieldLabel: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;дата',
                                name: 'className',
                                id: 'rf_elemDatePackage',
                                listeners: {
                                    change: function(){
                                        var packageId = Ext.getCmp('rf_elemComboPackage').value;
                                        //alert(this.value);
                                        //alert(Ext.Date.format(this.value, 'd.m.Y'));
                                        rf_ruleListGrid.store.load({
                                            params: {
                                                op: 'GET_RULE_TITLES',
                                                packageId:Ext.getCmp('rf_elemComboPackage').value,
                                                date: Ext.Date.format(Ext.getCmp('rf_elemDatePackage').value, 'd.m.Y')
                                            }
                                        });
                                    }
                                },
                                format: 'd.m.Y'
                            },
                            rf_ruleListGrid
                        ]
                    },{
                        xtype: 'panel',
                        region: 'east',
                        width: 300,
                        title: 'Параметры сохранения',
                        items:[
                            {
                                xtype: 'panel',
                                height: 24,
                                hidden: true,
                                id: 'newRuleNamePanel',
                                items: [
                                    newRuleTextField
                                ]
                            },
                            {
                                xtype: 'textarea',
                                width: 300,
                                height: 250,
                                id: 'rf_ruleBody',
                                readOnly: true,
                            }
                        ],
                        tbar: [
                            {
                                text: 'новое правило',
                                icon: contextPathUrl+'/pics/checkbox_unchecked.png',
                                value: false,
                                boxLabel: 'новое правило',
                                id: 'rf_elemRuleStatus',
                                handler: function(){
                                    if(this.value){
                                        this.setIcon(contextPathUrl + '/pics/checkbox_unchecked.png');
                                        Ext.getCmp('newRuleNamePanel').hide();
                                    }
                                    else{
                                        this.setIcon(contextPathUrl + '/pics/checkbox_checked.png');
                                        Ext.getCmp('newRuleNamePanel').show();
                                    }


                                    this.value = !this.value;
                                }
                            }
                        ]
                    },{
                        xtype: 'panel',
                        region: 'south',
                        buttons: [buttonSave, buttonClose]
                    }
                ]
            }
        ]



    });


    return new Ext.Window({
        id: "createMCFormWin",
        layout: 'fit',
        title:'Правило',
        modal: true,
        maximizable: true,
        items:[
            Ext.create('Ext.form.Panel',{
                region: 'center',
                width: 600,
                //bodyPadding: '5 5 0',
                //defaultType: 'textfield',
                items: [

                    {
                        xtype: 'panel',
                        region: 'center',
                        title: 'center',
                        layout: 'border',
                        height: 150,
                        items: [
                            {
                                region: 'south',
                                xtype: 'panel',
                                title: 'EAST',
                                height: 50
                            },
                            {
                                xtype: 'panel',
                                region: 'center',
                                title: 'center'
                            }
                        ]
                    }
                ]
            })
        ]
    });

}