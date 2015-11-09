Ext.onReady(function(){
    Ext.create("Ext.Window",{
        id: 'modalDocSearchWindow',
        title : 'Добавление элемента массива',
        width : 400,
        modal : true,
        closable : true,
        closeAction: 'hide',
        items : [
            {
                id: "modalDocSearchForm",
                xtype: 'form',
                bodyPadding: '5 5 0',
                width: "100%",
                defaults: {
                    anchor: '100%'
                },
                autoScroll:true
            }],
        tbar : [{
            text : 'Сохранить новую запись' ,
            handler :function() {
                var form = Ext.getCmp('modalDocSearchForm');
                if (form.isValid()) {
                    var tree = Ext.getCmp('s_person_doc_tree');
                    //var selectedNode = tree.getSelectionModel().getLastSelected();
                    var selectedNode = tree.getRootNode().getChildAt(0);
                    var arrayIndex = selectedNode.childNodes.length;
                    var element = {
                        leaf: false,
                        title: "[" + arrayIndex + "]",
                        code: "[" + arrayIndex + "]",
                        type: selectedNode.data.childType,
                        metaId: selectedNode.data.childMetaId,
                        value: true
                    };
                    selectedNode.appendChild(element);
                    selectedNode.data.value = selectedNode.childNodes.length;
                    selectedNode = selectedNode.getChildAt(arrayIndex);
                    addField(form, element, "_edit", selectedNode);

                    //-----------------------------------------------------

                    var attributes = attrStore.getRange();
                    var idSuffix = "_add";

                    for (var i = 0; i < attributes.length; i++) {
                        var attr = attributes[i].data;
                        var field = Ext.getCmp(attr.code + "FromItem" + idSuffix);

                        var fieldValue;

                        if (attr.type == "DATE") {
                            fieldValue = field.getSubmitValue();
                        } else {
                            fieldValue = field.getValue();
                        }

                        var existingAttrNode = selectedNode.findChild('code', attr.code);

                        if (fieldValue) {
                            var subNode;

                            if (existingAttrNode) {
                                subNode = existingAttrNode;
                            } else {
                                selectedNode.appendChild(attr);
                                subNode = selectedNode.getChildAt(selectedNode.childNodes.length - 1);
                            }

                            subNode.data.value = fieldValue;

                            if (attr.simple) {
                                subNode.data.leaf = true;
                                subNode.data.iconCls = 'file';
                            } else {
                                subNode.data.leaf = false;
                                subNode.data.iconCls = 'folder';

                                if (attr.ref && attr.type == "META_CLASS") {
                                    loadSubEntity(subNode, idSuffix);
                                }
                            }
                        } else {
                            if (existingAttrNode) {
                                selectedNode.removeChild(existingAttrNode);
                            }
                        }
                    }

                    tree.getView().refresh();
                    Ext.getCmp('modalDocSearchWindow').hide();

                }
            }
        }]
    });
});
