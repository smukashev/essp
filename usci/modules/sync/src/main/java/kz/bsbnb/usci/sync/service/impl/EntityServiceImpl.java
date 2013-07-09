package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.sync.job.impl.DataJob;
import kz.bsbnb.usci.sync.service.IEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jws.Oneway;
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

    @PostConstruct
    public void init() {
        remoteEntityService  = (kz.bsbnb.usci.core.service.IEntityService) rmiProxyFactoryBean.getObject();
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
    public void update(BaseEntity baseEntitySave, BaseEntity baseEntityLoad) {
        remoteEntityService.update(baseEntitySave,baseEntityLoad);
    }

    @Override
    public BaseEntity search(BaseEntity baseEntity) {
        return remoteEntityService.search(baseEntity);
    }
}
