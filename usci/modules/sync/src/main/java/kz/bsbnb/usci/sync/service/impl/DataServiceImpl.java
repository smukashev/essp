package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.sync.service.IDataService;
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

            entityService.save(baseEntity);
        }

        return false;
    }

    public boolean isEntityExist(BaseEntity baseEntity)
    {
        // todo: implement
        return false;
    }
}
