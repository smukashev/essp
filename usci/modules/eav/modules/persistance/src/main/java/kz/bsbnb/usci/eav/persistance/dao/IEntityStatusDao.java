package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.EntityStatus;

import java.util.List;

/**
 * Created by maksat on 8/3/15.
 */
public interface IEntityStatusDao {

    Long insert(EntityStatus entityStatus);

    List<EntityStatus> getList(long batchId);

    List<EntityStatus> getList(long batchId, int firstIndex, int count);

    int getSuccessEntityCount(long batchId);

    int getErrorCount(long id);
}
