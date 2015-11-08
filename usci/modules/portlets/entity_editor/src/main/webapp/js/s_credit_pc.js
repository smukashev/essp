Ext.onReady(function() {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.CreditFormImpl';
    forms[serviceCode] = function(panel) {
        panel.removeAll();
        panel.add(Ext.create("Ext.form.field.Text",
            {
                id: 'edPrimaryContractNO',
                fieldLabel: 'Номер договора',
                width: '40%',
                margin: 10
            }));

        panel.add(Ext.create("Ext.form.field.Date",
            {
                id: 'edPrimaryContractDate',
                fieldLabel: 'Дата договора',
                width: '40%',
                format: 'd.m.Y',
                margin: 10
            }));

        panel.doSearch = function(){
            var params = {
                op : 'LIST_ENTITY',
                metaClass: 'credit',
                searchName: serviceCode,
                timeout: 120000,
                no: Ext.getCmp('edPrimaryContractNO').value,
                date: Ext.getCmp('edPrimaryContractDate').value,
                creditorId: Ext.getCmp('edCreditor').value

            };

            entityStore.load({
                params: params,
                callback: function (records, operation, success) {
                    if (!success) {
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(operation.request.proxy.reader.rawData.errorMessage));
                    }
                }});
        }
    };
});