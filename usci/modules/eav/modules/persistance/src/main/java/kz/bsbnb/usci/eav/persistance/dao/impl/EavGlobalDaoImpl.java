package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.EavGlobal;
import kz.bsbnb.usci.eav.persistance.dao.IEavGlobalDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.Insert;
import org.jooq.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_GLOBAL;

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
            throw new RuntimeException("Операция должна была обновить 1 запись. Былог обновлено " + count +
                    " записей;");
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
            throw new RuntimeException("Операция должна была удалить 1 запись. Было удалено " + count +
                    " записей;");
    }

    @Override
    public EavGlobal get(String type, String code) {
        return null;
    }
}
