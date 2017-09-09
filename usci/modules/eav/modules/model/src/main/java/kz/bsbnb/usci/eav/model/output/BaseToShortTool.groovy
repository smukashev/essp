package kz.bsbnb.usci.eav.model.output

import kz.bsbnb.usci.eav.model.base.IBaseEntity
import kz.bsbnb.usci.eav.model.base.IBaseSet
import kz.bsbnb.usci.eav.model.base.IBaseValue
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute
import kz.bsbnb.usci.eav.model.meta.IMetaClass
import kz.bsbnb.usci.eav.model.meta.IMetaSet
import kz.bsbnb.usci.eav.model.meta.IMetaType
import kz.bsbnb.usci.eav.model.meta.IMetaValue
import kz.bsbnb.usci.eav.model.persistable.IPersistable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Created by emles on 09.09.17
 */
class BaseToShortTool {

    protected static Logger logger = LoggerFactory.getLogger(BaseToShortTool.class)


    class TObject {

        protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd")

        protected String name
        protected IMetaType meta
        protected IMetaAttribute attribute
        protected IPersistable persistable
        protected IBaseValue baseValue

        TObject(String name, IMetaAttribute attribute, IMetaType metaType, IPersistable persistable, IBaseValue baseValue) {
            this.name = name
            this.attribute = attribute
            this.meta = metaType
            this.persistable = persistable
            this.baseValue = baseValue
        }

        String toString(Integer level = 0, StringBuffer buffer) {

            if (meta?.isReference() && !(attribute?.isKey() || attribute?.isOptionalKey()))
                return

            (0..level).each {
                buffer.append "\t"
            }
            if (baseValue && (attribute?.isKey() || attribute?.isOptionalKey()) && (meta.isComplex() || meta.isSet())) {
                buffer.append "${attribute?.getName()} /ID ${baseValue.getId()}/ >> "
            }
            buffer.append "$name /ID ${persistable ? persistable.getId() : ""} ${meta.isComplex() ? "COM" : "SIM"} ${meta.isSet() ? "SET" : "ATR"}/"

        }

    }

    class TEntity extends TObject {

        IMetaClass metaClazz

        IBaseEntity baseEntity

        List<TObject> childs

        TEntity(String name, IBaseValue baseValue, IMetaAttribute attribute, IMetaClass metaClazz, IBaseEntity baseEntity, List<TObject> childs) {
            super(name, attribute, (IMetaType) metaClazz, (IPersistable) baseEntity, baseValue)
            this.metaClazz = metaClazz
            this.baseEntity = baseEntity
            this.childs = childs
        }

        String toString(Integer level = 0, StringBuffer buffer) {
            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(baseEntity?.getReportDate()))
                    .append("\n")
            childs?.each { TObject child -> if (child) child.toString(level + 1, buffer) }
        }

    }

    class TSet extends TObject {

        IMetaAttribute attribute
        IMetaSet metaSet

        IBaseSet baseSet

        List<TObject> childs

        TSet(String name, IBaseValue baseValue, IMetaAttribute attribute, IMetaSet metaSet, IBaseSet baseSet, List<TObject> childs) {
            super(name, attribute, (IMetaType) metaSet, (IPersistable) baseSet, baseValue)
            this.attribute = attribute
            this.metaSet = metaSet
            this.baseSet = baseSet
            this.childs = childs
        }

        String toString(Integer level = 0, StringBuffer buffer) {
            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(baseValue?.getRepDate()))
                    .append("\n")
            childs?.each { TObject child -> if (child) child.toString(level + 1, buffer) }
        }

    }

    class TValue extends TObject {

        IMetaAttribute attribute
        IMetaValue metaValue

        Object value

        TValue(String name, IBaseValue baseValue, IMetaAttribute attribute, IMetaValue metaValue, Object value) {
            super(name, attribute, (IMetaType) metaValue, (IPersistable) baseValue, baseValue)
            this.baseValue = baseValue
            this.metaValue = metaValue
            this.value = value
        }

        String toString(Integer level = 0, StringBuffer buffer) {
            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(baseValue?.getRepDate()))
                    .append("\n")
            (0..(level + 1)).each {
                buffer.append "\t"
            }
            buffer.append(value).append("\n")
        }

    }


    TObject getBase(Map params) {

        StringBuffer buffer = params.buffer
        IBaseValue baseValue = params.baseValue
        String name = params.name
        IPersistable persistable = params.persistable
        IMetaAttribute metaAttribute = params.metaAttribute
        IMetaType metaType = params.metaType

        switch (persistable) {

            case IBaseEntity:

                IBaseEntity baseEntity = (IBaseEntity) persistable
                if (!metaType) metaType = baseEntity?.getMeta()
                IMetaClass metaClass = (IMetaClass) metaType
                name = metaClass?.getClassName()

                List<TObject> childs = metaClass.getAttributeNames().collect { String attrName ->
                    getBase(
                            buffer: buffer,
                            name: attrName,
                            baseValue: null,
                            persistable: baseEntity.getBaseValue(attrName),
                            metaAttribute: baseEntity.getMetaAttribute(attrName),
                            metaType: baseEntity.getMemberType(attrName)
                    )
                }

                return new TEntity(name, baseValue, metaAttribute, metaClass, baseEntity, childs)

            case IBaseSet:

                IBaseSet baseSet = (IBaseSet) persistable
                IMetaSet metaSet = (IMetaSet) metaType

                List<TObject> childs = baseSet.get().collect { IBaseValue childBaseValue ->
                    getBase(
                            buffer: buffer,
                            baseValue: null,
                            persistable: childBaseValue,
                            metaAttribute: childBaseValue?.getMetaAttribute(),
                            metaType: baseSet?.getMemberType()
                    )
                }

                return new TSet(name, baseValue, metaAttribute, metaSet, baseSet, childs)

            case IBaseValue:

                baseValue = (IBaseValue) persistable
                if (!metaType) metaType = baseValue.getMetaAttribute().getMetaType()

                if (baseValue.getValue() instanceof IPersistable)
                    return getBase(
                            buffer: buffer,
                            name: baseValue.getMetaAttribute()?.getName(),
                            baseValue: baseValue,
                            persistable: (IPersistable) baseValue?.getValue(),
                            metaAttribute: baseValue?.getMetaAttribute(),
                            metaType: baseValue?.getMetaAttribute()?.getMetaType()
                    )
                else {
                    return new TValue(name, baseValue, metaAttribute, (IMetaValue) metaType, baseValue.getValue())
                }

                break

            default:

                break

        }

    }

    static synchronized String print(IBaseEntity baseEntity) {
        StringBuffer buffer = new StringBuffer("")
        try {
            new BaseToShortTool().getBase(persistable: baseEntity).toString(buffer)
        } catch (Exception e) {
            logger.error("", e)
        }
        return buffer.toString()
    }

    static synchronized void print(StringBuffer buffer, IBaseEntity baseEntity) {
        try {
            new BaseToShortTool().getBase(persistable: baseEntity).toString(buffer)
        } catch (Exception e) {
            logger.error("", e)
        }
    }

}



