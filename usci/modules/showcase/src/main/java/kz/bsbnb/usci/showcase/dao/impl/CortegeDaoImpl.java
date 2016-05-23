package kz.bsbnb.usci.showcase.dao.impl;

import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.showcase.ShowCase;
import kz.bsbnb.usci.eav.showcase.ShowCaseField;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.showcase.dao.CommonDao;
import kz.bsbnb.usci.showcase.driver.ShowCaseJdbcTemplate;
import kz.bsbnb.usci.showcase.element.ArrayElement;
import kz.bsbnb.usci.showcase.element.KeyElement;
import kz.bsbnb.usci.showcase.element.PathElement;
import kz.bsbnb.usci.showcase.element.ValueElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Component
public class CortegeDaoImpl extends CommonDao {
    @Autowired
    private ShowCaseJdbcTemplate jdbcTemplateSC;

    private static final String ROOT = "root";
    private static final String ROOT_DOT = "root.";

    @SuppressWarnings("unchecked")
    @Transactional
    public void generate(IBaseEntity globalEntityApplied, ShowCase showCase) {
        if (showCase.getDownPath() != null && showCase.getDownPath().length() > 0) {
            List<BaseEntity> allApplied = (List<BaseEntity>) globalEntityApplied.getEls("{get}" + showCase.getDownPath());

            for (BaseEntity baseEntityApplied : allApplied)
                rootCortegeGenerate(globalEntityApplied, baseEntityApplied, showCase);
        } else {
            rootCortegeGenerate(globalEntityApplied, globalEntityApplied, showCase);
        }
    }

