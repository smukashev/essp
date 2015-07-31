package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.util.IGlobal;

/**
 * Created by maksat on 7/29/15.
 */
public interface IEavGlobalRepository {

    EavGlobal getGlobal(String type, String code);

    EavGlobal getGlobal(IGlobal global);

    EavGlobal getGlobal(Long id);

}
