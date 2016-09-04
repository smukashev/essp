package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.impl.DataJob;
import kz.bsbnb.usci.sync.service.IEntityService;
import kz.bsbnb.usci.tool.status.SyncStatus;
import kz.bsbnb.usci.tool.status.SyncStatusSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jws.Oneway;
import java.util.Date;
import java.util.List;

/**
 * @author k.tulbassiyev
 */
@Service
public class EntityServiceImpl implements IEntityService {
    @Autowired
    private DataJob dataJob;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "remoteEntityService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    private kz.bsbnb.usci.core.service.IEntityService remoteEntityService;

    @Autowired
    private SyncStatusSingleton syncStatusSingleton;

    @PostConstruct
    public void init() {
        remoteEntityService = (kz.bsbnb.usci.core.service.IEntityService) rmiProxyFactoryBean.getObject();
    }

    @Override
    @Oneway
    public void process(List<BaseEntity> entities) {
        dataJob.addAll(entities);
    }

    @Override
    public BaseEntity load(long id) {
        return remoteEntityService.load(id);
    }

    @Override
    public BaseEntity load(long id, Date date) {
        return remoteEntityService.load(id, date);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        return remoteEntityService.getRefListResponse(metaClassId, date, withHis);
    }

    @Override
    public int getQueueSize() {
        return dataJob.getQueueSize();
    }

    @Override
    public SyncStatus getStatus() {
        return syncStatusSingleton.getStatus();
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        return remoteEntityService.getRefColumns(metaClassId);
    }

    @Override
    public RefListResponse getRefListApprox(long metaClassId) {
        return remoteEntityService.getRefListApprox(metaClassId);
    }

    @Override
    public BaseEntity loadForDisplay(long entityId, Date reportDate) {
        return remoteEntityService.loadForDisplay(entityId, reportDate);
    }
}
