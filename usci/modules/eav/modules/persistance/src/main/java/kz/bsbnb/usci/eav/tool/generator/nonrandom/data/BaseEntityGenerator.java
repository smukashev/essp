package kz.bsbnb.usci.eav.tool.generator.nonrandom.data;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.tool.generator.random.data.AbstractDataGenerator;

import java.util.Date;

/**
 * @author abukabayev
 */

public class BaseEntityGenerator  extends AbstractDataGenerator {
    public BaseEntity generateBaseEntity(Batch batch, MetaClass metaClass, long index) {
        // TODO: Implement generation of the reporting date.
        BaseEntity entity = new BaseEntity(metaClass, new Date());

        for (String name : metaClass.getMemberNames()) {
            IMetaType metaType = metaClass.getMemberType(name);

            if(metaType.isComplex()) {
                BaseEntity tmpEntity = generateBaseEntity(batch, ((MetaClass) metaType), index);
                entity.put(name, new BaseValue(0, batch.getRepDate(), tmpEntity));
            } else {
                MetaValue metaValue = (MetaValue) metaType;
                entity.put(name, new BaseValue(0, batch.getRepDate(), getCastObject(metaValue.getTypeCode())));
            }
        }
        return entity;
    }
}