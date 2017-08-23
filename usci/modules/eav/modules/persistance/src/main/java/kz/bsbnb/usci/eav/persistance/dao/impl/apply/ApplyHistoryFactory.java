package kz.bsbnb.usci.eav.persistance.dao.impl.apply;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.IMetaValue;
import kz.bsbnb.usci.eav.persistance.dao.IBaseValueDao;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by emles on 22.08.17
 */
public class ApplyHistoryFactory {

    @Autowired
    protected IPersistableDaoPool persistableDaoPool;

    protected long creditorId;
    protected IBaseEntity baseEntityApplied;
    protected IBaseValue baseValueSaving;
    protected IBaseValue baseValueLoaded;
    protected IBaseEntityManager baseEntityManager;

    protected IMetaAttribute metaAttribute;
    protected IMetaType metaType;
    protected IMetaValue metaValue;

    protected IBaseValueDao valueDao;

    public ApplyHistoryFactory(long creditorId,
                               IBaseEntity baseEntityApplied, IBaseValue baseValueSaving, IBaseValue baseValueLoaded,
                               IBaseEntityManager baseEntityManager) {

        this.creditorId = creditorId;
        this.baseEntityApplied = baseEntityApplied;
        this.baseValueSaving = baseValueSaving;
        this.baseValueLoaded = baseValueLoaded;
        this.baseEntityManager = baseEntityManager;

        this.metaAttribute = baseValueSaving.getMetaAttribute();
        this.metaType = metaAttribute.getMetaType();
        this.metaValue = (IMetaValue) metaType;

        valueDao = persistableDaoPool
                .getPersistableDao(baseValueSaving.getClass(), IBaseValueDao.class);

    }

    public InitializingBaseValueDSLFactory initialize() {
        return new InitializingBaseValueDSLFactory(this).initialize();
    }

    public HistoricalBaseValueDSLFactory existing(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromExisting(value);
    }

    public HistoricalBaseValueDSLFactory closed(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromClosed(value);
    }

    public HistoricalBaseValueDSLFactory previous(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromPrevious(value);
    }

    public HistoricalBaseValueDSLFactory next(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromNext(value);
    }

    public HistoricalBaseValueDSLFactory last(IBaseValue value) {
        return new HistoricalBaseValueDSLFactory(this).fromLast(value);
    }

}



