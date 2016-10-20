Ext.onReady(function() {
    var serviceCode = 'kz.bsbnb.usci.core.service.form.searcher.impl.cr.PortfolioFormImpl';
    forms[serviceCode] = function(panel) {
        panel.removeAll();

       /*
        var portfolioTypes = Ext.create('Ext.data.Store', {
            fields: ['abbr', 'name'],
            data : [
                {"abbr":"msfo", "name":"МСФО"},
                {"abbr":"rfn", "name":"КФН"}
            ]
        });

        panel.add(Ext.create('Ext.form.ComboBox', {
                id: "portfolioType",
                fieldLabel: 'Портфели',
                store: portfolioTypes,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'abbr',
                margin: 10,
                renderTo: Ext.getBody()
            })
        );*/

        panel.doSearch = function(){
            var params = {
                op : 'LIST_ENTITY',
                metaClass: 'portfolio',
                searchName: serviceCode,
                timeout: 120000,
                date: Ext.getCmp('edDate').value,
                //portfolioType: Ext.getCmp('portfolioType').value,
                creditorId: Ext.getCmp('edCreditor').value,
                pageNo: userNavHistory.getNextPage()
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
                    if(records && records.length == 0)
                        Ext.MessageBox.alert(label_INFO, 'Поиск вернул 0 результатов');

                    Ext.getCmp('entityEditorShowBtn').enable();

                    var totalCount = operation.request.proxy.reader.rawData.totalCount;
                    if(totalCount) {
                        userNavHistory.success(totalCount);
                    }
                }});
        }
    };
});