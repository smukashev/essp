package kz.bsbnb.usci.showcase.dao.impl;

import kz.bsbnb.ddlutils.Platform;
import kz.bsbnb.ddlutils.PlatformFactory;
import kz.bsbnb.ddlutils.model.*;
import kz.bsbnb.ddlutils.model.Table;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
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
        switch (historyState) {
            case ACTUAL:
                tableName = getActualTableName(showcaseHolder.getShowCaseMeta());
                break;
            default:
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

        Map<String, String> prefixToColumn = showcaseHolder.generatePaths();

        // path ids
        for (String prefix : prefixToColumn.keySet()) {
            boolean hasFilter = false;

            // Filter check
            if(!showcaseHolder.getShowCaseMeta().getFilterFieldsList().isEmpty()) {
                for(ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFilterFieldsList()) {
                    if(sf.getAttributePath().equals(prefix) ||
                            ("root." + sf.getAttributePath()).equals(prefix)) {
                        hasFilter = true;
                        break;
                    }
                }
            }

            if(!hasFilter) {
                Column column = new Column();
                column.setName(COLUMN_PREFIX + prefixToColumn.get(prefix) + "_ID");
                column.setPrimaryKey(false);
                column.setRequired(false);
                column.setType("NUMERIC");
                column.setSize("14,0");
                column.setAutoIncrement(false);

                table.addColumn(column);
            }
        }

        // Custom fields
        if (showcaseHolder.getShowCaseMeta().getCustomFieldsList().size() > 0) {
            for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
                Column column = new Column();

                if (sf.getAttributePath().equals("ROOT")) { // ROOT ID
                    column.setName(COLUMN_PREFIX + sf.getColumnName());
                    column.setPrimaryKey(false);
                    column.setRequired(false);
                    column.setType("NUMERIC");
                    column.setSize("14,0");
                    column.setAutoIncrement(false);
                } else {
                    MetaClass root = metaService.getMetaClass("credit"); // TODO: fix credit arg
                    String path;

                    if(sf.getAttributePath() == null || sf.getAttributePath().equals("")) {
                        path = sf.getAttributeName();
                    } else {
                        path = sf.getAttributePath();
                    }

                    IMetaType metaType = root.getEl(path);

                    if(metaType.isComplex()) {
                        column.setType("NUMERIC");
                        column.setSize("14,0");
                    } else {
                        column.setType(getDBType(metaType));
                        column.setSize(getDBSize(metaType));
                    }

                    column.setName(COLUMN_PREFIX + sf.getColumnName());
                    column.setPrimaryKey(false);
                    column.setRequired(false);
                    column.setAutoIncrement(false);
                }

                table.addColumn(column);
            }
        }

        Index indexR = new NonUniqueIndex();
        indexR.setName("ind_" + tableName + "_" + prefixToColumn.get("root") + "_id");
        indexR.addColumn(new IndexColumn(COLUMN_PREFIX + prefixToColumn.get("root") + "_id"));
        table.addIndex(indexR);

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            if (field.getAttributeName().equals(""))
                continue;

            Column column = new Column();

            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType = showcaseHolder.getShowCaseMeta().getActualMeta().getEl(field.getPath());

            column.setType(getDBType(metaType));

            String columnSize = getDBSize(metaType);

            if (columnSize != null) {
                column.setSize(columnSize);
            }

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
        Index indexCDC = new NonUniqueIndex();
        indexCDC.setName("ind_" + tableName + "_CDC");
        indexCDC.addColumn(new IndexColumn("CDC"));
        table.addIndex(indexCDC);
        table.addColumn(column);

        if(!showcaseHolder.getShowCaseMeta().isFinal()) {
            column = new Column();
            column.setName("OPEN_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            Index indexOD = new NonUniqueIndex();
            indexOD.setName("ind_" + tableName + "_OPEN_DATE");
            indexOD.addColumn(new IndexColumn("OPEN_DATE"));
            table.addIndex(indexOD);
            table.addColumn(column);

            column = new Column();
            column.setName("CLOSE_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            Index indexCD = new NonUniqueIndex();
            indexCD.setName("ind_" + tableName + "_CLOSE_DATE");
            indexCD.addColumn(new IndexColumn("CLOSE_DATE"));
            table.addIndex(indexCD);
            table.addColumn(column);
        } else {
            column = new Column();
            column.setName("REP_DATE");
            column.setPrimaryKey(false);
            column.setRequired(false);
            column.setType("DATE");
            Index indexRD = new NonUniqueIndex();
            indexRD.setName("ind_" + tableName + "_REP_DATE");
            indexRD.addColumn(new IndexColumn("REP_DATE"));
            table.addIndex(indexRD);
            table.addColumn(column);
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

        sql = new StringBuilder("insert into ").append(tableName).append("(");

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
            sql.append(sf.getColumnName()).append(", ");
            values.append("?, ");
            Object o;

            if(sf.getAttributePath().equals("")) {
                o = entity.getEl(sf.getAttributeName());
            } else {
                o = entity.getEl(sf.getAttributePath());
            }

            try {
                if (o instanceof BaseEntity) {
                    vals[i++] = ((BaseEntity) o).getId();
                } else if(o instanceof Date) {
                    vals[i++] = DataUtils.convert((Date) o);
                } else {
                    vals[i++] = o;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        if(!showCaseHolder.getShowCaseMeta().isFinal()) {
            sql.append("CDC, OPEN_DATE, CLOSE_DATE");
            values.append("SYSDATE, ?, ? )");
            vals[i++] = openDate;
            vals[i] = closeDate;
        } else {
            sql.append("CDC, REP_DATE");
            values.append("SYSDATE, ?)");
            vals[i] = openDate;
        }

        sql.append(") values ").append(values);

        long t1 = System.currentTimeMillis();
        jdbcTemplateSC.update(sql.toString(), vals);
        long t2 = System.currentTimeMillis() - t1;
        stats.put("insert into (persistMap)", t2);
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

    private boolean merge(HashMap a, HashMap b) {
        if (compatible(a, b)) {
            for (Object o : b.keySet()) {
                String key = (String) o;
                if (!a.containsKey(key))
                    a.put(key, b.get(key));
            }
            return true;
        }
        return false;
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
        StringBuilder sql = new StringBuilder("insert into %s");

        for (String s : showcaseHolder.generatePaths().values()) {
            boolean hasFilter = false;

            for(ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFilterFieldsList()) {
                if(s.equals(sf.getAttributePath()))  {
                    hasFilter = true;
                    break;
                }
            }

            if(!hasFilter)
                select.append(COLUMN_PREFIX).append(s).append("_id").append(",");
        }

        // default fields
        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            if (sf.getAttributeName().equals(""))
                continue;

            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(",");
        }

        // custom fields
        for(ShowCaseField sf : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(",");
        }

        select.append("CDC, OPEN_DATE, CLOSE_DATE ");
        sql.append("(").append(select).append(")( select ")
                .append(select).append("from %s where %s%s_id = ? )");

        String sqlResult = String.format(sql.toString(), getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                getActualTableName(showcaseHolder.getShowCaseMeta()), COLUMN_PREFIX, 
                showcaseHolder.getRootClassName());

        long t1 = System.currentTimeMillis();
        jdbcTemplateSC.update(sqlResult, entity.getId());
        long t2 = System.currentTimeMillis() - t1;
        stats.put("insert into %s ( select from %s where %sROOT_ID = ? )", t2);

        sqlResult = String.format("DELETE FROM %s WHERE %s%s_ID = ? AND CLOSE_DATE IS NOT NULL",
                getActualTableName(showcaseHolder.getShowCaseMeta()),
                COLUMN_PREFIX, showcaseHolder.getRootClassName());
        
        long t3 = System.currentTimeMillis();
        jdbcTemplateSC.update(sqlResult, entity.getId());
        long t4 = System.currentTimeMillis() - t3;
        stats.put("DELETE FROM %s WHERE %sROOT_ID = ? AND CLOSE_DATE IS NOT NULL", t4);
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

                rows = jdbcTemplateSC.update(sql, e.getId());

                logger.debug(sql, e.getId());
                logger.debug("Rows deleted from " + getHistoryTableName(sh.getShowCaseMeta()) + ": " + rows);

                sql = "DELETE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getActualTableName(sh.getShowCaseMeta()),
                        COLUMN_PREFIX, holder.getRootClassName());

                rows = jdbcTemplateSC.update(sql, e.getId());

                logger.debug(sql, e.getId());
                logger.debug("Rows deleted from " + getActualTableName(sh.getShowCaseMeta()) + ": " + rows);
            }
        }

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getHistoryTableName(holder.getShowCaseMeta()),
                COLUMN_PREFIX, holder.getRootClassName());

        rows = jdbcTemplateSC.update(sql, e.getId());

        logger.debug(sql, e.getId());
        logger.debug("Rows deleted from " + getHistoryTableName(holder.getShowCaseMeta()) + ": " + rows);

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getActualTableName(holder.getShowCaseMeta()),
                COLUMN_PREFIX, holder.getRootClassName());

        rows = jdbcTemplateSC.update(sql, e.getId());

        logger.debug(sql, e.getId());
        logger.debug("Rows deleted from " + getActualTableName(holder.getShowCaseMeta()) + ": " + rows);

        return rows;
    }

    @Transactional
    public void generate(IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        List<BaseEntity> all = (List<BaseEntity>) entity.getEls("{get}" +
                showcaseHolder.getShowCaseMeta().getDownPath());

        for (BaseEntity baseEntity : all)
            dbCarteageGenerate(entity, baseEntity, showcaseHolder);
    }

    @Transactional
    void dbCarteageGenerate(IBaseEntity globalEntity, IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        Date openDate, closeDate = null;
        String sql;

        if(!showcaseHolder.getShowCaseMeta().isFinal()) {
            try {
                sql = "SELECT OPEN_DATE AS OPEN_DATE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                openDate = (Date) jdbcTemplateSC.queryForMap(sql, entity.getId()).get("OPEN_DATE");
            } catch (EmptyResultDataAccessException e) {
                openDate = null;
            }

            if (openDate == null) {
                openDate = entity.getReportDate();
            } else if(openDate.compareTo(entity.getReportDate()) == 0) {
                openDate = entity.getReportDate();

                sql = "DELETE FROM %s WHERE %s%s_ID = ? and OPEN_DATE = ?";
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                jdbcTemplateSC.update(sql, entity.getId(), openDate);
            } else if(openDate.compareTo(entity.getReportDate()) < 0) {
                boolean compResult = compareValues(HistoryState.ACTUAL, entity, showcaseHolder, entity.getId());

                if(compResult) return;

                updateActualLeftRange(entity, showcaseHolder);
                moveActualToHistory(entity, showcaseHolder);

                openDate = entity.getReportDate();
            } else {
                sql = "SELECT MIN(OPEN_DATE) as OPEN_DATE FROM %s WHERE %s%s_ID = ? AND OPEN_DATE > ? ";
                sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                closeDate = (Date) jdbcTemplateSC.queryForMap(sql, entity.getId(),
                        entity.getReportDate()).get("OPEN_DATE");

                if (closeDate == null) {
                    boolean compResult = compareValues(HistoryState.ACTUAL, entity, showcaseHolder, entity.getId());

                    if(compResult) {
                        sql = "UPDATE %s SET open_date = ? WHERE %s%s_id = ? AND open_date = ?";
                        sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                                showcaseHolder.getRootClassName());

                        jdbcTemplateSC.update(sql, entity.getReportDate(), entity.getId(), openDate);

                        return;
                    } else {
                        closeDate = openDate;
                    }
                } else {
                    boolean compResult = compareValues(HistoryState.HISTORY, entity, showcaseHolder, entity.getId());

                    if(compResult) {
                        sql = "UPDATE %s SET open_date = ? WHERE %s%s_id = ? AND open_date = ?";
                        sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                                showcaseHolder.getRootClassName());

                        jdbcTemplateSC.update(sql, entity.getReportDate(), entity.getId(), closeDate);

                        return;
                    } else {
                        closeDate = openDate;
                    }
                }

                openDate = entity.getReportDate();
                updateHistoryLeftRange(entity, showcaseHolder);
            }
        } else {
            openDate = entity.getReportDate();

            sql = "DELETE FROM %s WHERE %s%s_ID = ? and REP_DATE = ?";
            sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

            jdbcTemplateSC.update(sql, entity.getId(), openDate);
        }

        int n = showcaseHolder.getShowCaseMeta().getFieldsList().size();

        ShowCaseEntries[] showCases = new ShowCaseEntries[n];
        int i = 0;

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            showCases[i] = new ShowCaseEntries(entity, field.getAttributeName().equals("") ?
                    field.getAttributePath() : field.getAttributePath() + "." + field.getAttributeName(),
                    field.getColumnName(), showcaseHolder.generatePaths());
            i++;
        }

        int allRecordsSize = 0;
        for (i = 0; i < n; i++)
            allRecordsSize += showCases[i].getEntriesSize();

        boolean[] was = new boolean[allRecordsSize];
        boolean[] usedGroup = new boolean[n];
        int[] id = new int[allRecordsSize];

        List<HashMap> entries = new ArrayList<>(allRecordsSize);

        int yk = 0;
        for (i = 0; i < n; i++)
            for (HashMap m : showCases[i].getEntries()) {
                entries.add(yk, m);
                id[yk] = i;
                yk++;
            }

        boolean found = true;

        while (found) {
            found = false;

            for (i = 0; i < allRecordsSize; i++)
                if (!was[i]) {
                    was[i] = true;
                    HashMap map = (HashMap) entries.get(i).clone();
                    found = true;

                    Arrays.fill(usedGroup, false);
                    usedGroup[id[i]] = true;

                    for (int j = 0; j < allRecordsSize; j++)
                        if (i != j && !was[j] && !usedGroup[id[j]])
                            if (merge(map, entries.get(j))) {
                                was[j] = true;
                                usedGroup[id[j]] = true;
                            }

                    persistMap(map, openDate, closeDate, showcaseHolder, globalEntity);
                }
        }
    }

    public boolean compareValues(HistoryState state, IBaseEntity entity, ShowcaseHolder showcaseHolder, Long recordId) {
        boolean equalityFlag = true;
        List<HashMap<String, String>> mapList = new ArrayList<>();
        int fieldSize = showcaseHolder.getShowCaseMeta().getFieldsList().size();

        ShowCaseEntries[] showCases = new ShowCaseEntries[fieldSize];
        int i = 0;

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            showCases[i] = new ShowCaseEntries(entity, field.getAttributeName().equals("") ?
                    field.getAttributePath() : field.getAttributePath() + "." + field.getAttributeName(),
                    field.getColumnName(), showcaseHolder.generatePaths());
            i++;
        }

        int allRecordsSize = 0;
        for (i = 0; i < fieldSize; i++)
            allRecordsSize += showCases[i].getEntriesSize();

        boolean[] was = new boolean[allRecordsSize];
        boolean[] usedGroup = new boolean[fieldSize];
        int[] id = new int[allRecordsSize];

        List<HashMap> entries = new ArrayList<>(allRecordsSize);

        int yk = 0;
        for (i = 0; i < fieldSize; i++)
            for (HashMap m : showCases[i].getEntries()) {
                entries.add(yk, m);
                id[yk] = i;
                yk++;
            }

        boolean found = true;

        while (found) {
            found = false;

            for (i = 0; i < allRecordsSize; i++)
                if (!was[i]) {
                    was[i] = true;
                    HashMap map = (HashMap) entries.get(i).clone();
                    found = true;

                    Arrays.fill(usedGroup, false);
                    usedGroup[id[i]] = true;

                    for (int j = 0; j < allRecordsSize; j++) {
                        if (i != j && !was[j] && !usedGroup[id[j]])
                            if (merge(map, entries.get(j))) {
                                was[j] = true;
                                usedGroup[id[j]] = true;
                            }
                    }

                    mapList.add(map);
                }
        }

        for(HashMap<String, String> mapElement : mapList) {
            StringBuilder st = new StringBuilder();

            int colCounter = 0;
            for(String colName : mapElement.keySet()) {
                st.append(colName);

                if(++colCounter < mapElement.size())
                    st.append(", ");
            }

            String sql = "SELECT " + st.toString() + " FROM %s WHERE %s%s_id = ?";

            if(state == HistoryState.ACTUAL) {
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());
            } else {
                sql += " AND open_date = ?";
                sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName(), entity.getReportDate());
            }

            Map dbElement = jdbcTemplateSC.queryForMap(sql, recordId);

            for(String colName : mapElement.keySet()) {
                Object newValue = mapElement.get(colName);
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
        }

        return equalityFlag;
    }

    private String getDBType(IMetaType type) {
        if (type.isComplex())
            throw new IllegalArgumentException("ShowCase can't contain coplexType columns: "
                    + type.toString());

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
        if (type.isComplex())
            throw new IllegalArgumentException("ShowCase can't contain coplexType columns");

        if (type instanceof MetaSet)
            type = ((MetaSet) type).getMemberType();


        if (type.isSet())
            throw new IllegalArgumentException("ShowCase can't contain set columns");

        MetaValue metaValue = (MetaValue) type;

        switch (metaValue.getTypeCode()) {

            case INTEGER:
                return "10,0";
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
                .select(EAV_SC_SHOWCASE_FIELDS.ID, EAV_SC_SHOWCASE_FIELDS.TITLE, EAV_SC_SHOWCASE_FIELDS.NAME,
                        EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME, EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID,
                        EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_NAME, EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH,
                        EAV_SC_SHOWCASE_FIELDS.TYPE)
                .from(EAV_SC_SHOWCASE_FIELDS)
                .where(EAV_SC_SHOWCASE_FIELDS.SHOWCASE_ID.equal(showCase.getId()));

        logger.debug(select.toString());

        rows = jdbcTemplateSC.queryForList(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 0) {
            for (Map<String, Object> curRow : rows) {
                ShowCaseField showCaseField = new ShowCaseField();
                showCaseField.setName((String) curRow.get(EAV_SC_SHOWCASE_FIELDS.NAME.getName()));
                showCaseField.setTitle((String) curRow.get(EAV_SC_SHOWCASE_FIELDS.TITLE.getName()));
                showCaseField.setColumnName((String) curRow.get(EAV_SC_SHOWCASE_FIELDS.COLUMN_NAME.getName()));
                showCaseField.setType(((BigDecimal) curRow.get(EAV_SC_SHOWCASE_FIELDS.TYPE.getName())).intValue());
                showCaseField.setAttributeName((String) curRow.get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_NAME.getName()));

                if (curRow.get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH.getName()) != null) {
                    showCaseField.setAttributePath((String) curRow.
                            get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_PATH.getName()));
                } else {
                    showCaseField.setAttributePath("");
                }

                showCaseField.setAttributeId(((BigDecimal) curRow
                        .get(EAV_SC_SHOWCASE_FIELDS.ATTRIBUTE_ID.getName())).longValue());

                showCaseField.setId(((BigDecimal) curRow
                        .get(EAV_SC_SHOWCASE_FIELDS.ID.getName())).longValue());

                if (showCaseField.getType() == ShowCaseField.ShowCaseFieldTypes.CUSTOM) {
                    showCase.addCustomField(showCaseField);
                } else if (showCaseField.getType() == ShowCaseField.ShowCaseFieldTypes.FILTER) {
                    showCase.addFilterField(showCaseField);
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
    public void remove(ShowCase showCase) {
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
                .set(EAV_SC_SHOWCASE_FIELDS.TITLE, showCaseField.getTitle())
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

        for (ShowCaseField sf : showCase.getFilterFieldsList())
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

        for(ShowCaseField sf : showCaseSaving.getFilterFieldsList())
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

    class ShowCaseEntries {
        public final String columnName;
        final List<HashMap> entries = new ArrayList<>();
        final Map<String, String> prefixToColumn;

        ShowCaseEntries(IBaseEntity entity, String path, String columnName, Map<String, String> prefixToColumn) {
            this.columnName = columnName;
            if (path.startsWith("."))
                path = path.substring(1);
            this.prefixToColumn = prefixToColumn;
            gen(entity, path, new HashMap(), "root");
        }

        public List<HashMap> getEntries() {
            return entries;
        }

        public int getEntriesSize() {
            return entries.size();
        }

        public void gen(IBaseEntity entity, String curPath, HashMap map, String prefix) {
            if (curPath == null || curPath.equals("") || entity == null) {
                if (entity != null)
                    map.put(prefixToColumn.get(prefix) + "_id", entity.getId());
                entries.add(map);
                return;
            }

            MetaClass curMeta = entity.getMeta();
            String path = (curPath.indexOf('.') == -1) ? curPath : curPath.substring(0, curPath.indexOf('.'));
            String nextPath = curPath.indexOf('.') == -1 ? null : curPath.substring(curPath.indexOf('.') + 1);
            IMetaAttribute attribute = curMeta.getMetaAttribute(path);

            if(prefixToColumn.get(prefix) != null)
                map.put(prefixToColumn.get(prefix) + "_id", entity.getId());

            if (!attribute.getMetaType().isComplex()) {
                map.put(prefixToColumn.get(prefix) + "_id", entity.getId());
                if (!attribute.getMetaType().isSet()) {
                    map.put(columnName, entity.getEl(path));
                    entries.add(map);
                } else {
                    IBaseSet set = (IBaseSet) entity.getEl(path);

                    if (set != null) {
                        for (IBaseValue o : set.get()) {
                            HashMap nmap = (HashMap) map.clone();
                            nmap.put(columnName, o.getValue());
                            entries.add(nmap);
                        }
                    }
                }
            } else if (attribute.getMetaType().isSet()) {
                IBaseSet next = (IBaseSet) entity.getEl(path);

                if (next != null) {
                    for (Object o : next.get()) {
                        gen((IBaseEntity) ((IBaseValue) o).getValue(), nextPath, (HashMap) map.clone(),
                                prefix + "." + path);
                    }
                }
            } else {
                IBaseEntity next = (IBaseEntity) entity.getEl(path);
                gen(next, nextPath, (HashMap) map.clone(), prefix + "." + path);
            }
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
