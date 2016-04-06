package kz.bsbnb.usci.eav.showcase;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import java.io.Serializable;

public class QueueEntry implements Serializable {
    private final static long serialVersionUID = 1L;

    private final IBaseEntity baseEntityApplied;

    QueueEntry(final IBaseEntity baseEntityApplied) {
        this.baseEntityApplied = baseEntityApplied;
    }

    public IBaseEntity getBaseEntityApplied() {
        return baseEntityApplied;
    }
}
