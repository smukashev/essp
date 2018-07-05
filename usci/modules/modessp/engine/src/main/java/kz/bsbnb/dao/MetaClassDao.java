package kz.bsbnb.dao;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.stereotype.Component;

public interface MetaClassDao {
    MetaClass load(long id);
}
