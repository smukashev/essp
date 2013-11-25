package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.BatchEntry;

import java.util.List;

public interface IBatchEntryService
{
    public long save(BatchEntry batchEntry);
    public BatchEntry load(long batchEntryId);
    public List<BatchEntry> getListByUser(long userId);
}
