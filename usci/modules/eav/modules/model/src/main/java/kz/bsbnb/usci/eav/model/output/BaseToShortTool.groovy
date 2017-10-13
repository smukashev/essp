package kz.bsbnb.usci.eav.model.output

import kz.bsbnb.usci.eav.model.base.IBaseEntity
import kz.bsbnb.usci.eav.model.base.IBaseEntityReportDate
import kz.bsbnb.usci.eav.model.base.IBaseSet
import kz.bsbnb.usci.eav.model.base.IBaseValue
import kz.bsbnb.usci.eav.model.meta.*
import kz.bsbnb.usci.eav.model.persistable.IPersistable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Created by emles on 09.09.17
 */
class BaseToShortTool {

    protected static Logger logger = LoggerFactory.getLogger(BaseToShortTool.class)


    protected List<Closure> filters = []

    void addFilter(Closure closure) {
        filters.add(closure)
    }

    protected tab = "  "
    protected nl = "\n"

    class NodeInfo {
        Boolean list = true
        Integer level
        String path
        TObject node
    }

    def treeListsSearch = { int level = 0, String path = "", final List<NodeInfo> map ->
        if (this_.doFilters()) return
        def info = NodeInfo.newInstance(level: level, path: (path = "$path.$name<${persistable?.getId()}>"), node: this_)
        map.add(info)
        if (level >= 2) return
        childs.each { TObject child ->
            if (child instanceof TEntity || child instanceof TSet) {
                info.list = false
                child.treeListsSearch(level + 1, path, map)
            }
        }
    }

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

        protected Boolean filterFlag = null

        boolean doFilters() {
            if (filterFlag == null) {
                filterFlag = false
                for (Closure closure : filters) {
                    if (!closure.call(name: name, meta: meta, attribute: attribute, persistable: persistable)) {
                        filterFlag = true
                        break
                    }
                }
            }
            return filterFlag
        }

