package kz.bsbnb.usci.eav_model.comparator;

import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;

/**
 * @author k.tulbassiyev
 */
public interface IBaseEntityComparator
{
    public boolean compare(BaseEntity c1, BaseEntity c2) throws IllegalStateException;
}