    /* Performs main operations on showcase  */
    @Transactional
    private void rootCortegeGenerate(IBaseEntity globalEntity, IBaseEntity entity, ShowCase showCase) {
        String sql;

        HashMap<ArrayElement, HashMap<ValueElement, Object>> savingMap = generateMap(entity, showCase);

        if (savingMap == null || savingMap.size() == 0) return;

        boolean rootExecutionFlag = false;

        for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> entry : savingMap.entrySet()) {
            HashMap<ValueElement, Object> entryMap = entry.getValue();

            if (showCase.isChild()) {
                if (!entity.getMeta().isReference())
                    entryMap.put(new ValueElement("creditor_id", 0L, 0), entity.getBaseEntityReportDate().getCreditorId());

                if (showCase.getDownPath() != null && showCase.getDownPath().length() > 0)
                    entryMap.put(new ValueElement(globalEntity.getMeta().getClassName() + "_id", 0L, 0), globalEntity.getId());
            }

            addCustomKeys(entryMap, globalEntity, showCase);

            KeyElement rootKeyElement = new KeyElement(entryMap, showCase.getRootKeyFieldsList());
            KeyElement historyKeyElement = new KeyElement(entryMap, showCase.getHistoryKeyFieldsList());

            /* Deletes data by root ids */
            if (!rootExecutionFlag) {
                cleanCurrentReportDate(showCase, rootKeyElement, entity);

                /* Execute only ones */
                rootExecutionFlag = true;
            }

            if (globalEntity.getOperation() != null)
                continue;

            if (!showCase.isFinal()) {
                Date maxOpenDate;
                try {
                    sql = "SELECT MAX(open_date) AS open_date FROM %s WHERE " + historyKeyElement.queryKeys;
                    sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                    maxOpenDate = (Date) jdbcTemplateSC.queryForMap("SELECT MAX(open_date) AS open_date FROM %s WHERE  + historyKeyElement.queryKeys", sql, historyKeyElement.values).get("OPEN_DATE");
                } catch (EmptyResultDataAccessException e) {
                    maxOpenDate = null;
                }

                if (maxOpenDate == null) {
                    /* No data, insert to actual data */
                    entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                    simpleInsertValueElement(entryMap, getActualTableName(showCase));
                } else if (entity.getReportDate().compareTo(maxOpenDate) > 0) { // forward
                    sql = "SELECT * FROM %s WHERE " + historyKeyElement.queryKeys;
                    sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                    Map<String, Object> dbMap = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE + historyKeyElement.queryKeys", sql, historyKeyElement.values);
                    dbMap.remove("CDC");
                    dbMap.remove("ID");

                    if (!checkMaps(entryMap, dbMap)) {
                        dbMap.put("CLOSE_DATE", entity.getReportDate());

                        simpleInsertString(dbMap, getHistoryTableName(showCase));

                        sql = "DELETE FROM %s WHERE " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        jdbcTemplateSC.update("DELETE FROM %s WHERE + historyKeyElement.queryKeys", sql, historyKeyElement.values);

                        entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                        simpleInsertValueElement(entryMap, getActualTableName(showCase));
                    }
                } else if (entity.getReportDate().compareTo(maxOpenDate) < 0) { // backward
                    /* Closest upper date */
                    sql = "SELECT MIN(open_date) as open_date FROM %s WHERE " + historyKeyElement.queryKeys + " AND open_date > ? ";
                    sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                    Date historyMin = (Date) jdbcTemplateSC.queryForMap("SELECT MIN(open_date) as open_date FROM %s WHERE + historyKeyElement.queryKeys + AND open_date > ?",
                            sql, getObjectArray(false, historyKeyElement.values, entity.getReportDate())).get("OPEN_DATE");

                    /* Closest lower date */
                    sql = "SELECT MAX(open_date) as open_date FROM %s WHERE " + historyKeyElement.queryKeys + " AND open_date < ? ";
                    sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName());

                    Date historyMax = (Date) jdbcTemplateSC.queryForMap("SELECT MAX(open_date) as open_date FROM %s WHERE + historyKeyElement.queryKeys +  AND open_date < ?",
                            sql, getObjectArray(false, historyKeyElement.values, entity.getReportDate())).get("OPEN_DATE");

                    /* No data in history */
                    if (historyMin == null && historyMax == null) {
                        /* Compares with actual data */
                        sql = "SELECT * FROM %s WHERE " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getActualTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        Map<String, Object> dbMap = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE + historyKeyElement.queryKeys", sql, historyKeyElement.values);

                        if (checkMaps(entryMap, dbMap)) {
                            /* Data's are same, update report date */
                            sql = "UPDATE %s SET open_date = ? WHERE " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getActualTableName(showCase));

                            jdbcTemplateSC.update("UPDATE %s SET open_date = ? WHERE + historyKeyElement.queryKeys", sql,
                                    getObjectArray(true, historyKeyElement.values, entity.getReportDate()));
                        } else {
                            /* Data's are not same, insert to history table */
                            entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                            entryMap.put(new ValueElement("CLOSE_DATE", 0L, 0), maxOpenDate);
                            simpleInsertValueElement(entryMap, getHistoryTableName(showCase));
                        }
                    } else if (historyMin != null && historyMax != null) {
                        sql = "SELECT * FROM %s WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        Map<String, Object> dbMapHistoryMax = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE open_date = ? AND + historyKeyElement.queryKeys",
                                sql, getObjectArray(true, historyKeyElement.values, historyMax));

                        if (!checkMaps(entryMap, dbMapHistoryMax)) {
                            sql = "SELECT * FROM %s WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                            Map<String, Object> dbMapHistoryMin = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE open_date = ? AND  + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, historyMin));

                            if (checkMaps(entryMap, dbMapHistoryMin)) {
                                /* Data's are same, update report date */
                                sql = "UPDATE %s SET open_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                                sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                                jdbcTemplateSC.update("UPDATE %s SET open_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, entity.getReportDate(), historyMin));
                            } else {
                                /* Upper and lower data exists in history and they're different */
                                /* Update close_date lower data */
                                sql = "UPDATE %s SET close_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                                sql = String.format(sql, getHistoryTableName(showCase));

                                /* Insert to history table, close_date to historyMin */
                                jdbcTemplateSC.update("UPDATE %s SET close_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, entity.getReportDate(), historyMax));
                                entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                                entryMap.put(new ValueElement("CLOSE_DATE", 0L, 0), historyMin);
                                simpleInsertValueElement(entryMap, getHistoryTableName(showCase));
                            }
                        } else {
                            /* Increase close_date to historyMin */
                            sql = "UPDATE %s SET close_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                            jdbcTemplateSC.update("UPDATE %s SET close_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, historyMin, historyMax));
                        }
                    } else if (historyMin != null) {
                        /* Compare with upper data */
                        sql = "SELECT * FROM %s WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        Map<String, Object> dbMap = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, historyMin));

                        if (checkMaps(entryMap, dbMap)) {
                            /* Data's are same, update report date */
                            sql = "UPDATE %s SET open_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                            jdbcTemplateSC.update("UPDATE %s SET open_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, entity.getReportDate(), historyMin));
                        } else {
                            /* Data's are not same, insert to history table */
                            entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                            entryMap.put(new ValueElement("CLOSE_DATE", 0L, 0), historyMin);
                            simpleInsertValueElement(entryMap, getHistoryTableName(showCase));
                        }
                    } else { // if (historyMax != null)
                        /* Compare with lower data */
                        sql = "SELECT * FROM %s WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                        sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                        Map<String, Object> dbMap = jdbcTemplateSC.queryForMap("SELECT * FROM %s WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, historyMax));

                        if (!checkMaps(entryMap, dbMap)) {
                            /* Data's are not same, insert to history table */
                            sql = "UPDATE %s SET close_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                            jdbcTemplateSC.update("UPDATE %s SET close_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, entity.getReportDate(), historyMax));

                            entryMap.put(new ValueElement("OPEN_DATE", 0L, 0), entity.getReportDate());
                            entryMap.put(new ValueElement("CLOSE_DATE", 0L, 0), maxOpenDate);
                            simpleInsertValueElement(entryMap, getHistoryTableName(showCase));
                        } else {
                            /* Increase close_date to maxOpenDate */
                            sql = "UPDATE %s SET close_date = ? WHERE open_date = ? AND " + historyKeyElement.queryKeys;
                            sql = String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX, showCase.getRootClassName().toUpperCase());

                            jdbcTemplateSC.update("UPDATE %s SET close_date = ? WHERE open_date = ? AND + historyKeyElement.queryKeys", sql, getObjectArray(true, historyKeyElement.values, maxOpenDate, historyMax));
                        }
                    }
                }
            } else {
                sql = "DELETE FROM %s WHERE " + historyKeyElement.queryKeys + " and rep_date = ?";

                jdbcTemplateSC.update("DELETE FROM %s WHERE + historyKeyElement.queryKeys + and rep_date = ?", String.format(sql, getActualTableName(showCase), COLUMN_PREFIX,
                        showCase.getRootClassName()), getObjectArray(false, historyKeyElement.values, entity.getReportDate()));

                entryMap.put(new ValueElement("REP_DATE", 0L, 0), entity.getReportDate());
                simpleInsertValueElement(entryMap, getActualTableName(showCase));
            }
        }
    }

    private HashMap<ArrayElement, HashMap<ValueElement, Object>> generateMap(IBaseEntity entity, ShowCase ShowCase) {
        HashSet<PathElement> keyPaths = new HashSet<>();
        HashMap<String, HashSet<PathElement>> paths = generatePaths(entity, ShowCase, keyPaths);

        HashSet<PathElement> rootAttributes;

        if (paths.size() == 0 || paths.get(ROOT) == null) {
            rootAttributes = new HashSet<>();
        } else {
            rootAttributes = paths.get(ROOT);
        }

        rootAttributes.add(new PathElement(ROOT, ROOT, entity.getMeta().getClassName() + "_id"));
        keyPaths.add(new PathElement(ROOT, ROOT, entity.getMeta().getClassName() + "_id"));

        HashMap<ValueElement, Object> dirtyMap = readMap(ROOT, entity, paths);

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

    /* Generates path for relational tables using showCase */
    private HashMap<String, HashSet<PathElement>> generatePaths(IBaseEntity entity, ShowCase showCase, HashSet<PathElement> keyPaths) {
        HashMap<String, HashSet<PathElement>> paths = new HashMap<>();

        HashSet<PathElement> tmpSet;

        for (ShowCaseField sf : showCase.getFieldsList()) {
            IMetaType attributeMetaType = entity.getMeta().getEl(sf.getAttributePath());

            if (sf.getAttributePath().contains(".")) {
                if (attributeMetaType.isComplex()) {
                    if (paths.get(ROOT_DOT + sf.getAttributePath()) != null) {
                        tmpSet = paths.get(ROOT_DOT + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(ROOT, sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT_DOT + sf.getAttributePath(), tmpSet);

                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get(ROOT_DOT + path) != null) {
                        tmpSet = paths.get(ROOT_DOT + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT_DOT + path, tmpSet);
                } else {
                    String path = sf.getAttributePath().substring(0, sf.getAttributePath().lastIndexOf("."));
                    String name = sf.getAttributePath().substring(sf.getAttributePath().lastIndexOf(".") + 1);

                    if (paths.get(ROOT_DOT + path) != null) {
                        tmpSet = paths.get(ROOT_DOT + path);
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(name, sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT_DOT + path, tmpSet);
                }
            } else {
                if (paths.get(ROOT) != null) {
                    tmpSet = paths.get(ROOT);
                } else {
                    tmpSet = new HashSet<>();
                }

                if (attributeMetaType.isSet()) {
                    keyPaths.add(new PathElement(ROOT_DOT + sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName()));
                    tmpSet.add(new PathElement(ROOT_DOT + sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT, tmpSet);

                    if (paths.get(ROOT_DOT + sf.getAttributePath()) != null) {
                        tmpSet = paths.get(ROOT_DOT + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(ROOT, sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT_DOT + sf.getAttributePath(), tmpSet);
                } else if (attributeMetaType.isComplex()) {
                    tmpSet.add(new PathElement(ROOT_DOT + sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT, tmpSet);

                    if (paths.get(ROOT_DOT + sf.getAttributePath()) != null) {
                        tmpSet = paths.get(ROOT_DOT + sf.getAttributePath());
                    } else {
                        tmpSet = new HashSet<>();
                    }

                    tmpSet.add(new PathElement(ROOT, sf.getAttributePath(), sf.getColumnName()));
                    paths.put(ROOT_DOT + sf.getAttributePath(), tmpSet);
                } else {
                    tmpSet.add(new PathElement(sf.getAttributePath(), sf.getAttributePath(), sf.getColumnName()));

                    paths.put(ROOT, tmpSet);
                }
            }
        }

        return paths;
    }

    private HashMap<ValueElement, Object> readMap(String curPath, IBaseEntity entity, HashMap<String, HashSet<PathElement>> paths) {
        HashSet<PathElement> attributes = paths.get(curPath);

        HashMap<ValueElement, Object> map = new HashMap<>();

        if (attributes != null) {
            for (PathElement attribute : attributes) {
                if (attribute.elementPath.equals(ROOT)) {
                    map.put(new ValueElement(attribute.columnName, entity.getId(), 0), entity.getId());
                } else {
                    if (attribute.elementPath.contains(ROOT_DOT)) {
                        Object container = entity.getEl(attribute.elementPath.substring(
                                attribute.elementPath.indexOf(".") + 1));

                        if (container == null) continue;

                        if (container instanceof BaseEntity) {
                            BaseEntity innerEntity = (BaseEntity) container;

                            map.put(new ValueElement(attribute.columnName, innerEntity.getId(), 0),
                                    readMap(attribute.elementPath, innerEntity, paths));
                        } else if (container instanceof BaseSet) {
                            BaseSet innerSet = (BaseSet) container;

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            if (innerSet.getMemberType().isComplex()) {
                                int arrayIndex = 0;
                                for (IBaseValue bValue : innerSet.get()) {
                                    BaseEntity bValueEntity = (BaseEntity) bValue.getValue();
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValueEntity.getId(),
                                            false, arrayIndex++), readMap(attribute.elementPath, bValueEntity, paths));
                                }

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), true, false, 0), arrayMap);
                            } else {
                                for (IBaseValue bValue : innerSet.get())
                                    arrayMap.put(new ValueElement(attribute.elementPath, bValue.getId(), false, 0), bValue.getValue());

                                map.put(new ValueElement(attribute.elementPath, ((BaseSet) container).getId(), true, true, 0), arrayMap);
                            }
                        }
                    } else {
                        IBaseValue iBaseValue = entity.getBaseValue(attribute.elementPath);

                        if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                !iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId(), 0),
                                    readMap(curPath + "." + attribute.elementPath, (BaseEntity) iBaseValue.getValue(), paths));
                        } else if (iBaseValue != null && iBaseValue.getMetaAttribute().getMetaType().isComplex() &&
                                iBaseValue.getMetaAttribute().getMetaType().isSet()) {
                            throw new UnsupportedOperationException(Errors.compose(Errors.E274));
                        } else if (iBaseValue != null && iBaseValue.getValue() instanceof BaseSet) {
                            BaseSet bSet = (BaseSet) iBaseValue.getValue();

                            HashMap<ValueElement, Object> arrayMap = new HashMap<>();

                            int arrayIndex = 0;
                            for (IBaseValue innerValue : bSet.get())
                                arrayMap.put(new ValueElement(attribute.elementPath, innerValue.getId(), false, true, arrayIndex++), innerValue.getValue());

                            map.put(new ValueElement(attribute.elementPath, iBaseValue.getId(), true, true, 0), arrayMap);
                        } else if (iBaseValue != null) {
                            map.put(new ValueElement(attribute.columnName, iBaseValue.getId(), 0), iBaseValue.getValue());
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

                    for (Map.Entry<ArrayElement, HashMap<ValueElement, Object>> recEntry : recursiveMap.entrySet())
                        arrayEl.put(new ArrayElement(index++, innerEntry.getKey()), (HashMap) recEntry.getValue());
                }
            } else if (entry.getKey().isArray && entry.getKey().isSimple) {
                HashMap<ValueElement, Object> innerMap = (HashMap) entry.getValue();

                int arrayIndex = 0;
                for (Map.Entry<ValueElement, Object> innerEntry : innerMap.entrySet()) {
                    HashMap<ValueElement, Object> newHashMap = new HashMap<>();
                    newHashMap.put(innerEntry.getKey(), innerEntry.getValue());

                    newHashMap.put(new ValueElement(innerEntry.getKey().columnName + "_id",
                            innerEntry.getKey().elementId, arrayIndex++), innerEntry.getKey().elementId);

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

            arrayEl.put(new ArrayElement(index, new ValueElement(ROOT, 0L, 0)), singleMap);
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
    private void addCustomKeys(HashMap<ValueElement, Object> entryMap, IBaseEntity globalEntity, ShowCase showCase) {
        for (ShowCaseField sf : showCase.getCustomFieldsList()) {
            if (sf.getAttributePath().equals(ROOT)) {
                entryMap.put(new ValueElement(sf.getColumnName(), globalEntity.getId(), 0), globalEntity.getId());
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
                    entryMap.put(new ValueElement(sf.getColumnName(), ((BaseEntity) customObject).getId(), 0), ((BaseEntity) customObject).getId());
                } else if (customObject instanceof BaseSet) {
                    throw new UnsupportedOperationException(Errors.compose(Errors.E272));
                } else {
                    entryMap.put(new ValueElement(sf.getColumnName(), 0L, 0), customObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cleanCurrentReportDate (ShowCase showCase, KeyElement rootKeyElement, IBaseEntity entity) {
        String sql;
        if (!showCase.isFinal()) {
            sql = "DELETE FROM %s WHERE " + rootKeyElement.queryKeys + " and open_date = ?";

            jdbcTemplateSC.update("DELETE FROM ACTUAL WHERE  + rootKeyElement.queryKeys +  and open_date = ?", String.format(sql, getActualTableName(showCase), COLUMN_PREFIX,
                    showCase.getRootClassName()), getObjectArray(false, rootKeyElement.values, entity.getReportDate()));

            jdbcTemplateSC.update("DELETE FROM HISTORY WHERE  + rootKeyElement.queryKeys +  and open_date = ?", String.format(sql, getHistoryTableName(showCase), COLUMN_PREFIX,
                    showCase.getRootClassName()), getObjectArray(false, rootKeyElement.values, entity.getReportDate()));
        } else {
            sql = "DELETE FROM %s WHERE " + rootKeyElement.queryKeys + " and rep_date = ?";

            jdbcTemplateSC.update("DELETE FROM %s WHERE + rootKeyElement.queryKeys + and rep_date = ?", String.format(sql, getActualTableName(showCase), COLUMN_PREFIX,
                    showCase.getRootClassName()), getObjectArray(false, rootKeyElement.values, entity.getReportDate()));
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

    private boolean compareValue(Object newValue, Object dbValue) {
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

    private boolean checkMaps(Map<ValueElement, Object> savingMap, Map<String, Object> dbMap) {
        boolean equalityFlag = true;

        for (Map.Entry<ValueElement, Object> innerEntry : savingMap.entrySet()) {
            Object savingValue = innerEntry.getValue();
            Object dbValue = dbMap.get(innerEntry.getKey().columnName.toUpperCase());

            if (savingValue == null && dbValue == null)
                continue;

            if (savingValue == null || dbValue == null) {
                equalityFlag = false;
                break;
            }

            if (!compareValue(savingValue, dbValue)) {
                equalityFlag = false;
                break;
            }
        }

        return equalityFlag;
    }

    @Transactional
    private void simpleInsertValueElement(Map<ValueElement, Object> map, String tableName) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(");
        StringBuilder values = new StringBuilder("(");

        Object[] valueArray = new Object[map.size()];

        int i = 0;

        for (Map.Entry<ValueElement, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey().columnName).append(", ");
            values.append("?, ");
            valueArray[i++] = entry.getValue();
        }

        sql.append("CDC");
        values.append("SYSDATE)");

        sql.append(") VALUES ").append(values);

        jdbcTemplateSC.update("SIMPLE INSERT VALUE ELEMENT " + tableName, sql.toString(), valueArray);
    }

    @Transactional
    private void simpleInsertString(Map<String, Object> map, String tableName) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(");
        StringBuilder values = new StringBuilder("(");

        Object[] valueArray = new Object[map.size()];

        int i = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sql.append(COLUMN_PREFIX).append(entry.getKey()).append(", ");
            values.append("?, ");
            valueArray[i++] = entry.getValue();
        }

        sql.append("CDC");
        values.append("SYSDATE)");

        sql.append(") VALUES ").append(values);

        jdbcTemplateSC.update("SIMPLE INSERT STRING " + tableName, sql.toString(), valueArray);
    }
}
