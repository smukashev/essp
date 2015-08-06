package kz.bsbnb.usci.eav.persistance.dao;


import kz.bsbnb.usci.eav.model.BatchStatus;

import java.util.List;

/**
 * Created by maksat on 8/3/15.
 */
public interface IBatchStatusDao {

    Long insert(BatchStatus batchStatus);

    List<BatchStatus> getList(long batchId);

}
