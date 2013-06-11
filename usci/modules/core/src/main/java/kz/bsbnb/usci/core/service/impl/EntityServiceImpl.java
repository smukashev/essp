package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

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
        long t1 = System.currentTimeMillis();
        baseEntityDao.save(baseEntity);
        long t2 = System.currentTimeMillis() - t1;

        System.out.println("[core][save]                :           " + t2);
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
//        return baseEntityDao.search(baseEntity);
        ArrayList<Long> result = searcher.findAll(baseEntity) ;
//        System.out.println(result.size());

       return baseEntityDao.load(result.get(0));
    }

    @Override
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {
        for (String s : baseEntityLoad.getMeta().getAttributeNames()){
            System.out.println(s);
        }
//        System.out.println(baseEntityLoad.getMeta().getMemberType("test"));

        Long id = metaClassDao.save(baseEntityLoad.getMeta());

        MetaClass meta = baseEntityLoad.getMeta();
        IMetaAttribute attribute = meta.getMetaAttribute("test");

        System.out.println(attribute.getId());
         baseEntityDao.saveOrUpdate(baseEntityLoad);
    }
}
