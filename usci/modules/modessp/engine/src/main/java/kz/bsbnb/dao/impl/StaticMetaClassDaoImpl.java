package kz.bsbnb.dao.impl;


import kz.bsbnb.dao.MetaClassDao;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class StaticMetaClassDaoImpl implements MetaClassDao {

    protected MetaClass metaCredit;

    public StaticMetaClassDaoImpl() {
    }

    public StaticMetaClassDaoImpl(MetaClass metaCredit) {
        this.metaCredit = metaCredit;
    }

    @Override
    public MetaClass load(long id) {
        if(metaCredit.getId() == id)
            return metaCredit;

        throw new RuntimeException("No such meta with id: " + id);
    }
}
