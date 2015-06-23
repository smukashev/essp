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

import static kz.bsbnb.usci.showcase.generated.Tables.*;

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

        if (!showcaseHolder.getShowCaseMeta().isFinal())
            createTable(HistoryState.HISTORY, showcaseHolder);

        getHolders().add(showcaseHolder);
    }

    private void createTable(HistoryState historyState, ShowcaseHolder showcaseHolder) {
        String tableName;

        if (historyState == HistoryState.ACTUAL) {
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
        entityIdColumn.setPrimaryKey(false);
        entityIdColumn.setRequired(true);
        entityIdColumn.setType("NUMERIC");
        entityIdColumn.setSize("14,0");
        entityIdColumn.setAutoIncrement(false);

        table.addColumn(entityIdColumn);

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            Column column = new Column();
            column.setName(COLUMN_PREFIX + field.getColumnName().toUpperCase());
            column.setPrimaryKey(false);
            column.setRequired(false);

            IMetaType metaType = showcaseHolder.getShowCaseMeta().getActualMeta().getEl(field.getAttributePath());

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

        for (ShowCaseField field : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
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

        if (!showcaseHolder.getShowCaseMeta().isFinal()) {
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

    void persistMap(HashMap<ValueElement, Object> map, Date openDate, Date closeDate, ShowcaseHolder showCaseHolder,
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

        if (!showCaseHolder.getShowCaseMeta().isFinal()) {
            vals = new Object[map.size() + 2 + showCaseHolder.getShowCaseMeta().getCustomFieldsList().size()];
        } else {
            vals = new Object[map.size() + 1 + showCaseHolder.getShowCaseMeta().getCustomFieldsList().size()];
        }

        int i = 0;

        for (Map.Entry<ValueElement, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey().columnName).append(", ");
            values.append("?, ");
            vals[i++] = entry.getValue();
        }

        for (ShowCaseField sf : showCaseHolder.getShowCaseMeta().getCustomFieldsList()) {
            if (sf.getAttributePath().equals("root")) {
                sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                vals[i++] = entity.getId();
                values.append("?, ");
                continue;
            }

            Object customObject = entity.getEl(sf.getAttributePath());

            try {
                if (customObject instanceof BaseEntity) {
                    sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                    values.append("?, ");
                    vals[i++] = ((BaseEntity) customObject).getId();
                } else if (customObject instanceof BaseSet) {
                    throw new UnsupportedOperationException("CustomSet is not supported!");
                } else {
                    sql.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
                    values.append("?, ");
                    vals[i++] = customObject;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!showCaseHolder.getShowCaseMeta().isFinal()) {
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

    @Transactional
    void updateMapLeftRange(HistoryState historyState, KeyData keyData, IBaseEntity entity, ShowcaseHolder showCaseHolder) {
        String sql = "UPDATE %s SET close_date = ? WHERE " + keyData.queryKeys;

        if (historyState == HistoryState.ACTUAL) {
            sql = String.format(sql, getActualTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());
        } else {
            sql = String.format(sql, getHistoryTableName(showCaseHolder.getShowCaseMeta()), COLUMN_PREFIX,
                    showCaseHolder.getRootClassName());
        }

        jdbcTemplateSC.update(sql, getObjectArray(true, keyData.vals, entity.getReportDate()));
    }

    String getActualTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName();
    }

    String getHistoryTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName() + HISTORY_POSTFIX;
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
        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getCustomFieldsList()) {
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
    public void generate(IBaseEntity globalEntity, ShowcaseHolder showcaseHolder) {
        List<BaseEntity> all;

        if (showcaseHolder.getShowCaseMeta().getDownPath() != null) {
            all = (List<BaseEntity>) globalEntity.getEls("{get}" +
                    showcaseHolder.getShowCaseMeta().getDownPath());
            for (BaseEntity baseEntity : all)
                dbCarteageGenerate(globalEntity, baseEntity, showcaseHolder);
        } else {
            dbCarteageGenerate(globalEntity, globalEntity, showcaseHolder);
        }
    }

    public Object[] getObjectArray(boolean reverse, Object[] elementArray, Object... elements) {
        Object[] newObjectArray = new Object[elementArray.length + elements.length];

        int index = 0;
        if (!reverse) {
            for (Object object : elementArray) newObjectArray[index++] = object;
            for (Object object : elements) newObjectArray[index++] = object;
        } else {
            for (Object object : elements) newObjectArray[index++] = object;
            for (Object object : elementArray) newObjectArray[index++] = object;
        }

        return newObjectArray;
    }

    @Transactional
    void dbCarteageGenerate(IBaseEntity globalEntity, IBaseEntity entity, ShowcaseHolder showcaseHolder) {
        Date openDate = null, closeDate = null;
        String sql;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> savingMap = generateMap(entity, showcaseHolder);

        if (savingMap == null || savingMap.size() == 0)
            return;

        for (Map.Entry entry : savingMap.entrySet()) {
            HashMap<ValueElement, Object> entryMap = (HashMap) entry.getValue();

            KeyData keyData = new KeyData(entryMap);

            if (!showcaseHolder.getShowCaseMeta().isFinal()) {
                try {
                    sql = "SELECT MAX(open_date) AS open_date FROM %s WHERE " + keyData.queryKeys;
                    sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName().toUpperCase());

                    openDate = (Date) jdbcTemplateSC.queryForMap(sql, keyData.vals).get("OPEN_DATE");
                } catch (EmptyResultDataAccessException e) {
                    openDate = null;
                }

                boolean compResult;

                if (openDate == null) {
                    openDate = entity.getReportDate();
                } else if (openDate.compareTo(entity.getReportDate()) == 0) {
                    openDate = entity.getReportDate();

                    sql = "DELETE FROM %s WHERE " + keyData.queryKeys + " and open_date = ?";
                    sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName());

                    jdbcTemplateSC.update(sql, getObjectArray(false, keyData.vals, openDate));
                } else if (openDate.compareTo(entity.getReportDate()) < 0) {
                    compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showcaseHolder, keyData);

                    if (compResult) continue;

                    updateMapLeftRange(HistoryState.ACTUAL, keyData, entity, showcaseHolder);
                    moveActualMapToHistory(keyData, entity, showcaseHolder);

                    openDate = entity.getReportDate();
                } else {
                    sql = "SELECT MIN(open_date) as open_date FROM %s WHERE " + keyData.queryKeys +
                            " AND open_date > ? ";

                    sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                            COLUMN_PREFIX, showcaseHolder.getRootClassName());

                    closeDate = (Date) jdbcTemplateSC.queryForMap(sql,
                            getObjectArray(false, keyData.vals, entity.getReportDate())).get("OPEN_DATE");

                    if (closeDate == null) {
                        compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showcaseHolder, keyData);

                        if (compResult) {
                            sql = "UPDATE %s SET open_date = ? WHERE " + keyData.queryKeys + " AND open_date = ?";
                            sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, keyData.vals,
                                    entity.getReportDate()), openDate));

                            continue;
                        } else {
                            closeDate = openDate;
                        }
                    } else {
                        compResult = compareValues(HistoryState.HISTORY, entryMap, entity, showcaseHolder, keyData);

                        if (compResult) {
                            sql = "UPDATE %s SET open_date = ? WHERE " + keyData.queryKeys + " AND open_date = ?";
                            sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                                    COLUMN_PREFIX, showcaseHolder.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, keyData.vals,
                                    entity.getReportDate()), closeDate));

                            continue;
                        } else {
                            closeDate = openDate;
                        }
                    }

                    openDate = entity.getReportDate();
                    updateMapLeftRange(HistoryState.HISTORY, keyData, entity, showcaseHolder);
                }
            } else {
                openDate = entity.getReportDate();

                sql = "DELETE FROM %s WHERE " + keyData.queryKeys + " and rep_date = ?";
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                jdbcTemplateSC.update(sql, getObjectArray(false, keyData.vals, openDate));
            }

            persistMap(entryMap, openDate, closeDate, showcaseHolder, globalEntity);
        }
    }

    public HashMap<String, HashSet<PathElement>> generatePaths(IBaseEntity entity, ShowcaseHolder showcaseHolder,  HashSet<PathElement> keyPaths) {
        HashMap<String, HashSet<PathElement>> paths = new HashMap<>();

        HashSet<PathElement> tmpSet;

        for (ShowCaseField sf : showcaseHolder.getShowCaseMeta().getFieldsList()) {
            IMetaType attributeMetaType = entity.getMeta().getEl(sf.getAttributePath());

            if (sf.getAttributePath().contains(".")) {
                if (attributeMetaType.isComplex()) {
                    if (paths.get("root." + sf.getAttributePath()) != null) {
                        tmpSet = paths.get("root." + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + sf.getAttributePath(), tmpSet);

                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + path, tmpSet);
                } else {
                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName(), false));
                    paths.put("root." + path, tmpSet);
                }
            } else {
                if (paths.get("root") != null) {
                    tmpSet = paths.get("root");
                } else {
                    tmpSet = new HashSet<>();
                }

                if(attributeMetaType.isSet()) {
                    keyPaths.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName(), true));

                    tmpSet.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName(), false));
                    paths.put("root", tmpSet);

                    tmpSet = new HashSet<>();
                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName(), true));
                    paths.put("root." + sf.getAttributePath(), tmpSet);
                } else if (attributeMetaType.isComplex()) {
                    tmpSet.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName(), false));
                    paths.put("root", tmpSet);

                    tmpSet = new HashSet<>();
                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName(), true));
                    paths.put("root." + sf.getAttributePath(), tmpSet);
                } else {
                    tmpSet.add(new PathElement(sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName(), false));

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

        if (attributes != null) {
            for (PathElement attribute : attributes) {
                if (entity != null && attribute.elementPath.equals("root")) {
                    map.put(new ValueElement(attribute.columnName, entity.getId(), !curPath.contains(".")
                            || parentIsArray), entity.getId());
                } else {
                    if (attribute.elementPath.contains("root.")) {
                        Object container = entity.getEl(attribute.elementPath.substring(
                                attribute.elementPath.indexOf(".") + 1));

                        if (container == null) continue;

                        if (container instanceof BaseEntity) {
                            BaseEntity innerEntity = (BaseEntity) container;

                            if (innerEntity != null) {
                                map.put(new ValueElement(attribute.columnName, innerEntity.getId(), false),
                                        readMap(attribute.elementPath, innerEntity, paths, false));
                            }
                        } else if (container instanceof BaseSet) {
                            BaseSet innerSet = (BaseSet) container;

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            if (innerSet.getMemberType().isComplex()) {
                                for (IBaseValue bValue : innerSet.get()) {
                                    BaseEntity bValueEntity = (BaseEntity) bValue.getValue();
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValueEntity.getId(), true,
                                            false), readMap(attribute.elementPath, bValueEntity, paths, true));
                                }

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), false,
                                        true, false), arrayMap);
                            } else {
                                for (IBaseValue bValue : innerSet.get())
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValue.getId(), false, false),
                                            bValue.getValue());

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), false,
                                        true, true), arrayMap);
                            }
                        }
                    } else {
                        IBaseValue iBaseValue = entity.getBaseValue(attribute.elementPath);

                        if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                !iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId(), false)
                                    , readMap(curPath + "." + attribute.elementPath,
                                    (BaseEntity) iBaseValue.getValue(), paths, false));
                        } else if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            throw new UnsupportedOperationException("Complex entity cannot contain complex set");
                        } else if (iBaseValue != null && iBaseValue.getValue() instanceof BaseSet) {
                            BaseSet bSet = (BaseSet) iBaseValue.getValue();

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            for (IBaseValue innerValue : bSet.get())
                                arrayMap.put(new ValueElement(attribute.elementPath, innerValue.getId(),
                                        false, false, true), innerValue.getValue());

                            map.put(new ValueElement(attribute.elementPath, iBaseValue.getId(), false, true, true),
                                    arrayMap);
                        } else if (iBaseValue != null) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId(), false),
                                    iBaseValue.getValue());
                        }
                    }
                }
            }
        }

        return map;
    }

    public HashMap<ArrayElement, HashMap<ValueElement, Object>> gen(HashMap<ValueElement, Object> dirtyMap) {
        HashMap<ArrayElement, HashMap<ValueElement, Object>> arrayEl = new HashMap<>();

        int index = 0;

        Iterator mainIterator = dirtyMap.entrySet().iterator();
        while (mainIterator.hasNext()) {
            Map.Entry<ValueElement, Object> entry = (Map.Entry) mainIterator.next();

            if (entry.getKey().isArray && !entry.getKey().isSimple) {
                HashMap<ValueElement, Object> innerMap = (HashMap) entry.getValue();
                Iterator innerIterator = innerMap.entrySet().iterator();

                while (innerIterator.hasNext()) {
                    Map.Entry<ValueElement, Object> innerEntry = (Map.Entry) innerIterator.next();
                    HashMap<ArrayElement, HashMap<ValueElement, Object>> recursiveMap =
                            gen((HashMap) innerEntry.getValue());

                    for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> recEntry : recursiveMap.entrySet()) {
                        arrayEl.put(new ArrayElement(index++, innerEntry.getKey()), (HashMap) recEntry.getValue());
                    }
                }
            } else if (entry.getKey().isArray && entry.getKey().isSimple) {
                HashMap<ValueElement, Object> innerMap = (HashMap) entry.getValue();
                Iterator innerIterator = innerMap.entrySet().iterator();

                while (innerIterator.hasNext()) {
                    Map.Entry<ValueElement, Object> innerEntry = (Map.Entry) innerIterator.next();
                    HashMap<ValueElement, Object> newHashMap = new HashMap<>();
                    newHashMap.put(innerEntry.getKey(), innerEntry.getValue());

                    newHashMap.put(new ValueElement(innerEntry.getKey().columnName + "_id",
                            innerEntry.getKey().elementId, true), innerEntry.getKey().elementId);

                    arrayEl.put(new ArrayElement(index++, innerEntry.getKey()), newHashMap);
                }
            }
        }

        if (arrayEl.size() > 0) {
            Iterator simpleIterator = dirtyMap.entrySet().iterator();
            while (simpleIterator.hasNext()) {
                Map.Entry<ValueElement, Object> entry = (Map.Entry) simpleIterator.next();

                if (!entry.getKey().isArray) {
                    for (ArrayElement element : arrayEl.keySet()) {
                        HashMap<ValueElement, Object> tempMap = arrayEl.get(element);
                        tempMap.put(entry.getKey(), entry.getValue());
                        arrayEl.put(element, tempMap);
                    }
                }
            }
        } else {
            HashMap<ValueElement, Object> singleMap = new HashMap<>();
            Iterator simpleIterator = dirtyMap.entrySet().iterator();
            while (simpleIterator.hasNext()) {
                Map.Entry<ValueElement, Object> entry = (Map.Entry) simpleIterator.next();

                singleMap.put(entry.getKey(), entry.getValue());
            }

            arrayEl.put(new ArrayElement(index, new ValueElement("root", 0L, true)), singleMap);
        }

        return arrayEl;
    }

    public HashMap<ValueElement, Object> clearDirtyMap(HashMap<ValueElement, Object> dirtyMap) {
        HashMap<ValueElement, Object> returnMap = new HashMap<>();

        Iterator iter = dirtyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ValueElement, Object> entry = (Map.Entry) iter.next();

            if (entry.getValue() instanceof HashMap) {
                HashMap<ValueElement, Object> tmpMap = clearDirtyMap((HashMap) entry.getValue());

                for(Map.Entry<ValueElement, Object> tmpMapEntry : tmpMap.entrySet())
                    returnMap.put(tmpMapEntry.getKey(), tmpMapEntry.getValue());

            } else {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }

        return returnMap;
    }

    public HashMap<ArrayElement, HashMap<ValueElement, Object>> generateMap(IBaseEntity entity,
                                                                            ShowcaseHolder showcaseHolder) {
        HashSet<PathElement> keyPaths = new HashSet<>();
        HashMap<String, HashSet<PathElement>> paths = generatePaths(entity, showcaseHolder, keyPaths);

        HashSet<PathElement> rootAttributes = paths.get("root");
        rootAttributes.add(new PathElement("root", "root", entity.getMeta().getClassName() + "_id", true));

        keyPaths.add(new PathElement("root", "root", entity.getMeta().getClassName() + "_id", true));

        HashMap<ValueElement, Object> dirtyMap = readMap("root", entity, paths, false);

        if (dirtyMap == null)
            return null;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> globalMap = gen(dirtyMap);
        HashMap<ArrayElement, HashMap<ValueElement, Object>> clearedGlobalMap = new HashMap<>();

        for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> globalEntry : globalMap.entrySet()) {
            HashMap<ValueElement, Object> tmpMap = clearDirtyMap(globalEntry.getValue());
            boolean hasMandatoryKeys = true;

            for(PathElement pElement : keyPaths) {
                boolean eFound = false;
                for(ValueElement vElement : tmpMap.keySet()) {
                    if(vElement.columnName.equals(pElement.columnName)) {
                        eFound = true;
                        break;
                    }
                }

                if(!eFound) {
                    hasMandatoryKeys = false;
                    break;
                }
            }

            if(hasMandatoryKeys)
                clearedGlobalMap.put(globalEntry.getKey(), tmpMap);
        }

        return clearedGlobalMap;
    }

    public boolean compareValues(HistoryState state, HashMap<ValueElement, Object> savingMap,
                                 IBaseEntity entity, ShowcaseHolder showcaseHolder, KeyData keyData) {
        StringBuilder st = new StringBuilder();
        boolean equalityFlag = true;

        try {
            int colCounter = 0;
            for (ValueElement valueElement : savingMap.keySet()) {
                st.append(valueElement.columnName);

                if (++colCounter < savingMap.size())
                    st.append(", ");
            }

            String sql = "SELECT " + st.toString() + " FROM %s WHERE " + keyData.queryKeys;

            Map dbElement = null;

            if (state == HistoryState.ACTUAL) {
                sql = String.format(sql, getActualTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName());

                dbElement = jdbcTemplateSC.queryForMap(sql, keyData.vals);
            } else {
                sql += " AND open_date = ?";
                sql = String.format(sql, getHistoryTableName(showcaseHolder.getShowCaseMeta()),
                        COLUMN_PREFIX, showcaseHolder.getRootClassName(), entity.getReportDate());

                dbElement = jdbcTemplateSC.queryForMap(sql, getObjectArray(false, keyData.vals, entity.getReportDate()));
            }

            for (ValueElement valueElement : savingMap.keySet()) {
                Object newValue = savingMap.get(valueElement);
                Object dbValue = dbElement.get(valueElement.columnName);

                if (newValue == null && dbValue == null)
                    continue;

                if (newValue == null || dbValue == null) {
                    equalityFlag = false;
                    break;
                }

                if (newValue instanceof Double) {
                    if (!Double.valueOf((Double) newValue).equals(Double.valueOf(dbValue.toString()))) {
                        equalityFlag = false;
                        break;
                    }
                } else if (newValue instanceof Integer) {
                    if (!Integer.valueOf((Integer) newValue).equals(Integer.valueOf(dbValue.toString()))) {
                        equalityFlag = false;
                        break;
                    }
                } else if (newValue instanceof Boolean) {
                    if (!Boolean.valueOf((Boolean) newValue).equals(Boolean.valueOf(dbValue.toString()))) {
                        equalityFlag = false;
                        break;
                    }
                } else if (newValue instanceof Long) {
                    if (!Long.valueOf((Long) newValue).equals(Long.valueOf(dbValue.toString()))) {
                        equalityFlag = false;
                        break;
                    }
                } else if (newValue instanceof String) {
                    if (!newValue.toString().equals(dbValue.toString())) {
                        equalityFlag = false;
                        break;
                    }
                } else if (newValue instanceof Date) {
                    if (!newValue.equals(new Date(((Timestamp) dbValue).getTime()))) {
                        equalityFlag = false;
                        break;
                    }
                } else {
                    if (!newValue.equals(dbValue)) {
                        equalityFlag = false;
                        break;
                    }
                }
            }

            return equalityFlag;
        } catch (Exception e) {
            System.err.println("ERROR ENTITY ID: " + entity.getId() +", " + showcaseHolder.getShowCaseMeta().getTableName());
            System.err.println(keyData.queryKeys);

            for(Object o : keyData.vals)
                System.err.print(o + ", ");

            System.out.println("-------------------------");
            e.printStackTrace();
            System.out.println("-------------------------");

            return false;
        }
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
        if (type.isComplex())
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
    public void remove(ShowCase showCase) {
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

        for (ShowCaseField sf : showCaseSaving.getCustomFieldsList())
            insertField(sf, showCaseSaving.getId());
    }

    long insertWithId(String query, Object[] values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplateSC.update(new GenericInsertPreparedStatementCreator(query, values), keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Long insertBadEntity(Long entityId, Long scId, Date report_date, String stackTrace, String message) {
        if (scId == null)
            scId = 0L;

        Insert insert = context
                .insertInto(EAV_SC_BAD_ENTITIES)
                .set(EAV_SC_BAD_ENTITIES.ENTITY_ID, entityId)
                .set(EAV_SC_BAD_ENTITIES.SC_ID, scId)
                .set(EAV_SC_BAD_ENTITIES.REPORT_DATE, DataUtils.convert(report_date))
                .set(EAV_SC_BAD_ENTITIES.STACK_TRACE, stackTrace)
                .set(EAV_SC_BAD_ENTITIES.MESSAGE, message);

        if (logger.isDebugEnabled())
            logger.debug(insert.toString());

        return insertWithId(insert.getSQL(), insert.getBindValues().toArray());
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

    public enum HistoryState {
        ACTUAL,
        HISTORY
    }

    class KeyData {
        Object[] keys;
        Object[] vals;
        String queryKeys = "";

        public KeyData(HashMap<ValueElement, Object> map) {
            int keySize = 0;

            for (ValueElement valueElement : map.keySet()) if (valueElement.isKey) keySize++;

            keys = new Object[keySize];
            vals = new Object[keySize];

            int keyCounter = 0;
            for (Map.Entry<ValueElement, Object> entry : map.entrySet()) {
                if (entry.getKey().isKey) {
                    keys[keyCounter] = entry.getKey().columnName;
                    vals[keyCounter] = map.get(entry.getKey());

                    queryKeys += entry.getKey().columnName + " = ? ";
                    if (++keyCounter < keySize) queryKeys += " AND ";

                }
            }
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
            return elementPath + ", " + columnName + ", " + isKey;
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

    class ArrayElement {
        public int index;
        public ValueElement valueElement;

        public ArrayElement(int index, ValueElement valueElement) {
            this.index = index;
            this.valueElement = valueElement;
        }

        @Override
        public String toString() {
            return index + ", " + valueElement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ArrayElement that = (ArrayElement) o;

            if (index != that.index) return false;
            return valueElement.equals(that.valueElement);

        }

        @Override
        public int hashCode() {
            int result = valueElement.hashCode();
            result = 31 * result + index;
            return result;
        }
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
}
