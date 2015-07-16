package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.model.EntityHolder;
import kz.bsbnb.usci.eav.model.RefColumnsResponse;
import kz.bsbnb.usci.eav.model.RefListItem;
import kz.bsbnb.usci.eav.model.RefListResponse;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityApplyDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.repository.IBaseEntityRepository;
import kz.bsbnb.usci.eav.repository.IBatchRepository;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.tool.Quote;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Repository
public class BaseEntityProcessorDaoImpl extends JDBCSupport implements IBaseEntityProcessorDao {
    private final Logger logger = LoggerFactory.getLogger(BaseEntityProcessorDaoImpl.class);

    @Autowired
    IBatchRepository batchRepository;

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntityRepository baseEntityCacheDao;

    @Autowired
    IPersistableDaoPool persistableDaoPool;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @Autowired
    IBaseEntityApplyDao baseEntityApplyDao;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    private IDaoListener applyListener;

    @Autowired
    public void setApplyListener(IDaoListener applyListener) {
        this.applyListener = applyListener;
    }

    @Override
    public long search(IBaseEntity baseEntity) {
        IMetaClass metaClass = baseEntity.getMeta();
        Long baseEntityId = searcherPool.getSearcher(metaClass.getClassName()).findSingle((BaseEntity) baseEntity);
        return baseEntityId == null ? 0 : baseEntityId;
    }

    public List<Long> search(long metaClassId) {
        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        List<Long> baseEntityIds = new ArrayList<>();
        for (Map<String, Object> row : rows)
            baseEntityIds.add(((BigDecimal) row.get(EAV_BE_ENTITIES.ID.getName())).longValue());

        return baseEntityIds;
    }

    public List<Long> search(String className) {
        MetaClass metaClass = metaClassRepository.getMetaClass(className);
        if (metaClass != null)
            return search(metaClass.getId());

        return new ArrayList<>();
    }

    public IBaseEntity postPrepare(IBaseEntity baseEntity, IBaseEntity parentEntity) {
        MetaClass metaClass = baseEntity.getMeta();

        for (String attribute : baseEntity.getAttributes()) {
            IMetaType memberType = baseEntity.getMemberType(attribute);

            if (memberType.isComplex()) {
                IBaseValue memberValue = baseEntity.getBaseValue(attribute);

                if (memberValue.getValue() != null) {
                    if (memberType.isSet()) {
                        IMetaSet childMetaSet = (IMetaSet) memberType;
                        IMetaType childMetaType = childMetaSet.getMemberType();

                        if (childMetaType.isSet()) {
                            throw new UnsupportedOperationException("Не реализовано;");
                        } else {
                            IBaseSet childBaseSet = (IBaseSet) memberValue.getValue();

                            for (IBaseValue childBaseValue : childBaseSet.get()) {
                                IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();

                                if (childBaseEntity.getValueCount() != 0)
                                    postPrepare((IBaseEntity) childBaseValue.getValue(), baseEntity);
                            }
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) memberValue.getValue();

                        if (childBaseEntity.getValueCount() != 0)
                            postPrepare((IBaseEntity) memberValue.getValue(), baseEntity);
                    }
                }
            }
        }

        if (parentEntity != null && metaClass.isSearchable() && metaClass.isParentIsKey()) {
            Long baseEntityId = searcherPool.getImprovedBaseEntityLocalSearcher().
                    findSingleWithParent((BaseEntity) baseEntity, (BaseEntity) parentEntity);

            if (baseEntityId == null) baseEntity.setId(0);
            else baseEntity.setId(baseEntityId);
        }

        return baseEntity;
    }

