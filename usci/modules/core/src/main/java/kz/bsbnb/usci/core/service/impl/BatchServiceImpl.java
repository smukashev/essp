package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_persistance.persistance.dao.IBatchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author k.tulbassiyev
 */
@Service
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
