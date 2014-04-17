package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.Batch;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IShowCaseDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.jooq.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_VALUES;

/**
 * Created by a.tkachenko on 4/8/14.
 */
public class ShowCaseDaoImpl extends JDBCSupport implements IShowCaseDao
{
    private final Logger logger = LoggerFactory.getLogger(ShowCaseDaoImpl.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public ShowCase load(long id)
    {
        Select select = context
                .select(EAV_SC_SHOWCASES.TITLE,
                        EAV_SC_SHOWCASES.TABLE_NAME,
                        EAV_SC_SHOWCASES.NAME)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.ID.equal(id));


        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for ShowCase return more than one row.");
        }

        if (rows.size() < 1)
        {
            throw new RuntimeException("ShowCase not found.");
        }

        Map<String, Object> row = rows.iterator().next();

        ShowCase showCase = new ShowCase();

        showCase.setId(id);
        showCase.setName((String)row.get(EAV_SC_SHOWCASES.NAME.getName()));
        showCase.setTitle((String) row.get(EAV_SC_SHOWCASES.TITLE.getName()));
        showCase.setTableName((String) row.get(EAV_SC_SHOWCASES.TABLE_NAME.getName()));

        select = context
                .select(EAV_SC_SHOWCASE_FIELDS.ID,
                        EAV_SC_SHOWCASE_FIELDS.TITLE,
                        EAV_SC_SHOWCASE_FIELDS.NAME,
                        EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_NAME,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH)
                .from(EAV_SC_SHOWCASE_FIELDS)
                .where(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID.equal(showCase.getId()));

        logger.debug(select.toString());
        rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            for (Map<String, Object> curRow : rows) {
                ShowCaseField showCaseField = new ShowCaseField();

                showCaseField.setName((String)row.get(EAV_SC_SHOWCASE_FIELDS.NAME.getName()));
                showCaseField.setTitle((String) row.get(EAV_SC_SHOWCASE_FIELDS.TITLE.getName()));
                showCaseField.setColumnName((String) row.get(EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME.getName()));
                showCaseField.setAttributeName((String) row.get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_NAME.getName()));
                showCaseField.setAttributePath((String) row.get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH.getName()));
                showCaseField.setAttributeId(((BigDecimal) row
                        .get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID.getName())).longValue());
                showCaseField.setId(((BigDecimal) row
                        .get(EAV_SC_SHOWCASE_FIELDS.ID.getName())).longValue());

                showCase.addField(showCaseField);
            }
        }

        return showCase;
    }

    public long getIdByName(String name)
    {
        Select select = context
                .select(EAV_SC_SHOWCASES.ID)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.NAME.equal(name));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
        {
            throw new RuntimeException("Query for ShowCase return more than one row.");
        }

        if (rows.size() < 1)
        {
            return 0;
        }

        Map<String, Object> row = rows.iterator().next();

        return ((BigDecimal) row
                .get(EAV_SC_SHOWCASES.ID.getName())).longValue();
    }

    @Override
    @Transactional
    public long save(ShowCase showCaseForSave)
    {
        if (showCaseForSave.getId() < 0) {
            showCaseForSave.setId(getIdByName(showCaseForSave.getName()));
        }

        if (showCaseForSave.getId() < 0) {
            return insert(showCaseForSave);
        } else {
            update(showCaseForSave);
            return showCaseForSave.getId();
        }
    }

    @Override
    public void remove(ShowCase showCase)
    {
        throw new RuntimeException("Unimplemented");
    }

    private long insertField(ShowCaseField showCaseField, long showCaseId) {
        Insert insert = context
                .insertInto(EAV_SC_SHOWCASE_FIELDS)
                .set(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID, showCaseField.getAttributeId())
                .set(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_NAME, showCaseField.getAttributeName())
                .set(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH, showCaseField.getAttributePath())
                .set(EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME, showCaseField.getColumnName())
                .set(EAV_SC_SHOWCASE_FIELDS.NAME, showCaseField.getName())
                .set(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID, showCaseId)
                .set(EAV_SC_SHOWCASE_FIELDS.TITLE, showCaseField.getTitle());

        logger.debug(insert.toString());
        long showCaseFieldId =  insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        showCaseField.setId(showCaseFieldId);

        return showCaseFieldId;
    }

    private long insert(ShowCase showCase) {
        Insert insert = context
                .insertInto(EAV_SC_SHOWCASES)
                .set(EAV_SC_SHOWCASES.NAME, showCase.getName())
                .set(EAV_SC_SHOWCASES.TABLE_NAME, showCase.getTableName())
                .set(EAV_SC_SHOWCASES.TITLE, showCase.getTitle());

        logger.debug(insert.toString());
        long showCaseId =  insertWithId(insert.getSQL(), insert.getBindValues().toArray());
        showCase.setId(showCaseId);

        for (ShowCaseField showCaseField : showCase.getFieldsList()) {
            insertField(showCaseField, showCase.getId());
        }

        return showCaseId;
    }

    private void update(ShowCase showCase) {
        if (showCase.getId() < 1) {
            throw new IllegalArgumentException("UPDATE couldn't be done without ID.");
        }

        ShowCase showCaseLoaded = load(showCase.getId());

        String tableAlias = "sc";
        Update update = context
                .update(EAV_SC_SHOWCASES.as(tableAlias))
                .set(EAV_SC_SHOWCASES.as(tableAlias).NAME, showCase.getName())
                .set(EAV_SC_SHOWCASES.as(tableAlias).TABLE_NAME, showCase.getTableName())
                .set(EAV_SC_SHOWCASES.as(tableAlias).TITLE, showCase.getTitle())
                .where(EAV_SC_SHOWCASES.as(tableAlias).as(tableAlias).ID.equal(showCase.getId()));

        logger.debug(update.toString());
        int count = updateWithStats(update.getSQL(), update.getBindValues().toArray());
        if (count != 1)
        {
            throw new RuntimeException("UPDATE operation should be update only one record.");
        }

        //TODO: add fields dao calls here
    }
}
