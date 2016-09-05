function formBasic(node, callback){

    Ext.Ajax.request({
        url: dataUrl,
        params: {
            op: 'LIST_ATTRIBUTES',
            metaId: node.data.childMetaId
        },
        timeout: 120000,
        success: function (result) {
            //myMask.hide();
            var json = JSON.parse(result.responseText);
            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            var form = Ext.create('Ext.form.Panel',{
                bodyPadding: '5 5 0',
                width: "100%",
                defaults: {
                    anchor: '100%'
                },
                autoScroll: true,
                elem: Ext.create('entityModel', {
                    title: 'test',
                    code: 'code',
                    simple: false,
                    array: false
                }),
                addField: function(attr){
                    var labelWidth = "60%";
                    var width = "40%";

                    var allowBlank = !(attr.isRequired || attr.isKey);

                    if(attr.ref) {
                        this.add(Ext.create("Ext.form.field.ComboBox", {
                            //id: attr.code + "FromItem" + idSuffix,
                            fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                            labelWidth: labelWidth,
                            width: width,
                            //readOnly: readOnly,
                            allowBlank: allowBlank,
                            blankText: label_REQUIRED_FIELD,
                            store: Ext.create('Ext.data.Store', {
                                model: 'refStoreModel',
                                pageSize: 100,
                                proxy: {
                                    type: 'ajax',
                                    url: dataUrl,
                                    extraParams: {op: 'LIST_BY_CLASS_SHORT', metaId: attr.metaId},
                                    actionMethods: {
                                        read: 'POST'
                                    },
                                    reader: {
                                        type: 'json',
                                        root: 'data',
                                        totalProperty: 'total'
                                    }
                                },
                                autoLoad: true,
                                timeout: 120000,
                                remoteSort: true
                            }),
                            displayField: 'title',
                            valueField: 'ID',
                            value: attr.value,
                            editable: false,
                            commit: function(){
                                if(this.getValue()) {
                                    var refNode = Ext.create('entityModel', {
                                        title: attr.title,
                                        code: 'code',
                                        value: this.getValue(),
                                        ref: true,
                                        metaId: attr.metaId
                                    });
                                    form.elem.appendChild(refNode);
                                    refChange(refNode, this.getValue());
                                }
                            }
                        }));
                    } else {
                        this.add(Ext.create("Ext.form.field.Text", {
                            //id: attr.code + "FromItem" + idSuffix,
                            fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                            labelWidth: labelWidth,
                            width: width,
                            value: attr.value,
                            //readOnly: readOnly,
                            allowBlank: allowBlank,
                            blankText: label_REQUIRED_FIELD,
                            commit: function(){
                                if(this.getValue()) {
                                    form.elem.appendChild({
                                        title: attr.title,
                                        code: attr.code,
                                        leaf: true,
                                        value: this.getValue(),
                                        simple: true,
                                        type: attr.type
                                    });
                                }
                            }
                        }));
                    }
                }
            });

            var wdw = Ext.create("Ext.Window", {
                title: 'Добавление в ' + node.data.title,
                width: 400,
                modal: true,
                closable: true,
                closeAction: 'hide',
                items: [form],
                tbar: [{
                    text: 'Сохранить новую запись',
                    handler: function () {
                        if (form.isValid()) {
                            //saveFormValues(FORM_ADD);
                            for(var i = 0;i<form.items.items.length;i++) {
                                form.items.items[i].commit();
                            }
                            wdw.close();
                            callback(form);
                        }
                    }
                }]
            }).show();


            for(var i=0;i<attributes.length;i++) {
                form.addField(attributes[i].data);
            }
        },
        failure: function(){
            Ext.MessageBox.alert("Ошибка", "Ведутся профилактические работы, попробуйте выполнить запрос позже");
            //myMask.hide();
        }
    });
}
