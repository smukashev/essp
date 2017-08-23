package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.type.DataTypes;

import java.util.Date;

/**
 * Created by emles on 22.08.17
 */
public class InitializingBaseValueDSLFactory extends BaseValueDSLFactory {

    private Long id;
    private Date date;

    public InitializingBaseValueDSLFactory(ApplyHistoryFactory memorizingTool) {
        super(memorizingTool);
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

    public InitializingBaseValueDSLFactory id(Long id) {
        this.id = id;
        return this;
    }

    public InitializingBaseValueDSLFactory date(Date date) {
        this.date = date;
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

    public InitializingBaseValueDSLFactory create() {

        value = BaseValueFactory.create(
                MetaContainerTypes.META_CLASS,
                memorizingTool.metaType,
                id == null
                        ? (from == null ? 0L : from.getId())
                        : id,
                memorizingTool.creditorId,
                new Date((
                        date == null
                                ? (from == null ? new Date() : from.getRepDate())
                                : date
                ).getTime()),
                castedValue(memorizingTool.metaValue, memorizingTool.baseValueSaving),
                closed == null
                        ? from != null && from.isClosed()
                        : closed,
                last == null
                        ? from != null && from.isLast()
                        : last);

        super.create();

        return this;

    }

    public IBaseValue value() {
        if (value == null) this.create();
        return value;
    }

    private Object castedValue(IMetaValue metaValue, IBaseValue baseValue) {
        return metaValue.getTypeCode() == DataTypes.DATE
                ? new Date(((Date) baseValue.getValue()).getTime())
                : baseValue.getValue();
    }

}



