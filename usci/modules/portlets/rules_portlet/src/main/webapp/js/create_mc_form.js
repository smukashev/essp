Ext.require([
    'Ext.Msg',
    'Ext.panel.*'
]);

function createMCForm(classId, className, grid, record)
{
    var buttonSave = Ext.create('Ext.button.Button', {
        id: "createMCFormOK",
        text: label_SAVE,
        handler : function (){
            var form = Ext.getCmp('createMCForm').getForm();
            if(form.isValid()){
                form.submit({
                    waitMsg: label_SENDING,
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
                            record.set('className', response.result.data.name);
                            record.set('classId', response.result.data.id);
                            Ext.getCmp('createMCFormWin').destroy();
                            record.commit();
                        }
                    },
                    failure : function(){
                        Ext.Msg.alert(label_LISTEN, action.result.errorMessage);
                    }
                });
            }else{
                Ext.Msg.alert(label_LISTEN, label_FILL_ALL);
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
            fieldLabel: label_CODE,
            name: 'classId',
            value: classId
        }, {
            fieldLabel: label_TITLE,
            name: 'className',
            value: className
        }],

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
