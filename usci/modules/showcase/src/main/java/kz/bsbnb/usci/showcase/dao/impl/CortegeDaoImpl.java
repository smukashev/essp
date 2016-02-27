package kz.bsbnb.usci.showcase.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.showcase.dao.CommonDao;
import kz.bsbnb.usci.showcase.element.ArrayElement;
import kz.bsbnb.usci.showcase.element.KeyElement;
import kz.bsbnb.usci.showcase.element.PathElement;
import kz.bsbnb.usci.showcase.element.ValueElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Component
public class CortegeDaoImpl extends CommonDao {
    private JdbcTemplate jdbcTemplateSC;

    /* Same showcases could not be processed in parallel */
    private static final Set<Long> cortegeElements = Collections.synchronizedSet(new HashSet<Long>());

    @Autowired
    public void setDataSourceSC(DataSource dataSourceSC) {
        this.jdbcTemplateSC = new JdbcTemplate(dataSourceSC);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void generate(IBaseEntity globalEntityApplied, ShowCase showCase) {
        if (showCase.isChild()) {
            childCortegeGenerate(globalEntityApplied, showCase);
        } else if (showCase.getDownPath() != null) {
            List<BaseEntity> allApplied = (List<BaseEntity>) globalEntityApplied.getEls("{get}" + showCase.getDownPath(), true);

            for (BaseEntity baseEntityApplied : allApplied)
                rootCortegeGenerate(globalEntityApplied, baseEntityApplied, showCase);
        } else {
            rootCortegeGenerate(globalEntityApplied, globalEntityApplied, showCase);
        }
    }

    /* Performs main operations on showcase  */
    @Transactional
    private void rootCortegeGenerate(IBaseEntity globalEntity, IBaseEntity entity, ShowCase showCase) {
        Date openDate = null, closeDate = null;
        String sql;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> savingMap = generateMap(entity, showCase);

        if (savingMap == null || savingMap.size() == 0)
            return;

        waitShowCase(showCase.getId());

        boolean rootExecutionFlag = false;

        try {
            for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> entry : savingMap.entrySet()) {
                HashMap<ValueElement, Object> entryMap = entry.getValue();

                addCustomKeys(entryMap, globalEntity, showCase);

                KeyElement rootKeyElement = new KeyElement(entryMap, showCase.getRootKeyFieldsList());
                KeyElement historyKeyElement = new KeyElement(entryMap, showCase.getHistoryKeyFieldsList());

                ValueElement keyValueElement = new ValueElement("_operation", -1L, false);

                if (entryMap.containsKey(keyValueElement)) {
                    OperationType ot = (OperationType) entryMap.get(keyValueElement);
                    switch (ot) {
                        case DELETE:
                            sql = "DELETE FROM %s WHERE " + rootKeyElement.queryKeys;
                            sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX,
                                    showCase.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, rootKeyElement.values));
                            break;
                        case CLOSE:
                            sql = "UPDATE %s SET close_date = ? WHERE " + rootKeyElement.queryKeys;
                            sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                            jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, rootKeyElement.values, entity.getReportDate())));

