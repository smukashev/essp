Ext.require([
    'Ext.Msg',
    'Ext.panel.*'
]);

function createCommandForm()
{
    var buttonSave = Ext.create('Ext.button.Button', { 
                id: "itemFormOK",
                text: 'Сохранить',
                handler : function (){
                    var form = Ext.getCmp('itemForm').getForm();
                    if(form.isValid()){
                        commandField = form.findField('command').getValue();

                        form.submit({
                            waitMsg:'Идет отправка...',
                            url: 'command.php',
                            params : {command: commandField},
                            actionMethods: {
                                read: 'POST'
                            },
                            success: function(form,response) {
                                Ext.getCmp('itemFormWin').destroy();
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
                id: "itemFormCancel",
                text: 'Отмена',
                handler : function (){
                    Ext.getCmp('itemFormWin').destroy();
                }
            });

    var itemForm = Ext.create('Ext.form.Panel', {
            id: 'itemForm',
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
                fieldLabel: 'Комманда',
                name: 'command'
            }],
            
            buttons: [buttonSave, buttonClose]
      });

    itemFormWin = new Ext.Window({
                        id: "itemFormWin",
                        layout: 'fit',
                        title:'Комманда',
                        modal: true,
                        maximizable: true,
                        items:[itemForm]
                    });

    return itemFormWin;
}
