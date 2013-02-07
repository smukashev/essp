package kz.bsbnb.usci.eav.model.metadata;

import kz.bsbnb.usci.eav.model.BaseEntity;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactory
{
    public BaseEntity getBaseEntity(String className);
}
