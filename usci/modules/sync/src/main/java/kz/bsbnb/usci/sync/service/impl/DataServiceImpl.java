package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.job.impl.DataJob;
import kz.bsbnb.usci.sync.service.IDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class DataServiceImpl implements IDataService
{
    @Autowired
    private DataJob dataJob;

    @Override
    public void process(List<BaseEntity> entities)
    {
        dataJob.addAll(entities);
    }
}
