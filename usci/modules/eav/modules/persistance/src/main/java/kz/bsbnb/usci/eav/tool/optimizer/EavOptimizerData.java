package kz.bsbnb.usci.eav.tool.optimizer;

public class EavOptimizerData {
    private long id;
    private long metaId;
    private long entityId;
    private String keyString;

    public EavOptimizerData(long metaId, long entityId, String keyString) {
        this.metaId = metaId;
        this.entityId = entityId;
        this.keyString = keyString;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMetaId() {
        return metaId;
    }

    public void setMetaId(long metaId) {
        this.metaId = metaId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getKeyString() {
        return keyString;
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }
}
