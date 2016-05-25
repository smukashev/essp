package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.repository.IRefRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RefRepositoryImpl implements IRefRepository, InitializingBean {
    private Map<Long, List<Map<String, Object>>> prepareMap = new HashMap<>();

    @Autowired
    private IMetaClassDao metaClassDao;

    @Qualifier("metaClassRepositoryImpl")
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
       /* long t1 = System.currentTimeMillis();
        for (MetaClass meta : metaClassRepository.getMetaClasses()) {
            if (meta.isReference())
                prepareMap.put(meta.getId(), metaClassDao.getSimpleResult(meta.getId()));
        }
        System.out.println((System.currentTimeMillis() - t1));*/
    }

    @Override
    public long prepareRef(IBaseEntity baseEntity) {
        List<Map<String, Object>> mapList = prepareMap.get(baseEntity.getMeta().getId());

        for (Map<String, Object> map : mapList) {

        }

        return 0;
    }

    private Map<String, Object> convert (IBaseEntity baseEntity) {
        for (String attributeName : baseEntity.getMeta().getAttributeNames()) {
            IMetaAttribute metaAttribute = baseEntity.getMeta().getMetaAttribute(attributeName);
            IMetaType metaType = metaAttribute.getMetaType();

            if (metaType.isSet())

            if (metaAttribute.isKey()) {
                IBaseValue baseValue = baseEntity.getBaseValue(attributeName);

                if (metaType.isComplex()) {
                    IBaseEntity refEntity = (IBaseEntity) baseValue.getValue();
                }
            }
        }

        return null;
    }
}
