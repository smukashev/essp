package kz.bsbnb.usci.eav.persistance.searcher.impl;

import kz.bsbnb.usci.eav.Errors;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.persistance.searcher.pool.impl.BasicBaseEntitySearcherPool;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_COMPLEX_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

@Component
public class CreditSearcher extends JDBCSupport implements IBaseEntitySearcher {
    @Autowired
    private BasicBaseEntitySearcherPool searcherPool;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public String getClassName() {
        return "credit";
    }

    @Override
    public Long findSingle(BaseEntity entity, Long creditorId) {
        if (entity.getId() > 0)
            return entity.getId();

        List<Long> ids = searcherPool.getSearcher(entity.getMeta().getClassName()).findAll(entity, creditorId);

        if (ids.size() > 1)
            throw new RuntimeException(Errors.E174+"");

        if (ids.size() < 1)
            return null;

        entity.setId(ids.get(0));

        return ids.get(0);
    }

    @Override
    public ArrayList<Long> findAll(BaseEntity entity, Long creditorId) {
        ArrayList<Long> res = new ArrayList<>();

        BaseEntity primaryContract = (BaseEntity) entity.getEl("primary_contract");

        if (primaryContract != null) {
            Long primaryContractId;

            if (primaryContract.getId() > 0) {
                primaryContractId = primaryContract.getId();
            } else {
                primaryContractId = searcherPool.getSearcher(primaryContract.getMeta().
                        getClassName()).findSingle(primaryContract, creditorId);
            }

            if (primaryContractId != null && creditorId != null && primaryContractId > 0 && creditorId > 0) {
                SelectConditionStep select = (SelectConditionStep) context
                        .select(EAV_BE_ENTITIES.as("en").ID.as("inner_id"))
                        .from(EAV_BE_ENTITIES.as("en"))
                        .join(EAV_BE_COMPLEX_VALUES.as("co"))
                        .on(EAV_BE_ENTITIES.as("en").ID.equal(EAV_BE_COMPLEX_VALUES.as("co").ENTITY_ID))
                        .and(EAV_BE_COMPLEX_VALUES.as("co").ATTRIBUTE_ID.equal(entity.
                                getMetaAttribute("primary_contract").getId()))
                        .and(EAV_BE_COMPLEX_VALUES.as("co").ENTITY_VALUE_ID.equal(primaryContractId))
                        .join(EAV_BE_COMPLEX_VALUES.as("co2"))
                        .on(EAV_BE_ENTITIES.as("en").ID.equal(EAV_BE_COMPLEX_VALUES.as("co2").ENTITY_ID))
                        .and(EAV_BE_COMPLEX_VALUES.as("co2").ATTRIBUTE_ID.equal(entity.
                                getMetaAttribute("creditor").getId()))
                        .and(EAV_BE_COMPLEX_VALUES.as("co2").ENTITY_VALUE_ID.equal(creditorId))
                        .where(EAV_BE_ENTITIES.as("en").DELETED.eq(DataUtils.convert(false)));

                List<Map<String, Object>> rows = queryForListWithStats(select.getSQL(),
                        select.getBindValues().toArray());

                for (Map<String, Object> row : rows) {
                    long innerId = ((BigDecimal) row.get("inner_id")).longValue();
                    res.add(innerId);
                }
            }
        }

        return res;
    }
}