Ext.onReady(function() {
    var serviceCode = 's_credit_pc';
    forms[serviceCode] = function(panel) {
        panel.removeAll();
        panel.add(Ext.create("Ext.form.field.Text",
            {
                id: 'edPrimaryContractNO',
                fieldLabel: 'Номер договора',
                width: '40%',
                margin: 10
            }));

        panel.add(Ext.create("Ext.form.field.Date",
            {
                id: 'edPrimaryContractDate',
                fieldLabel: 'Дата договора',
                width: '40%',
                format: 'd.m.Y',
                margin: 10
            }));
    };
});