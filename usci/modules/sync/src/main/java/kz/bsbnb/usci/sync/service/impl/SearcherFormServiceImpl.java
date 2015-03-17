package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.util.Pair;
import kz.bsbnb.usci.sync.service.IBatchService;
import kz.bsbnb.usci.sync.service.ISearcherFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author b.makhambetov
 */
@Service
public class SearcherFormServiceImpl implements ISearcherFormService {
    @Autowired
    @Qualifier(value = "remoteSearcherFormService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.ISearcherFormService remoteSearcherFormService;

    @PostConstruct
    public void init() {
        remoteSearcherFormService = (kz.bsbnb.usci.core.service.ISearcherFormService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public List<Pair> getMetaClasses(long userId) {
        return remoteSearcherFormService.getMetaClasses(userId);
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass) {
        return remoteSearcherFormService.getDom(userId, metaClass);
    }
}
