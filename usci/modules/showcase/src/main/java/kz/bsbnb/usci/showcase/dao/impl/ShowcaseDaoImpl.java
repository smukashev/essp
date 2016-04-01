package kz.bsbnb.usci.showcase.dao.impl;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.model.*;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.showcase.dao.CommonDao;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

import static kz.bsbnb.usci.showcase.generated.Tables.*;

@Component
public class ShowcaseDaoImpl extends CommonDao implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(ShowcaseDaoImpl.class);

    private JdbcTemplate jdbcTemplateSC;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private IMetaFactoryService metaService;

    @Autowired
    public void setDataSourceSC(DataSource dataSourceSC) {
        this.jdbcTemplateSC = new JdbcTemplate(dataSourceSC);
    }

    public DataSource getDataSourceSc() {
        return jdbcTemplateSC.getDataSource();
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        showCases = populateShowCases();
    }

    public List<ShowCase> getShowCases() {
        return showCases;
    }

    public ShowCase getHolderByClassName(String className) {
        for (ShowCase showCase : showCases) {
            if (showCase.getMeta().getClassName().equals(className))
                return showCase;
        }

        throw new UnknownError("showCase with name: " + className + " not found");
    }

    public void reloadCache() {
        showCases = populateShowCases();
    }

    private ArrayList<ShowCase> populateShowCases() {
        ArrayList<ShowCase> showCases = new ArrayList<>();
        List<Long> list;

        Select select = context.select(EAV_SC_SHOWCASES.ID)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.PARENT_ID.equal(0L));

        list = jdbcTemplateSC.queryForList(select.getSQL(), Long.class, select.getBindValues().toArray());

        for (Long id : list) {
            ShowCase showCase = load(id);
            showCases.add(showCase);
        }

        return showCases;
    }

    /* Creates both actual & history tables for !isFinal() */
    public void createTables(ShowCase showCase) {
        createTable(HistoryState.ACTUAL, showCase);

        if (!showCase.isFinal())
            createTable(HistoryState.HISTORY, showCase);

        getShowCases().add(showCase);
    }

    /* Creates table for showcase with history state */
    private void createTable(HistoryState historyState, ShowCase showCase) {
        String tableName;

        if (historyState == HistoryState.ACTUAL) {
            tableName = getActualTableName(showCase);
        } else {
            tableName = getHistoryTableName(showCase);
        }

        Database model = new Database();
        model.setName("model");

        Table table = new Table();
        table.setName(tableName);
        //table.setDescription();

        Column idColumn = new Column();
        idColumn.setName("ID");
        idColumn.setPrimaryKey(true);
        idColumn.setRequired(true);
        idColumn.setType("NUMERIC");
        idColumn.setSize("14,0");
        idColumn.setAutoIncrement(true);

        table.addColumn(idColumn);

        Column entityIdColumn = new Column();
        entityIdColumn.setName(showCase.getRootClassName().toUpperCase() + "_ID");
        entityIdColumn.setPrimaryKey(false);
        entityIdColumn.setRequired(true);
        entityIdColumn.setType("NUMERIC");
        entityIdColumn.setSize("14,0");
        entityIdColumn.setAutoIncrement(false);

        table.addColumn(entityIdColumn);

        for (ShowCaseField field : showCase.getFieldsList()) {
            Column column = new Column();
            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType = showCase.getActualMeta().getEl(field.getAttributePath());

            if (metaType.isSet() && !metaType.isComplex()) {
                Column simpleArrayColumn = new Column();

                simpleArrayColumn.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase() + "_ID");
                simpleArrayColumn.setPrimaryKey(false);
                simpleArrayColumn.setRequired(false);
                simpleArrayColumn.setType(getDBType(metaType));
                simpleArrayColumn.setSize(getDBSize(metaType));
                simpleArrayColumn.setAutoIncrement(false);

                table.addColumn(simpleArrayColumn);
            }

            column.setType(getDBType(metaType));
            column.setSize(getDBSize(metaType));

            //column.setDefaultValue(xmlReader.getAttributeValue(idx));
            column.setAutoIncrement(false);
            //column.setDescription(xmlReader.getAttributeValue(idx));
            //column.setJavaName(xmlReader.getAttributeValue(idx));

            table.addColumn(column);
        }

        for (ShowCaseField field : showCase.getCustomFieldsList()) {
            Column column = new Column();
            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType;

            if (field.getAttributePath().equals("root")) {
                metaType = metaService.getMetaClass(field.getMetaId());
            } else {
                metaType = metaService.getMetaClass(field.getMetaId()).getEl(field.getAttributePath());
            }

            column.setType(getDBType(metaType));
            column.setSize(getDBSize(metaType));

            //column.setDefaultValue(xmlReader.getAttributeValue(idx));
            column.setAutoIncrement(false);
            //column.setDescription(xmlReader.getAttributeValue(idx));
            //column.setJavaName(xmlReader.getAttributeValue(idx));

            table.addColumn(column);
        }

        Column column = new Column();
        column.setName("CDC");
        column.setPrimaryKey(false);
        column.setRequired(false);
        column.setType("DATE");
        table.addColumn(column);

        if (!showCase.isFinal()) {
            column = new Column();
            column.setName("OPEN_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);

            column = new Column();
            column.setName("CLOSE_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);
        } else {
            column = new Column();
            column.setName("REP_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);
        }

        for (Index index : showCase.getIndexes())
            table.addIndex(index);

        model.addTable(table);

        Platform platform = PlatformFactory.createNewPlatformInstance(jdbcTemplateSC.getDataSource());
        platform.createModel(model, false, true);
    }

    private String getDBType(IMetaType type) {
        if (type.isComplex())
            return "NUMERIC";

        if (type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();

        if (type.isSet())
            throw new IllegalArgumentException(Errors.compose(Errors.E277, type.toString()));

        MetaValue metaValue = (MetaValue) type;

        switch (metaValue.getTypeCode()) {
            case INTEGER:
                return "NUMERIC";
            case DATE:
                return "DATE";
            case STRING:
                return "VARCHAR";
            case BOOLEAN:
                return "NUMERIC";
            case DOUBLE:
                return "NUMERIC";
            default:
                throw new IllegalArgumentException(Errors.compose(Errors.E276));
        }
    }

    private String getDBSize(IMetaType type) {
        if (type.isComplex())
            return "14, 0";

        if (type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();

        if (type.isSet())
            throw new IllegalArgumentException(Errors.compose(Errors.E275));

        MetaValue metaValue = (MetaValue) type;

        switch (metaValue.getTypeCode()) {
            case INTEGER:
                return "14,0";
            case DATE:
                return null;
            case STRING:
                return "1024";
            case BOOLEAN:
                return "1";
            case DOUBLE:
                return "17,3";
            default:
                throw new IllegalArgumentException(Errors.compose(Errors.E276));
        }
    }

    private void loadShowCase(ShowCase showCase) {
        Select select = context
                .select(EAV_SC_SHOWCASES.ID,
                        EAV_SC_SHOWCASES.PARENT_ID,
                        EAV_SC_SHOWCASES.TABLE_NAME,
                        EAV_SC_SHOWCASES.NAME,
                        EAV_SC_SHOWCASES.CLASS_NAME,
                        EAV_SC_SHOWCASES.DOWN_PATH,
                        EAV_SC_SHOWCASES.IS_FINAL,
                        EAV_SC_SHOWCASES.IS_CHILD)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.ID.equal(showCase.getId()));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E278));

        if (rows.size() < 1)
            throw new RuntimeException(Errors.compose(Errors.E279));

        Map<String, Object> row = rows.iterator().next();

        showCase.setParenId(((BigDecimal) row.get(EAV_SC_SHOWCASES.PARENT_ID.getName())).longValue());
        showCase.setName((String) row.get(EAV_SC_SHOWCASES.NAME.getName()));
        showCase.setTableName((String) row.get(EAV_SC_SHOWCASES.TABLE_NAME.getName()));
        showCase.setDownPath((String) row.get(EAV_SC_SHOWCASES.DOWN_PATH.getName()));
        showCase.setFinal((row.get(EAV_SC_SHOWCASES.IS_FINAL.getName())).toString().equals("1"));
        showCase.setChild((row.get(EAV_SC_SHOWCASES.IS_CHILD.getName())).toString().equals("1"));

        String metaClassName = (String) row.get(EAV_SC_SHOWCASES.CLASS_NAME.getName());
        MetaClass metaClass = metaService.getMetaClass(metaClassName);
        showCase.setMeta(metaClass);

        select = context
                .select(EAV_SC_SHOWCASE_FIELDS.ID,
                        EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH,
                        EAV_SC_SHOWCASE_FIELDS.TYPE)
                .from(EAV_SC_SHOWCASE_FIELDS)
                .where(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID.equal(showCase.getId()));

        logger.debug(select.toString());

        rows = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            for (Map<String, Object> curRow : rows) {
                ShowCaseField showCaseField = new ShowCaseField();

                showCaseField.setColumnName((String) curRow.get(EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME.getName()));

                showCaseField.setType(((BigDecimal) curRow.get(EAV_SC_SHOWCASE_FIELDS.TYPE.getName())).intValue());

                showCaseField.setAttributePath((String) curRow.
                        get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH.getName()));

                showCaseField.setAttributeId(((BigDecimal) curRow
                        .get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID.getName())).longValue());

                showCaseField.setId(((BigDecimal) curRow
                        .get(EAV_SC_SHOWCASE_FIELDS.ID.getName())).longValue());

                if (showCaseField.getType() == ShowCaseField.ShowCaseFieldTypes.CUSTOM) {
                    showCase.addCustomField(showCaseField);
                } else if (showCaseField.getType() == ShowCaseField.ShowCaseFieldTypes.ROOT_KEY) {
                    showCase.addRootKeyField(showCaseField);
                } else if (showCaseField.getType() == ShowCaseField.ShowCaseFieldTypes.HISTORY_KEY) {
                    showCase.addHistoryKeyField(showCaseField);
                } else {
                    showCase.addField(showCaseField);
                }
            }
        }
    }

    public ShowCase load(long id) {
        ShowCase showCase = new ShowCase();
        showCase.setId(id);

        loadShowCase(showCase);

        Select select = context
                .select(EAV_SC_SHOWCASES.ID,
                        EAV_SC_SHOWCASES.PARENT_ID,
                        EAV_SC_SHOWCASES.TABLE_NAME,
                        EAV_SC_SHOWCASES.NAME,
                        EAV_SC_SHOWCASES.CLASS_NAME,
                        EAV_SC_SHOWCASES.DOWN_PATH,
                        EAV_SC_SHOWCASES.IS_FINAL,
                        EAV_SC_SHOWCASES.IS_CHILD)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.PARENT_ID.equal(id));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray());

        for (Map<String, Object> row : rows) {
            ShowCase childShowCase = new ShowCase();
            Long childShowCaseId = ((BigDecimal) row.get(EAV_SC_SHOWCASES.ID.getName())).longValue();
            childShowCase.setId(childShowCaseId);
            loadShowCase(childShowCase);

            showCase.addChildShowCase(childShowCase);
        }

        return showCase;
    }

    public ShowCase load(String name) {
        Long id = getIdByName(name);
        return load(id);
    }

    private long getIdByName(String name) {
        Select select = context.select(EAV_SC_SHOWCASES.ID).from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.NAME.equal(name));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(),
                select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException(Errors.compose(Errors.E278));

        if (rows.size() < 1)
            return 0;

        Map<String, Object> row = rows.iterator().next();

        return ((BigDecimal) row
                .get(EAV_SC_SHOWCASES.ID.getName())).longValue();
    }

    private long insertField(ShowCaseField showCaseField, long showCaseId) {
        Insert insert = context
                .insertInto(EAV_SC_SHOWCASE_FIELDS)
                .set(EAV_SC_SHOWCASE_FIELDS.META_ID, showCaseField.getMetaId())
                .set(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID, showCaseField.getAttributeId())
                .set(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH, showCaseField.getAttributePath())
                .set(EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME, showCaseField.getColumnName())
                .set(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID, showCaseId)
                .set(EAV_SC_SHOWCASE_FIELDS.TYPE, showCaseField.getType());

        logger.debug(insert.toString());

        long showCaseFieldId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        showCaseField.setId(showCaseFieldId);

        return showCaseFieldId;
    }

    public long insert(ShowCase showCase) {
        Insert insert = context
                .insertInto(EAV_SC_SHOWCASES)
                .set(EAV_SC_SHOWCASES.PARENT_ID, 0L)
                .set(EAV_SC_SHOWCASES.NAME, showCase.getName())
                .set(EAV_SC_SHOWCASES.TABLE_NAME, showCase.getTableName())
                .set(EAV_SC_SHOWCASES.CLASS_NAME, showCase.getMeta().getClassName())
                .set(EAV_SC_SHOWCASES.DOWN_PATH, showCase.getDownPath())
                .set(EAV_SC_SHOWCASES.IS_FINAL, showCase.isFinal() ? 1 : 0)
                .set(EAV_SC_SHOWCASES.IS_CHILD, showCase.isChild() ? 1 : 0);

        logger.debug(insert.toString());

        long showCaseId = insertWithId(insert.getSQL(), insert.getBindValues().toArray());

        showCase.setId(showCaseId);

        for (ShowCaseField sf : showCase.getFieldsList()) insertField(sf, showCaseId);

        for (ShowCaseField sf : showCase.getCustomFieldsList()) insertField(sf, showCaseId);

        for (ShowCaseField sf : showCase.getRootKeyFieldsList()) insertField(sf, showCaseId);

        for (ShowCaseField sf : showCase.getHistoryKeyFieldsList()) insertField(sf, showCaseId);

        for (ShowCase csc : showCase.getChildShowCases()) {
            Insert childInsert = context.insertInto(EAV_SC_SHOWCASES)
                    .set(EAV_SC_SHOWCASES.PARENT_ID, showCaseId)
                    .set(EAV_SC_SHOWCASES.NAME, csc.getName())
                    .set(EAV_SC_SHOWCASES.TABLE_NAME, csc.getTableName())
                    .set(EAV_SC_SHOWCASES.CLASS_NAME, csc.getMeta().getClassName())
                    .set(EAV_SC_SHOWCASES.DOWN_PATH, csc.getDownPath())
                    .set(EAV_SC_SHOWCASES.IS_FINAL, csc.isFinal() ? 1 : 0)
                    .set(EAV_SC_SHOWCASES.IS_CHILD, csc.isChild() ? 1 : 0);

            logger.debug(childInsert.toString());

            long childShowCaseId = insertWithId(childInsert.getSQL(), childInsert.getBindValues().toArray());

            for (ShowCaseField sf : csc.getFieldsList()) insertField(sf, childShowCaseId);

            for (ShowCaseField sf : csc.getCustomFieldsList()) insertField(sf, childShowCaseId);

            for (ShowCaseField sf : csc.getRootKeyFieldsList()) insertField(sf, childShowCaseId);

            for (ShowCaseField sf : csc.getHistoryKeyFieldsList()) insertField(sf, childShowCaseId);
        }

        return showCaseId;
    }

    private long insertWithId(String query, Object[] values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateSC.update(new GenericInsertPreparedStatementCreator(query, values), keyHolder);
        return keyHolder.getKey().longValue();
    }
}
