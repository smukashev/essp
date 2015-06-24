package kz.bsbnb.usci.showcase.dao.impl;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.model.*;
import kz.bsbnb.ddlutils.model.Table;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.stats.SQLQueriesStats;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.showcase.ShowcaseHolder;
import kz.bsbnb.usci.showcase.dao.ShowcaseDao;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static kz.bsbnb.usci.showcase.generated.Tables.EAV_SC_BAD_ENTITIES;
import static kz.bsbnb.usci.showcase.generated.Tables.EAV_SC_SHOWCASES;
import static kz.bsbnb.usci.showcase.generated.Tables.EAV_SC_SHOWCASE_FIELDS;

@Component
public class ShowcaseDaoImpl implements ShowcaseDao, InitializingBean {
    private final static String TABLES_PREFIX = "R_";
    private final static String COLUMN_PREFIX = "";
    private final static String HISTORY_POSTFIX = "_HIS";
    private final Logger logger = LoggerFactory.getLogger(ShowcaseDaoImpl.class);
    private JdbcTemplate jdbcTemplateSC;
    private ArrayList<ShowcaseHolder> holders;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private SQLQueriesStats stats;

    @Autowired
    private IMetaFactoryService metaService;

    @Autowired
    public void setDataSourceSC(DataSource dataSourceSC) {
        this.jdbcTemplateSC = new JdbcTemplate(dataSourceSC);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        holders = populateHolders();
    }

    public ArrayList<ShowcaseHolder> getHolders() {
        return holders;
    }

    public ShowcaseHolder getHolderByClassName(String className) {
        for (ShowcaseHolder h : holders) {
            if (h.getShowCaseMeta().getMeta().getClassName().equals(className))
                return h;
        }

        throw new UnknownError("ShowcaseHolder with name: " + className + " not found");
    }

    public void reloadCache() {
        holders = populateHolders();
    }

    private ArrayList<ShowcaseHolder> populateHolders() {
        ArrayList<ShowcaseHolder> holders = new ArrayList<>();
        List<Long> list;

        Select select = context.select(EAV_SC_SHOWCASES.ID).from(EAV_SC_SHOWCASES);
        list = jdbcTemplateSC.queryForList(select.getSQL(), Long.class, select.getBindValues().toArray());

        for (Long id : list) {
            ShowCase showcase = load(id);
            ShowcaseHolder holder = new ShowcaseHolder();
            holder.setShowCaseMeta(showcase);
            holders.add(holder);
        }

        return holders;
    }

    public void createTables(ShowcaseHolder showcaseHolder) {
        createTable(HistoryState.ACTUAL, showcaseHolder);

        if(!showcaseHolder.getShowCaseMeta().isFinal())
            createTable(HistoryState.HISTORY, showcaseHolder);

        getHolders().add(showcaseHolder);
    }

