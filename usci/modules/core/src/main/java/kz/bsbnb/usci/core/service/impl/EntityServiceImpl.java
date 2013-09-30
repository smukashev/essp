package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author k.tulbassiyev
 */
@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService {
    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    IBaseEntitySearcher searcher;

    @Autowired
    IMetaClassDao metaClassDao;

    public EntityServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void save(BaseEntity baseEntity) {
        //System.out.println(baseEntity.toString());

        long t1 = System.currentTimeMillis();
        baseEntityDao.save(baseEntity);
        long t2 = System.currentTimeMillis() - t1;

        System.out.println("[core][save]                :           " + t2);
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
//        throw new UnsupportedOperationException("Not yet implemented.");
        System.out.println("metaclass : "+baseEntity.getMeta().getClassName());
        System.out.println("reportdate : "+baseEntity.getReportDate());
        Long result = searcher.findSingle(baseEntity) ;
        System.out.println("Result : "+result);

        BaseEntity baseEntity1 = (BaseEntity)baseEntityDao.load(result);

        System.out.println(baseEntity1.getId());
        System.out.println(baseEntity1.getAttributeCount());
        System.out.println(baseEntity1.getMetaAttribute("attr2"));

        System.out.println(baseEntity1.getBaseValue("attr2").getBatch().getId());
        return baseEntity1;
    }

    @Override
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {


        Long id = metaClassDao.save(baseEntityLoad.getMeta());
         baseEntityDao.saveOrUpdate(baseEntityLoad);
    }

    @Override
    public BaseEntity load(long id) {
        throw new UnsupportedOperationException("Not yet implemented.");
        //return baseEntityDao.load(id);
    }
}
