package kz.bsbnb.usci.core.service.impl;

import kz.bsbnb.usci.core.service.IGlobalService;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.repository.IEavGlobalRepository;
import kz.bsbnb.usci.eav.util.IGlobal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by maksat on 7/29/15.
 */
@Service
public class GlobalServiceImpl implements IGlobalService {

    @Autowired
    private IEavGlobalRepository eavGlobalRepository;

    @Override
    public EavGlobal getGlobal(IGlobal global) {
        return eavGlobalRepository.getGlobal(global);
    }

    @Override
    public EavGlobal getGlobal(Long id) {
        return eavGlobalRepository.getGlobal(id);
    }

    @Override
    public void update(String type, String code, String value){
        eavGlobalRepository.update(type, code, value);
    }

    @Override
    public void updateValue(EavGlobal global) {
        eavGlobalRepository.update(global.getType(), global.getCode(), global.getValue());
    }

    @Override
    public String getValue(String type, String code){
        return eavGlobalRepository.getValue(type, code);
    }

    @Override
    public String getValueFromDb(String queueSetting, String queueLoadEnabled) {
        return "false";
    }
}
