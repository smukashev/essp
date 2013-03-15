package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
        long t1 = System.currentTimeMillis();
        baseEntityDao.save(baseEntity);
        long t2 = System.currentTimeMillis() - t1;

        System.out.println("[core][save]                :           " + t2);
    }
}
