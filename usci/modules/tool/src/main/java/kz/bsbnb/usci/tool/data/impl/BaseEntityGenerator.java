package kz.bsbnb.usci.tool.data.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.BaseSet;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.IBaseContainer;
import kz.bsbnb.usci.eav.model.batchdata.impl.BaseValue;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.tool.data.AbstractGenerator;

import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class BaseEntityGenerator  extends AbstractGenerator
{
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

                    for(int i = 0; i < 2 + rand.nextInt(10); i++)
                    {
                        BaseEntity tmpEntity = generateBaseEntity(batch, (MetaClass)metaSet.getMemberType(), index);

                        baseSet.put(new BaseValue(batch, index, tmpEntity));
                    }

                    entity.put(name, new BaseValue(batch, index, baseSet));
                }
                else
                {
                    MetaClass meta = (MetaClass) metaType;

                    BaseEntity tmpEntity = generateBaseEntity(batch, meta, index);

                    entity.put(name, new BaseValue(batch, index, tmpEntity));
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaSet metaSet = (MetaSet) metaType;

                    BaseSet baseSet = new BaseSet(metaSet.getMemberType());

                    for(int i = 0; i < 2 + rand.nextInt(20); i++)
                    {
                        baseSet.put(new BaseValue(batch, index, getCastObject(metaSet.getTypeCode())));
                    }
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

    public Object getCastObject(DataTypes typeCode)
    {
        switch(typeCode)
        {
            case INTEGER:
                return rand.nextInt(1000);
            case DATE:
                return new Date();
            case STRING:
                return "string_" + rand.nextInt(1000);
            case BOOLEAN:
                return rand.nextBoolean();
            case DOUBLE:
                return rand.nextDouble()*10000;
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }
}
