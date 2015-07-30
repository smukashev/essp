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

}
