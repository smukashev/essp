package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService
{
    @Autowired
    IBaseEntityDao baseEntityDao;

    public EntityServiceImpl() throws RemoteException
    {
        super();
    }

    @Override
    public void save(BaseEntity baseEntity)
    {
        baseEntityDao.save(baseEntity);
    }
}
