package kz.bsbnb.usci.core.service;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.util.IGlobal;

/**
 * Created by maksat on 7/29/15.
 */
public interface IGlobalService {

    EavGlobal getGlobal(IGlobal global);

    EavGlobal getGlobal(Long id);

}
