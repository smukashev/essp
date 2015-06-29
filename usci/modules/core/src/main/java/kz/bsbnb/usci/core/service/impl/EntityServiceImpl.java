package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.json.BatchFullJModel;
import kz.bsbnb.usci.eav.model.json.EntityStatusJModel;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.IMailDao;
import kz.bsbnb.usci.eav.persistance.dao.IMetaClassDao;
import kz.bsbnb.usci.eav.persistance.searcher.pool.IBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.stats.QueryEntry;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.tool.couchbase.EntityStatuses;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusProperties;
import kz.bsbnb.usci.tool.couchbase.singleton.StatusSingleton;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * @author k.tulbassiyev
 */
@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService {
    private final Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);
    @Autowired
    protected StatusSingleton statusSingleton;
    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;
    @Autowired
    IBaseEntitySearcherPool searcherPool;
    @Autowired
    IMetaClassDao metaClassDao;
    @Autowired
    SQLQueriesStats stats;
    @Autowired
    IMailDao mailDao;

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

            EntityStatusJModel entityStatus = new EntityStatusJModel(
                    entity.getBatchIndex() - 1,
                    EntityStatuses.COMPLETED, "" + entity.getId(), new Date());

            StatusProperties.fillSpecificProperties(entityStatus, baseEntity);

            statusSingleton.addContractStatus(baseEntity.getBatchId(), entityStatus);
        } catch (Exception e) {
            logger.error("Batch id: " + baseEntity.getBatchId() + ", index: " + (baseEntity.getBatchIndex() - 1) +
                    ExceptionUtils.getStackTrace(e));

            EntityStatusJModel entityStatus = new EntityStatusJModel(
                    baseEntity.getBatchIndex() - 1,
                    EntityStatuses.ERROR, e.getMessage(), new Date());

            StatusProperties.fillSpecificProperties(entityStatus, baseEntity);

            statusSingleton.addContractStatus(baseEntity.getBatchId(), entityStatus);
        }
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
        ArrayList<Long> result = searcherPool.getSearcher(baseEntity.getMeta().getClassName()).findAll(baseEntity);
        if(result.size() > 0)
            baseEntity.setId(result.get(0));
        return baseEntity;
    }

    @Override
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {
        // TODO: Uncomment and fix
        /*Long id = metaClassDao.save(baseEntityLoad.getMeta());
        baseEntityProcessorDao.saveOrUpdate(baseEntityLoad);*/
    }

    @Override
    public List<Long> getEntityIDsByMetaclass(long id) {
        return baseEntityProcessorDao.getEntityIDsByMetaclass(id);
    }

    @Override
    public BaseEntity load(long id) {
        System.out.println("Load with id: " + id);
        return (BaseEntity) baseEntityProcessorDao.load(id);
    }

    @Override
    public BaseEntity load(long id, Date date) {
        return (BaseEntity) baseEntityProcessorDao.loadByMaxReportDate(id, date);
    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        return baseEntityProcessorDao.getRefsByMetaclass(metaClassId);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        return baseEntityProcessorDao.getRefListResponse(metaClassId, date, withHis);
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
        return baseEntityProcessorDao.getRefColumns(metaClassId);
    }

    @Override
    public void removeAllByMetaClass(IMetaClass metaClass) {
        while (true) {
            long baseEntityId = baseEntityProcessorDao.getRandomBaseEntityId(metaClass);
            if (baseEntityId <= 0) {
                break;
            }

            baseEntityProcessorDao.remove(baseEntityId);
        }
    }
}
