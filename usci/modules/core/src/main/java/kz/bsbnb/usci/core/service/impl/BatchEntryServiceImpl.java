package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchEntryService;
import kz.bsbnb.usci.eav.model.BatchEntry;
import kz.bsbnb.usci.eav.persistance.dao.IBatchEntriesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchEntryServiceImpl implements IBatchEntryService {
    @Autowired
    private IBatchEntriesDao batchEntriesDao;

    @Override
    public long save(BatchEntry batch) {
        return batchEntriesDao.save(batch);
    }

    @Override
    public BatchEntry load(long batchId) {
        return batchEntriesDao.load(batchId);
    }

    @Override
    public List<BatchEntry> getListByUser(long userId) {
        return batchEntriesDao.getBatchEntriesByUserId(userId);
    }

    @Override
    public void delete(long batchEntryId) {
        BatchEntry batchEntry = new BatchEntry();
        batchEntry.setId(batchEntryId);
        batchEntriesDao.remove(batchEntry);
    }

    @Override
    public void delete(List<Long> batchEntryIds) {
        batchEntriesDao.remove(batchEntryIds);
    }
}
