package kz.bsbnb.usci.tool.data.impl;

import kz.bsbnb.usci.eav.model.BaseEntity;
import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.metadata.DataTypes;
import kz.bsbnb.usci.eav.model.metadata.type.IMetaType;
import kz.bsbnb.usci.eav.model.metadata.type.impl.*;
import kz.bsbnb.usci.tool.data.AbstractGenerator;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author k.tulbassiyev
 */
public class BaseEntityGenerator  extends AbstractGenerator
{
    public BaseEntity generateBaseEntity(Batch batch, MetaClass metaClass, long index)
    {
        BaseEntity entity = new BaseEntity(metaClass, batch);

        for (String name : metaClass.getMemberNames())
        {
            IMetaType metaType = metaClass.getMemberType(name);

            if(metaType.isComplex())
            {
                if(metaType.isArray())
                {
                    MetaClassArray metaClassArray = (MetaClassArray) metaType;

                    for(int i = 0; i < rand.nextInt(10); i++)
                    {
                        BaseEntity tmpEntity = generateBaseEntity(batch,
                                metaClassArray.getMembersType().getMeta(), index);

                        entity.addToArray(name, index, tmpEntity);
                    }
                }
                else
                {
                    MetaClassHolder metaClassHolder = (MetaClassHolder) metaType;

                    BaseEntity tmpEntity = generateBaseEntity(batch, metaClassHolder.getMeta(), index);

                    entity.set(name, index, tmpEntity);
                }
            }
            else
            {
                if(metaType.isArray())
                {
                    MetaValueArray metaValueArray = (MetaValueArray) metaType;

                    entity.addToArray(name, index, getCastObject(metaValueArray.getTypeCode(), index));
                }
                else
                {
                    MetaValue metaValue = (MetaValue) metaType;

                    entity.set(name, index, getCastObject(metaValue.getTypeCode(), index));
                }
            }
        }

        return entity;
    }

    public Object getCastObject(DataTypes typeCode, long index)
    {
        switch(typeCode)
        {
            case INTEGER:
                return Integer.valueOf(rand.nextInt());
            case DATE:
                return new Date();
            case STRING:
                return "string_" + index;
            case BOOLEAN:
                return rand.nextBoolean();
            case DOUBLE:
                return rand.nextDouble();
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }
}
