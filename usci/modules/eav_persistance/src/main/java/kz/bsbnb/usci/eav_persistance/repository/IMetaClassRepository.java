package kz.bsbnb.usci.eav_persistance.repository;

import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;

/**
 * Caches crud operations with MetaClass objects.
 */
public interface IMetaClassRepository
{
    public MetaClass getMetaClass(String className);
    public MetaClass getMetaClass(long id);

}
