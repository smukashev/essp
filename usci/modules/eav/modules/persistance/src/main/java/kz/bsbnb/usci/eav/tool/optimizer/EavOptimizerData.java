package kz.bsbnb.usci.eav.tool.optimizer;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class EavOptimizerData extends Persistable {
    private final Long creditorId;
    private final Long metaId;
    private final Long entityId;
    private final String keyString;

    public EavOptimizerData(Long creditorId, Long metaId, Long entityId, String keyString) {
        this.creditorId = creditorId;
        this.metaId = metaId;
        this.entityId = entityId;
        this.keyString = keyString;
    }

    public long getMetaId() {
        return metaId;
    }

    public long getEntityId() {
        return entityId;
    }

    public Long getCreditorId() {
        return creditorId;
    }

    public String getKeyString() {
        return keyString;
    }

    @Override
    public String toString() {
        return getKeyString();
    }
}
