Ext.require([
    'Ext.Msg',
    'Ext.panel.*'
]);

function createMCAttrForm(classId, parentPath, attrPath, callback)
{
    var buttonSave = Ext.create('Ext.button.Button', {
        id: "createMCAttrFormOK",
        text: 'Сохранить',
        handler : function (){
            var form = Ext.getCmp('createMCAttrForm').getForm();
            if(form.isValid()){
                form.submit({
                    waitMsg:'Идет отправка...',
                    url: dataUrl,
                    params : {op : "SAVE_ATTR"},
                    actionMethods: {
                        read: 'POST'
                    },
                    success: function(form,response) {
                        //if(record == null)
                        //{
                            //reloadInfinitGrid(grid);
                        //    Ext.getCmp('createMCAttrFormWin').destroy();
                        //} else {
                            //record.set('className', response.result.data.id);
                            //record.set('classId', response.result.data.id);
                        if(callback != null) {
                            attrTypeField = Ext.getCmp('createMCAttrFormAttrType');
                            attrPathPart = Ext.getCmp('attrPathPart');
                            attrPathCode = Ext.getCmp('attrPathCode');
                            attrTitle = Ext.getCmp('attrTitle');
                            is_Key = Ext.getCmp('is_Key');
                            is_Required = Ext.getCmp('is_Required');
                            is_Nullable = Ext.getCmp('is_Nullable');
                            is_Final = Ext.getCmp('is_Final');
                            if(attrTypeField.getValue() == 1 || attrTypeField.getValue() == 3) {
                                callback(attrPathPart.getValue() + attrPathCode.getValue(),
                                    attrTitle.getValue(), true);
                            } else {
                                callback(attrPathPart.getValue() + attrPathCode.getValue(),
                                    attrTitle.getValue(), false);
                            }
                        }
                        Ext.getCmp('createMCAttrFormWin').destroy();
                            //record.commit();
                        //}
                    },
                    failure : function(){
                        Ext.Msg.alert('Внимание', "Произошла ошибка");
                    }
                });
            }else{
                Ext.Msg.alert('Внимание', 'Заполните ВСЕ поля');
            }
        }
    });

    var buttonClose = Ext.create('Ext.button.Button', {
        id: "createMCAttrFormCancel",
        text: 'Отмена',
        handler : function (){
            Ext.getCmp('createMCAttrFormWin').destroy();
        }
    });

    var attributeRequireType = [
        ['true', 'Да'],
        ['false', 'Нет'],
    ];

    var attributeTypes = [
        [1, 'Простой'],
        [2, 'Составной'],
        [3, 'Массив простых'],
        [4, 'Массив составных']
    ];

    var attributeSimpleTypes = [
        ['INTEGER', 'Число'],
        ['DATE', 'Дата'],
        ['STRING', 'Строка'],
        ['BOOLEAN', 'Булево'],
        ['DOUBLE', 'Число с плавающей точкой']
    ];

    Ext.define('classesStoreModel', {
        extend: 'Ext.data.Model',
        fields: ['classId','className']
    });

    var classesStore = Ext.create('Ext.data.Store', {
        model: 'classesStoreModel',
        pageSize: 100,
        proxy: {
            type: 'ajax',
            url: dataUrl,
            extraParams: {op : 'LIST_ALL'},
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
        remoteSort: true
    });

    attrPathCode = null;
    attrPathPart = null;

    if (attrPath != null) {
        pathArray = attrPath.split(".");

        attrPathCode = pathArray[pathArray.length - 1];

        attrPathPart = attrPath.substring(0, attrPath.length -(attrPathCode.length + 1));
    } else {
        attrPathPart = parentPath;
    }

    var createMCAttrForm = Ext.create('Ext.form.Panel', {
        id: 'createMCAttrForm',
        region: 'center',
        width: 615,
        fieldDefaults: {
            msgTarget: 'side'
        },
        defaults: {
            anchor: '100%'
        },

        defaultType: 'textfield',
        bodyPadding: '5 5 0',
        items: [{
            fieldLabel: 'Код класса',
            name: 'classId',
            value: classId,
            readOnly: true
        },{
            fieldLabel: 'Код родителя',
            name: 'parentPath',
            value: parentPath,
            readOnly: true
        }, {
            fieldLabel: 'Путь аттрибута',
            id: 'attrPathPart',
            name: 'attrPathPart',
            value: attrPathPart,
            readOnly: true
        }, {
            fieldLabel: 'Код аттрибута',
            id: 'attrPathCode',
            name: 'attrPathCode',
            value: attrPathCode
        }, {
            fieldLabel: 'Наименование аттрибута',
            id: 'attrTitle',
            name: 'attrTitle'
        },{
            fieldLabel: 'Вид аттрибута',
            id: 'createMCAttrFormAttrType',
            name: 'attrType',
            xtype: 'combobox',
            store: new Ext.data.SimpleStore({
                id:0,
                fields:
                    [
                        'Id',
                        'Text'
                    ],
                data: attributeTypes
            }),
            valueField:'Id',
            displayField:'Text',
            queryMode:'local',
            listeners: {
                change: function (field, newValue, oldValue) {
                    attrSimpleTypeField = Ext.getCmp('createMCAttrFormAttrSimpleType');
                    attrComplexTypeField = Ext.getCmp('createMCAttrFormAttrComplexType');

                    if(newValue == 1 || newValue == 3) {
                        attrSimpleTypeField.setDisabled(false);
                        attrComplexTypeField.setDisabled(true);
                    } else {
                        attrSimpleTypeField.setDisabled(true);
                        attrComplexTypeField.setDisabled(false);
                    }
                }
            }
        },{
            fieldLabel: 'Тип аттрибута',
            id: 'createMCAttrFormAttrSimpleType',
            name: 'attrSimpleType',
            xtype: 'combobox',
            store: new Ext.data.SimpleStore({
                id:0,
                fields:
                    [
                        'Id',
                        'Text'
                    ],
                data: attributeSimpleTypes
            }),
            valueField:'Id',
            displayField:'Text',
            queryMode:'local'
        },{
            fieldLabel: 'Класс аттрибута',
            id: 'createMCAttrFormAttrComplexType',
            name: 'attrComplexType',
            xtype: 'combobox',
            store: classesStore,
            valueField:'classId',
            displayField:'className'
        },{
            fieldLabel: 'Ключевой аттрибут',
            id: 'is_Key',
            name: 'is_Key',
            xtype: 'combobox',
            store: new Ext.data.SimpleStore({
                id:0,
                fields:
                    [
                        'Id',
                        'Text'
                    ],
                data: attributeRequireType
            }),
            valueField:'Id',
            displayField:'Text',
            queryMode:'local'
        },{
            fieldLabel: 'Обязательный аттрибут',
            id: 'is_Required',
            name: 'is_Required',
            xtype: 'combobox',
            store: new Ext.data.SimpleStore({
                id:0,
                fields:
                    [
                        'Id',
                        'Text'
                    ],
                data: attributeRequireType
            }),
            valueField:'Id',
            displayField:'Text',
            queryMode:'local'
        },{
            fieldLabel: 'IS NULLABLE',
            id: 'is_Nullable',
            name: 'is_Nullable',
            xtype: 'combobox',
            store: new Ext.data.SimpleStore({
                id:0,
                fields:
                    [
                        'Id',
                        'Text'
                    ],
                data: attributeRequireType
            }),
            valueField:'Id',
            displayField:'Text',
            queryMode:'local'
        },
            {
                fieldLabel: 'IS FINAL',
                id: 'is_Final',
                name: 'is_Final',
                xtype: 'combobox',
                store: new Ext.data.SimpleStore({
                    id:0,
                    fields:
                        [
                            'Id',
                            'Text'
                        ],
                    data: attributeRequireType
                }),
                valueField:'Id',
                displayField:'Text',
                queryMode:'local'
            }
        ],

        buttons: [buttonSave, buttonClose]
    });

    attrSimpleTypeField = Ext.getCmp('createMCAttrFormAttrSimpleType');
    attrComplexTypeField = Ext.getCmp('createMCAttrFormAttrComplexType');

    if(attrPath != null) {
        Ext.Ajax.request({
            url: dataUrl,
            waitMsg:'Идет загрузка...',
            params : {attrPath : attrPath, op: "GET_ATTR"},
            actionMethods: {
                read: 'POST'
            },
            success: function(response, opts) {
                data = JSON.parse(response.responseText);
                if (typeof data.data === "undefined")
                {
                    Ext.MessageBox.alert('Ошибка', 'Ошибка');
                } else {
                    attrTypeField = Ext.getCmp('createMCAttrFormAttrType');
                    attrTypeField.setValue(data.data.type);

                    if(data.data.type == 1 || data.data.type == 3) {
                        attrSimpleTypeField.setValue(data.data.simpleType);
                        attrComplexTypeField.setDisabled(true);
                    } else {
                        attrComplexTypeField.setValue(data.data.complexType);
                        attrSimpleTypeField.setDisabled(true);
                    }

                    attrPathCode = Ext.getCmp('attrPathCode');
                    attrPathCode.setReadOnly(true);

                    attrTitle = Ext.getCmp('attrTitle');
                    attrTitle.setValue(data.data.title);

                    is_Key = Ext.getCmp('is_Key');
                    is_Key.setValue(data.data.is_key);
                    is_Required = Ext.getCmp('is_Required');
                    is_Required.setValue(data.data.is_required);
                    is_Required = Ext.getCmp('is_Final');
                    is_Required.setValue(data.data.is_final);
                    if(data.data.is_key=='true')
                    {
                        is_Nullable = Ext.getCmp('is_Nullable');
                        is_Nullable.setValue(data.data.is_nullable);
                        is_Nullable.setDisabled(true);
                    }
                }
            },
            failure: function(response, opts) {
                data = JSON.parse(response.responseText);
                alert(data.errorMessage);
            }
        });
    } else {
        attrSimpleTypeField.setDisabled(true);
        attrComplexTypeField.setDisabled(true);
    }


    var form = Ext.getCmp('createMCAttrForm').getForm();

    createMCAttrFormWin = new Ext.Window({
        id: "createMCAttrFormWin",
        layout: 'fit',
        title:'Аттрибут',
        modal: true,
        maximizable: true,
        items:[createMCAttrForm]
    });

    return createMCAttrFormWin;
}
