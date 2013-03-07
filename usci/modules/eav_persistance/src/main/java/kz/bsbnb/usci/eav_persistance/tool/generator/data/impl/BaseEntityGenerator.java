package kz.bsbnb.usci.eav_persistance.tool.generator.data.impl;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.*;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.AbstractDataGenerator;

/**
 * @author k.tulbassiyev
 */
public class BaseEntityGenerator  extends AbstractDataGenerator
{
    private final int MAX_ARRAY_ELEMENTS = 20;
    private final int MIN_ARRAY_ELEMENTS = 5;

    public BaseEntity generateBaseEntity(Batch batch, MetaClass metaClass, long index)
    {
        BaseEntity entity = new BaseEntity(metaClass);

        for (String name : metaClass.getMemberNames())
        {
            IMetaType metaType = metaClass.getMemberType(name);

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {
                    MetaSet metaSet = (MetaSet) metaType;

                    BaseSet baseSet = new BaseSet(metaSet.getMemberType());

                    for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                    {
                        BaseEntity tmpEntity = generateBaseEntity(batch,
                                (MetaClass) metaSet.getMemberType(), index);

                        baseSet.put(new BaseValue(batch, index, tmpEntity));
                    }

                    entity.put(name, new BaseValue(batch, index, baseSet));
                }
                else
                {
                    BaseEntity tmpEntity = generateBaseEntity(batch, ((MetaClass) metaType), index);

                    entity.put(name, new BaseValue(batch, index, tmpEntity));
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaSet metaSet = (MetaSet) metaType;

                    BaseSet baseSet = new BaseSet(metaSet.getMemberType());

                    for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                        baseSet.put(new BaseValue(batch, index, getCastObject(metaSet.getTypeCode())));

                    entity.put(name, new BaseValue(batch, index, baseSet));
                }
                else
                {
                    MetaValue metaValue = (MetaValue) metaType;

                    entity.put(name, new BaseValue(batch, index, getCastObject(metaValue.getTypeCode())));
                }
            }
        }

        return entity;
    }
}
