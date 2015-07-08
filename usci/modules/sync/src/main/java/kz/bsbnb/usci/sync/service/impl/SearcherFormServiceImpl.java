package kz.bsbnb.usci.sync.service.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.util.Pair;
import kz.bsbnb.usci.sync.service.ISearcherFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

/**
 * @author b.makhambetov
 */
@Service
public class SearcherFormServiceImpl implements ISearcherFormService {
    @Autowired
    @Qualifier(value = "remoteSearcherFormService")
    RmiProxyFactoryBean rmiProxyFactoryBean;

    kz.bsbnb.usci.core.service.form.ISearcherFormService remoteSearcherFormService;

    @PostConstruct
    public void init() {
        remoteSearcherFormService = (kz.bsbnb.usci.core.service.form.ISearcherFormService) rmiProxyFactoryBean.getObject();
    }

    @Override
    public List<String[]> getMetaClasses(long userId) {
        return remoteSearcherFormService.getMetaClasses(userId);
    }

    @Override
    public String getDom(Long userId, String search, IMetaClass metaClass) {
        return remoteSearcherFormService.getDom(userId, search, metaClass);
    }

    @Override
    public List<BaseEntity> search(String searchClassName, HashMap<String, String> parameters, MetaClass metaClass) {
        return remoteSearcherFormService.search(searchClassName, parameters, metaClass);
    }
}
