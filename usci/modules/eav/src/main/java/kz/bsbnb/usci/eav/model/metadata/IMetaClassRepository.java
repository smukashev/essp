package kz.bsbnb.usci.eav.model.metadata;

import kz.bsbnb.usci.eav.model.metadata.type.impl.MetaClass;

public interface IMetaClassRepository
{
    public MetaClass getMetaClass(String className);
}
