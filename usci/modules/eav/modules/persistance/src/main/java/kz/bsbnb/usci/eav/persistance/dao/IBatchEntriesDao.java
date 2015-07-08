package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.BatchEntry;

import java.util.List;

public interface IBatchEntriesDao extends IDao<BatchEntry> {
    List<BatchEntry> getBatchEntriesByUserId(long userId);

    void remove(List<Long> batchEntryIds);
}