                            moveActualMapToHistory(rootKeyElement, showCase);
                            break;
                        default:
                            throw new IllegalStateException("Операция не поддерживается(" + ot + ")!;");
                    }
                    continue;
                }

                if (!rootExecutionFlag) {
                    if (!showCase.isFinal()) {
                        sql = "DELETE FROM %s WHERE " + rootKeyElement.queryKeys + " and open_date = ?";
                    } else {
                        sql = "DELETE FROM %s WHERE " + rootKeyElement.queryKeys + " and rep_date = ?";
                    }

                    sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                    jdbcTemplateSC.update(sql, getObjectArray(false, rootKeyElement.values, entity.getReportDate()));

                    rootExecutionFlag = true;
                }


                if (!showCase.isFinal()) {
                    try {
                        sql = "SELECT MAX(open_date) AS open_date FROM %s WHERE " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getActualTableName(showCase),
                                COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        openDate = (Date) jdbcTemplateSC.queryForMap(sql, historyKeyElement.values).get("OPEN_DATE");
                    } catch (EmptyResultDataAccessException e) {
                        openDate = null;
                    }

                    boolean compResult;

                    if (openDate == null) {
                        openDate = entity.getReportDate();
                    } else if (openDate.compareTo(entity.getReportDate()) < 0) { // forward
                        compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showCase, historyKeyElement);

                        if (compResult) continue;

                        updateMapLeftRange(HistoryState.ACTUAL, historyKeyElement, entity, showCase);
                        moveActualMapToHistory(historyKeyElement, showCase);

                        openDate = entity.getReportDate();
                    } else { // backward
                        sql = "SELECT MIN(open_date) as open_date FROM %s WHERE " + historyKeyElement.queryKeys +
                                " AND open_date > ? ";

                        sql = String.format(sql, getHistoryTableName(showCase),
                                COLUMN_PREFIX, showCase.getRootClassName());

                        closeDate = (Date) jdbcTemplateSC.queryForMap(sql,
                                getObjectArray(false, historyKeyElement.values, entity.getReportDate())).get("OPEN_DATE");

                        if (closeDate == null) {
                            compResult = compareValues(HistoryState.ACTUAL, entryMap, entity, showCase, rootKeyElement);

                            if (compResult) {
                                sql = "UPDATE %s SET open_date = ? WHERE " + historyKeyElement.queryKeys + " AND open_date = ?";
                                sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                                jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, historyKeyElement.values,
                                        entity.getReportDate()), openDate));

                                continue;
                            } else {
                                closeDate = openDate;
                            }
                        } else {
                            compResult = compareValues(HistoryState.HISTORY, entryMap, entity, showCase, rootKeyElement);

                            if (compResult) {
                                sql = "UPDATE %s SET open_date = ? WHERE " + rootKeyElement.queryKeys + " AND open_date = ?";
                                sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                                jdbcTemplateSC.update(sql, getObjectArray(false, getObjectArray(true, rootKeyElement.values,
                                        entity.getReportDate()), closeDate));

                                continue;
                            } else {
                                closeDate = openDate;
                            }
                        }

                        openDate = entity.getReportDate();
                        updateMapLeftRange(HistoryState.HISTORY, rootKeyElement, entity, showCase);
                    }
                }

                persistMap(entryMap, openDate, closeDate, showCase);
            }
        } finally {
            removeShowCase(showCase.getId());
        }
    }

    @Transactional
    private void childCortegeGenerate(IBaseEntity entity, ShowCase showCase) {
        String sql;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> savingMap = generateMap(entity, showCase);

        if (savingMap == null || savingMap.size() == 0)
            return;

        waitShowCase(showCase.getId());

        try {
            for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> entry : savingMap.entrySet()) {
                HashMap<ValueElement, Object> entryMap = entry.getValue();

                for (ShowCaseField sf : showCase.getRootKeyFieldsList()) {
                    ValueElement oldKey = null, newKey = null;
                    for (Map.Entry<ValueElement, Object> innerEntry : entryMap.entrySet()) {
                        if (innerEntry.getKey().columnName.equals(sf.getAttributePath())) {
                            oldKey = innerEntry.getKey();

                            newKey = new ValueElement(sf.getColumnName(), oldKey.elementId, oldKey.isArray, oldKey.isSimple);
                        }
                    }
                    entryMap.put(newKey, entryMap.remove(oldKey));
                }

                KeyElement rootKeyElement = new KeyElement(entryMap, showCase.getRootKeyFieldsList());

                // compare == 0 update
                //updateCurrentData(entity, rootKeyElement, entryMap, showCase);

                // compare == 1 go for each put history, insert news to actual
                if (!showCase.isFinal())
                    updateHistoryData(entity, rootKeyElement, entryMap, showCase);

                // update histories, if there is nothing, insert

            }
        } finally {
            removeShowCase(showCase.getId());
        }
    }

    @Transactional
    private void updateHistoryData (IBaseEntity entity, KeyElement rootKeyElement, HashMap<ValueElement, Object> newMap, ShowCase showCase) {
        String sql = "SELECT * FROM %s WHERE " + rootKeyElement.queryKeys + " and open_date < ?"; // todo: fix <=
        sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        List<Map<String, Object>> resultList = jdbcTemplateSC.queryForList(sql, getObjectArray(false, rootKeyElement.values, entity.getReportDate()));

        for (Map<String, Object> resultMap : resultList) {
            boolean equalityFlag = true;

            for (Map.Entry<ValueElement, Object> newEntry : newMap.entrySet()) {
                Object newValue = newEntry.getValue();
                Object dbValue = resultMap.get(newEntry.getKey().columnName.toUpperCase());

                if (newValue == null && dbValue == null) continue;

                if (newValue == null || dbValue == null) {
                    equalityFlag  = false;
                    break;
                }

                if (!compareValue(newValue, dbValue)) {
                    equalityFlag = false;
                    break;
                }
            }

            if (!equalityFlag) {
                Object actualRecordId = resultMap.remove("ID");
                resultMap.remove("CDC");
                resultMap.put("CLOSE_DATE", entity.getReportDate());

                simpleInsert(resultMap, showCase, getHistoryTableName(showCase));

                sql = "DELETE FROM %s WHERE ID = ?";
                sql = String.format(sql, getActualTableName(showCase));
                jdbcTemplateSC.update(sql, actualRecordId);

                for (Map.Entry<ValueElement, Object> newEntry : newMap.entrySet())
                    resultMap.put(newEntry.getKey().columnName.toUpperCase(), newEntry.getValue());

                resultMap.remove("CLOSE_DATE");
                resultMap.put("OPEN_DATE", entity.getReportDate());

                simpleInsert(resultMap, showCase, getActualTableName(showCase));
            }
        }
    }

    @Transactional
    private void simpleInsert(Map<String, Object> map, ShowCase showCase, String tableName) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(");
        StringBuilder values = new StringBuilder("(");

        Object[] valueArray = new Object[map.size()];

        int i = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey()).append(", ");
            values.append("?, ");
            valueArray[i++] = entry.getValue();
        }

        if (!showCase.isFinal()) {
            sql.append("CDC");
            values.append("SYSDATE)");
        } else {
            sql.append("CDC");
            values.append("SYSDATE)");
        }

        sql.append(") VALUES ").append(values);

        jdbcTemplateSC.update(sql.toString(), valueArray);
    }

    private void updateCurrentData (IBaseEntity entity, KeyElement rootKeyElement, HashMap<ValueElement, Object> entryMap, ShowCase showCase) {
        String sql = "UPDATE %s SET ";

        Object values[] = new Object[entryMap.size()];

        int valuesIndex = 0;

        for (Map.Entry<ValueElement, Object> innerEntry : entryMap.entrySet()) {
            sql += innerEntry.getKey().columnName + " = ? ";
            values[valuesIndex++] = innerEntry.getValue();
            if (valuesIndex < entryMap.size()) sql += ", ";
        }

        if (!showCase.isFinal()) {
            sql += "WHERE " + rootKeyElement.queryKeys + " and open_date = ?";
        } else {
            sql += "WHERE " + rootKeyElement.queryKeys + " and rep_date = ?";
        }

        jdbcTemplateSC.update(String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName()),
                getObjectArray(false, values, getObjectArray(false, rootKeyElement.values, entity.getReportDate())));

        jdbcTemplateSC.update(String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName()),
                getObjectArray(false, values, getObjectArray(false, rootKeyElement.values, entity.getReportDate())));
    }

    private HashMap<ArrayElement, HashMap<ValueElement, Object>> generateMap(IBaseEntity entity, ShowCase ShowCase) {
        HashSet<PathElement> keyPaths = new HashSet<>();
        HashMap<String, HashSet<PathElement>> paths = generatePaths(entity, ShowCase, keyPaths);

        HashSet<PathElement> rootAttributes;

        if (paths.size() == 0 || paths.get("root") == null) {
            rootAttributes = new HashSet<>();
        } else {
            rootAttributes = paths.get("root");
        }

        rootAttributes.add(new PathElement("root", "root", entity.getMeta().getClassName() + "_id"));
        keyPaths.add(new PathElement("root", "root", entity.getMeta().getClassName() + "_id"));

        HashMap<ValueElement, Object> dirtyMap = readMap("root", entity, paths);

        if (dirtyMap == null)
            return null;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> globalMap = gen(dirtyMap);
        HashMap<ArrayElement, HashMap<ValueElement, Object>> clearedGlobalMap = new HashMap<>();

        for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> globalEntry : globalMap.entrySet()) {
            HashMap<ValueElement, Object> tmpMap = clearDirtyMap(globalEntry.getValue());
            boolean hasMandatoryKeys = true;

            for (PathElement pElement : keyPaths) {
                boolean eFound = false;
                for (ValueElement vElement : tmpMap.keySet()) {
                    if (vElement.columnName.equals(pElement.columnName)) {
                        eFound = true;
                        break;
                    }
                }

                if (!eFound) {
                    hasMandatoryKeys = false;
                    break;
                }
            }

            if (hasMandatoryKeys)
                clearedGlobalMap.put(globalEntry.getKey(), tmpMap);
        }

        return clearedGlobalMap;
    }

    /* Persists generated map to showcase table */
    @Transactional
    private void persistMap(HashMap<ValueElement, Object> map, Date openDate, Date closeDate, ShowCase showCase) {
        StringBuilder sql;
        StringBuilder values = new StringBuilder("(");
        String tableName;

        if (closeDate == null)
            tableName = getActualTableName(showCase);
        else
            tableName = getHistoryTableName(showCase);

        sql = new StringBuilder("INSERT INTO ").append(tableName).append("(");

        Object[] valueArray;

        if (!showCase.isFinal()) {
            valueArray = new Object[map.size() + 2];
        } else {
            valueArray = new Object[map.size() + 1];
        }

        int i = 0;

        for (Map.Entry<ValueElement, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey().columnName).append(", ");
            values.append("?, ");
            valueArray[i++] = entry.getValue();
        }

        if (!showCase.isFinal()) {
            sql.append("cdc, open_date, close_date");
            values.append("sysdate, ?, ?)");
            valueArray[i++] = openDate;
            valueArray[i] = closeDate;
        } else {
            sql.append("cdc, rep_date");
            values.append("SYSDATE, ?)");
            valueArray[i] = openDate;
        }

        sql.append(") VALUES ").append(values);

        jdbcTemplateSC.update(sql.toString(), valueArray);
    }

    /* Updates close_date column using @keyElement with entity.getReportDate() */
    @Transactional
    private void updateMapLeftRange(HistoryState historyState, KeyElement keyElement, IBaseEntity entity, ShowCase showCase) {
        String sql = "UPDATE %s SET close_date = ? WHERE " + keyElement.queryKeys;

        if (historyState == HistoryState.ACTUAL) {
            sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());
        } else {
            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());
        }

        jdbcTemplateSC.update(sql, getObjectArray(true, keyElement.values, entity.getReportDate()));
    }

    /* Does CLOSE operation on showcases */
    @Transactional
    public synchronized void closeEntities(Long scId, IBaseEntity entity, List<ShowCase> showCases) {
        for (ShowCase showCase : showCases) {
            if (!showCase.getMeta().getClassName().equals(entity.getMeta().getClassName()))
                continue;

            if (scId == null || scId == 0L || scId == showCase.getId()) {
                if (showCase.getDownPath() == null || showCase.getDownPath().length() == 0)
                    closeEntity(entity, showCase);
            }
        }
    }

    /* Performs close on entity using showCase */
    @Transactional
    private void closeEntity(IBaseEntity entity, ShowCase showCase) {
        String sql;

        sql = "UPDATE %s SET close_date = ? WHERE " + showCase.getRootClassName() + "_id = ?";
        sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        jdbcTemplateSC.update(sql, entity.getBaseEntityReportDate().getReportDate(), entity.getId());

        StringBuilder select = new StringBuilder();
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO %s");

        select.append(COLUMN_PREFIX).append(showCase.getRootClassName()).append("_id, ");

        for (ShowCaseField sf : showCase.getFieldsList())
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");

        for (ShowCaseField sf : showCase.getCustomFieldsList())
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");

        select.append("cdc, open_date, close_date ");
        sqlBuilder.append("(").append(select).append(")( SELECT ")
                .append(select).append("FROM %s WHERE ")
                .append(showCase.getRootClassName()).append("_id = ?)");

        String sqlResult = String.format(sqlBuilder.toString(), getHistoryTableName(showCase),
                getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        jdbcTemplateSC.update(sqlResult, entity.getId());

        sql = "DELETE FROM %s WHERE " + showCase.getRootClassName() + "_id = ?";
        sql = String.format(sql, getActualTableName(showCase),
                COLUMN_PREFIX, showCase.getRootClassName());

        jdbcTemplateSC.update(sql, entity.getId());
    }

    /* Moves data from actual table to history */
    @Transactional
    private void moveActualMapToHistory(KeyElement keyElement, ShowCase showCase) {
        StringBuilder select = new StringBuilder();
        StringBuilder sql = new StringBuilder("INSERT INTO %s");

        select.append(COLUMN_PREFIX).append(showCase.getRootClassName()).append("_id, ");

        // default fields
        for (ShowCaseField sf : showCase.getFieldsList())
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");

        // custom fields
        for (ShowCaseField sf : showCase.getCustomFieldsList()) {
            select.append(COLUMN_PREFIX).append(sf.getColumnName()).append(", ");
        }

        select.append("CDC, OPEN_DATE, CLOSE_DATE ");
        sql.append("(").append(select).append(")( SELECT ").append(select)
                .append("FROM %s WHERE ").append(keyElement.queryKeys).append(")");

        String sqlResult = String.format(sql.toString(), getHistoryTableName(showCase),
                getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        jdbcTemplateSC.update(sqlResult, keyElement.values);

        sqlResult = String.format("DELETE FROM %s WHERE " + keyElement.queryKeys + " AND CLOSE_DATE IS NOT NULL",
                getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        jdbcTemplateSC.update(sqlResult, keyElement.values);
    }

    /* Physically deletes data from showcase */
    public int deleteById(ShowCase showCase, IBaseEntity e) {
        String sql;
        int rows = 0;

        for (ShowCase sh : showCases) {
            if (!sh.getTableName().equals(showCase.getTableName()) &&
                    sh.getRootClassName().equals(showCase.getRootClassName())) {
                sql = "DELETE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getHistoryTableName(sh), COLUMN_PREFIX, showCase.getRootClassName());

                rows += jdbcTemplateSC.update(sql, e.getId());

                sql = "DELETE FROM %s WHERE %s%s_ID = ?";
                sql = String.format(sql, getActualTableName(sh), COLUMN_PREFIX, showCase.getRootClassName());

                rows += jdbcTemplateSC.update(sql, e.getId());
            }
        }

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        rows += jdbcTemplateSC.update(sql, e.getId());

        sql = "DELETE FROM %s WHERE %s%s_ID = ?";
        sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

        rows += jdbcTemplateSC.update(sql, e.getId());

        return rows;
    }

    /* Generates path for relational tables using showCase */
    private HashMap<String, HashSet<PathElement>> generatePaths(IBaseEntity entity, ShowCase showCase,
                                                                HashSet<PathElement> keyPaths) {
        HashMap<String, HashSet<PathElement>> paths = new HashMap<>();

        HashSet<PathElement> tmpSet;

        for (ShowCaseField sf : showCase.getFieldsList()) {
            IMetaType attributeMetaType = entity.getMeta().getEl(sf.getAttributePath());

            if (sf.getAttributePath().contains(".")) {
                if (attributeMetaType.isComplex()) {
                    if (paths.get("root." + sf.getAttributePath()) != null) {
                        tmpSet = paths.get("root." + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName()));
                    paths.put("root." + sf.getAttributePath(), tmpSet);

                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName()));
                    paths.put("root." + path, tmpSet);
                } else {
                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get("root." + path) != null) {
                        tmpSet = paths.get("root." + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName()));
                    paths.put("root." + path, tmpSet);
                }
            } else {
                if (paths.get("root") != null) {
                    tmpSet = paths.get("root");
                } else {
                    tmpSet = new HashSet<>();
                }

                if (attributeMetaType.isSet()) {
                    keyPaths.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName()));

                    tmpSet.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName()));
                    paths.put("root", tmpSet);

                    tmpSet = new HashSet<>();
                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName()));
                    paths.put("root." + sf.getAttributePath(), tmpSet);
                } else if (attributeMetaType.isComplex()) {
                    tmpSet.add(new PathElement("root." + sf.getAttributePath(), sf.getAttributePath(),
                            sf.getColumnName()));
                    paths.put("root", tmpSet);

                    tmpSet = new HashSet<>();
                    tmpSet.add(new PathElement("root", sf.getAttributePath(), sf.getColumnName()));
                    paths.put("root." + sf.getAttributePath(), tmpSet);
                } else {
                    tmpSet.add(new PathElement(sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName()));

                    paths.put("root", tmpSet);
                }
            }
        }

        return paths;
    }

    private HashMap<ValueElement, Object> readMap(String curPath, IBaseEntity entity, HashMap<String, HashSet<PathElement>> paths) {
        HashSet<PathElement> attributes = paths.get(curPath);

        HashMap<ValueElement, Object> map = new HashMap<>();

        if (entity.getOperation() != null)
            map.put(new ValueElement("_operation", -1L, false), entity.getOperation());

        if (attributes != null) {
            for (PathElement attribute : attributes) {
                if (attribute.elementPath.equals("root")) {
                    map.put(new ValueElement(attribute.columnName, entity.getId()), entity.getId());
                } else {
                    if (attribute.elementPath.contains("root.")) {
                        Object container = entity.getEl(attribute.elementPath.substring(
                                attribute.elementPath.indexOf(".") + 1));

                        if (container == null) continue;

                        if (container instanceof BaseEntity) {
                            BaseEntity innerEntity = (BaseEntity) container;

                            map.put(new ValueElement(attribute.columnName, innerEntity.getId()),
                                    readMap(attribute.elementPath, innerEntity, paths));
                        } else if (container instanceof BaseSet) {
                            BaseSet innerSet = (BaseSet) container;

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            if (innerSet.getMemberType().isComplex()) {
                                for (IBaseValue bValue : innerSet.get()) {
                                    BaseEntity bValueEntity = (BaseEntity) bValue.getValue();
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValueEntity.getId(), false),
                                            readMap(attribute.elementPath, bValueEntity, paths));
                                }

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(),
                                        true, false), arrayMap);
                            } else {
                                for (IBaseValue bValue : innerSet.get())
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValue.getId(), false),
                                            bValue.getValue());

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(),
                                        true, true), arrayMap);
                            }
                        }
                    } else {
                        IBaseValue iBaseValue = entity.getBaseValue(attribute.elementPath);

                        if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                !iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId()),
                                    readMap(curPath + "." + attribute.elementPath,
                                            (BaseEntity) iBaseValue.getValue(), paths));
                        } else if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            throw new UnsupportedOperationException("Complex entity cannot contain complex set");
                        } else if (iBaseValue != null && iBaseValue.getValue() instanceof BaseSet) {
                            BaseSet bSet = (BaseSet) iBaseValue.getValue();

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            for (IBaseValue innerValue : bSet.get())
                                arrayMap.put(new ValueElement(attribute.elementPath, innerValue.getId(), false, true),
                                        innerValue.getValue());

                            map.put(new ValueElement(attribute.elementPath, iBaseValue.getId(), true, true),
                                    arrayMap);
                        } else if (iBaseValue != null) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId()), iBaseValue.getValue());
                        }
                    }
                }
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private HashMap<ArrayElement, HashMap<ValueElement, Object>> gen(HashMap<ValueElement, Object> dirtyMap) {
        HashMap<ArrayElement, HashMap<ValueElement, Object>> arrayEl = new HashMap<>();

        int index = 0;

        for (Map.Entry<ValueElement, Object> entry : dirtyMap.entrySet()) {
            if (entry.getKey().isArray && !entry.getKey().isSimple) {
                HashMap<ValueElement, Object> innerMap = (HashMap<ValueElement, Object>) entry.getValue();

                for (Map.Entry<ValueElement, Object> innerEntry : innerMap.entrySet()) {
                    HashMap<ArrayElement, HashMap<ValueElement, Object>> recursiveMap =
                            gen((HashMap) innerEntry.getValue());

                    for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> recEntry : recursiveMap.entrySet()) {
                        arrayEl.put(new ArrayElement(index++, innerEntry.getKey()), (HashMap) recEntry.getValue());
                    }
                }
            } else if (entry.getKey().isArray && entry.getKey().isSimple) {
                HashMap<ValueElement, Object> innerMap = (HashMap) entry.getValue();

                for (Map.Entry<ValueElement, Object> innerEntry : innerMap.entrySet()) {
                    HashMap<ValueElement, Object> newHashMap = new HashMap<>();
                    newHashMap.put(innerEntry.getKey(), innerEntry.getValue());

                    newHashMap.put(new ValueElement(innerEntry.getKey().columnName + "_id",
                            innerEntry.getKey().elementId), innerEntry.getKey().elementId);

                    arrayEl.put(new ArrayElement(index++, innerEntry.getKey()), newHashMap);
                }
            }
        }

        if (arrayEl.size() > 0) {
            for (Map.Entry<ValueElement, Object> entry : dirtyMap.entrySet()) {
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

            for (Map.Entry<ValueElement, Object> entry : dirtyMap.entrySet()) {
                singleMap.put(entry.getKey(), entry.getValue());
            }

            arrayEl.put(new ArrayElement(index, new ValueElement("root", 0L)), singleMap);
        }

        return arrayEl;
    }

    @SuppressWarnings("unchecked")
    private HashMap<ValueElement, Object> clearDirtyMap(HashMap<ValueElement, Object> dirtyMap) {
        HashMap<ValueElement, Object> returnMap = new HashMap<>();

        for (Map.Entry<ValueElement, Object> entry : dirtyMap.entrySet()) {
            if (entry.getValue() instanceof HashMap) {
                HashMap<ValueElement, Object> tmpMap = clearDirtyMap((HashMap) entry.getValue());

                for (Map.Entry<ValueElement, Object> tmpMapEntry : tmpMap.entrySet())
                    returnMap.put(tmpMapEntry.getKey(), tmpMapEntry.getValue());
            } else {
                returnMap.put(entry.getKey(), entry.getValue());
            }
        }

        return returnMap;
    }

    /* Adds custom keys to existing map */
    public void addCustomKeys(HashMap<ValueElement, Object> entryMap, IBaseEntity globalEntity, ShowCase showCase) {
        for (ShowCaseField sf : showCase.getCustomFieldsList()) {
            if (sf.getAttributePath().equals("root")) {
                entryMap.put(new ValueElement(sf.getColumnName(), globalEntity.getId()), globalEntity.getId());
                continue;
            }

            Object customObject = null;

            try {
                customObject = globalEntity.getEl(sf.getAttributePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (customObject instanceof BaseEntity) {
                    entryMap.put(new ValueElement(sf.getColumnName(), ((BaseEntity) customObject).getId()),
                            ((BaseEntity) customObject).getId());
                } else if (customObject instanceof BaseSet) {
                    throw new UnsupportedOperationException("CustomSet is not supported!");
                } else {
                    entryMap.put(new ValueElement(sf.getColumnName(), 0L), customObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Returns array elementArray + elements in  both order */
    private Object[] getObjectArray(boolean reverse, Object[] elementArray, Object... elements) {
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

    private boolean compareValue (Object newValue, Object dbValue) {
        if (newValue instanceof Double) {
            double value = ((BigDecimal) dbValue).doubleValue();
            if (!newValue.equals(value))
                return false;
        } else if (newValue instanceof Integer) {
            int value = ((BigDecimal) dbValue).intValue();
            if (!newValue.equals(value)) {
                return false;
            }
        } else if (newValue instanceof Boolean) {
            boolean value = ((BigDecimal) dbValue).longValue() == 1;
            if (!newValue.equals(value)) {
                return false;
            }
        } else if (newValue instanceof Long) {
            if (!newValue.equals(Long.valueOf(dbValue.toString()))) {
                return false;
            }
        } else if (newValue instanceof String) {
            if (!newValue.toString().equals(dbValue.toString())) {
                return false;
            }
        } else if (newValue instanceof Date) {
            Date value = DataUtils.convertToSQLDate((Timestamp) dbValue);
            if (!newValue.equals(value)) {
                return false;
            }
        } else {
            if (!newValue.equals(dbValue)) {
             return false;
            }
        }

        return true;
    }

    private boolean compareValues(HistoryState state, HashMap<ValueElement, Object> savingMap, IBaseEntity entity, ShowCase showCase, KeyElement keyElement) {
        StringBuilder st = new StringBuilder();
        boolean equalityFlag = true;

        try {
            int colCounter = 0;
            for (ValueElement valueElement : savingMap.keySet()) {
                st.append(valueElement.columnName);

                if (++colCounter < savingMap.size())
                    st.append(", ");
            }

            String sql = "SELECT " + st.toString() + " FROM %s WHERE " + keyElement.queryKeys;

            Map dbElement;

            if (state == HistoryState.ACTUAL) {
                sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                dbElement = jdbcTemplateSC.queryForMap(sql, keyElement.values);
            } else {
                sql += " AND open_date = ?";
                sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName(), entity.getReportDate());

                dbElement = jdbcTemplateSC.queryForMap(sql, getObjectArray(false, keyElement.values, entity.getReportDate()));
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

                if (!compareValue(newValue, dbValue)) {
                    equalityFlag = false;
                    break;
                }
            }

            return equalityFlag;
        } catch (IncorrectResultSizeDataAccessException ir) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitShowCase(Long id) {
        while (true) {
            synchronized (cortegeElements) {
                if (!cortegeElements.contains(id)) {
                    cortegeElements.add(id);
                    break;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void removeShowCase(Long id) {
        synchronized (cortegeElements) {
            cortegeElements.remove(id);
        }
    }

}
