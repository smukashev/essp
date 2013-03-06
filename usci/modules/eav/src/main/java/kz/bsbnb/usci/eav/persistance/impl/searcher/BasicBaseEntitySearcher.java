package kz.bsbnb.usci.eav.persistance.impl.searcher;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.ComplexKeyTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaAttribute;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;
import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaSet;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

/**
 * Used to compare BaseEntity in memory, and to retrieve BaseEntities from storage by example.
 */
@Component
public class BasicBaseEntitySearcher implements IBaseEntitySearcher
{
    Logger logger = LoggerFactory.getLogger(BasicBaseEntitySearcher.class);

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public BaseEntity findSingle(BaseEntity meta)
    {
        //TODO: method stub
        return null;
    }

    @Override
    public ArrayList<BaseEntity> findAll(BaseEntity meta)
    {
        //TODO: method stub
        return null;
    }
}
