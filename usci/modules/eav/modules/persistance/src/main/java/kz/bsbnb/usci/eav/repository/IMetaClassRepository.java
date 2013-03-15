package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

/**
 * Caches crud operations with MetaClass objects.
 */
public interface IMetaClassRepository
{
    public MetaClass getMetaClass(String className);
    public MetaClass getMetaClass(long id);

}
