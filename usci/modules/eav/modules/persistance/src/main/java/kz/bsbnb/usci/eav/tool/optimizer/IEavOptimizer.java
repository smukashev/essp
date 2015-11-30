package kz.bsbnb.usci.eav.tool.optimizer;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;

public interface IEavOptimizer {
    String getMetaName();

    String getKeyString(IBaseEntity iBaseEntity);
}
