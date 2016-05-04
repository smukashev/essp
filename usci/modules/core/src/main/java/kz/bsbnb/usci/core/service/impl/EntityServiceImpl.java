package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.util.Errors;
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
import org.apache.commons.lang.StringUtils;
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
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    private IRefProcessorDao refProcessorDao;

    @Autowired
    private SQLQueriesStats stats;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    private IBatchService batchService;

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

            stats.put("process(" + mockEntity.getMeta().getClassName() + ")", t2);

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
                    entityStatus.setDescription(StatusProperties.getSpecificParams(mockEntity));
                    entityStatus.setErrorCode(Errors.compose(Errors.E195));
                    entityStatus.setDevDescription(Errors.checkLength(error));
                    entityStatus.setIndex(mockEntity.getBatchIndex() - 1);
                    entityStatus.setReceiptDate(new Date());

                    batchService.addEntityStatus(entityStatus);
                }
            } else {
                EntityStatus entityStatus = new EntityStatus();
                entityStatus.setBatchId(mockEntity.getBatchId());
                entityStatus.setEntityId(-1);
                entityStatus.setStatus(EntityStatuses.ERROR);
                entityStatus.setDescription(StatusProperties.getSpecificParams(mockEntity));

                String[] params = e.getMessage().split(Errors.SEPARATOR);
                if (params[0].length() > 4) {
                    entityStatus.setErrorCode(Errors.checkLength(params[0]));
                } else {
                    entityStatus.setErrorCode(params[0]);
                    if (params.length > 1)
                        entityStatus.setDevDescription(StringUtils.join(Arrays.copyOfRange(params, 1, params.length), Errors.SEPARATOR.replaceAll("\\\\","")));
                }

                entityStatus.setIndex(mockEntity.getBatchIndex() - 1);
                entityStatus.setReceiptDate(new Date());

                batchService.addEntityStatus(entityStatus);
            }
        }
    }

    @Override
    public BaseEntity load(long id) {
        return (BaseEntity) baseEntityLoadDao.load(id);
    }

    @Override
    public BaseEntity load(long id, Date date) {
        return (BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, date);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        return refProcessorDao.getRefListResponse(metaClassId, date, withHis);
    }

    @Override
    public Map<String, QueryEntry> getSQLStats() {
        return stats.getStats();
    }

    @Override
    public RefListResponse getRefListApprox(long metaClassId) {
        return refProcessorDao.getRefListApprox(metaClassId);
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        return refProcessorDao.getRefColumns(metaClassId);
    }
}
