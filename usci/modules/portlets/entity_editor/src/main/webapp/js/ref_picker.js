var refStore;

function refPicker(node){

    var refUrl = 'ref?p_p_id=refportlet_WAR_ref_editor001SNAPSHOT&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view';

    Ext.define('refStoreModel', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'title',     type: 'string'},
            {name: 'code',     type: 'string'},
            {name: 'value',     type: 'string'},
            {name: 'simple',     type: 'boolean'},
            {name: 'array',     type: 'boolean'},
            {name: 'ref',     type: 'boolean'},
            {name: 'type',     type: 'string'},
            {name: 'isKey',     type: 'boolean'},
            {name: 'isRequired',     type: 'boolean'},
            {name: 'metaId',     type: 'string'},
            {name: 'childMetaId',     type: 'string'},
            {name: 'childType',     type: 'string'},
            {name: 'isHidden',     type: 'boolean'},
            {name: 'name',     type: 'string'},
            {name: 'name_ru',     type: 'string'},
            {name: 'short_name',     type: 'string'},
            {name: 'ID',     type: 'integer'},
            {name: 'is_convertible',     type: 'boolean'},
            {name: 'rating',     type: 'string'},
            {name: 'name_kz',     type: 'string'},
        ]
    });

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


            var displayVal = 'name';
            var displayFormat = '{name} ({code})';

            if(json.data[0].name_ru) {
                displayVal = 'name_ru';
                displayFormat = '{name_ru} ({short_name})';
            }

            new Ext.Window({
                id: 'refSelectForm',
                modal: 'true',
                title: 'Выбор справочника \"' + node.data.title + '\"',
                items: [
                    Ext.create('Ext.form.Panel',{
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
                                            getInnerTpl: function() {
                                                return displayFormat
                                            }
                                        },
                                        listeners: {
                                            'change': function(me, a, b){
                                                refStore.clearFilter();
                                                var val = this.getRawValue();
                                                refStore.filterBy(function(record,id){
                                                    return record.get('name').toLowerCase().indexOf(val) > -1 ||
                                                        record.get('name_ru').toLowerCase().indexOf(val) > -1 ||
                                                        record.get('short_name').toLowerCase().indexOf(val) > -1;
                                                });
                                            }
                                        }
                                    }),
                                    Ext.create('Ext.button.Button',{
                                        text: 'Изменить',
                                        handler: function() {
                                            var refId = Ext.getCmp('refSelectCombo').value;
                                            refStore.clearFilter();
                                            var model = refStore.getAt(refStore.find('ID',refId));
                                            console.log(model.data);
                                            node.removeAll();

                                            var myMask = new Ext.LoadMask(Ext.getCmp("refSelectForm"), {msg: "Идет загрузка..."});
                                            myMask.show();


                                            subEntityStore.load({
                                                params: {
                                                    op: 'LIST_ENTITY',
                                                    entityId: model.data.ID,
                                                    date: Ext.getCmp('edDate').value,
                                                    asRoot: false
                                                },
                                                callback: function (records, operation, success) {
                                                    node.data.value = records[0].data.value;

                                                    while (records[0].childNodes.length > 0) {
                                                        node.appendChild(records[0].childNodes[0]);
                                                    }

                                                    myMask.hide();
                                                    Ext.getCmp('refSelectForm').close();
                                                    Ext.getCmp('entityTreeView').getView().refresh();
                                                }
                                            });
                                        }

                                    })
                                ]
                            }
                        ]
                    })
                ],
                listeners: {
                    show: function(panel) {
                    }
                }
            }).show();

        }
    });

}