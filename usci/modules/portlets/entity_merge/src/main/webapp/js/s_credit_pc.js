Ext.onReady(function () {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.CreditFormImpl';
    forms[serviceCode] = function (panel) {
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

        panel.doSearch = function () {
            var params = {
                op: 'LIST_ENTITY_SELECT',
                metaClass: 'credit',
                searchName: serviceCode,
                timeout: 120000,
                no: Ext.getCmp('edPrimaryContractNO').value,
                date: Ext.getCmp('edPrimaryContractDate').value,
                creditorId: Ext.getCmp('edCreditor').value

            };
            //loader
            var myMask = new Ext.LoadMask(Ext.getCmp("entityTreeViewSelect"), {msg: "Please wait..."});
            myMask.show();

            entityStoreSelect.load({

                params: params,
                callback: function (records, operation, success) {
                    myMask.hide();

                    if (!success) {
                        var error = '';
                        if (operation.error != undefined) {
                            if (operation.error.statusText in errors)
                                error = errors[operation.error.statusText];
                            else
                                error = operation.error.statusText;
                        }
                        else
                            error = operation.request.proxy.reader.rawData.errorMessage;
                        Ext.MessageBox.alert(label_ERROR, label_ERROR_NO_DATA_FOR.format(error));
                    }
                    if (records && records.length == 0)
                        Ext.MessageBox.alert(label_INFO, 'Поиск вернул 0 результатов');

                    var totalCount = operation.request.proxy.reader.rawData.totalCount;
                    if (totalCount) {
                        userNavHistory.success(totalCount);
                    }
                }
            });
        }
    };
});