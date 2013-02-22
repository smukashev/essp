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

    List<Long> tSaveList = new ArrayList<Long>();

    private final Logger logger = Logger.getLogger(EntityServiceImpl.class);

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
        tSaveList.add(t2);
        logger.info("[save entity]          :       " + t2);
        System.out.println("[save entity]          :       " + t2);
    }

    public void setBaseEntityDao(IBaseEntityDao baseEntityDao)
    {
        this.baseEntityDao = baseEntityDao;
    }

    public Long getAvgSaveTime()
    {
        if(tSaveList.size() == 0)
            return 0L;

        return (getTotalSaveTime() / tSaveList.size());
    }

    public Long getTotalSaveTime()
    {
        Long t = 0L;

        for (Long time : tSaveList)
            t += time;

        return t;
    }

    public void resetSaveTime()
    {
        tSaveList.clear();
    }
}
