package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.IGlobal;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_GLOBAL;

@Repository
public class EavGlobalDaoImpl extends JDBCSupport implements IEavGlobalDao {
    private final Logger logger = LoggerFactory.getLogger(EavGlobalDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public Long insert(EavGlobal eavGlobal) {
        Insert insert = context
                .insertInto(EAV_GLOBAL)
                .set(EAV_GLOBAL.TYPE, eavGlobal.getType())
                .set(EAV_GLOBAL.CODE, eavGlobal.getCode())
                .set(EAV_GLOBAL.VALUE, eavGlobal.getValue())
                .set(EAV_GLOBAL.DESCRIPTION, eavGlobal.getDescription());

        logger.debug(insert.toString());
        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void update(EavGlobal eavGlobal) {
        String tableAlias = "eg";
        Update update = context
                .update(EAV_GLOBAL.as(tableAlias))
                .set(EAV_GLOBAL.as(tableAlias).TYPE, eavGlobal.getType())
                .set(EAV_GLOBAL.as(tableAlias).CODE, eavGlobal.getCode())
                .set(EAV_GLOBAL.as(tableAlias).VALUE, eavGlobal.getValue())
                .set(EAV_GLOBAL.as(tableAlias).DESCRIPTION, eavGlobal.getDescription())
                .where(EAV_GLOBAL.as(tableAlias).ID.equal(eavGlobal.getId()));

        logger.debug(update.toString());

        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new RuntimeException(Errors.E156+"|" + count );
    }

    @Override
    public void delete(Long id) {
        String tableAlias = "eg";
        Delete delete = context
                .delete(EAV_GLOBAL.as(tableAlias))
                .where(EAV_GLOBAL.as(tableAlias).ID.equal(id));

        logger.debug(delete.toString());

        int count = updateWithStats(delete.getSQL(), delete.getBindValues().toArray());

        if (count != 1)
            throw new RuntimeException(Errors.E154+"|" + count);
    }

    @Override
    public EavGlobal get(String type, String code) {
        Select select = context.selectFrom(EAV_GLOBAL)
                .where(EAV_GLOBAL.TYPE.eq(type).and(EAV_GLOBAL.CODE.eq(code)));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            return new EavGlobal(
                    ((BigDecimal) rows.get(0).get(EAV_GLOBAL.ID.getName())).longValue(),
                    (String) rows.get(0).get(EAV_GLOBAL.TYPE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.CODE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.VALUE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.DESCRIPTION.getName())
            );
        }

        return null;
    }

    @Override
    public EavGlobal get(Long id) {
        Select select = context.selectFrom(EAV_GLOBAL)
                .where(EAV_GLOBAL.ID.eq(id));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            return new EavGlobal(
                    ((BigDecimal) rows.get(0).get(EAV_GLOBAL.ID.getName())).longValue(),
                    (String) rows.get(0).get(EAV_GLOBAL.TYPE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.CODE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.VALUE.getName()),
                    (String) rows.get(0).get(EAV_GLOBAL.DESCRIPTION.getName())
            );
        }

        return null;
    }

    @Override
    public List<EavGlobal> getAll() {
        List<EavGlobal> eavGlobals = new ArrayList<>();

        Select select = context.selectFrom(EAV_GLOBAL);

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            EavGlobal eavGlobal = new EavGlobal();
            eavGlobal.setId(((BigDecimal) row.get(EAV_GLOBAL.ID.getName())).longValue());
            eavGlobal.setCode((String) row.get(EAV_GLOBAL.CODE.getName()));
            eavGlobal.setType((String) row.get(EAV_GLOBAL.TYPE.getName()));
            eavGlobal.setValue((String) row.get(EAV_GLOBAL.VALUE.getName()));
            eavGlobal.setDescription((String) row.get(EAV_GLOBAL.DESCRIPTION.getName()));

            eavGlobals.add(eavGlobal);
        }

        return eavGlobals;
    }


    @Override
    public void update(String type, String code, String value){
        Update update = context.update(EAV_GLOBAL)
                .set(EAV_GLOBAL.VALUE, value)
                .where(EAV_GLOBAL.TYPE.eq(type))
                .and(EAV_GLOBAL.CODE.eq(code));

        updateWithStats(update.getSQL(), update.getBindValues().toArray());
    }

    @Override
    public String getValue(String type, String code){
        Select select = context.select(EAV_GLOBAL.VALUE)
                .from(EAV_GLOBAL)
                .where(EAV_GLOBAL.TYPE.eq(type))
                .and(EAV_GLOBAL.CODE.eq(code));

        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            return (String)row.get("VALUE");
        }

        throw new RuntimeException(Errors.E155+"");
    }
}