    public IBaseEntity prepare(IBaseEntity baseEntity) {
        IMetaClass metaClass = baseEntity.getMeta();

        for (String attribute : baseEntity.getAttributes()) {
            IMetaType metaType = baseEntity.getMemberType(attribute);
            if (metaType.isComplex()) {
                IBaseValue baseValue = baseEntity.getBaseValue(attribute);
                if (baseValue.getValue() != null) {
                    if (metaType.isSet()) {
                        IMetaSet childMetaSet = (IMetaSet) metaType;
                        IMetaType childMetaType = childMetaSet.getMemberType();
                        if (childMetaType.isSet()) {
                            throw new UnsupportedOperationException("Не реализовано;");
                        } else {
                            IBaseSet childBaseSet = (IBaseSet) baseValue.getValue();
                            for (IBaseValue childBaseValue : childBaseSet.get()) {
                                IBaseEntity childBaseEntity = (IBaseEntity) childBaseValue.getValue();
                                if (childBaseEntity.getValueCount() != 0) {
                                    prepare((IBaseEntity) childBaseValue.getValue());
                                }
                            }
                        }
                    } else {
                        IBaseEntity childBaseEntity = (IBaseEntity) baseValue.getValue();
                        if (childBaseEntity.getValueCount() != 0) {
                            prepare((IBaseEntity) baseValue.getValue());
                        }
                    }
                }
            }
        }

        if (metaClass.isSearchable()) {
            long baseEntityId = search(baseEntity);
            if (baseEntityId > 0)
                baseEntity.setId(baseEntityId);
        }

        return baseEntity;
    }

    private boolean historyExists(long metaId, long entityId) {
        List<Map<String, Object>> rows = getRefListResponseWithHis(metaId, entityId);
        return rows.size() > 1;
    }

    @Override
    @Transactional
    public IBaseEntity process(final IBaseEntity baseEntity) {
        EntityHolder entityHolder = new EntityHolder();

        IBaseEntityManager baseEntityManager = new BaseEntityManager();
        IBaseEntity baseEntityPrepared = prepare(((BaseEntity) baseEntity).clone());
        IBaseEntity baseEntityPostPrepared = postPrepare(((BaseEntity) baseEntityPrepared).clone(), null);
        IBaseEntity baseEntityApplied;

        if (baseEntityPostPrepared.getOperation() != null) {
            switch (baseEntityPostPrepared.getOperation()) {
                case DELETE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new RuntimeException("Сущность для удаления не найдена!");

                    if (baseEntity.getMeta().isReference() && historyExists(
                            baseEntityPostPrepared.getMeta().getId(), baseEntityPostPrepared.getId()))
                        throw new RuntimeException("Справочник с историей не может быть удалена");

                    baseEntityManager.registerAsDeleted(baseEntityPostPrepared);
                    baseEntityApplied = ((BaseEntity) baseEntityPostPrepared).clone();
                    entityHolder.setApplied(baseEntityApplied);
                    entityHolder.setSaving(baseEntityPostPrepared);
                    break;
                case CLOSE:
                    if (baseEntityPostPrepared.getId() <= 0)
                        throw new RuntimeException("Сущность для закрытия не найдена!");

                    baseEntityApplied = baseEntityPostPrepared;

                    break;
                default:
                    throw new UnsupportedOperationException("Операция не поддерживается: "
                            + baseEntityPostPrepared.getOperation());
            }
        } else {
            baseEntityApplied = baseEntityApplyDao.apply(baseEntityPostPrepared, baseEntityManager, entityHolder);
            baseEntityApplyDao.applyToDb(baseEntityManager);
        }

        /* if (applyListener != null)
            applyListener.applyToDBEnded(entityHolder.saving, entityHolder.loaded,
                    entityHolder.applied, baseEntityManager); */

        return baseEntityApplied;
    }

    public List<Long> getEntityIDsByMetaclass(long metaClassId) {
        ArrayList<Long> entityIds = new ArrayList<>();

        Select select = context
                .select(EAV_BE_ENTITIES.ID)
                .from(EAV_BE_ENTITIES)
                .where(EAV_BE_ENTITIES.CLASS_ID.equal(metaClassId));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        Iterator<Map<String, Object>> i = rows.iterator();
        while (i.hasNext()) {
            Map<String, Object> row = i.next();

            entityIds.add(((BigDecimal) row.get(EAV_BE_ENTITIES.ID.getName())).longValue());
        }

        return entityIds;
    }

