package kz.bsbnb.usci.eav.tool.generator.data.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.*;
import kz.bsbnb.usci.eav.tool.generator.data.AbstractDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author k.tulbassiyev
 */
public class BaseEntityGenerator  extends AbstractDataGenerator {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityGenerator.class);
    private final int MAX_ARRAY_ELEMENTS = 20;
    private final int MIN_ARRAY_ELEMENTS = 3;

    public BaseEntity generateBaseEntity(Batch batch, MetaClass metaClass, long index) {
        BaseEntity entity = new BaseEntity(metaClass);

        for (String name : metaClass.getMemberNames()) {
            IMetaType metaType = metaClass.getMemberType(name);

            if(metaType.isComplex()) {
                if(metaType.isSet()) {
                    BaseSet baseSet = generateBaseSet(batch, (MetaSet)metaType, index);
                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), baseSet));
                } else {
                    BaseEntity tmpEntity = generateBaseEntity(batch, ((MetaClass) metaType), index);
                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), tmpEntity));
                }
            } else {
                if(metaType.isSet()) {
                    BaseSet baseSet = generateBaseSet(batch, (MetaSet)metaType, index);
                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), baseSet));
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(),
                            getCastObject(metaValue.getTypeCode())));
                }
            }
        }

        return entity;
    }

    private BaseSet generateBaseSet(Batch batch, MetaSet metaSet, long index) {
        IMetaType metaTypeChild = metaSet.getMemberType();
        BaseSet baseSet = new BaseSet(metaSet.getMemberType());

        if (metaTypeChild.isSet()) {
            logger.debug("Generating values set of the sets.");
            if (metaTypeChild.isComplex()) {
                MetaSet metaSetChild = (MetaSet)metaTypeChild;
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseSetChild));
                }
            } else {
                MetaSet metaSetChild = (MetaSet)metaTypeChild;
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseSetChild));
                }
            }
        } else {
            if (metaSet.isComplex()) {
                logger.debug("Generating values for complex set.");
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
                    BaseEntity baseEntity = generateBaseEntity(batch, (MetaClass) metaTypeChild, index);
                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseEntity));
                }
            } else {
                logger.debug("Generating values for simple set.");
                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(),
                            getCastObject(metaSet.getTypeCode())));
            }
        }

        return baseSet;
    }
}
