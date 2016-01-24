package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntityReportDate;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.exceptions.KnownIterativeException;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.tool.status.StatusProperties;
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
    public void process(BaseEntity mockEntity) {
        try {
            long t1 = System.currentTimeMillis();
            BaseEntity baseEntity = (BaseEntity) baseEntityProcessorDao.process(mockEntity);
            long t2 = System.currentTimeMillis() - t1;

            baseEntity.setBatchId(mockEntity.getBatchId());
            baseEntity.setIndex(mockEntity.getBatchIndex());

            stats.put("core.EntityService.process()", t2);

            EntityStatus entityStatus = new EntityStatus();
            entityStatus.setBatchId(baseEntity.getBatchId());
            entityStatus.setEntityId(baseEntity.getId());
            entityStatus.setStatus(EntityStatuses.COMPLETED);
            entityStatus.setDescription(StatusProperties.getSpecificParams(baseEntity));
            entityStatus.setIndex(baseEntity.getBatchIndex());
            entityStatus.setReceiptDate(new Date());

            batchService.addEntityStatus(entityStatus);
        } catch (Exception e) {
            if (!(e instanceof KnownException))
                logger.error("Батч: " + mockEntity.getBatchId() + ", Индекс: " + (mockEntity.getBatchIndex() - 1)
                        + "\n" + e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));

            if (e instanceof KnownIterativeException) {
                for (String error : ((KnownIterativeException) e).getMessages()) {
                    EntityStatus entityStatus = new EntityStatus();
                    entityStatus.setBatchId(mockEntity.getBatchId());
                    entityStatus.setEntityId(-1);
                    entityStatus.setStatus(EntityStatuses.ERROR);
                    entityStatus.setDescription(StatusProperties.getSpecificParams(mockEntity) + " (" + e.getMessage() + ")");
                    entityStatus.setIndex(mockEntity.getBatchIndex() - 1);
                    entityStatus.setReceiptDate(new Date());

                    batchService.addEntityStatus(entityStatus);
                }
            } else {
                EntityStatus entityStatus = new EntityStatus();
                entityStatus.setBatchId(mockEntity.getBatchId());
                entityStatus.setEntityId(-1);
                entityStatus.setStatus(EntityStatuses.ERROR);
                entityStatus.setDescription(StatusProperties.getSpecificParams(mockEntity) + " (" + e.getMessage() + ")");
                entityStatus.setIndex(mockEntity.getBatchIndex() - 1);
                entityStatus.setReceiptDate(new Date());

                batchService.addEntityStatus(entityStatus);
            }
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
            throw new UnsupportedOperationException("Запись не была найдена в базе; \n" + baseEntity);

        return (BaseEntity)baseEntityLoadDao.load(baseEntity.getId(), maxReportDate, baseEntity.getReportDate());
    }

    @Override
    public List<Long> getEntityIDsByMetaclass(long id) {
        return baseEntityProcessorDao.getEntityIDsByMetaclass(id);
    }

    @Override
    public BaseEntity load(long id) {
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
