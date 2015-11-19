package kz.bsbnb.usci.eav.persistance.dao;

import kz.bsbnb.usci.eav.model.EavGlobal;

import java.util.List;

public interface IEavGlobalDao {
    Long insert(EavGlobal eavGlobal);

    void update(EavGlobal eavGlobal);

    void delete(Long id);

    EavGlobal get(String type, String code);

    EavGlobal get(Long id);

    List<EavGlobal> getAll();

    void update(String type, String code, String value);

    String getValue(String type, String code);
}
