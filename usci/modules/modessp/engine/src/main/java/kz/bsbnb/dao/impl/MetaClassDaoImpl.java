package kz.bsbnb.dao.impl;

import kz.bsbnb.dao.MetaClassDao;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.stereotype.Component;

@Component
public class MetaClassDaoImpl implements MetaClassDao {
    @Override
    public MetaClass load(long id) {
        throw new RuntimeException("not implemented");
    }
}
