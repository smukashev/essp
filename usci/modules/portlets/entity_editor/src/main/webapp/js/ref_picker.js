var refStore;

function refChange(node, refId, callback) {

    node.removeAll();

    var myMask = new Ext.LoadMask(Ext.getBody(), {msg: "Идет загрузка..."});
    myMask.show();

    subEntityStore.load({
        params: {
            op: 'LIST_ENTITY',
            entityId: refId,
            date: new Date(),
            asRoot: false
        },
        callback: function (records, operation, success) {
            node.data.value = records[0].data.value;

            while (records[0].childNodes.length > 0) {
                node.appendChild(records[0].childNodes[0]);
            }

            myMask.hide();
            if(callback)
                callback();
        }
    });
}

function refPicker(node) {

    return function () {
        var refUrl = 'ref?p_p_id=refportlet_WAR_ref_editor001SNAPSHOT&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view';

        refStore = Ext.create('Ext.data.Store', {
            id: 'refStore',
            storeId: 'refStore',
            model: 'refStoreModel'
        });

        Ext.Ajax.request({
            url: refUrl,
            params: {
                op: 'LIST_BY_CLASS',
                metaId: node.data.metaId,
                withHis: false,
                date: new Date()
            },
            success: function (result) {
                var json = JSON.parse(result.responseText);

                refStore.removeAll();
                refStore.add(json.data);


                var displayVal;
                var displayFormat;

                if(json.data[0].name) {
                    displayVal = 'name';
                    displayFormat = '{name}';
                } else if(json.data[0].name_ru) {
                    displayVal = 'name_ru';
                    displayFormat = '{name_ru}';
                }

                displayFormat += ' ';

                if(json.data[0].short_name) {
                    displayFormat += '({short_name})';
                } else if(json.data[0].no_) {
                    displayFormat += '({no_})';
                } else if(json.data[0].code) {
                    displayFormat += '({code})';
                }


                new Ext.Window({
                    id: 'refSelectForm',
                    modal: 'true',
                    title: 'Выбор справочника \"' + node.data.title + '\"',
                    items: [
                        Ext.create('Ext.form.Panel', {
                            region: 'center',
                            width: 1200,
                            height: 300,
                            layout: 'border',
                            items: [
                                {
                                    region: 'center',
                                    bodyStyle: 'padding: 20px',
                                    items: [
                                        Ext.create('Ext.form.field.ComboBox', {
                                            id: 'refSelectCombo',
                                            fieldLabel: 'Выберите справочник',
                                            displayField: displayVal,
                                            valueField: 'ID',
                                            width: 1100,
                                            labelWidth: 130,
                                            store: refStore,
                                            queryMode: 'local',
                                            listConfig: {
                                                getInnerTpl: function () {
                                                    return displayFormat
                                                }
                                            },
                                            listeners: {
                                                'change': function (me, a, b) {
                                                    refStore.clearFilter();
                                                    var val = this.getRawValue();
                                                    refStore.filterBy(function (record, id) {
                                                        val = val.toLowerCase();
                                                        return record.get('name').toLowerCase().indexOf(val) > -1 ||
                                                            record.get('name_ru').toLowerCase().indexOf(val) > -1 ||
                                                            record.get('short_name').toLowerCase().indexOf(val) > -1 ||
                                                            record.get('no_').toLowerCase().indexOf(val) > -1;
                                                    });
                                                }
                                            }
                                        }),
                                        Ext.create('Ext.button.Button', {
                                            text: 'Изменить',
                                            handler: function () {
                                                var refId = Ext.getCmp('refSelectCombo').value;
                                                refStore.clearFilter();
                                                refChange(node, refId, function(){
                                                    Ext.getCmp('refSelectForm').close();
                                                    Ext.getCmp('entityTreeView').getView().refresh();
                                                    editorAction.commitEdit();
                                                });
                                            }

                                        })
                                    ]
                                }
                            ]
                        })
                    ],
                    listeners: {
                        show: function (panel) {
                        }
                    }
                }).show();

            }
        });
    }
}
