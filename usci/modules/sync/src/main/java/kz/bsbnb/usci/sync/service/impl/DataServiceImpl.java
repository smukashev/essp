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

    IEntityService entityService;

    @Autowired
    RmiProxyFactoryBean rmiProxyFactoryBean;

    @PostConstruct
    public void init()
    {
        entityService = (IEntityService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public void process(List<BaseEntity> entities)
    {
        for (BaseEntity entity : entities)
        {
            if(!isEntityExist(entity))
            {
                entityService.save(entity);
            }
            else
            {

            }
        }
    }

    public boolean isEntityExist(BaseEntity baseEntity)
    {
        for (BaseEntity entity : data)
            if(entity.equals(baseEntity))
                return true;

        return true;
    }
}
