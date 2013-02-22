package kz.bsbnb.usci.batch.parser.listener.impl;

import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.service.IDataService;

/**
 * @author k.tulbassiyev
 */
public class RmiListener implements IListener
{
    IDataService dataService;

    public RmiListener(IDataService dataService)
    {
        this.dataService = dataService;
    }

    @Override
    public void put(BaseEntity baseEntity)
    {
        dataService.add(baseEntity);
    }
}
