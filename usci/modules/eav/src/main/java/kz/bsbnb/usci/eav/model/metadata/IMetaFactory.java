package kz.bsbnb.usci.eav.model.metadata;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;

/**
 * @author k.tulbassiyev
 */
public interface IMetaFactory
{
    public BaseEntity getBaseEntity(String className, Batch batch);
}
