package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.repository.IEavGlobalRepository;
import kz.bsbnb.usci.eav.util.IGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public class EavGlobalRepositoryImpl implements IEavGlobalRepository, InitializingBean {

    private HashMap<String, EavGlobal> cache = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(EavGlobalRepositoryImpl.class);

    @Autowired
    private IEavGlobalDao eavGlobalDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<EavGlobal> eavGlobalList = eavGlobalDao.getAll();

        for (EavGlobal eavGlobal : eavGlobalList)
            cache.put(eavGlobal.getType() + "___" + eavGlobal.getCode(), eavGlobal);
    }

    @Override
    public EavGlobal getGlobal(String type, String code) {
        String key = type + "___" + code;

        EavGlobal eavGlobal = cache.get(key);

        if (eavGlobal == null) {
            eavGlobal = eavGlobalDao.get(type, code);
            cache.put(key, eavGlobal);
        }

        return eavGlobal;
    }

    @Override
    public EavGlobal getGlobal(IGlobal global) {
        return getGlobal(global.type(), global.code());
    }

    @Override
    public EavGlobal getGlobal(Long id) {
        return eavGlobalDao.get(id);
    }

    @Override
    public void update(String type, String code, String value){
        eavGlobalDao.update(type, code, value);
        cache.remove(type + "___" + code);
    }

    @Override
    public String getValue(String type, String code){
        try {
            return getGlobal(type, code).getValue();
        } catch (Exception e) {
            logger.warn("global ("+type + "," + code + ") not found: " + e.getMessage() );
            return null;
        }
    }
}
