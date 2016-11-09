var isDevMode = false;


Ext.onReady(function () {
    if(Ext.util.Cookies.get('isDevMode'))
        isDevMode = Ext.util.Cookies.get('isDevMode') == 'true';

    if (isDevMode)
        devModule = {
            region: 'south',
            split: true,
            tbar: [{
                text: 'autofill',
                handler: function() {
                    if (!Ext.getCmp('edCreditor').value) {
                        Ext.getCmp('edCreditor').setValue(2371);
                    }

                    creditorId = Ext.getCmp('edCreditor').value;

                    if (!creditorId) {
                        Ext.Msg.alert("", "creditorId cannot be set");
                        return;
                    }

                    var combo = Ext.getCmp('edSearch');
                    var toSelect = "kz.bsbnb.usci.core.service.form.searcher.impl.cr.CreditFormImpl";

                    if (!combo.value) {
                        combo.select(toSelect);
                        var record = combo.getStore().findRecord('searchName', toSelect);
                        combo.fireEvent('select', combo, [record]);
                    }

                    var pNo = Ext.getCmp('edPrimaryContractNO').value ? Ext.getCmp('edPrimaryContractNO').value : "";
                    var pDate = Ext.getCmp('edPrimaryContractDate').value ? Ext.getCmp('edPrimaryContractDate').value : "";

                    var params = {
                        op: 'LIST_ENTITY',
                        metaClass: 'credit',
                        dev: 'true',
                        searchName: toSelect,
                        timeout: 120000,
                        no: Ext.getCmp('edPrimaryContractNO').value,
                        pDate: Ext.getCmp('edPrimaryContractDate').value,
                        creditorId: Ext.getCmp('edCreditor').value,
                        date: Ext.getCmp('edDate').value
                    };

                    var myMask = new Ext.LoadMask(Ext.getCmp("entityTreeView"), {msg: "Please wait..."});
                    myMask.show();

                    entityStore.load({
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
                },{
                    text: 'autocreate',
                    handler: function(){

                        var tree = Ext.getCmp('entityTreeView');
                        var rootNode = tree.getRootNode();

                        var creditor = Ext.create('entityModel', {
                            title: 'БВУ/НО',
                            code: 'creditor',
                            metaId: 8,
                            ref: true,
                        });

                        rootNode.appendChild(Ext.create('entityModel',{
                            title: 'Договор займа/условного обязательства(кредит)',
                            code: 'credit',
                            ref: false,
                            metaId: 59,
                            children: [
                                creditor,
                                Ext.create('entityModel', {
                                    title: 'Договор',
                                    code: 'primary_contract',
                                    ref: false,
                                    metaId: 58,
                                    children:[
                                        Ext.create('entityModel', {
                                            title: 'Номер',
                                            code: 'no',
                                            value: '777888',
                                            ref: false,
                                            leaf: true,
                                            type: 'STRING',
                                            simple: true
                                        }),
                                        Ext.create('entityModel', {
                                            title: 'Дата',
                                            code: 'date',
                                            format: 'd.m.Y',
                                            value: '04.11.2016',
                                            ref: false,
                                            leaf: true,
                                            type: 'DATE',
                                            simple: true
                                        })
                                    ]
                                })]}));

                        // refChange(creditor, Ext.getCmp('edCreditor').value);
                        // editorAction.commitInsert();

                    }


        }]


        };

});