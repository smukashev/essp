package kz.bsbnb.usci.eav.repository;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.util.IGlobal;

public interface IEavGlobalRepository {
    EavGlobal getGlobal(String type, String code);

    EavGlobal getGlobal(IGlobal global);

    EavGlobal getGlobal(Long id);

    void update(String type, String code, String value);

    String getValue(String type, String code);
}
