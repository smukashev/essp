Ext.require([
    'Ext.tab.*'
]);

Ext.onReady(function(){
    var tabs = Ext.widget('tabpanel', {
        renderTo: 'content',
        width: '100%',
        height: '100%',
        activeTab: 0,
        defaults :{
            bodyPadding: 0
        },
        items: [createViewGrid(), {
                title: 'О программе',
                loader: {
                    url: 'version.php',
                    autoLoad: true
                }
            }]
    });

    tabs.show();
});
