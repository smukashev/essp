var packageControlClassId = null;

function updateButtons(){
    if(packageControlClassId != null)
        Ext.getCmp('btnPackageControlAdd').setDisabled(false);
}

function packageControlForm(){
    //var cars = [{id: 10, name: 'sdf'}];
    /*var packageStore = Ext.create('Ext.data.ArrayStore', {
     fields: ['id','name'],
     data: cars
     });*/

    var packageStore = Ext.create('Ext.data.Store',{
        id: 'packageStore',
        model: 'packageListModel',
        //data: myData,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                op: 'PACKAGE_ALL'
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        },
        autoLoad: true
    });

    var packageVersionStore = Ext.create('Ext.data.Store',{
        id: 'packageVersionStore',
        model: 'packageListModel',
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {
                op: 'PACKAGE_VERSIONS'
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        }
    });

    var packageVersionGrid = Ext.create('Ext.grid.Panel',{
        store: packageVersionStore,
        columns: [{
            text     : label_DATE,
            dataIndex: 'name'
        }],
        forceFit: true,
        height: 600
    });

    return new Ext.Window({
        id: 'packageControlForm',
        layout: 'fit',
        modal: 'true',
        title: 'Управление пакетами',
        items: [
            Ext.create('Ext.form.Panel',{
                region: 'center',
                width: 600,
                items: [
                    {
                        xtype: 'combobox',
                        layout: 'border',
                        store: packageStore,
                        displayField: 'name',
                        valueField: 'id',
                        margin: 5,
                        listeners: {
                            change: function (control, newValue, oldValue, eOpts) {
                                packageControlClassId = control.value;
                                updateButtons();

                                packageVersionStore.load({
                                    params: {
                                        packageId: control.value
                                    }});
                            }
                        }
                    },
                    Ext.create('Ext.form.Panel',{
                        tbar: [
                            {
                                text: label_ADD,
                                id: 'btnPackageControlAdd',
                                //disabled: true,
                                handler: function(){

                                    new Ext.Window ({
                                        title: 'Управление пакетами',
                                        id: 'wdwDatePicker',
                                        modal: 'true',
                                        items: [
                                            {
                                                id: 'inner-date-id',
                                                xtype: 'datefield',
                                                format: 'd.m.Y',
                                                value: new Date()
                                            },
                                            {
                                                xtype: 'button',
                                                text: 'ok',
                                                handler: function(a,b,c){
                                                    Ext.Ajax.request({
                                                        url: dataUrl,
                                                        params: {
                                                            op: 'NEW_PACKAGE_VERSION',
                                                            packageId: packageControlClassId,
                                                            date: Ext.getCmp('inner-date-id').value
                                                        }
                                                    });
                                                    Ext.getCmp('wdwDatePicker').close();
                                                }
                                            }
                                        ]
                                    }).show();

                                }
                            },
                            {
                                text: label_DEL,
                                id: 'btnPackageControlDelete',
                                disabled: true
                            },
                            {
                                text: label_REFRESH,
                                id: 'btnPackageControlUpdate',
                                disabled: true
                            }
                        ]
                    }),
                    packageVersionGrid
                ]
            })
        ]
    });
}
