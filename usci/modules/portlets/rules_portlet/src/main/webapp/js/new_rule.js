function newRuleForm(){
    return new Ext.Window({
        id: 'newRuleForm',
        layout: 'fit',
        modal: 'true',
        title: 'Новое правило',
        items: [
            Ext.create('Ext.form.Panel',{
                region: 'center',
                width: 1200,
                height: 700,
                items: [
                    Ext.create('Ext.form.TextField', {
                        fieldLabel: 'пакет',
                        labelWidth: 55,
                        value: Ext.getCmp('elemComboPackage').getRawValue(),
                        disabled: true,
                        padding: 3
                    }),
                    Ext.create('Ext.form.DateField', {
                        id: 'elemNewRuleDate',
                        fieldLabel: 'дата',
                        labelWidth: 55,
                        format: 'd.m.Y',
                        padding: 3
                    }),
                    Ext.create('Ext.form.TextField', {
                        id: 'txtTitle',
                        fieldLabel: 'название',
                        labelWidth: 55,
                        padding: 3
                    }),
                    Ext.create('Ext.form.Panel',{
                        tbar: [
                            {
                                text: 'Добавить',
                                id: 'btnNewRuleSubmit',
                                //disabled: true,
                                handler: function(){
                                    Ext.Ajax.request({
                                        disableCaching: false,
                                        url: dataUrl,
                                        params: {
                                            op: 'NEW_RULE',
                                            title: Ext.getCmp('txtTitle').value,
                                            ruleBody: newRuleEditor.getSession().getValue(),
                                            date: Ext.getCmp('elemNewRuleDate').value,
                                            packageId: Ext.getCmp('elemComboPackage').value,
                                            pkgName: Ext.getCmp('elemComboPackage').getRawValue()
                                        },
                                        success: function(response){
											var r = JSON.parse(response.responseText);

											if(!r.success) {
											  Ext.Msg.alert('ошибка',r.errorMessage);
											} else {
											  Ext.Msg.alert('', 'Правило добавлено');
											}
                                        },
                                        failure: function(response){
                                            Ext.Msg.alert('ошибка',Ext.decode(response.responseText).errorMessage);
                                        }
                                    });
                                }
                            }
                        ]
                    }),
                    {
                        html: "<div id='bknew-rule' style='height: 600px;'>function(){}</div>",
                    }
                ]
            })
        ]
    });
}