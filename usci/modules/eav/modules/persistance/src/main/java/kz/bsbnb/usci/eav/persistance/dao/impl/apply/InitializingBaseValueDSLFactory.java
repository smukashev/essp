package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
public class InitializingBaseValueDSLFactory extends BaseValueDSLFactory {

    protected Integer containerType;
    protected IMetaType metaType;
    protected IMetaValue metaValue;
    protected Boolean child = false;
    protected Long creditorId;
    protected IBaseValue creditorIdFrom;

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

    public InitializingBaseValueDSLFactory metaType(IMetaType metaType) {
        this.metaType = metaType;
        return this;
    }

    public InitializingBaseValueDSLFactory metaValue(IMetaValue metaValue) {
        this.metaValue = metaValue;
        return this;
    }

    public InitializingBaseValueDSLFactory child(Boolean child) {
        this.child = child;
        return this;
    }

    public InitializingBaseValueDSLFactory containerType(Integer containerType) {
        this.containerType = containerType;
        return this;
    }

    public InitializingBaseValueDSLFactory creditorId(Long creditorId) {
        this.creditorId = creditorId;
        return this;
    }

    public InitializingBaseValueDSLFactory creditorIdFrom(IBaseValue creditorIdFrom) {
        this.creditorIdFrom = creditorIdFrom;
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

        IMetaType mt;
        IMetaValue mv;

        mt = metaType == null
                ? memorizingTool.metaType
                : metaType;

        mv = metaValue == null
                ? memorizingTool.metaValue
                : metaValue;

        if (child) {
            mt = memorizingTool.childMetaType;
            mv = memorizingTool.childMetaValue;
        }

        result = BaseValueFactory.create(
                containerType == null
                        ? MetaContainerTypes.META_CLASS
                        : containerType,
                mt,
                id == null
                        ? (from == null ? 0L : from.getId())
                        : id,
                creditorId == null
                        ? (creditorIdFrom == null ? memorizingTool.creditorId : creditorIdFrom.getCreditorId())
                        : creditorId,
                new Date((
                        date == null
                                ? (from == null ? new Date() : from.getRepDate())
                                : date
                ).getTime()),
                value == null
                        ? (from == null ? null : memorizingTool.castedValue(mv, from))
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
        return (IBaseValue) result;
    }

}



