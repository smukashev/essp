package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.dao.IRefProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService {
    private final Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IRefProcessorDao refProcessorDao;

    @Autowired
    IBaseEntitySearcherPool searcherPool;

    @Autowired
    IMetaClassDao metaClassDao;

    @Autowired
    SQLQueriesStats stats;

    @Autowired
    IMailDao mailDao;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IEntityService entityService;

    @Autowired
    IBatchService batchService;

    public EntityServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void process(BaseEntity baseEntity) {
        try {
            long t1 = System.currentTimeMillis();
            BaseEntity entity = (BaseEntity) baseEntityProcessorDao.process(baseEntity);
            long t2 = System.currentTimeMillis() - t1;

            stats.put("coreService", t2);

            EntityStatus entityStatus = new EntityStatus();
            entityStatus.setBatchId(baseEntity.getBatchId());
            entityStatus.setEntityId(baseEntity.getId());
            entityStatus.setStatus(EntityStatuses.COMPLETED);
            entityStatus.setDescription("" + entity.getId());
            entityStatus.setIndex(entity.getBatchIndex() - 1);
            entityStatus.setReceiptDate(new Date());

            Map<String, String> params = StatusProperties.getSpecificParams(baseEntity);

            Long entityStatusId = batchService.addEntityStatus(entityStatus);
            batchService.addEntityStatusParams(entityStatusId, params);

        } catch (Exception e) {
            logger.error("Batch id: " + baseEntity.getBatchId() + ", index: " + (baseEntity.getBatchIndex() - 1) +
                    ExceptionUtils.getStackTrace(e));

            EntityStatus entityStatus = new EntityStatus();
            entityStatus.setBatchId(baseEntity.getBatchId());
            entityStatus.setEntityId(baseEntity.getId());
            entityStatus.setStatus(EntityStatuses.ERROR);
            entityStatus.setDescription(e.getMessage());
            entityStatus.setIndex(baseEntity.getBatchIndex() - 1);
            entityStatus.setReceiptDate(new Date());

            Map<String, String> params = StatusProperties.getSpecificParams(baseEntity);

            Long entityStatusId = batchService.addEntityStatus(entityStatus);
            batchService.addEntityStatusParams(entityStatusId, params);
        }
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
        ArrayList<Long> result = searcherPool.getSearcher(baseEntity.getMeta().getClassName()).findAll(baseEntity);
        if (result.size() > 0)
            baseEntity.setId(result.get(0));
        return baseEntity;
    }

    @Override
    public List<Long> getEntityIDsByMetaclass(long id) {
        return baseEntityProcessorDao.getEntityIDsByMetaclass(id);
    }

    @Override
    public BaseEntity load(long id) {
        System.out.println("Load with id: " + id);
        return (BaseEntity) baseEntityLoadDao.load(id);
    }

    @Override
    public BaseEntity load(long id, Date date) {
        return (BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, date);
    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        return refProcessorDao.getRefsByMetaclass(metaClassId);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        return refProcessorDao.getRefListResponse(metaClassId, date, withHis);
    }

    @Override
    public HashMap<String, QueryEntry> getSQLStats() {
        return stats.getStats();
    }

    @Override
    public void clearSQLStats() {
        stats.clear();
    }

    @Override
    public void remove(long id) {
        baseEntityProcessorDao.remove(id);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds) {
        return baseEntityProcessorDao.getChildBaseEntityIds(parentBaseEntityIds);
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        return refProcessorDao.getRefColumns(metaClassId);
    }
}
