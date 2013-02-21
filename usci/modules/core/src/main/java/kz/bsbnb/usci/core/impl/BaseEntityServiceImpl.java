package kz.bsbnb.usci.core.impl;

import kz.bsbnb.usci.core.BaseEntityService;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author k.tulbassiyev
 */
@Service
public class BaseEntityServiceImpl extends UnicastRemoteObject implements BaseEntityService
{
    public BaseEntityServiceImpl() throws RemoteException
    {

    }

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Override
    public void save(BaseEntity baseEntity)
    {
        baseEntityDao.save(baseEntity);
    }

    public void setBaseEntityDao(IBaseEntityDao baseEntityDao)
    {
        this.baseEntityDao = baseEntityDao;
    }
}
