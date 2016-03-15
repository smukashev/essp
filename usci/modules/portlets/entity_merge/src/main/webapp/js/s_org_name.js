Ext.onReady(function () {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.OrgFormImpl';
    forms[serviceCode] = function (panel, edDateComponent, edCreditorComponent, flag) {
        panel.removeAll();
        panel.add(Ext.create("Ext.form.field.Text",
            {
                id: (flag == 'left') ? 'edNameLeft' : 'edNameRight',
                fieldLabel: 'Наименование',
                width: '40%',
                margin: 10
            }));

        var userNavHistory;
        if (flag == 'left') {
            userNavHistory = userNavHistoryLeft;
        } else {
            userNavHistory = userNavHistoryRight;
        }

        panel.doSearch = function () {
            var params = {
                op: 'LIST_ENTITY_SELECT',
                metaClass: 'organization_info',
                searchName: serviceCode,
                timeout: 120000,
                name: (flag == 'left') ? panel.getComponent('edNameLeft').value : panel.getComponent('edNameRight').value,
                date: edDateComponent.value,
                creditorId: edCreditorComponent.value,
                pageNo: userNavHistory.getNextPage()
            };

            var entityStoreSelect;
            if (flag == 'left') {
                entityStoreSelect = entityStoreSelectLeft;
            } else {
                entityStoreSelect = entityStoreSelectRight;
            }

            entityStoreSelect.load({
                params: params,
                callback: function (records, operation, success) {
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