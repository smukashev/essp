Ext.onReady(function() {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.CreditFormImpl';
    forms[serviceCode] = function(panel) {
        panel.removeAll();
        panel.add(Ext.create("Ext.form.field.Text",
            {
                id: 'edPrimaryContractNO',
                fieldLabel: label_CONTRUCT_NO,
                width: '40%',
                margin: 10
            }));

        panel.add(Ext.create("Ext.form.field.Date",
            {
                id: 'edPrimaryContractDate',
                fieldLabel: label_CONTRUCT_DATE,
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
                pDate: Ext.getCmp('edPrimaryContractDate').value,
                creditorId: Ext.getCmp('edCreditor').value,
                date: Ext.getCmp('edDate').value

            };
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
                    if(records && records.length == 0) {
                        Ext.MessageBox.alert({
                            //title: 'Потверждение на удаление?',
                            msg: 'Поиск вернул 0 результатов, желаете добавить новый договор ?',
                            buttons: Ext.MessageBox.YESNO,
                            buttonText:{
                                yes: "Да",
                                no: "Нет"
                            },
                            fn: function(val){
                                if(val == 'yes') {
                                    insertNewCredit();
                                }
                            }
                        });
                    }

                    var totalCount = operation.request.proxy.reader.rawData.totalCount;
                    if(totalCount) {
                        userNavHistory.success(totalCount);
                    }
                    Ext.getCmp('entityEditorShowBtn').enable();
                }});
        }
    };
});