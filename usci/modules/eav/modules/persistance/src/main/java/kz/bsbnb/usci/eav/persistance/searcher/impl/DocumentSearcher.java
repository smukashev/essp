package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class DocumentSearcher extends JDBCSupport implements IBaseEntitySearcher {
    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    private final Logger logger = LoggerFactory.getLogger(DocumentSearcher.class);

    @Override
    public String getClassName() {
        return "document";
    }

    @Override
    public Long findSingle(BaseEntity entity, Long creditorId) {
        if (entity.getId() > 0)
            return entity.getId();

        List<Long> ids = searcherPool.getSearcher(entity.getMeta().getClassName()).findAll(entity, creditorId);

        if (ids.size() > 1)
            throw new IllegalStateException("Найдено более одного документа;\n" + entity);

        if (ids.size() < 1)
            return null;

        entity.setId(ids.get(0));

        return ids.get(0);
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity entity, Long creditorId) {
        ArrayList<Long> res = new ArrayList<>();

        BaseEntity docType = (BaseEntity) entity.getEl("doc_type");

        if (docType != null) {
            Long docTypeId;

            if (docType.getId() > 0) {
                docTypeId = docType.getId();
            } else {
                docTypeId = searcherPool.getSearcher(docType.getMeta().
                        getClassName()).findSingle(docType, creditorId);
            }

            if (docTypeId == null)
                return res;

            if (docTypeId > 0) {
                SelectConditionStep select = (SelectConditionStep) context.
                        select(EAV_BE_ENTITIES.ID.as("inner_id")).hint("/* +PARALLEL(3)*/")
                        .from(EAV_BE_ENTITIES).
                        join(EAV_BE_COMPLEX_VALUES).
                        on(EAV_BE_ENTITIES.ID.equal(EAV_BE_COMPLEX_VALUES.ENTITY_ID)).
                        and(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.equal(entity.getMetaAttribute("doc_type").getId())).
                        and(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.equal(docTypeId)).
                        join(EAV_BE_STRING_VALUES).
                        on(EAV_BE_ENTITIES.ID.equal(EAV_BE_STRING_VALUES.ENTITY_ID))
                        .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.equal(entity.getMetaAttribute("no").getId()))
                        .and(EAV_BE_STRING_VALUES.CREDITOR_ID.equal(creditorId))
                                .and(EAV_BE_STRING_VALUES.VALUE.equal((String) (entity.getBaseValue("no").getValue())));

                logger.debug(select.toString());
                List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                        select.getBindValues().toArray());

                for (Map<String, Object> row : rows) {
                    long newId = ((BigDecimal) row.get("inner_id")).longValue();
                    res.add(newId);
                }
            }
        }

        return res;
    }
}