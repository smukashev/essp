package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.eav.persistance.generated.tables.EavBeComplexValues;
import kz.bsbnb.eav.persistance.generated.tables.EavBeStringValues;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.persistance.dao.IRefProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.tool.Quote;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_M_COMPLEX_SET;

@Component
public class RefProcessorDaoImpl extends JDBCSupport implements IRefProcessorDao {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).
                where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select complexAttrsSelect = context.select().from(EAV_M_COMPLEX_ATTRIBUTES).
                where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select simpleSetsSelect = context.select().from(EAV_M_SIMPLE_SET).
                where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(metaClassId));
        Select complexSetsSelect = context.select().from(EAV_M_COMPLEX_SET).
                where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(),
                simpleAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexAttrs = queryForListWithStats(complexAttrsSelect.getSQL(),
                complexAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> simpleSets = queryForListWithStats(simpleSetsSelect.getSQL(),
                simpleSetsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexSets = queryForListWithStats(complexSetsSelect.getSQL(),
                complexSetsSelect.getBindValues().toArray());

        List<String> names = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : complexAttrs) {
            String attrName = (String) attr.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_COMPLEX_ATTRIBUTES.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : simpleSets) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_SET.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_SIMPLE_SET.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        for (Map<String, Object> attr : complexSets) {
            String attrName = (String) attr.get(EAV_M_COMPLEX_SET.NAME.getName());
            String attrTitle = (String) attr.get(EAV_M_COMPLEX_SET.TITLE.getName());
            names.add(attrName);
            titles.add(attrTitle);
        }

        return new RefColumnsResponse(names, titles);
    }

    @Override
    public RefListResponse getRefListResponse(long metaClassId, Date date, boolean withHis) {
        List<Map<String, Object>> rows = getRefListResponseWithHis(metaClassId, null);

        addOpenCloseDates(rows);

        if (date != null) {
            rows = filter(rows, date);
        }

        // check: rows must be sorted at this stage

        if (!withHis) {
            rows = removeHistory(rows);
        }

        return new RefListResponse(rows);
    }

    public List<RefListItem> getRefsByMetaclass(long metaClassId) {
        List<RefListItem> items = getRefsByMetaclassInner(metaClassId, false);
        return items;
    }

    public List<RefListItem> getRefsByMetaclassRaw(long metaClassId) {
        List<RefListItem> items = getRefsByMetaclassInner(metaClassId, true);
        return items;
    }

    private List<RefListItem> getRefsByMetaclassInner(long metaClassId, boolean raw) {
        List<Map<String, Object>> rows = getRefListResponseWithoutHis(metaClassId, null);

        List<RefListItem> items = new ArrayList<RefListItem>();

        String titleKey = null;

        if (!rows.isEmpty()) {
            Set<String> keys = rows.get(0).keySet();

            for (String key : keys) {
                if (key.startsWith("name")) {
                    titleKey = key;
                    break;
                }
            }
        }

        for (Map<String, Object> row : rows) {
            Object id = row.get("ID");
            String title = titleKey != null ? (String) row.get(titleKey) : "------------------------";

            RefListItem item = new RefListItem();
            item.setId(((BigDecimal) id).longValue());
            item.setTitle(raw ? title : Quote.addSlashes(title));
            items.add(item);
        }

        return items;
    }

    private List<Map<String, Object>> getRefListResponseWithHis(long metaClassId, Long entityId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).
                where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        Collection<Field> complexAttrsFields = new ArrayList<>();

        Field keyAttrField = context.select(DSL.field("id")).from(EAV_M_SIMPLE_ATTRIBUTES)
                .where(EAV_M_COMPLEX_ATTRIBUTES.as("m1").CLASS_ID.eq(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID))
                .and(EAV_M_SIMPLE_ATTRIBUTES.IS_KEY.eq(DataUtils.convert(true))).asField("KEY_ATTR");

        Field settingAttrField = context.select(EAV_M_SIMPLE_ATTRIBUTES.as("m3").ID)
                .from(EAV_M_SIMPLE_ATTRIBUTES.as("m3"))
                .join(EAV_M_CLASSES.as("m4")).on(EAV_M_SIMPLE_ATTRIBUTES.as("m3").CONTAINING_ID.eq(EAV_M_CLASSES.as("m4").ID))
                .join(EAV_GLOBAL.as("m5")).on(EAV_M_CLASSES.as("m4").NAME.eq(EAV_GLOBAL.as("m5").CODE))
                .where(EAV_M_CLASSES.as("m4").ID.eq(EAV_M_COMPLEX_ATTRIBUTES.as("m1").CLASS_ID))
                .and(EAV_GLOBAL.as("m5").TYPE.eq("REF_VIEW_SETTING"))
                .and(EAV_GLOBAL.as("m5").VALUE.eq(EAV_M_SIMPLE_ATTRIBUTES.as("m3").NAME)).asField("SETTING_ATTR");

        complexAttrsFields.add(DSL.field("id"));
        complexAttrsFields.add(DSL.field("name"));
        complexAttrsFields.add(keyAttrField);
        complexAttrsFields.add(settingAttrField);

        Select complexAttrsSelect = context.select(complexAttrsFields.toArray(new Field[]{})).from(EAV_M_COMPLEX_ATTRIBUTES.as("m1"))
                .where(EAV_M_COMPLEX_ATTRIBUTES.as("m1").CONTAINING_ID.eq(metaClassId));

        System.out.println(complexAttrsSelect.toString());


        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(),
                simpleAttrsSelect.getBindValues().toArray());

        List<Map<String, Object>> complexAttrs = queryForListWithStats(complexAttrsSelect.getSQL(),
                complexAttrsSelect.getBindValues().toArray());

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(DSL.field("id"));
        fields.add(DSL.min(DSL.field("report_date")).as("report_date"));
        fields.add(DSL.field("\"closed_date\""));

        Collection<Field> groupByFields = new ArrayList<Field>();
        groupByFields.add(DSL.field("id"));
        groupByFields.add(DSL.field("\"closed_date\""));

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            attrName = "\"" + attrName + "\"";
            fields.add(DSL.field(attrName));
            groupByFields.add(DSL.field(attrName));
        }

        Collection<Field> fieldsInner = new ArrayList<Field>();

        fieldsInner.add(DSL.field("\"dat\".id"));
        fieldsInner.add(DSL.field("\"dat\".report_date"));
        fieldsInner.add(context.select(DSL.min(DSL.field("report_date")))
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(DSL.field("entity_id").eq(DSL.field("\"dat\".id")))
                .and(DSL.field("is_closed").eq(DataUtils.convert(true))).asField("closed_date"));

        for (Map<String, Object> attr : simpleAttrs) {
            BigDecimal attrId = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.ID.getName());
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrType = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE.getName());
            BigDecimal attrFinal = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL.getName());
            boolean isFinal = attrFinal != null && attrFinal.byteValue() != 0;
            Table valuesTable = getValuesTable(attrType);

            SelectConditionStep<Record1<Object>> selectMaxRepDate = context.select(DSL.max(DSL.field("report_date")))
                    .from(valuesTable)
                    .where(DSL.field("attribute_id").eq(attrId))
                    .and(DSL.field("entity_id").eq(DSL.field("\"dat\".id")));

            if (isFinal) {
                selectMaxRepDate.and(DSL.field("report_date").eq(DSL.field("\"dat\".report_date")));
            } else {
                selectMaxRepDate.and(DSL.field("report_date").le(DSL.field("\"dat\".report_date")));
            }


            Field fieldInner = context.select(DSL.field("value")).from(valuesTable)
                    .where(DSL.field("attribute_id").eq(attrId)).and(DSL.field("report_date").eq(
                            selectMaxRepDate
                    ).and(DSL.field("entity_id").eq(DSL.field("\"dat\".id"))))
                    .asField(attrName);

            fieldsInner.add(fieldInner);
        }

        for(Map<String,Object> attr: complexAttrs) {
            Long attrId = ((BigDecimal) attr.get(EAV_M_COMPLEX_ATTRIBUTES.ID.getName())).longValue();
            String attrName = (String) attr.get(EAV_M_COMPLEX_ATTRIBUTES.NAME.getName());

            Long childAttrId = null;

            if(attr.get("SETTING_ATTR") != null)
                childAttrId = ((BigDecimal)attr.get("SETTING_ATTR")).longValue();

            if(childAttrId == null && attr.get("KEY_ATTR") != null)
                childAttrId = ((BigDecimal)attr.get("KEY_ATTR")).longValue();

            if(childAttrId == null)
                continue;

            fields.add(DSL.field("\"" + attrName + "\""));
            groupByFields.add(DSL.field("\"" + attrName + "\""));

            EavBeComplexValues comp = EAV_BE_COMPLEX_VALUES.as("t1");
            EavBeStringValues str = EAV_BE_STRING_VALUES.as("t2");

            SelectConditionStep<Record1<java.sql.Date>> selectMaxRepDate = context.select(DSL.max(str.REPORT_DATE))
                    .from(comp)
                    .join(str).on(comp.ENTITY_VALUE_ID.eq(str.ENTITY_ID))
                    .where(comp.ENTITY_ID.eq(DSL.field("\"dat\".id").cast(Long.class)))
                    .and(comp.ATTRIBUTE_ID.eq(attrId))
                    .and(str.ATTRIBUTE_ID.eq(childAttrId));


            Field fieldInner = context.select(str.VALUE)
                    .from(comp)
                    .join(str).on(comp.ENTITY_VALUE_ID.eq(str.ENTITY_ID))
                    .where(comp.ENTITY_ID.eq(DSL.field("\"dat\".id").cast(Long.class)))
                    .and(comp.ATTRIBUTE_ID.eq(attrId))
                    .and(str.ATTRIBUTE_ID.eq(childAttrId))
                    .and(str.REPORT_DATE.eq(selectMaxRepDate)).asField(attrName);


            fieldsInner.add(fieldInner);
        }

        SelectLimitStep select = context.select(fields.toArray(new Field[]{})).from(
                context.select(fieldsInner.toArray(new Field[]{})).from(
                        context.select(EAV_BE_ENTITIES.ID, EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                                .from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES)
                                .on(EAV_BE_ENTITIES.ID.eq(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(entityId != null ? EAV_BE_ENTITIES.ID.eq(entityId) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
                                .and(EAV_BE_ENTITY_REPORT_DATES.IS_CLOSED.ne(DataUtils.convert(true)))
                                .asTable("dat")
                )
        ).groupBy(groupByFields).orderBy(DSL.field("id"), DSL.min(DSL.field("report_date")));

        return queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getRefListResponseWithoutHis(long metaClassId, Date date) {
        java.sql.Date dt = null;

        if (date != null) {
            dt = new java.sql.Date(date.getTime());
        }

        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).
                where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(),
                simpleAttrsSelect.getBindValues().toArray());

        Collection<Field> fields = new ArrayList<Field>();

        fields.add(DSL.field("\"enr\".id"));
        fields.add(DSL.field("\"enr\".report_date"));

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            fields.add(DSL.field("\"s_" + attrName + "\".value").as(attrName));
        }

        SelectJoinStep select = context.select(fields.toArray(new Field[]{})).from(
                context.select().from(
                        context.select(
                                EAV_BE_ENTITIES.ID,
                                EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE,
                                DSL.rowNumber().over().partitionBy(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID).
                                        orderBy(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.desc()).as("p")
                        ).from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES).
                                on(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(dt != null ? EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.le(dt) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
                                .and(EAV_BE_ENTITY_REPORT_DATES.IS_CLOSED.ne(DataUtils.convert(true)))
                                .asTable("sub")
                ).where(DSL.field("\"sub\".\"p\"").eq(1)).asTable("enr")
        );

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrType = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE.getName());
            String attrSubTable = "s_" + attrName;

            Table valuesTable = getValuesTable(attrType);

            select.leftOuterJoin(
                    context.select().from(
                            context.select(
                                    valuesTable.field("ENTITY_ID"),
                                    valuesTable.field("VALUE"),
                                    valuesTable.field("REPORT_DATE"),
                                    DSL.rowNumber().over().partitionBy(valuesTable.field("ENTITY_ID")).
                                            orderBy(valuesTable.field("REPORT_DATE").desc()).as("p")
                            ).from(valuesTable)
                                    .where(valuesTable.field("ATTRIBUTE_ID").eq(attr.get("ID")))
                                    .and(dt != null ? valuesTable.field("REPORT_DATE").le(dt) : DSL.trueCondition())
                                    .asTable("sub")
                    ).where(DSL.field("\"sub\".\"p\"").eq(1))
                            .asTable(attrSubTable)
            ).on("\"" + attrSubTable + "\"" + "." + "ENTITY_ID = \"enr\".id");
        }

        return queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
    }

    private Table getValuesTable(String attrType) {
        switch (attrType) {
            case "STRING":
                return EAV_BE_STRING_VALUES;
            case "DOUBLE":
                return EAV_BE_DOUBLE_VALUES;
            case "INTEGER":
                return EAV_BE_INTEGER_VALUES;
            case "DATE":
                return EAV_BE_DATE_VALUES;
            case "BOOLEAN":
                return EAV_BE_BOOLEAN_VALUES;
        }

        return null;
    }

    private void addOpenCloseDates(List<Map<String, Object>> rows) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

        Map<String, Object> prev = null;

        for (Map<String, Object> row : rows) {
            Date repDate = (Date) row.get("report_date");
            String sRepDate = df.format(repDate);

            row.put("open_date", sRepDate);

            if (prev != null) {
                Object id = row.get("ID");
                Object prevId = prev.get("ID");
                if(row.get("closed_date") != null)
                    row.put("close_date", df.format(row.get("closed_date")));

                if (id.equals(prevId)) {
                    prev.put("close_date", sRepDate);
                }
            }
            prev = row;
        }
    }

    private List<Map<String, Object>> removeHistory(List<Map<String, Object>> rows) {
        Map<Object, Map<String, Object>> groupedRows = new TreeMap<>();

        for (Map<String, Object> row : rows) {
            String groupProperty = "ID";
            Object groupPropertyValue = row.get(groupProperty);
            groupedRows.put(groupPropertyValue, row);
        }

        return new ArrayList<>(groupedRows.values());
    }

    private List<Map<String, Object>> filter(List<Map<String, Object>> rows, Date date) {
        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Date repDate = (Date) row.get("report_date");

            if (!repDate.after(date)) {
                filtered.add(row);
            }
        }

        return filtered;
    }

    @Override
    public boolean historyExists(long metaId, long entityId) {
        List<Map<String, Object>> rows = getRefListResponseWithHis(metaId, entityId);
        return rows.size() > 1;
    }
}
