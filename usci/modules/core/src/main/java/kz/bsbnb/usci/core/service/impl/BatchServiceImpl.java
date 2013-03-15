package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author k.tulbassiyev
 */
public class BatchServiceImpl implements IBatchService
{
    @Autowired
    private IBatchDao batchDao;

    @Override
    public long save(Batch batch)
    {
        return batchDao.save(batch);
    }
}
