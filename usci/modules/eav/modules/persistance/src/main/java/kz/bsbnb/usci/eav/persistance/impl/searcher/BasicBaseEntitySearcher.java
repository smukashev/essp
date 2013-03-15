package kz.bsbnb.usci.eav.persistance.impl.searcher;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
