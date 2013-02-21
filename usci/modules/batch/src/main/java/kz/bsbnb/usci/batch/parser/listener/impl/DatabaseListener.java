package kz.bsbnb.usci.batch.parser.listener.impl;

import kz.bsbnb.usci.batch.parser.listener.IListener;
import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.IBaseContainer;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author k.tulbassiyev
 */
public class DatabaseListener implements IListener
{
    IBaseEntityDao baseEntityDao;

    @Override
    public void put(BaseEntity baseEntity)
    {
        baseEntityDao.save(baseEntity);
    }

    public void setBaseEntityDao(IBaseEntityDao baseEntityDao)
    {
        this.baseEntityDao = baseEntityDao;
    }
}
