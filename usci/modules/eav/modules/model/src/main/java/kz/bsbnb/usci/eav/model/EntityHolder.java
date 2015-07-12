package kz.bsbnb.usci.eav.model;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

import java.io.Serializable;

public class EntityHolder implements Serializable {
    private IBaseEntity saving;
    private IBaseEntity loaded;
    private IBaseEntity applied;

    public EntityHolder() {
        super();
    }

    public EntityHolder(IBaseEntity saving, IBaseEntity loaded, IBaseEntity applied) {
        this.saving = saving;
        this.loaded = loaded;
        this.applied = applied;
    }

    public IBaseEntity getSaving() {
        return saving;
    }

    public void setSaving(IBaseEntity saving) {
        this.saving = saving;
    }

    public IBaseEntity getLoaded() {
        return loaded;
    }

    public void setLoaded(IBaseEntity loaded) {
        this.loaded = loaded;
    }

    public IBaseEntity getApplied() {
        return applied;
    }

    public void setApplied(IBaseEntity applied) {
        this.applied = applied;
    }
}
