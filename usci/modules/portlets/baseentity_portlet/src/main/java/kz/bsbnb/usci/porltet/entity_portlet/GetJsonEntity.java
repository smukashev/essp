package kz.bsbnb.usci.porltet.entity_portlet;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author abukabayev
 */
public class GetJsonEntity {

    public BaseEntityJson getBaseEntity(MetaClass metaClass,BaseEntity baseEntity){
        BaseEntityJson entity = new BaseEntityJson();

        for (String name : metaClass.getMemberNames()) {
            IMetaType metaType = metaClass.getMemberType(name);
            BaseValue baseValue = (BaseValue) baseEntity.getBaseValue(name);

            if(metaType.isComplex()) {
                if(metaType.isSet()) {
                    BaseSetJson baseSet = getBaseSet((MetaSet) metaType);
                    entity.put(metaClass,name, new BaseValueJson(getBatch(baseValue.getBatch()), baseValue.getIndex(), (Date) baseValue.getRepDate(), baseSet));
                } else {
//                    BaseEntity tmpEntity = generateBaseEntity(batch, ((MetaClass) metaType), index);
//                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), tmpEntity));
                }
            } else {
                if(metaType.isSet()) {
//                    BaseSet baseSet = generateBaseSet(batch, (MetaSet)metaType, index);
//                    entity.put(name, new BaseValue(batch, index, batch.getRepDate(), baseSet));
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
                    entity.put(metaClass, name, new BaseValueJson(getBatch(baseValue.getBatch()), baseValue.getIndex(), (Date) baseValue.getRepDate(), baseValue.getValue()));
                }
            }
        }

        return entity;
    }

    public BaseSetJson getBaseSet(MetaSet metaSet){

        IMetaType metaTypeChild = metaSet.getMemberType();
        BaseSetJson baseSetJson = new BaseSetJson(metaSet.getMemberType());


//        if (metaTypeChild.isSet()) {
//
//            if (metaTypeChild.isComplex()) {
//                MetaSet metaSetChild = (MetaSet)metaTypeChild;
//                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
//                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
//                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseSetChild));
//                }
//            } else {
//                MetaSet metaSetChild = (MetaSet)metaTypeChild;
//                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
//                    BaseSet baseSetChild = generateBaseSet(batch, metaSetChild, index);
//                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseSetChild));
//                }
//            }
//        } else {
//            if (metaSet.isComplex()) {
//                logger.debug("Generating values for complex set.");
//                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++) {
//                    BaseEntity baseEntity = generateBaseEntity(batch, (MetaClass) metaTypeChild, index);
//                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(), baseEntity));
//                }
//            } else {
//
//                for(int i = 0; i < MIN_ARRAY_ELEMENTS + rand.nextInt(MAX_ARRAY_ELEMENTS); i++)
//                    baseSet.put(new BaseValue(batch, index, batch.getRepDate(),
//                            getCastObject(metaSet.getTypeCode())));
//            }
//        }


        return  baseSetJson;
    }

    public BatchJson getBatch(Batch batch){
       BatchJson batchJson = new BatchJson();
        batchJson.setId(batch.getId());
        batchJson.setReceiptDate((Timestamp) batch.getReceiptDate());
        batchJson.setRepDate((Date) batch.getRepDate());
       return batchJson;
    }
}
