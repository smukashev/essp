package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.RefListItem;
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

    @Autowired
    @Qualifier(value = "remoteEntityService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.IEntityService remoteEntityService;

    @Autowired
    SyncStatusSingleton syncStatusSingleton;

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
        System.out.println("Load with id: " + id);
        return remoteEntityService.load(id);
    }

    @Override
    public BaseEntity load(long id, Date date) {
        return remoteEntityService.load(id, date);
    }

    @Override
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {
        remoteEntityService.update(baseEntitySave, baseEntityLoad);
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
        return remoteEntityService.search(baseEntity);
    }

    @Override
    public List<Long> getEntityIDsByMetaclass(long id) {
        return remoteEntityService.getEntityIDsByMetaclass(id);
    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        return remoteEntityService.getRefsByMetaclass(metaClassId);
    }

    @Override
    public int getQueueSize() {
        return dataJob.getQueueSize();
    }

    @Override
    public void setThreadsCount(int threadsCount, boolean allowAutoIncrement) {
        dataJob.setCurrentThread(threadsCount);
        dataJob.setAutoChooseThreshold(allowAutoIncrement);
    }

    @Override
    public SyncStatus getStatus() {
        return syncStatusSingleton.getStatus();
    }

    @Override
    public void remove(long id) {
        remoteEntityService.remove(id);
    }
}