    private void createTable(HistoryState historyState, ShowcaseHolder showcaseHolder) {
        String tableName;

        if(historyState == HistoryState.ACTUAL) {
            tableName = getActualTableName(showcaseHolder.getShowCaseMeta());
        } else {
            tableName = getHistoryTableName(showcaseHolder.getShowCaseMeta());
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
        entityIdColumn.setName(showcaseHolder.getRootClassName().toUpperCase() + "_ID");
        entityIdColumn.setPrimaryKey(true);
        entityIdColumn.setRequired(true);
        entityIdColumn.setType("NUMERIC");
        entityIdColumn.setSize("14,0");
        entityIdColumn.setAutoIncrement(true);

        table.addColumn(entityIdColumn);

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            Column column = new Column();
            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType = showcaseHolder.getShowCaseMeta().getActualMeta().getEl(field.getAttributePath());

            column.setType(getDBType(metaType));
            column.setSize(getDBSize(metaType));

            //column.setDefaultValue(xmlReader.getAttributeValue(idx));
            column.setAutoIncrement(false);
            //column.setDescription(xmlReader.getAttributeValue(idx));
            //column.setJavaName(xmlReader.getAttributeValue(idx));

            table.addColumn(column);
        }

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            Column column = new Column();
            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType;

            if(field.getAttributePath().equals("root")) {
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

        if(!showcaseHolder.getShowCaseMeta().isFinal()) {
            column = new Column();
            column.setName("OPEN_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);

            Index indexOD = new NonUniqueIndex();
            indexOD.setName("ind_" + tableName + "_OPEN_DATE");
            indexOD.addColumn(new IndexColumn("OPEN_DATE"));
            table.addIndex(indexOD);

            column = new Column();
            column.setName("CLOSE_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);

            Index indexCD = new NonUniqueIndex();
            indexCD.setName("ind_" + tableName + "_CLOSE_DATE");
            indexCD.addColumn(new IndexColumn("CLOSE_DATE"));
            table.addIndex(indexCD);
        } else {
            column = new Column();
            column.setName("REP_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            table.addColumn(column);

            Index indexRD = new NonUniqueIndex();
            indexRD.setName("ind_" + tableName + "_REP_DATE");
            indexRD.addColumn(new IndexColumn("REP_DATE"));
            table.addIndex(indexRD);
        }

        model.addTable(table);

        Platform platform = PlatformFactory.createNewPlatformInstance(jdbcTemplateSC.getDataSource());
        platform.createModel(model, false, true);
    }

    void persistMap(HashMap<String, Object> map, Date openDate, Date closeDate, ShowcaseHolder showCaseHolder,
                    IBaseEntity entity) {
        StringBuilder sql;
        StringBuilder values = new StringBuilder("(");
        String tableName;

        if (closeDate == null)
            tableName = getActualTableName(showCaseHolder.getShowCaseMeta());
        else
            tableName = getHistoryTableName(showCaseHolder.getShowCaseMeta());

        sql = new StringBuilder("INSERT INTO ").append(tableName).append("(");

        Object[] vals;

        if(!showCaseHolder.getShowCaseMeta().isFinal()) {
            vals = new Object[map.size() + 2 + showCaseHolder.getShowCaseMeta().getCustomFieldsList().size()];
        } else {
            vals = new Object[map.size() + 1 + showCaseHolder.getShowCaseMeta().getCustomFieldsList().size()];
        }

        int i = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey()).append(", ");
            values.append("?, ");
            vals[i++] = entry.getValue();
        }

        for(ShowCaseField sf : showCaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            if(sf.getAttributePath().equals("root")) {
                sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                vals[i++] = entity.getId();
                values.append("?, ");
                continue;
            }

            Object o = entity.getEl(sf.getAttributePath());

            try {
                if (o instanceof BaseEntity) {
                    sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                    values.append("?, ");
                    vals[i++] = ((BaseEntity) o).getId();
             } else {
                    sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                    values.append("?, ");
                    vals[i++] = o;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        if(!showCaseHolder.getShowCaseMeta().isFinal()) {
            sql.append("cdc, open_date, close_date");
            values.append("sysdate, ?, ?)");
            vals[i++] = openDate;
            vals[i] = closeDate;
        } else {
            sql.append("cdc, rep_date");
            values.append("SYSDATE, ?)");
            vals[i] = openDate;
        }

        sql.append(") VALUES ").append(values);

        jdbcTemplateSC.update(sql.toString(), vals);
    }

    private boolean compatible(HashMap a, HashMap b) {
        for (Object o : a.keySet()) {
            String key = (String) o;
            if (key.endsWith("_id"))
                if (b.containsKey(key) && !a.get(key).equals(b.get(key)))
                    return false;
        }
        return true;
    }

    @Transactional
    void updateMapLeftRange(HistoryState historyState, KeyData keyData, IBaseEntity entity, ShowcaseHolder showCaseHolder) {
        String sql = "UPDATE %s SET close_date = ? WHERE " + keyData.queryKeys;

        if(historyState == HistoryState.ACTUAL) {
            sql = String.format(sql, getActualTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());
        } else {
            sql = String.format(sql, getHistoryTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());
        }

        jdbcTemplateSC.update(sql, getObjectArray(true, keyData.vals, entity.getReportDate()));
    }

    @Transactional
    void updateActualLeftRange(IBaseEntity entity, ShowcaseHolder showCaseHolder) {
        String sql = "UPDATE %s SET close_date = ? WHERE %s%s_id = ?";

        sql = String.format(sql, getActualTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                showCaseHolder.getRootClassName());

        jdbcTemplateSC.update(sql, entity.getReportDate(), entity.getId());
    }

    @Transactional
    void updateHistoryLeftRange(IBaseEntity entity, ShowcaseHolder showCaseHolder) {
        String sql;
        Date openDate;

        sql = "SELECT MAX(open_date) AS OPEN_DATE FROM %s WHERE open_date  <= ? AND %s%s_id = ?";
        sql = String.format(sql, getHistoryTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                showCaseHolder.getRootClassName());

        Map m = jdbcTemplateSC.queryForMap(sql, entity.getReportDate(), entity.getId());

        openDate = (Date) m.get("OPEN_DATE");

        if(openDate == null) return;

        if(new Date(openDate.getTime()).equals(entity.getReportDate())) {
            sql = "DELETE FROM %s WHERE %s%s_id = ? AND open_date = ?";
            sql = String.format(sql, getHistoryTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());

            jdbcTemplateSC.update(sql, entity.getId(), entity.getReportDate());
        } else {
            sql = "UPDATE %s SET close_date = ? WHERE %s%s_id = ? AND open_date = ?";
            sql = String.format(sql, getHistoryTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());

            jdbcTemplateSC.update(sql, entity.getReportDate(), entity.getId(), openDate);
        }
    }

    String getActualTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName();
    }

    String getHistoryTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName() + HISTORY_POSTFIX;
    }

    @Transactional
    void moveActualToHistory(IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        StringBuilder select = new StringBuilder();
        StringBuilder sql = new StringBuilder("INSERT INTO %s");

        select.append(COLUMN_PREFIX).append(showcaseHolder.getRootClassName()).append("_id, ");

        // default fields
        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFieldsList())
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");

        // custom fields
        for(ShowCaseField sf : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
        }

        select.append("CDC, OPEN_DATE, CLOSE_DATE ");
        sql.append("(").append(select).append(")( select ")
                .append(select).append("FROM %s WHERE %s%s_id = ? )");

        String sqlResult = String.format(sql.toString(), getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                getActualTableName(showcaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                showcaseHolder.getRootClassName());

        jdbcTemplateSC.update(sqlResult, entity.getId());

        sqlResult = String.format("DELETE FROM %s WHERE %s%s_ID = ? AND CLOSE_DATE IS NOT NULL",
                getActualTableName(showcaseHolder.getShowCaseMeta()),
                COLUMN_PREFIX, showcaseHolder.getRootClassName());

        jdbcTemplateSC.update(sqlResult, entity.getId());
    }

    @Transactional
    void moveActualMapToHistory(KeyData keyData, IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        StringBuilder select = new StringBuilder();
        StringBuilder sql = new StringBuilder("INSERT INTO %s");

        select.append(COLUMN_PREFIX).append(showcaseHolder.getRootClassName()).append("_id, ");

        // default fields
        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFieldsList())
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");

        // custom fields
        for(ShowCaseField sf : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
        }

        select.append("CDC, OPEN_DATE, CLOSE_DATE ");
        sql.append("(").append(select).append(")( SELECT ")
                .append(select).append("FROM %s WHERE " + keyData.queryKeys + ")");

        String sqlResult = String.format(sql.toString(), getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                getActualTableName(showcaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                showcaseHolder.getRootClassName());

        jdbcTemplateSC.update(sqlResult, keyData.vals);

        sqlResult = String.format("DELETE FROM %s WHERE " + keyData.queryKeys + " AND CLOSE_DATE IS NOT NULL",
                getActualTableName(showcaseHolder.getShowCaseMeta()),
                COLUMN_PREFIX, showcaseHolder.getRootClassName());

        jdbcTemplateSC.update(sqlResult, keyData.vals);
    }

    public int deleteById(ShowcaseHolder holder, IBaseEntity e) {
        String sql;
        int rows = 0;

        for (ShowcaseHolder sh : holders) {
            if (!sh.getShowCaseMeta().getTableName().equals(holder.getShowCaseMeta().getTableName()) &&
                    sh.getRootClassName().equals(holder.getRootClassName())) {
                sql = "DELETE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getHistoryTableName(sh.getShowCaseMeta()),
                        COLUMN_PREFIX, holder.getRootClassName());

                rows += jdbcTemplateSC.update(sql, e.getId());

                sql = "DELETE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getActualTableName(sh.getShowCaseMeta()),
                        COLUMN_PREFIX, holder.getRootClassName());

                rows += jdbcTemplateSC.update(sql, e.getId());
            }
        }

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getHistoryTableName(holder.getShowCaseMeta()),
                COLUMN_PREFIX, holder.getRootClassName());

        rows += jdbcTemplateSC.update(sql, e.getId());

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getActualTableName(holder.getShowCaseMeta()),
                COLUMN_PREFIX, holder.getRootClassName());

