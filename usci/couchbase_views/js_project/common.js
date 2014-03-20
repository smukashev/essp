function preloadImages(images) {
    var i = 0;
    var imageArray = new Array();
    imageArray = images.split(',');
    
    for(i=0; i<=imageArray.length-1; i++) {
        var imageObj = new Image();
        //document.write('<img src="' + imageArray[i] + '" />');// Write to page (uncomment to check images)
        imageObj.src=imageArray[i];
    }
}

function reloadInfinitGrid(grid)
{
	reloadInfinitStore(grid.getStore());
}

function reloadInfinitStore(store)
{
    //store.data.clear();
    //store.loadPage(1);
    store.load({
        callback: function(records, operation, success) {
            if (!success) {
                Ext.MessageBox.alert('Ошибка', 'Не возможно получить данные: ' + operation.error);
            }
        }
    });
}

function createBrunchesComboStore()
{
    Ext.define('branchesComboModel', {
        extend: 'Ext.data.Model',
        fields: ['id','title']
     });   
     
     var branchesComboStore = Ext.create('Ext.data.Store', {
        model: 'branchesComboModel',
        pageSize: 50,
        proxy: {
             type: 'ajax',
             url: 'branches.php',
             actionMethods: {
                 read: 'POST'
             },
             extraParams: {branch_id: getBranchIdComboValue()},
             reader: {
                 type: 'json',
                 root: 'data',
                 totalProperty: 'total'
             }
         },
         autoLoad: true,
         remoteSort: true
    });

     return branchesComboStore;
}

function createSharedComboStore(type_code)
{
    Ext.define('sharedComboModel', {
        extend: 'Ext.data.Model',
        fields: ['id','value']
     });   
     
     var branchesComboStore = Ext.create('Ext.data.Store', {
        model: 'sharedComboModel',
        pageSize: 50,
        proxy: {
             type: 'ajax',
             url: 'shared.php',
             actionMethods: {
                 read: 'POST'
             },
             extraParams: {type_code: type_code},
             reader: {
                 type: 'json',
                 root: 'data',
                 totalProperty: 'total'
             }
         },
         autoLoad: true,
         remoteSort: true
    });

     return branchesComboStore;
}


function popupImage(title, url)
{
    var win = new Ext.Window({
        html: '<img src="' + url + '" />',
        title: title,
        height: 450,
        width: 450,
        modal: true,
        autoScroll: true
    });

    win.show();
}

function createItemsComboStore()
{
    Ext.define('itemsComboModel', {
        extend: 'Ext.data.Model',
        fields: ['id','marking']
     });   
     
     var itemsComboStore = Ext.create('Ext.data.Store', {
        model: 'itemsComboModel',
        pageSize: 50,
        proxy: {
             type: 'ajax',
             url: 'items.php',
             actionMethods: {
                 read: 'POST'
             },
             //extraParams: {branch_id: getBranchIdComboValue()},
             reader: {
                 type: 'json',
                 root: 'data',
                 totalProperty: 'total'
             }
         },
         autoLoad: true,
         remoteSort: true
    });

     return itemsComboStore;
}