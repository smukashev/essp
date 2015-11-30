package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.persistance.dao.IEavOptimizerDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.tool.optimizer.EavOptimizerData;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_OPTIMIZER;

@Component
public class EavOptimizerDaoImpl extends JDBCSupport implements IEavOptimizerDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public long insert(EavOptimizerData eavOptimizerData) {
        Insert insert = context
                .insertInto(EAV_OPTIMIZER)
                .set(EAV_OPTIMIZER.META_ID, eavOptimizerData.getMetaId())
                .set(EAV_OPTIMIZER.ENTITY_ID, eavOptimizerData.getEntityId())
                .set(EAV_OPTIMIZER.KEY_STRING, eavOptimizerData.getKeyString());

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public long find(String keyString) {
        String tableAlias = "e";
        Select select = context
                .select(EAV_OPTIMIZER.as(tableAlias).ENTITY_ID)
                .from(EAV_OPTIMIZER.as(tableAlias))
                .where(EAV_OPTIMIZER.as(tableAlias).KEY_STRING.equal(keyString));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new IllegalArgumentException("Найдено более одной записи, " + keyString + ";");

        if (rows.size() < 1)
            return 0;

        Map<String, Object> row = rows.get(0);

        return ((BigDecimal) row.get(EAV_OPTIMIZER.as(tableAlias).ENTITY_ID.getName())).longValue();
    }
}
