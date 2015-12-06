package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.tool.optimizer.EavOptimizerData;

public interface IEavOptimizerDao {
    long insert (EavOptimizerData eavOptimizerData);

    long find (String keyString);

    long find (Long entityId);

    void delete(long baseEntityId);

    void update(EavOptimizerData eavOptimizerData);
}
