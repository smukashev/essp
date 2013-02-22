package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.service.IDataService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class DataServiceImpl implements IDataService
{
    private List<BaseEntity> data = new ArrayList<BaseEntity>();

    private Logger logger = Logger.getLogger(DataServiceImpl.class);

    IEntityService entityService;

    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    @PostConstruct
    public void init()
    {
        entityService = (IEntityService)rmiProxyFactoryBean.getObject();
    }

    @Override
    public boolean add(BaseEntity baseEntity)
    {
        // todo: implement
        if(!isEntityExist(baseEntity))
        {
            data.add(baseEntity);

            long t1 = System.currentTimeMillis();
            entityService.save(baseEntity);
            long t2 = System.currentTimeMillis() - t1;
            logger.info("[save entity]          :       " + t2);
        }

        return false;
    }

    public boolean isEntityExist(BaseEntity baseEntity)
    {
        // todo: implement
        return false;
    }
}
