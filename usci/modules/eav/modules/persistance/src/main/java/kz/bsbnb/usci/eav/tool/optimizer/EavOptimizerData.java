package kz.bsbnb.usci.eav.tool.optimizer;

import kz.bsbnb.usci.eav.model.persistable.impl.Persistable;

public class EavOptimizerData extends Persistable {
    private final long metaId;
    private final long entityId;
    private final String keyString;

    public EavOptimizerData(long metaId, long entityId, String keyString) {
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

    public String getKeyString() {
        return keyString;
    }

    @Override
    public String toString() {
        return getKeyString();
    }
}
