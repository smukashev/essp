package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IBatchService;
import kz.bsbnb.usci.core.service.IEntityService;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.model.EntityStatus;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.exceptions.KnownException;
import kz.bsbnb.usci.eav.model.exceptions.KnownIterativeException;
import kz.bsbnb.usci.eav.persistance.dao.*;
import kz.bsbnb.usci.eav.model.stats.QueryEntry;
import kz.bsbnb.usci.eav.model.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.EntityStatuses;
import kz.bsbnb.usci.tool.status.StatusProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

@Service
public class EntityServiceImpl extends UnicastRemoteObject implements IEntityService {
    private final Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);

    @Qualifier("baseEntityProcessor")
    @Autowired
    private IBaseEntityProcessorDao baseEntityProcessorDao;

    @Qualifier("rulesStatelessProcessor")
    @Autowired
    private IBaseEntityProcessorDao ruleProcessorDao;

    @Autowired
    private IRefProcessorDao refProcessorDao;

    @Autowired
    private SQLQueriesStats stats;

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    private IBatchService batchService;

    @Autowired
    private IBaseEntityReportDateDao baseEntityReportDateDao;

    public EntityServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void process(BaseEntity mockEntity) {
        long t1 = System.currentTimeMillis();
        try {
            BaseEntity baseEntity = (BaseEntity) baseEntityProcessorDao.process(mockEntity);
            baseEntity.setBatchId(mockEntity.getBatchId());
            baseEntity.setIndex(mockEntity.getBatchIndex());

            stats.put("java::process", (System.currentTimeMillis() - t1));

            EntityStatus entityStatus = new EntityStatus();
            entityStatus.setBatchId(baseEntity.getBatchId());
            entityStatus.setEntityId(baseEntity.getId());
            entityStatus.setStatus(EntityStatuses.COMPLETED);
            entityStatus.setDescription(StatusProperties.getSpecificParams(baseEntity));
            entityStatus.setIndex(baseEntity.getBatchIndex());
            entityStatus.setReceiptDate(new Date());

            batchService.addEntityStatus(entityStatus);
        } catch (Exception e) {
            stats.put("java::process_error", (System.currentTimeMillis() - t1));

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

                if (e.getMessage() != null) {
                    String[] params = e.getMessage().split(Errors.SEPARATOR);
                    if (params[0].length() > 4) {
                        entityStatus.setErrorCode(Errors.checkLength(params[0]));
                    } else {
                        entityStatus.setErrorCode(params[0]);
                        if (params.length > 1)
                            entityStatus.setDevDescription(Errors.checkLength(
                                    StringUtils.join(Arrays.copyOfRange(params, 1, params.length),
                                    Errors.SEPARATOR.replaceAll("\\\\", ""))));
                    }
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
    public BaseEntity loadForDisplay(long entityId, Date reportDate) {
        BaseEntity baseEntity = (BaseEntity) baseEntityLoadDao.loadByMaxReportDate(entityId, reportDate);
        baseEntityProcessorDao.prepareClosedDates(baseEntity, baseEntity.getBaseEntityReportDate().getCreditorId());
        return baseEntity;
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        return refProcessorDao.getRefColumns(metaClassId);
    }

    @Override
    public List<String> getValidationErrors(IBaseEntity baseEntity) {

        List<String> errors = new ArrayList<>();

        try {
            ruleProcessorDao.process(baseEntity);
        } catch (Exception e) {
            if(e instanceof KnownIterativeException) {
                for(String s : ((KnownIterativeException) e).getMessages()) {
                    errors.add(s);
                }

                return errors;
            }

            throw e;

        }

        return errors;
    }

    @Override
    public Date getPreviousReportDate(long entityId, Date reportDate) {
        return baseEntityReportDateDao.getPreviousReportDate(entityId, reportDate);
    }
}
