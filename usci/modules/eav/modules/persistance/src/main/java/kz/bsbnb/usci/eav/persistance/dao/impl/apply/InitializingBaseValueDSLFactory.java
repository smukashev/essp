package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
public class InitializingBaseValueDSLFactory extends BaseValueDSLFactory {

    protected IMetaType metaType;

    public InitializingBaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        super(memorizingTool);
        this.parent = true;
        this.attribute = true;
    }

    public InitializingBaseValueDSLFactory initialize() {
        return this;
    }

    public InitializingBaseValueDSLFactory from(IBaseValue from) {
        this.from = from;
        return this;
    }

    public InitializingBaseValueDSLFactory from(IBaseValue from, Boolean closed, Boolean last) {
        this.from = from;
        this.closed = closed;
        this.last = last;
        return this;
    }

    public InitializingBaseValueDSLFactory from(Long id, Date date, Boolean closed, Boolean last) {
        this.id = id;
        this.date = date;
        this.closed = closed;
        this.last = last;
        return this;
    }

    public BaseValueDSLFactory metaType(IMetaType metaType) {
        this.metaType = metaType;
        return this;
    }

    public InitializingBaseValueDSLFactory clone() throws CloneNotSupportedException {

        InitializingBaseValueDSLFactory cloneValue = new InitializingBaseValueDSLFactory(memorizingTool).initialize();

        cloneValue.from = this.from;

        cloneValue.id = this.id;
        cloneValue.date = this.date;
        cloneValue.closed = this.closed;
        cloneValue.last = this.last;

        cloneValue.parent = this.parent;
        cloneValue.attribute = this.attribute;
        cloneValue.persistence = this.persistence;

        return cloneValue;

    }

    public InitializingBaseValueDSLFactory execute() {

        result = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                metaType == null
                        ? memorizingTool.metaType
                        : metaType,
                id == null
                        ? (from == null ? 0L : from.getId())
                        : id,
                memorizingTool.creditorId,
                new Date((
                        date == null
                                ? (from == null ? new Date() : from.getRepDate())
                                : date
                ).getTime()),
                value == null
                        ? (from == null ? null : memorizingTool.castedValue(from))
                        : value,
                closed == null
                        ? from != null && from.isClosed()
                        : closed,
                last == null
                        ? from != null && from.isLast()
                        : last);

        super.execute();

        return this;

    }

    public IBaseValue result() {
        if (result == null) this.execute();
        return result;
    }

}