        void toString(Integer level = 0, StringBuffer buffer) {

            for (def i = 0; i < level; i++) {
                buffer.append tab
            }
            if (baseValue && (attribute?.isKey() || attribute?.isOptionalKey()) && (meta.isComplex() || meta.isSet())) {
                buffer.append "${attribute?.getName()} /ID ${baseValue.getId()}/ >> "
            }
            buffer.append "$name /ID ${persistable ? persistable?.getId() : ""} ${meta?.isComplex() ? "COM" : "SIM"} ${meta?.isSet() ? "SET" : "ATR"}${persistable instanceof IBaseEntityReportDate ? " TRepDate" : ""}/"

        }

    }

    class TEntity extends TObject {

        IMetaClass metaClazz

        IBaseEntity baseEntity

        List<TObject> childs

        TEntity this_

        TEntity(String name, IBaseValue baseValue, IMetaAttribute attribute, IMetaClass metaClazz, IBaseEntity baseEntity, List<TObject> childs) {
            super(name, attribute, (IMetaType) metaClazz, (IPersistable) baseEntity, baseValue)
            this.metaClazz = metaClazz
            this.baseEntity = baseEntity
            this.childs = childs
            this_ = this
        }

        void headToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(baseEntity?.reportDate))
                    .append(" RD ")
            buffer.append(dateFormat.format(baseEntity?.baseEntityReportDate?.reportDate))
                    .append(" ")
            buffer.append(baseEntity?.baseEntityReportDate?.closed)
            if (false) {
                buffer.append(" BV ")
                if (baseValue != null && baseValue.repDate != null) buffer.append(dateFormat.format(baseValue?.repDate))
                        .append(" ")
                buffer.append(baseValue?.closed)
            }
            buffer.append(nl)

        }

        void childsToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            childs?.each { TObject child -> if (child) child.toString(level + 1, buffer) }

        }

        void toString(Integer level = 0, StringBuffer buffer) {

            headToString(level, buffer)

            childsToString(level, buffer)

        }

    }

    class TSet extends TObject {

        IMetaAttribute attribute
        IMetaSet metaSet

        IBaseSet baseSet

        List<TObject> childs

        TSet this_

        TSet(String name, IBaseValue baseValue, IMetaAttribute attribute, IMetaSet metaSet, IBaseSet baseSet, List<TObject> childs) {
            super(name, attribute, (IMetaType) metaSet, (IPersistable) baseSet, baseValue)
            this.attribute = attribute
            this.metaSet = metaSet
            this.baseSet = baseSet
            this.childs = childs
            this_ = this
        }

        void headToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(baseValue ? dateFormat.format(baseValue.repDate) : "")
                    .append(nl)

        }

        void childsToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            childs?.each { TObject child -> if (child) child.toString(level + 1, buffer) }

        }

        void toString(Integer level = 0, StringBuffer buffer) {

            headToString(level, buffer)

            childsToString(level, buffer)

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

        void headToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(baseValue?.repDate))
                    .append(nl)

        }

        void childsToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            for (def i = 0; i < level + 1; i++) {
                buffer.append tab
            }
            buffer.append("[").append(value).append("]").append(nl)

        }

        void toString(Integer level = 0, StringBuffer buffer) {

            headToString(level, buffer)

            childsToString(level, buffer)

        }

    }

    {
        TEntity.metaClass.treeListsSearch = treeListsSearch
        TSet.metaClass.treeListsSearch = treeListsSearch
    }

    class TReportDate extends TObject {

        IBaseEntityReportDate reportDate
        IBaseEntity baseEntity

        TReportDate(String name, IBaseEntityReportDate reportDate, IBaseEntity baseEntity, IMetaType metaType) {
            super(name, null, metaType, reportDate, null)
            this.reportDate = reportDate
            this.baseEntity = baseEntity
        }

        void headToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

            super.toString(level, buffer)
            buffer
                    .append(" ")
                    .append(dateFormat.format(reportDate?.getReportDate()))
                    .append(nl)

        }

        void childsToString(Integer level = 0, StringBuffer buffer) {

            if (super.doFilters()) return

        }

        void toString(Integer level = 0, StringBuffer buffer) {

            headToString(level, buffer)

            childsToString(level, buffer)

        }

    }


    TObject getBase(Map params) {

        StringBuffer buffer = params.buffer
        IBaseValue baseValue = params.baseValue
        String name = params.name
        IPersistable persistable = params.persistable
        IMetaAttribute metaAttribute = params.metaAttribute
        IMetaType metaType = params.metaType
        IBaseEntityReportDate reportDate = params.reportDate

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
                            persistable: baseEntity?.getBaseValue(attrName),
                            metaAttribute: baseEntity?.getMetaAttribute(attrName),
                            metaType: baseEntity?.getMemberType(attrName)
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
                if (!metaType) metaType = baseValue?.getMetaAttribute()?.getMetaType()

                if (baseValue.getValue() instanceof IPersistable)
                    return getBase(
                            buffer: buffer,
                            name: baseValue?.getMetaAttribute()?.getName(),
                            baseValue: baseValue,
                            persistable: (IPersistable) baseValue?.getValue(),
                            metaAttribute: baseValue?.getMetaAttribute(),
                            metaType: baseValue?.getMetaAttribute()?.getMetaType()
                    )
                else {
                    return new TValue(name, baseValue, metaAttribute, (IMetaValue) metaType, baseValue.getValue())
                }

                break

            case IBaseEntityReportDate:

                reportDate = (IBaseEntityReportDate) persistable

                return new TReportDate(reportDate?.baseEntity?.meta?.className, reportDate, reportDate?.baseEntity, reportDate?.baseEntity?.meta)

                break

            default:

                try {
                    if (persistable) println "NO sach CLASS: <${persistable}> <${persistable?.class}> <${persistable?.class?.name}>"
                } catch (e) {
                }

                break

        }

    }

    static synchronized String print(IBaseEntity baseEntity) {
        Closure filter = { Map binding ->
            return !(binding.meta?.isReference() && !(binding.attribute?.isKey() || binding.attribute?.isOptionalKey()))
        }
        BaseToShortTool tool = new BaseToShortTool()
        tool.addFilter filter
        StringBuffer buffer = new StringBuffer("")
        try {
            tool.getBase(persistable: baseEntity).toString(0, buffer)
        } catch (Exception e) {
            logger.error("", e)
        }
        return buffer.toString()
    }

    static synchronized void print(StringBuffer buffer, IPersistable persistable) {
        Closure filter = { Map binding ->
            return !(binding.meta?.isReference() && !(binding.attribute?.isKey() || binding.attribute?.isOptionalKey()))
        }
        BaseToShortTool tool = new BaseToShortTool()
        tool.addFilter filter
        try {
            tool.getBase(persistable: persistable).toString(0, buffer)
        } catch (Exception e) {
            logger.error("", e)
        }
    }

    static synchronized void print(Map<String, String> map, IPersistable persistable) {
        final List<NodeInfo> list = new ArrayList<>()
        Closure filter = { Map binding ->
            return !(binding.meta?.isReference() && !(binding.attribute?.isKey() || binding.attribute?.isOptionalKey()))
        }
        BaseToShortTool tool = new BaseToShortTool()
        tool.addFilter filter
        try {
            tool.getBase(persistable: persistable).treeListsSearch(list)
            list.each { NodeInfo info ->
                StringBuffer buffer = new StringBuffer("")
                if (info.list)
                    info.node.toString(info.level, buffer)
                else
                    info.node.headToString(info.level, buffer)
                map.put(info.path, buffer.toString())
            }
        } catch (Exception e) {
            logger.error("", e)
        }
    }

}



