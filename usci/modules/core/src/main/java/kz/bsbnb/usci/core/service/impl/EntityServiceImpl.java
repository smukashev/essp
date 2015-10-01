package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
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
    IBatchService batchService;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    public EntityServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void process(BaseEntity baseEntity) {
        try {
            long t1 = System.currentTimeMillis();
            BaseEntity entity = (BaseEntity) baseEntityProcessorDao.process(baseEntity);
            long t2 = System.currentTimeMillis() - t1;

            entity.setBatchId(baseEntity.getBatchId());
            entity.setIndex(baseEntity.getBatchIndex());

            stats.put("coreService", t2);

            EntityStatus entityStatus = new EntityStatus();
            entityStatus.setBatchId(entity.getBatchId());
            entityStatus.setEntityId(entity.getId());
            entityStatus.setStatus(EntityStatuses.COMPLETED);
            entityStatus.setDescription("" + entity.getId());
            entityStatus.setIndex(entity.getBatchIndex());
            entityStatus.setReceiptDate(new Date());

            Map<String, String> params = StatusProperties.getSpecificParams(baseEntity);

            Long entityStatusId = batchService.addEntityStatus(entityStatus);
            batchService.addEntityStatusParams(entityStatusId, params);
        } catch (Exception e) {
            String log = "Batch id: " + baseEntity.getBatchId() + ", Index: " + (baseEntity.getBatchIndex() - 1);

            if (!(e instanceof IllegalStateException || e instanceof UnsupportedOperationException))
                log += "\n" + ExceptionUtils.getStackTrace(e);

            logger.error(log);

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
        long creditorId = 0L;

        if (baseEntity.getMeta().getClassName().equals("credit"))
            creditorId = ((BaseEntity) baseEntity.getEl("creditor")).getId();

        ArrayList<Long> result = searcherPool.getSearcher(baseEntity.getMeta().getClassName()).
                findAll(baseEntity, creditorId);
        if (result.size() > 0)
            baseEntity.setId(result.get(0));
        return baseEntity;
    }

    @Override
    public BaseEntity prepare(BaseEntity baseEntity, long creditorId) {
        baseEntityProcessorDao.prepare(baseEntity, creditorId);
        return baseEntity;
    }

    @Override
    public BaseEntity getActualBaseEntity(BaseEntity baseEntity) {

        if(baseEntity.getId() < 1)
            throw new IllegalArgumentException(baseEntity.getMeta().getClassTitle() + " не найден");

        IBaseEntityReportDateDao baseEntityReportDateDao =
                persistableDaoPool.getPersistableDao(BaseEntityReportDate.class, IBaseEntityReportDateDao.class);

        Date maxReportDate = baseEntityReportDateDao.getMaxReportDate(baseEntity.getId(), baseEntity.getReportDate());

        if(maxReportDate == null)
            throw new UnsupportedOperationException("maxReportDate not found");

        BaseEntity baseEntityLoaded
                = (BaseEntity)baseEntityLoadDao.load(baseEntity.getId(), maxReportDate, baseEntity.getReportDate());

        return baseEntityLoaded;
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
