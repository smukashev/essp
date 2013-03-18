package kz.bsbnb.usci.eav_persistance.tool.generator.nonrandom.data;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav_model.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav_persistance.tool.generator.random.data.AbstractDataGenerator;

/**
 * @author abukabayev
 */

public class BaseEntityGenerator  extends AbstractDataGenerator
{

    public BaseEntity generateBaseEntity(Batch batch, MetaClass metaClass, long index)
    {
        BaseEntity entity = new BaseEntity(metaClass);

        for (String name : metaClass.getMemberNames())
        {
            IMetaType metaType = metaClass.getMemberType(name);

            if(metaType.isComplex())
            {
                    BaseEntity tmpEntity = generateBaseEntity(batch, ((MetaClass) metaType), index);

                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), tmpEntity));
            }
            else
            {
                    MetaValue metaValue = (MetaValue) metaType;
                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), getCastObject(metaValue.getTypeCode())));
            }
        }

        return entity;
    }


}
