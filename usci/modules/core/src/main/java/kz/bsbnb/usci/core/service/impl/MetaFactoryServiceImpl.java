package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaAttribute;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.dao.IBatchDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class MetaFactoryServiceImpl implements IMetaFactoryService {
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    IBaseEntityDao baseEntityDao;

    @Autowired
    private IBatchDao batchDao;


    @Override
    public BaseEntity getBaseEntity(String className) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClassRepository.getMetaClass(className), new Date());
    }

    @Override
    public BaseEntity getBaseEntity(MetaClass metaClass) {
        // TODO: Implement generation of the reporting date.
        return new BaseEntity(metaClass, new Date());
    }

    @Override
    public BaseSet getBaseSet(IMetaType meta) {
        return new BaseSet(meta);
    }

    @Override
    public List<BaseEntity> getBaseEntities() {



        MetaClass metaStreetHolder = new MetaClass("street");
        metaStreetHolder.setMetaAttribute("lang",
                new MetaAttribute(false, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("value",
                new MetaAttribute(true, false, new MetaValue(DataTypes.STRING)));
        metaStreetHolder.setMetaAttribute("repDate",
                new MetaAttribute(false, false, new MetaValue(DataTypes.DATE)));

        metaClassDao.save(metaStreetHolder);

        Batch batch = new Batch(new Timestamp(new java.util.Date().getTime()), new java.sql.Date(new java.util.Date().getTime()));
        batchDao.save(batch);

        BaseEntity streetEntity = new BaseEntity(metaClassRepository.getMetaClass("street"), new Date());
        streetEntity.put("lang", new BaseValue(batch, 1, "KAZ"));
        streetEntity.put("value", new BaseValue(batch, 1, "Street1"));
        streetEntity.put("repDate", new BaseValue(batch, 1, new Date()));
        baseEntityDao.save(streetEntity);
                return null;
//        List<BaseEntity> baseEntityList = new ArrayList<BaseEntity>();
//
//        for (MetaClass metaClass : metaClassRepository.getMetaClasses()){
//            baseEntityList.add(new BaseEntity(metaClass,new Date()));
//        }
//
//        return baseEntityList;

    }

}
