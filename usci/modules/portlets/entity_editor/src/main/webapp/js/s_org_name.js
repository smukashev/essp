Ext.onReady(function() {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.OrgFormImpl';
    forms[serviceCode] = function(panel) {
        panel.removeAll();
        panel.add(Ext.create("Ext.form.field.Text",
            {
                id: 'edName',
                fieldLabel: 'Наименование',
                width: '40%',
                margin: 10
            }));

        panel.doSearch = function(){
            var params = {
                op : 'LIST_ENTITY',
                metaClass: 'organization_info',
                searchName: serviceCode,
                timeout: 120000,
                name: Ext.getCmp('edName').value,
                date: Ext.getCmp('edDate').value,
                creditorId: Ext.getCmp('edCreditor').value,
                pageNo: userNavHistory.getNextPage()
            };

            entityStore.load({
                params: params,
                callback: function (records, operation, success) {
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
                    if(records && records.length == 0)
                        Ext.MessageBox.alert(label_INFO, 'Поиск вернул 0 результатов');

                    var totalCount = operation.request.proxy.reader.rawData.totalCount;
                    if(totalCount) {
                        userNavHistory.success(totalCount);
                    }
                }});
        }
    };
});