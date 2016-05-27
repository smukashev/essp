package kz.bsbnb.usci.eav.persistance.dao;

import org.jooq.Select;

import java.util.List;
import java.util.Map;

public interface ISQLGenerator {
    List<Map<String, Object>> getSimpleResult(long metaId, boolean onlyKey);

    Select getSimpleSelect(long metaId, boolean onlyKey);
}
