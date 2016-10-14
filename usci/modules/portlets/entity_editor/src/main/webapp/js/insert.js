function getForm(node){
    var form =  Ext.create('Ext.form.Panel',{
        bodyPadding: '5 5 0',
        width: "100%",
        defaults: {
            anchor: '100%'
        },
        autoScroll: true,
        elem: node ? node : Ext.create('entityModel', {
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
                                code: attr.code,
                                value: this.getValue(),
                                ref: true,
                                metaId: attr.metaId
                            });
                            form.elem.appendChild(refNode);
                            refChange(refNode, this.getValue());
                        }
                    }
                }));
            } else if(attr.type == 'STRING'){
                form.add(Ext.create("Ext.form.field.Text", {
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
            } else if (attr.type == "DATE") {
                form.add(Ext.create("Ext.form.field.Date",
                    {
                        //id: attr.code + "FromItem" + idSuffix,
                        fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                        labelWidth: labelWidth,
                        width: width,
                        format: 'd.m.Y',
                        value: attr.value ? new Date(attr.value.replace(/(\d{2})\.(\d{2})\.(\d{4})/, '$3-$2-$1')) : null,
                        //readOnly: readOnly,
                        allowBlank: allowBlank,
                        blankText: label_REQUIRED_FIELD,
                        commit: function(){
                            if(this.getValue()) {
                                form.elem.appendChild({
                                    title: attr.title,
                                    code: attr.code,
                                    leaf: true,
                                    value: this.getSubmitValue(),
                                    simple: true,
                                    type: attr.type
                                });
                            }
                        }
                    })
                );
            } else if (attr.type == "INTEGER" || attr.type == "DOUBLE") {
                form.add(Ext.create(Ext.form.NumberField,
                    {
                        //id: attr.code + "FromItem" + idSuffix,
                        fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                        labelWidth: labelWidth,
                        width: width,
                        value: attr.value,
                        /*minValue: 0,*/
                        allowDecimals: attr.type == "DOUBLE",
                        forcePrecision: attr.type == "DOUBLE",
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
                    })
                );
            } else if (attr.type == "BOOLEAN") {
                form.add(Ext.create("Ext.form.field.ComboBox",
                    {
                        //id: attr.code + "FromItem" + idSuffix,
                        fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                        labelWidth: labelWidth,
                        width: width,
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
                        },
                        editable: false,
                        store: Ext.create('Ext.data.Store', {
                            fields: ['value', 'title'],
                            data: [
                                {value: 'true', title: 'Да'},
                                {value: 'false', title: 'Нет'}
                            ]
                        }),
                        displayField: 'title',
                        valueField: 'value',
                        value: attr.value
                    })
                );
            } else {
                form.add(Ext.create("MyCheckboxField",
                    {
                        //id: attr.code + "FromItem" + idSuffix,
                        fieldLabel: (!allowBlank ? "<b style='color:red'>*</b> " : "") + attr.title,
                        labelWidth: labelWidth,
                        width: width,
                        //readOnly: readOnly,
                        allowBlank: allowBlank,
                        blankText: label_REQUIRED_FIELD,
                        commit: function(){
                            if(this.checked) {
                                form.elem.appendChild({
                                    title: attr.title,
                                    code: attr.code,
                                    //value: this.getValue(),
                                    metaId: attr.metaId,
                                    childMetaId: attr.childMetaId,
                                    simple: false,
                                    array: attr.array,
                                    type: attr.type
                                });
                            }
                        },
                        checked: (attr.isKey || attr.value)
                    })
                );
            }
        }
    });

    return form;
}

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

            var form = getForm();

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

function formAdvanced(node, callback){
    Ext.Ajax.request({
        url: dataUrl,
        params: {
            op: 'LIST_ATTRIBUTES',
            metaId: node.data.metaId
        },
        timeout: 120000,
        success: function (result) {
            //myMask.hide();
            var json = JSON.parse(result.responseText);
            attrStore.removeAll();
            attrStore.add(json.data);
            var attributes = attrStore.getRange();

            var form = getForm(node);

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


            var totalEditableFields = 0;
            for(var i=0;i<attributes.length;i++) {
                var alreadyHas = false;
                for(var j=0;j< node.childNodes.length; j++) {
                    if(attributes[i].data.code == node.childNodes[j].data.code)
                        alreadyHas = true;
                }

                if(!alreadyHas) {
                    form.addField(attributes[i].data);
                    totalEditableFields ++;
                }
            }
            
            if(totalEditableFields == 0) {
                wdw.close();
                Ext.MessageBox.alert("", "Вся информация по " + node.data.title  + " уже добавлена");
            }
        },
        failure: function(){
            Ext.MessageBox.alert("Ошибка", "Ведутся профилактические работы, попробуйте выполнить запрос позже");
            //myMask.hide();
        }
    });
}

function insertNewCredit() {
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
                        title: 'sample title',
                        code: 'Номер',
                        value: Ext.getCmp('edPrimaryContractNO').value,
                        ref: false,
                        leaf: true,
                        type: 'STRING'
                    }),
                    Ext.create('entityModel', {
                        title: 'Дата',
                        code: 'date',
                        format: 'd.m.Y',
                        value: Ext.getCmp('edPrimaryContractDate').value,
                        ref: false,
                        leaf: true,
                        type: 'DATE'
                    })
                ]
            })]}));

    refChange(creditor, Ext.getCmp('edCreditor').value);
}