        rows += jdbcTemplateSC.update(sql, e.getId());

        return rows;
    }

    @Transactional
    public void generate(IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        List<BaseEntity> all;

        if(showcaseHolder.getShowCaseMeta().getDownPath() != null) {
            all = (List<BaseEntity>) entity.getEls("{get}" +
                    showcaseHolder.getShowCaseMeta().getDownPath());

            for (BaseEntity baseEntity : all)
                dbCarteageGenerate(entity, baseEntity, showcaseHolder);
        } else {
            dbCarteageGenerate(entity, entity, showcaseHolder);
        }
    }

    class KeyData {
        Object[] keys;
        Object[] vals;
        String queryKeys = "";

        public KeyData(HashMap<String, Object> keyMap, HashMap<String, Object> map) {
            keys = new Object[keyMap.size()];
            vals = new Object[keyMap.size()];

            int keyCounter = 0;
            for(Map.Entry entry : keyMap.entrySet()) {
                if(map.get(entry.getKey()) != null) {
                    keys[keyCounter] = entry.getKey();
                    vals[keyCounter] = map.get(entry.getKey());

                    queryKeys += entry.getKey() + " = ? ";
                    if(++keyCounter < keyMap.size()) queryKeys += " AND ";

                } else {
                    System.err.println("KEY VALUE IS NULL!!!!");
                }
            }
        }
    }

    public Object[] getObjectArray(boolean reverse, Object[] elementArray, Object... elements) {
        Object[] newObjectArray = new Object[elementArray.length + elements.length];

        int index = 0;
        if(!reverse) {
            for (Object object : elementArray) newObjectArray[index++] = object;
            for (Object object : elements) newObjectArray[index++] = object;
        } else {
            for (Object object : elements) newObjectArray[index++] = object;
            for (Object object : elementArray) newObjectArray[index++] = object;
        }

        return newObjectArray;
    }

    @Transactional
    synchronized void dbCarteageGenerate(IBaseEntity globalEntity, IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        Date openDate = null, closeDate = null;
        String sql;

        HashMap<Object, HashMap<String, Object>> savingMap = generateMap(entity, showcaseHolder);

        if(savingMap == null || savingMap.size() == 0)
            throw new UnsupportedOperationException("Map is empty!");

        HashMap<String, Object> keyMap = savingMap.get("_KEYMAP");
        savingMap.remove("_KEYMAP");

        for(Map.Entry entry : savingMap.entrySet()) {
            HashMap<String, Object> entryMap = (HashMap<String, Object>) entry.getValue();

            KeyData keyData = new KeyData(keyMap, entryMap);

            if(!showcaseHolder.getShowCaseMeta().isFinal()) {
                try {
                    sql = "SELECT MAX(OPEN_DATE) AS OPEN_DATE FROM %s WHERE " + keyData.queryKeys;
                    sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName().toUpperCase());

                    openDate = (Date) jdbcTemplateSC.queryForMap(sql, keyData.vals).get("OPEN_DATE");
                } catch (EmptyResultDataAccessException e) {
                    openDate = null;
                }

                boolean compResult;

                if (openDate == null) {
                    openDate = entity.getReportDate();
                } else if(openDate.compareTo(entity.getReportDate()) == 0) {
                    openDate = entity.getReportDate();

                    sql = "DELETE FROM %s WHERE " + keyData.queryKeys + " and OPEN_DATE = ?";
                    sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName());

                    jdbcTemplateSC.update(sql, getObjectArray(false, keyData.vals, openDate));
                } else if(openDate.compareTo(entity.getReportDate()) < 0) {
                    compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showcaseHolder, keyData);

                    if(compResult) continue;

                    updateMapLeftRange(HistoryState.ACTUAL, keyData, entity, showcaseHolder);
                    moveActualMapToHistory(keyData, entity, showcaseHolder);

                    openDate = entity.getReportDate();
                } else {
                    sql = "SELECT MIN(OPEN_DATE) as OPEN_DATE FROM %s WHERE " + keyData.queryKeys + " AND OPEN_DATE > ? ";
                    sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName());

                    closeDate = (Date) jdbcTemplateSC.queryForMap(sql,
                            getObjectArray(false, keyData.vals, entity.getReportDate())).get("OPEN_DATE");

                    if (closeDate == null) {
                        compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showcaseHolder, keyData);

                        if(compResult) {
                            sql = "UPDATE %s SET open_date = ? WHERE " + keyData.queryKeys + " AND open_date = ?";
                            sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, keyData.vals,
                                    entity.getReportDate()), openDate));

                            return;
                        } else {
                            closeDate = openDate;
                        }
                    } else {
                        compResult = compareValues(HistoryState.HISTORY, entryMap, entity, showcaseHolder, keyData);

                        if(compResult) {
                            sql = "UPDATE %s SET open_date = ? WHERE " + keyData.queryKeys + " AND open_date = ?";
                            sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, keyData.vals,
                                    entity.getReportDate()), closeDate));

                            return;
                        } else {
                            closeDate = openDate;
                        }
                    }

                    openDate = entity.getReportDate();
                    updateMapLeftRange(HistoryState.HISTORY, keyData, entity, showcaseHolder);
                }
            } else {
                openDate = entity.getReportDate();

                sql = "DELETE FROM %s WHERE " + keyData.queryKeys + " and REP_DATE = ?";
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                jdbcTemplateSC.update(sql, getObjectArray(false, keyData.vals, openDate));
            }

            persistMap(entryMap, openDate, closeDate, showcaseHolder, globalEntity);
        }
    }

    class PathElement {
        public String elementPath;
        public String attributePath;
        public String columnName;
        public boolean isKey = false;

        public PathElement(String elementPath, String attributePath, String columnName, boolean isKey) {
            this.elementPath = elementPath;
            this.attributePath = attributePath;
            this.columnName = columnName;
            this.isKey = isKey;
        }

        @Override
        public String toString() {
            return elementPath + ", " + columnName;
        }
    }

    class ValueElement {
        public String columnName;
        public Long elementId;
        public boolean isKey;
        public boolean isArray;
        public boolean isSimple;

        public ValueElement(String columnName, Long elementId, boolean isKey) {
            this.columnName = columnName;
            this.elementId = elementId;
            this.isKey = isKey;
            this.isArray = false;
            this.isSimple = false;
        }

        public ValueElement(String columnName, Long elementId, boolean isKey, boolean isArray) {
            this.columnName = columnName;
            this.elementId = elementId;
            this.isKey = isKey;
            this.isArray = isArray;
            this.isSimple = false;
        }

        public ValueElement(String columnName, Long elementId, boolean isKey, boolean isArray, boolean isSimple) {
            this.columnName = columnName;
            this.elementId = elementId;
            this.isKey = isKey;
            this.isArray = isArray;
            this.isSimple = isSimple;
        }

        @Override
        public String toString() {
            return columnName + ", " + elementId + ", " + isKey + ", " + isArray;
        }
    }

    public HashMap<String, HashSet<PathElement>> generatePaths(IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        HashMap<String, HashSet<PathElement>> paths = new HashMap<>();

        HashSet<PathElement> tmpSet;

        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            IMetaType attributeMetaType = entity.getMeta().getEl(sf.getAttributePath());

            if (sf.getAttributePath().contains(".")) {
                if(attributeMetaType.isComplex()) {
                    if(paths.get("root." + sf.getAttributePath()) != null) {
                        tmpSet = paths.get("root." + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + sf.getAttributePath(), tmpSet);

                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if(paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + path, tmpSet);
                } else {
                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if(paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + path, tmpSet);
                }
            } else {
                if(paths.get("root") != null) {
                    tmpSet = paths.get("root");
                } else {
                    tmpSet = new HashSet<>();
                }

                if(attributeMetaType.isComplex() || attributeMetaType.isSet()) {
                    tmpSet.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName(), false));
                    paths.put("root", tmpSet);

                    tmpSet = new HashSet<>();
                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName(), true));
                    paths.put("root." + sf.getAttributePath(), tmpSet);
                } else {
                    tmpSet.add(new PathElement(sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root", tmpSet);
                }
            }
        }

        return paths;
    }

    HashMap<ValueElement, Object> readMap(String curPath, IBaseEntity entity, HashMap<String,
            HashSet<PathElement>> paths, boolean parentIsArray) {
        HashSet<PathElement> attributes = paths.get(curPath);

        HashMap<ValueElement, Object> map = new HashMap<>();

        if(attributes != null) {
            for(PathElement attribute : attributes) {
                if (attribute.elementPath.equals("root") && entity != null) {
                    map.put(new ValueElement(attribute.columnName, entity.getId(), !curPath.contains(".")
                            || parentIsArray), entity.getId());
                } else {
                    if (attribute.elementPath.contains("root.")) {
                        Object container = entity.getEl(attribute.elementPath.substring(
                                attribute.elementPath.indexOf(".") + 1));

                        if(container == null) continue;

                        if (container instanceof BaseEntity) {
                            BaseEntity innerEntity = (BaseEntity) container;

                            if(innerEntity != null)
                                map.put(new ValueElement(attribute.elementPath, innerEntity.getId(), false),
                                        readMap(attribute.elementPath, innerEntity, paths, false));
                        } else if (container instanceof BaseSet) {
                            BaseSet innerSet = (BaseSet) container;

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            if(innerSet.getMemberType().isComplex()) {
                                for(IBaseValue bValue : innerSet.get()) {
                                    BaseEntity bValueEntity = (BaseEntity) bValue.getValue();
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValueEntity.getId(), true, false),
                                            readMap(attribute.elementPath, bValueEntity, paths, true));
                                }

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), false, true, false), arrayMap);
                            } else {
                                for(IBaseValue bValue : innerSet.get())
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValue.getId(), false, false),
                                            bValue.getValue());

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), false, true, true), arrayMap);
                            }
                        } else {
                            System.err.println("Operation is not supported!");
                        }

                    } else {
                        IBaseValue iBaseValue = entity.getBaseValue(attribute.elementPath);

                        if(iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex()) {
                            map.put(new ValueElement(curPath + "." + attribute.elementPath, iBaseValue.getId(), false)
                                    , readMap(curPath + "." + attribute.elementPath,
                                    (BaseEntity) iBaseValue.getValue(), paths, false));
                        } else if(iBaseValue != null && iBaseValue.getValue() instanceof BaseSet) {
                            BaseSet bSet = (BaseSet) iBaseValue.getValue();

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            for(IBaseValue innerValue : bSet.get())
                                arrayMap.put(new ValueElement(attribute.elementPath, innerValue.getId(),
                                        false, false), innerValue.getValue());

                            map.put(new ValueElement(attribute.elementPath, iBaseValue.getId(), false, true, true),
                                    arrayMap);
                        } else if(iBaseValue != null) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId(), false),
                                    iBaseValue.getValue());
                        }
                    }
                }
            }
        }

        return map;
    }

    public HashMap<ValueElement, Object> clearDirtyMap(HashMap<ValueElement, Object> dirtyMap) {
        HashMap<ValueElement, Object> tmpMap = new HashMap<>();

        Iterator mainArrayIterator = dirtyMap.entrySet().iterator();
        while(mainArrayIterator.hasNext()) {
            Map.Entry<ValueElement, Object> entry = (Map.Entry) mainArrayIterator.next();

            if(entry.getKey().isArray) {
                if(entry.getKey().isSimple) {
                    Iterator arrayIterator = ((HashMap) entry.getValue()).entrySet().iterator();

                    while(arrayIterator.hasNext()) {
                        Map.Entry<ValueElement, Object> arrayEntry = (Map.Entry) arrayIterator.next();

                        HashMap<ValueElement, Object> rMap = new HashMap<>();
                        rMap.put(arrayEntry.getKey(), arrayEntry.getValue());
                        tmpMap.put(arrayEntry.getKey(), rMap);
                    }
                } else {
                    Iterator arrayIterator = ((HashMap) entry.getValue()).entrySet().iterator();

                    while(arrayIterator.hasNext()) {
                        Map.Entry<ValueElement, Object> arrayEntry = (Map.Entry) arrayIterator.next();

                        HashMap<ValueElement, Object> rMap = clearDirtyMap((HashMap) arrayEntry.getValue());
                        tmpMap.put(arrayEntry.getKey(), rMap);
                    }
                }

            }
        }

        Iterator mainIterator = dirtyMap.entrySet().iterator();
        while(mainIterator.hasNext()) {
            Map.Entry<ValueElement, Object> entry = (Map.Entry) mainIterator.next();

            if(!entry.getKey().isArray) {
                for(ValueElement tmpKey : tmpMap.keySet()) {
                    HashMap<ValueElement, Object> innerTmpMap = (HashMap) tmpMap.get(tmpKey);

                    innerTmpMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return tmpMap;
    }

    public synchronized HashMap<Object, HashMap<String, Object>> generateMap(IBaseEntity entity,
                                               ShowcaseHolder showcaseHolder){
        HashMap<String, HashSet<PathElement>> paths = generatePaths(entity, showcaseHolder);

        HashSet<PathElement> rootAttributes = paths.get("root");
        rootAttributes.add(new PathElement("root", "root", entity.getMeta().getClassName()+"_id", true));

        HashMap<ValueElement, Object> dirtyMap = readMap("root", entity, paths, false);

        if(dirtyMap == null)
            System.err.println("Map is null!");

        HashMap<ValueElement, Object> returnMap = clearDirtyMap(dirtyMap);

        return null;
    }

    public boolean compareValues(HistoryState state, HashMap<String, Object> savingMap,
                                 IBaseEntity entity, ShowcaseHolder showcaseHolder, KeyData keyData) {
        StringBuilder st = new StringBuilder();
        boolean equalityFlag = true;

        int colCounter = 0;
        for(String colName : savingMap.keySet()) {
            st.append(colName);

            if(++colCounter < savingMap.size())
                st.append(", ");
        }

        String sql = "SELECT " + st.toString() + " FROM %s WHERE " + keyData.queryKeys;

        Map dbElement = null;

        if(state == HistoryState.ACTUAL) {
            sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

            dbElement = jdbcTemplateSC.queryForMap(sql, keyData.vals);
        } else {
            sql += " AND open_date = ?";
            sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                    COLUMN_PREFIX, showcaseHolder.getRootClassName(), entity.getReportDate());

            dbElement = jdbcTemplateSC.queryForMap(sql, getObjectArray(false, keyData.vals, entity.getReportDate()));
        }

        for(String colName : savingMap.keySet()) {
            Object newValue = savingMap.get(colName);
            Object dbValue = dbElement.get(colName);

            if(newValue == null && dbValue == null)
                continue;

            if(newValue == null || dbValue == null) {
                equalityFlag = false;
                break;
            }

            if(newValue instanceof Double) {
                if(!Double.valueOf((Double) newValue).equals(Double.valueOf(dbValue.toString()))) {
                    equalityFlag = false;
                    break;
                }
            } else if(newValue instanceof Integer) {
                if(!Integer.valueOf((Integer) newValue).equals(Integer.valueOf(dbValue.toString()))) {
                    equalityFlag = false;
                    break;
                }
            } else if(newValue instanceof Boolean) {
                if(!Boolean.valueOf((Boolean) newValue).equals(Boolean.valueOf(dbValue.toString()))) {
                    equalityFlag = false;
                    break;
                }
            } else if(newValue instanceof Long) {
                if(!Long.valueOf((Long) newValue).equals(Long.valueOf(dbValue.toString()))) {
                    equalityFlag = false;
                    break;
                }
            } else if(newValue instanceof String) {
                if(!newValue.toString().equals(dbValue.toString())) {
                    equalityFlag = false;
                    break;
                }
            } else if(newValue instanceof Date) {
                if(!newValue.equals(new Date(((Timestamp)dbValue).getTime()))) {
                    equalityFlag = false;
                    break;
                }
            } else {
                if(!newValue.equals(dbValue)) {
                    equalityFlag = false;
                    break;
                }
            }
        }


        return equalityFlag;
    }

    private String getDBType(IMetaType type) {
        if (type.isComplex())
            return "NUMERIC";

        if (type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();

        if (type.isSet())
            throw new IllegalArgumentException("ShowCase can't contain set columns: " +
                    type.toString());

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
                throw new IllegalArgumentException("Unknown simple type code");
        }
    }

    private String getDBSize(IMetaType type) {
        if(type.isComplex())
            return "14, 0";

        if (type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();

        if (type.isSet())
            throw new IllegalArgumentException("ShowCase can't contain set columns");

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
                throw new IllegalArgumentException("Unknown simple type code");
        }
    }

    @Override
    public ShowCase load(long id) {
        Select select = context
                .select(EAV_SC_SHOWCASES.ID, EAV_SC_SHOWCASES.TITLE, EAV_SC_SHOWCASES.TABLE_NAME,
                        EAV_SC_SHOWCASES.NAME, EAV_SC_SHOWCASES.CLASS_NAME, EAV_SC_SHOWCASES.DOWN_PATH,
                        EAV_SC_SHOWCASES.IS_FINAL)
                .from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.ID.equal(id));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(),
                select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException("Query for ShowCase return more than one row.");

        if (rows.size() < 1)
            throw new RuntimeException("ShowCase not found.");

        Map<String, Object> row = rows.iterator().next();

        ShowCase showCase = new ShowCase();
        showCase.setId(id);
        showCase.setName((String) row.get(EAV_SC_SHOWCASES.NAME.getName()));
        showCase.setTitle((String) row.get(EAV_SC_SHOWCASES.TITLE.getName()));
        showCase.setTableName((String) row.get(EAV_SC_SHOWCASES.TABLE_NAME.getName()));
        showCase.setDownPath((String) row.get(EAV_SC_SHOWCASES.DOWN_PATH.getName()));
        showCase.setFinal((row.get(EAV_SC_SHOWCASES.IS_FINAL.getName())).toString().equals("1"));

        String metaClassName = (String) row.get(EAV_SC_SHOWCASES.CLASS_NAME.getName());
        MetaClass metaClass = metaService.getMetaClass(metaClassName);
        showCase.setMeta(metaClass);

        select = context
                .select(EAV_SC_SHOWCASE_FIELDS.ID, EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME, EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH, EAV_SC_SHOWCASE_FIELDS.TYPE)
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
                } else {
                    showCase.addField(showCaseField);
                }
            }
        }

        return showCase;
    }

    @Override
    public ShowCase load(String name) {
        Long id = getIdByName(name);
        return load(id);
    }

    long getIdByName(String name) {
        Select select = context.select(EAV_SC_SHOWCASES.ID).from(EAV_SC_SHOWCASES)
                .where(EAV_SC_SHOWCASES.NAME.equal(name));

        logger.debug(select.toString());

        List<Map<String, Object>> rows = jdbcTemplateSC.queryForList(select.getSQL(),
                select.getBindValues().toArray());

        if (rows.size() > 1)
            throw new RuntimeException("Query for ShowCase return more than one row.");

        if (rows.size() < 1)
            return 0;

        Map<String, Object> row = rows.iterator().next();

        return ((BigDecimal) row
                .get(EAV_SC_SHOWCASES.ID.getName())).longValue();
    }

    @Override
    @Transactional
    public long save(ShowCase showCaseForSave) {
        if (showCaseForSave.getId() < 1)
            showCaseForSave.setId(getIdByName(showCaseForSave.getName()));

        if (showCaseForSave.getId() < 1) {
            return insert(showCaseForSave);
        } else {
            update(showCaseForSave);
            return showCaseForSave.getId();
        }
    }

    @Override
    public void remove (ShowCase showCase) {
        throw new RuntimeException("Unimplemented");
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

        long showCaseFieldId = insertWithId(insert.getSQL(),
                insert.getBindValues().toArray());

        showCaseField.setId(showCaseFieldId);

        return showCaseFieldId;
    }

    private long insert(ShowCase showCase) {
        Insert insert = context
                .insertInto(EAV_SC_SHOWCASES)
                .set(EAV_SC_SHOWCASES.NAME, showCase.getName())
                .set(EAV_SC_SHOWCASES.TABLE_NAME, showCase.getTableName())
                .set(EAV_SC_SHOWCASES.TITLE, showCase.getTitle())
                .set(EAV_SC_SHOWCASES.CLASS_NAME, showCase.getMeta().getClassName())
                .set(EAV_SC_SHOWCASES.DOWN_PATH, showCase.getDownPath())
                .set(EAV_SC_SHOWCASES.IS_FINAL, showCase.isFinal() ? 1 : 0);

        logger.debug(insert.toString());

        long showCaseId = insertWithId(insert.getSQL(),
                insert.getBindValues().toArray());

        showCase.setId(showCaseId);

        for (ShowCaseField sf : showCase.getFieldsList())
            insertField(sf, showCase.getId());

        for (ShowCaseField sf : showCase.getCustomFieldsList())
            insertField(sf, showCase.getId());

        return showCaseId;
    }

    private long deleteFields(long showCaseId) {
        Delete delete = context
                .delete(EAV_SC_SHOWCASE_FIELDS)
                .where(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID.equal(showCaseId));

        logger.debug(delete.toString());
        return jdbcTemplateSC.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    private void update(ShowCase showCaseSaving) {
        if (showCaseSaving.getId() < 1)
            throw new IllegalArgumentException("UPDATE couldn't be done without ID.");

        String tableAlias = "sc";
        Update update = context
                .update(EAV_SC_SHOWCASES.as(tableAlias))
                .set(EAV_SC_SHOWCASES.as(tableAlias).NAME, showCaseSaving.getName())
                .set(EAV_SC_SHOWCASES.as(tableAlias).TABLE_NAME, showCaseSaving.getTableName())
                .set(EAV_SC_SHOWCASES.as(tableAlias).TITLE, showCaseSaving.getTitle())
                .set(EAV_SC_SHOWCASES.as(tableAlias).DOWN_PATH, showCaseSaving.getDownPath())
                .set(EAV_SC_SHOWCASES.as(tableAlias).IS_FINAL, showCaseSaving.isFinal() ? 1 : 0)
                .where(EAV_SC_SHOWCASES.as(tableAlias).as(tableAlias).ID.equal(showCaseSaving.getId()));

        logger.debug(update.toString());
        int count = jdbcTemplateSC.update(update.getSQL(), update.getBindValues().toArray());

        if (count != 1)
            throw new RuntimeException("UPDATE operation should be update only one record.");

        deleteFields(showCaseSaving.getId());

        for (ShowCaseField sf : showCaseSaving.getFieldsList())
            insertField(sf, showCaseSaving.getId());

        for(ShowCaseField sf : showCaseSaving.getCustomFieldsList())
            insertField(sf, showCaseSaving.getId());
    }

    long insertWithId(String query, Object[] values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateSC.update(new GenericInsertPreparedStatementCreator(query, values), keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long insertBadEntity(Long entityId, Long scId, Date report_date, String stackTrace, String message) {
        if(scId == null)
            scId = 0L;

        Insert insert = context
                .insertInto(EAV_SC_BAD_ENTITIES)
                .set(EAV_SC_BAD_ENTITIES.ENTITY_ID, entityId)
                .set(EAV_SC_BAD_ENTITIES.SC_ID, scId)
                .set(EAV_SC_BAD_ENTITIES.REPORT_DATE, DataUtils.convert(report_date))
                .set(EAV_SC_BAD_ENTITIES.STACK_TRACE, stackTrace)
                .set(EAV_SC_BAD_ENTITIES.MESSAGE, message);

        if(logger.isDebugEnabled())
            logger.debug(insert.toString());

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
    }

    public enum HistoryState {
        ACTUAL,
        HISTORY
    }

    class GenericInsertPreparedStatementCreator implements PreparedStatementCreator {
        final String query;
        final Object[] values;
        String keyName = "id";

        public GenericInsertPreparedStatementCreator(String query, Object[] values) {
            this.query = query;
            this.values = values.clone();
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    query, new String[]{keyName});

            int i = 1;
            for (Object obj : values) {
                ps.setObject(i++, obj);
            }

            return ps;
        }
    }

    @Transactional
    @Override
    public List<Map<String, Object>> view(Long id, int offset, int limit, Date reportDate) {
        ShowCase showcase = load(id);

        Select select = context.selectFrom(DSL.tableByName(TABLES_PREFIX + showcase.getTableName())).
                limit(limit).offset(offset);

        return jdbcTemplateSC.queryForList(select.getSQL(),
                select.getBindValues().toArray());
    }
}
