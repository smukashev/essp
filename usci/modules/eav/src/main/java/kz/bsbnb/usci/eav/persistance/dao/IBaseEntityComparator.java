package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.batchdata.IBaseValue;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;

/**
 * @author k.tulbassiyev
 */
public interface IBaseEntityComparator
{
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException;
}
