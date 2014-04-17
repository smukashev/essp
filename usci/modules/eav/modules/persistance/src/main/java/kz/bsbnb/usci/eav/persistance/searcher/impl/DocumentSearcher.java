package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_VALUES;

@Component
public class DocumentSearcher extends JDBCSupport implements IBaseEntitySearcher
{
    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public String getClassName()
    {
        return "document";
    }

    @Override
    public Long findSingle(BaseEntity entity)
    {
        if (entity.getId() > 0)
            return entity.getId();

        List<Long> ids = searcherPool.getSearcher(entity.getMeta().
                getClassName()).findAll(entity);

        if (ids.size() > 1) {
            //throw new RuntimeException("Found more than one instance of BaseEntity. Needed one.");
        }

        Long id = ids.size() >= 1 ? ids.get(0) : null;

        if (id != null)
            entity.setId(id);

        return id;
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity entity)
    {
        ArrayList<Long> res = new ArrayList<Long>();

        BaseEntity docType = (BaseEntity)entity.getEl("doc_type");

        if (docType != null) {
            Long docTypeId;

            if (docType.getId() > 0) {
                docTypeId = docType.getId();
            } else {
                docTypeId = searcherPool.getSearcher(docType.getMeta().
                        getClassName()).findSingle(docType);
            }

            if (docTypeId > 0) {
                /*SelectConditionStep select = context.
                        select(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID.as("inner_id")).
                        from(EAV_BE_STRING_VALUES.as("d_no")).
                            join(EAV_BE_COMPLEX_VALUES.as("d_dt")).
                                on(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_ID.equal(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID)).
                            join(EAV_BE_ENTITIES.as("d_root")).
                                on(EAV_BE_ENTITIES.as("d_root").ID.equal(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID)).
                        where(EAV_BE_ENTITIES.as("d_root").CLASS_ID.equal(entity.getMeta().getId())).

                            and(EAV_BE_COMPLEX_VALUES.as("d_dt").ATTRIBUTE_ID.equal(entity.getMetaAttribute("doc_type").getId())).
                            and(EAV_BE_STRING_VALUES.as("d_no").ATTRIBUTE_ID.equal(entity.getMetaAttribute("no").getId())).

                            and(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_VALUE_ID.equal(docTypeId)).
                            and(EAV_BE_STRING_VALUES.as("d_no").VALUE.equal((String)(entity.getBaseValue("no").getValue())));*/

                /*SelectConditionStep select = context.
                        select(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID.as("inner_id")).
                        from(EAV_BE_STRING_VALUES.as("d_no")).
                        join(EAV_BE_COMPLEX_VALUES.as("d_dt")).
                        on(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_ID.equal(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID)).
                        where(EAV_BE_COMPLEX_VALUES.as("d_dt").ATTRIBUTE_ID.equal(entity.getMetaAttribute("doc_type").getId())).
                        and(EAV_BE_STRING_VALUES.as("d_no").ATTRIBUTE_ID.equal(entity.getMetaAttribute("no").getId())).

                        and(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_VALUE_ID.equal(docTypeId)).
                        and(EAV_BE_STRING_VALUES.as("d_no").VALUE.equal((String)(entity.getBaseValue("no").getValue())));*/

                /*SelectConditionStep select = context.
                    select(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID.as("inner_id")).
                        from(EAV_BE_STRING_VALUES.as("d_no")).
                        where(
                            DSL.exists(
                                    context.select(DSL.val(1)).
                                    from(EAV_BE_COMPLEX_VALUES.as("d_dt")).
                                    where(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_ID.equal(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID)).
                                    and(EAV_BE_COMPLEX_VALUES.as("d_dt").ATTRIBUTE_ID.equal(entity.getMetaAttribute("doc_type").getId())).
                                and(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_VALUE_ID.equal(docTypeId))
                            ).
                        and(EAV_BE_STRING_VALUES.as("d_no").ATTRIBUTE_ID.equal(entity.getMetaAttribute("no").getId())).
                        and(EAV_BE_STRING_VALUES.as("d_no").VALUE.equal((String) (entity.getBaseValue("no").getValue()))));*/

                SelectConditionStep select = context.
                select(EAV_BE_STRING_VALUES.as("d_no").ENTITY_ID.as("inner_id")).
                from(EAV_BE_STRING_VALUES.as("d_no")).
                where(EAV_BE_STRING_VALUES.as("d_no").ATTRIBUTE_ID.equal(entity.getMetaAttribute("no").getId())).
                and(EAV_BE_STRING_VALUES.as("d_no").VALUE.equal((String) (entity.getBaseValue("no").getValue())));

                List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(), select.getBindValues().toArray()); 
                for (Map<String, Object> row : rows)
                {
                    long newId = ((BigDecimal)row.get("inner_id")).longValue();
                    SelectConditionStep selectInner = context.
                        select(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_ID.as("inner_id")).
                        from(EAV_BE_COMPLEX_VALUES.as("d_dt")).
                        where(EAV_BE_COMPLEX_VALUES.as("d_dt").ATTRIBUTE_ID.equal(entity.getMetaAttribute("doc_type").getId())).
                        and(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_VALUE_ID.equal(docTypeId)).
                        and(EAV_BE_COMPLEX_VALUES.as("d_dt").ENTITY_ID.equal(newId));

                    if (queryForListWithStats(selectInner.getSQL(), selectInner.getBindValues().toArray()).size() <= 0)
                        continue;

                    res.add(newId);
                }
            }
        }

        return res;
    }
}
