Ext.require([
    'Ext.Msg',
    'Ext.panel.*'
]);

function createMCForm(classId, className, PisDisabled,PisReference, grid, record)
{
    var isDisabled = [
        ['false', 'Активный'],
        ['true', 'Не активный'],
    ];

    var isReference = [
        ['false', 'Не справочник'],
        ['true', 'Справочник'],
    ];

    var buttonSave = Ext.create('Ext.button.Button', {
        id: "createMCFormOK",
        text: 'Сохранить',
        handler : function (){
            var form = Ext.getCmp('createMCForm').getForm();
            if(form.isValid()){
                form.submit({
                    waitMsg:'Идет отправка...',
                    url: dataUrl,
                    params : {op : "SAVE_CLASS"},
                    actionMethods: {
                        read: 'POST'
                    },
                    success: function(form,response) {
                        if(record == null)
                        {
                            //reloadInfinitGrid(grid);
                            Ext.getCmp('createMCFormWin').destroy();
                        } else {
                            //record.set('className', response.result.data.name);
                           // record.set('classId', response.result.data.id);
                           // record.set('isDisabled', response.result.data.disabled);
                            Ext.getCmp('createMCFormWin').destroy();
                            record.commit();
                            Ext.getCmp('metaClassesGrid').getStore().load();
                        }
                    },
                    failure : function(){
                        Ext.Msg.alert('Внимание', action.result.errorMessage);
                    }
                });
            }else{
                Ext.Msg.alert('Внимание', 'Заполните ВСЕ поля');
            }
        }
    });

    var buttonClose = Ext.create('Ext.button.Button', {
        id: "createMCFormCancel",
        text: 'Отмена',
        handler : function (){
            Ext.getCmp('createMCFormWin').destroy();
        }
    });

    var createMCForm = Ext.create('Ext.form.Panel', {
        id: 'createMCForm',
        region: 'center',
        width: 615,
        fieldDefaults: {
            msgTarget: 'side'
        },
        defaults: {
            anchor: '100%'
        },

        defaultType: 'textfield',
        bodyPadding: '5 5 0',
        items: [{
            fieldLabel: 'Код',
            name: 'classId',
            value: classId
        }, {
            fieldLabel: 'Наименование',
            name: 'className',
            value: className
        },
            {
                fieldLabel: 'Признак активности',
                id: 'isDisabled',
                name: 'isDisabled',
                xtype: 'combobox',
                store: new Ext.data.SimpleStore({
                    id:0,
                    fields:
                        [
                            'isDisabled',
                            'Text'
                        ],
                    data: isDisabled

                }),
                valueField:'isDisabled',
                displayField:'Text',
                queryMode:'local',
                listeners:{
                    scope: this,
                    afterRender: function(me){
                        me.setValue(PisDisabled.toString());
                    }
                }
            },
            {
                fieldLabel: 'Тип класса',
                id: 'isReference',
                name: 'isReference',
                xtype: 'combobox',
                store: new Ext.data.SimpleStore({
                    id:0,
                    fields:
                        [
                            'isReference',
                            'Text'
                        ],
                    data: isReference

                }),
                valueField:'isReference',
                displayField:'Text',
                queryMode:'local',
                listeners:{
                    scope: this,
                    afterRender: function(me){
                        me.setValue(PisReference.toString());
                    }
                }
            }
        ],

        buttons: [buttonSave, buttonClose]
    });

    var form = Ext.getCmp('createMCForm').getForm();

    createMCFormWin = new Ext.Window({
        id: "createMCFormWin",
        layout: 'fit',
        title:'Метакласс',
        modal: true,
        maximizable: true,
        items:[createMCForm]
    });

    return createMCFormWin;
}
