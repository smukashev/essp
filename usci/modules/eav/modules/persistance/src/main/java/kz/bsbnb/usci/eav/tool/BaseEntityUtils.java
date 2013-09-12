package kz.bsbnb.usci.eav.tool;

import kz.bsbnb.usci.eav.comparator.impl.BasicBaseEntityComparator;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

/**
 *
 */
public class BaseEntityUtils {

    private static boolean compare(BaseEntity comparingBaseEntity, BaseEntity anotherBaseEntity)
    {
        MetaClass comparingMetaClass = comparingBaseEntity.getMeta();
        MetaClass anotherMetaClass = anotherBaseEntity.getMeta();
        if (!comparingMetaClass.getClassName().equals(anotherMetaClass.getClassName()))
        {
            throw new IllegalArgumentException("Comparison BaseEntity with different metadata impossible.");
        }

        long comparingBaseEntityId = comparingBaseEntity.getId();
        long anotherBaseEntityId = anotherBaseEntity.getId();
        if (comparingBaseEntityId >= 1 && anotherBaseEntityId >= 1 && comparingBaseEntityId == anotherBaseEntityId)
        {
            return true;
        }
        else
        {
            BasicBaseEntityComparator comparator = new BasicBaseEntityComparator();
            return comparator.compare(comparingBaseEntity, anotherBaseEntity);
        }
    }

}
