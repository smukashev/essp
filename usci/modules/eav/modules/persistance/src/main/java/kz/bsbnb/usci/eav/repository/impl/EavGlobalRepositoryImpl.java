package kz.bsbnb.usci.eav.repository.impl;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.repository.IEavGlobalRepository;
import kz.bsbnb.usci.eav.util.IGlobal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * Created by maksat on 7/29/15.
 */
@Repository
public class EavGlobalRepositoryImpl implements IEavGlobalRepository {

    private HashMap<String, EavGlobal> cache = new HashMap<>();

    @Autowired
    private IEavGlobalDao eavGlobalDao;

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

}