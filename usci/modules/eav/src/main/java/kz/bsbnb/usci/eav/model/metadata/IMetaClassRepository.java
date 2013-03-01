package kz.bsbnb.usci.eav.model.metadata;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;

/**
 * Caches crud operations with MetaClass objects.
 */
public interface IMetaClassRepository
{

    public MetaClass getMetaClass(String className);

    public MetaClass getMetaClass(long id);

}
