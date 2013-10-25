package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.meta.MetaClassName;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import java.util.List;

/**
 * Caches crud operations with MetaClass objects.
 */
public interface IMetaClassRepository
{
    public MetaClass getMetaClass(String className);
    public MetaClass getMetaClass(long id);
    public List<MetaClass> getMetaClasses();
    public void saveMetaClass(MetaClass meta);
    public void resetCache();
    public List<MetaClassName> getMetaClassesNames();
    public boolean delMetaClass(String className);
}