    @Override
    public RefColumnsResponse getRefColumns(long metaClassId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select complexAttrsSelect = context.select().from(EAV_M_COMPLEX_ATTRIBUTES).where(EAV_M_COMPLEX_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));
        Select simpleSetsSelect = context.select().from(EAV_M_SIMPLE_SET).where(EAV_M_SIMPLE_SET.CONTAINING_ID.eq(metaClassId));
        Select complexSetsSelect = context.select().from(EAV_M_COMPLEX_SET).where(EAV_M_COMPLEX_SET.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexAttrs = queryForListWithStats(complexAttrsSelect.getSQL(), complexAttrsSelect.getBindValues().toArray());
        List<Map<String, Object>> simpleSets = queryForListWithStats(simpleSetsSelect.getSQL(), simpleSetsSelect.getBindValues().toArray());
        List<Map<String, Object>> complexSets = queryForListWithStats(complexSetsSelect.getSQL(), complexSetsSelect.getBindValues().toArray());

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

    private List<Map<String, Object>> removeHistory(List<Map<String, Object>> rows) {
        Map<Object, Map<String, Object>> groupedRows = new TreeMap<Object, Map<String, Object>>();

        for (Map<String, Object> row : rows) {
            String groupProperty = "ID";
            Object groupPropertyValue = row.get(groupProperty);
            groupedRows.put(groupPropertyValue, row);
        }

        return new ArrayList<>(groupedRows.values());
    }

    private List<Map<String, Object>> filter(List<Map<String, Object>> rows, Date date) {
        List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();

        for (Map<String, Object> row : rows) {
            Date repDate = (Date) row.get("report_date");

            if (!repDate.after(date)) {
                filtered.add(row);
            }
        }

        return filtered;
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

                if (id.equals(prevId)) {
                    prev.put("close_date", sRepDate);
                }
            }
            prev = row;
        }
    }

    private List<Map<String, Object>> getRefListResponseWithHis(long metaClassId, Long entityId) {
        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());

        Collection<Field> fields = new ArrayList<Field>();
        fields.add(DSL.field("id"));
        fields.add(DSL.min(DSL.field("report_date")).as("report_date"));

        Collection<Field> groupByFields = new ArrayList<Field>();
        groupByFields.add(DSL.field("id"));

        for (Map<String, Object> attr : simpleAttrs) {
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            attrName = "\"" + attrName + "\"";
            fields.add(DSL.field(attrName));
            groupByFields.add(DSL.field(attrName));
        }

        Collection<Field> fieldsInner = new ArrayList<Field>();

        fieldsInner.add(DSL.field("\"dat\".id"));
        fieldsInner.add(DSL.field("\"dat\".report_date"));

        for (Map<String, Object> attr : simpleAttrs) {
            BigDecimal attrId = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.ID.getName());
            String attrName = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.NAME.getName());
            String attrType = (String) attr.get(EAV_M_SIMPLE_ATTRIBUTES.TYPE_CODE.getName());
            BigDecimal attrFinal = (BigDecimal) attr.get(EAV_M_SIMPLE_ATTRIBUTES.IS_FINAL.getName());
            boolean isFinal = attrFinal != null && attrFinal.byteValue() != 0;
            Table valuesTable = getValuesTable(attrType);

            SelectConditionStep<Record1<Object>> selectMaxRepDate = context.select(DSL.max(DSL.field("report_date"))).from(valuesTable)
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

        SelectLimitStep select = context.select(fields.toArray(new Field[]{})).from(
                context.select(fieldsInner.toArray(new Field[]{})).from(
                        context.select(EAV_BE_ENTITIES.ID, EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                                .from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES)
                                .on(EAV_BE_ENTITIES.ID.eq(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(entityId != null ? EAV_BE_ENTITIES.ID.eq(entityId) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
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

        Select simpleAttrsSelect = context.select().from(EAV_M_SIMPLE_ATTRIBUTES).where(EAV_M_SIMPLE_ATTRIBUTES.CONTAINING_ID.eq(metaClassId));

        List<Map<String, Object>> simpleAttrs = queryForListWithStats(simpleAttrsSelect.getSQL(), simpleAttrsSelect.getBindValues().toArray());

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
                                DSL.rowNumber().over().partitionBy(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID).orderBy(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.desc()).as("p")
                        ).from(EAV_BE_ENTITIES).join(EAV_BE_ENTITY_REPORT_DATES).on(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(EAV_BE_ENTITIES.ID))
                                .where(EAV_BE_ENTITIES.CLASS_ID.eq(metaClassId))
                                .and(dt != null ? EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.le(dt) : DSL.trueCondition())
                                .and(EAV_BE_ENTITIES.DELETED.ne(DataUtils.convert(true)))
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
                                    DSL.rowNumber().over().partitionBy(valuesTable.field("ENTITY_ID")).orderBy(valuesTable.field("REPORT_DATE").desc()).as("p")
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

    public List<BaseEntity> getEntityByMetaclass(MetaClass meta) {
        List<Long> ids = getEntityIDsByMetaclass(meta.getId());

        ArrayList<BaseEntity> entities = new ArrayList<>();

        for (Long id : ids)
            entities.add((BaseEntity) baseEntityLoadDao.load(id));

        return entities;
    }

    @Override
     public boolean isApproved(long id) {
        Select select = context
                .select(EAV_A_CREDITOR_STATE.ID)
                .from(EAV_A_CREDITOR_STATE)
                .where(EAV_A_CREDITOR_STATE.CREDITOR_ID.equal(id));

        logger.debug(select.toString());
        List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray());

        if (rows.size() > 1) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean remove(long baseEntityId) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.deleteRecursive(baseEntityId);
    }

    @Override
    public long getRandomBaseEntityId(IMetaClass metaClass) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClass);
    }

    @Override
    public long getRandomBaseEntityId(long metaClassId) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getRandomBaseEntityId(metaClassId);
    }

    @Override
    public Set<Long> getChildBaseEntityIds(long parentBaseEntityIds) {
        IBaseEntityDao baseEntityDao = persistableDaoPool
                .getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
        return baseEntityDao.getChildBaseEntityIds(parentBaseEntityIds);
    }

    @Override
    public void populate(String metaName, Long scId, Date reportDate) {
        Long id = metaClassRepository.getMetaClass(metaName).getId();
        Insert ins = context.insertInto(SC_ID_BAG, SC_ID_BAG.ENTITY_ID, SC_ID_BAG.SHOWCASE_ID, SC_ID_BAG.REPORT_DATE)
                .select(context.select(EAV_BE_ENTITIES.ID, DSL.val(scId).as("SHOWCASE_ID"), EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                        .from(EAV_BE_ENTITIES.join(EAV_BE_ENTITY_REPORT_DATES).on(EAV_BE_ENTITIES.ID.eq(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID)))
                        .where(EAV_BE_ENTITIES.CLASS_ID.equal(id)
                                .and(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE.eq(new java.sql.Date(reportDate.getTime())))));
        jdbcTemplate.update(ins.getSQL(), ins.getBindValues().toArray());
    }

    @Override
    public void populateSC() {
        Long id = metaClassRepository.getMetaClass("credit").getId();
        Insert insert = context.insertInto(SC_ENTITIES, SC_ENTITIES.ENTITY_ID).select(
                context.select(EAV_BE_ENTITIES.ID)
                        .from(EAV_BE_ENTITIES)
                        .where(EAV_BE_ENTITIES.CLASS_ID.eq(id))
        );
        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public void populateSC(Long creditorId) {
        MetaClass metaClass = metaClassRepository.getMetaClass("credit");
        IMetaAttribute metaAttribute = metaClass.getMetaAttribute("creditor");

        Insert insert = context.insertInto(SC_ENTITIES, SC_ENTITIES.ENTITY_ID).select(
                context.selectDistinct(EAV_BE_COMPLEX_VALUES.ENTITY_ID)
                        .from(EAV_BE_COMPLEX_VALUES)
                        .where(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(metaAttribute.getId()))
                        .and(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.eq(creditorId))
        );
        jdbcTemplate.update(insert.getSQL(), insert.getBindValues().toArray());
    }

    @Override
    public List<Long> getNewTableIds(Long id) {
        List<Long> list;
        Select select = context.select(SC_ID_BAG.ID).from(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id)).limit(10);
        Select select2 = context.select(select.field(0)).from(select);
        list = jdbcTemplate.queryForList(select2.getSQL(), Long.class, select2.getBindValues().toArray());

        return list;
    }

    @Override
    public void removeNewTableIds(List<Long> list, Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.ID.in(list).and(SC_ID_BAG.SHOWCASE_ID.eq(id)));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<Long> getSCEntityIds(Long id) {
        List<Long> list;
        Select select = context.select(SC_ID_BAG.ID).from(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id)).limit(100);
        Select select2 = context.select(select.field(0)).from(select);
        list = jdbcTemplate.queryForList(select2.getSQL(), Long.class, select2.getBindValues().toArray());

        return list;
    }

    @Override
    public List<Long[]> getSCEntityIds(int limit, Long prevMaxId) {
        Select select = context.select(SC_ENTITIES.ID, SC_ENTITIES.ENTITY_ID).from(SC_ENTITIES)
                .where(SC_ENTITIES.ID.gt(prevMaxId))
                .orderBy(SC_ENTITIES.ID).limit(limit);
        Select select2 = context.select(select.field(0), select.field(1)).from(select);
        List<Long[]> result = jdbcTemplate.query(select2.getSQL(), select2.getBindValues().toArray(), new RowMapper<Long[]>() {
            @Override
            public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long[] row = new Long[2];
                row[0] = rs.getLong(SC_ENTITIES.ID.getName());
                row[1] = rs.getLong(SC_ENTITIES.ENTITY_ID.getName());
                return row;
            }
        });
        return result;
    }

    @Override
    public List<Date> getEntityReportDates(Long entityId) {
        Select select = context.select(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE)
                .from(EAV_BE_ENTITY_REPORT_DATES)
                .where(EAV_BE_ENTITY_REPORT_DATES.ENTITY_ID.eq(entityId))
                .orderBy(EAV_BE_ENTITY_REPORT_DATES.REPORT_DATE);
        List<Date> reportDates = jdbcTemplate.queryForList(select.getSQL(), Date.class, select.getBindValues().toArray());
        return reportDates;
    }

    @Override
    public void removeSCEntityIds(List<Long> list, Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.ID.in(list).and(SC_ID_BAG.SHOWCASE_ID.eq(id)));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public void removeSCEntityIds(List<Long> entityIds) {
        Delete delete = context.delete(SC_ENTITIES).where(SC_ENTITIES.ENTITY_ID.in(entityIds));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public void removeShowcaseId(Long id) {
        Delete delete = context.delete(SC_ID_BAG).where(SC_ID_BAG.SHOWCASE_ID.eq(id));
        jdbcTemplate.update(delete.getSQL(), delete.getBindValues().toArray());
    }

    @Override
    public List<Long> getShowcaseIdsToLoad() {
        Select select = context.selectDistinct(SC_ID_BAG.SHOWCASE_ID).from(SC_ID_BAG);
        return jdbcTemplate.queryForList(select.getSQL(), Long.class);
    }
}