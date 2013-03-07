package kz.bsbnb.usci.eav_persistance.tool.generator.data.impl;

import kz.bsbnb.usci.eav_model.model.Batch;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav_model.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav_model.model.meta.IMetaType;
import kz.bsbnb.usci.eav_model.model.meta.impl.*;
import kz.bsbnb.usci.eav_persistance.tool.generator.data.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author k.tulbassiyev
 */
public class BaseEntityGenerator  extends AbstractDataGenerator
{
    private final Logger logger = LoggerFactory.getLogger(BaseEntityGenerator.class);
    private final int MAX_ARRAY_ELEMENTS = 20;
    private final int MIN_ARRAY_ELEMENTS = 3;

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
                    BaseSet baseSet = generateBaseSet(batch, (MetaSet)metaType, index);
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
                    BaseSet baseSet = generateBaseSet(batch, (MetaSet)metaType, index);
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

    private BaseSet generateBaseSet(Batch batch, MetaSet metaSet, long index) {
        IMetaType metaTypeChild = metaSet.getMemberType();
        BaseSet baseSet = new BaseSet(metaSet.getMemberType());

        if (metaTypeChild.isArray())
        {
            logger.debug("Generating values set of the sets.");
            if (metaTypeChild.isComplex())
            {
                MetaSet metaSetChild = (MetaSet)metaTypeChild;
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                {
                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
                    baseSet.put(new BaseValue(batch, index, baseSetChild));
                }
            }
            else
            {
                MetaSet metaSetChild = (MetaSet)metaTypeChild;
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                {
                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
                    baseSet.put(new BaseValue(batch, index, baseSetChild));
                }
            }
        }
        else
        {
            if (metaSet.isComplex())
            {
                logger.debug("Generating values for complex set.");
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                {
                    BaseEntity baseEntity = generateBaseEntity(batch, (MetaClass) metaTypeChild, index);
                    baseSet.put(new BaseValue(batch, index, baseEntity));
                }
            }
            else
            {
                logger.debug("Generating values for simple set.");
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                    baseSet.put(new BaseValue(batch, index, getCastObject(metaSet.getTypeCode())));
            }
        }
        return baseSet;
    }
}
