package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.BatchEntry;

import java.util.List;

public interface IBatchEntryService
{
    long save(BatchEntry batchEntry);
    BatchEntry load(long batchEntryId);
    List<BatchEntry> getListByUser(long userId);
    void delete(long batchEntryId);
}
