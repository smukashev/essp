package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.impl.PaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_STRING_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_OPTIMIZER;

@Component
@Qualifier("devSearcherForm")
public class DevFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @Override
    public List<Pair> getMetaClasses(long userId) {
        return null;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        return null;
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {
        MetaClass pcMeta = metaClassRepository.getMetaClass("primary_contract");

        IMetaAttribute noAttribute = pcMeta.getMetaAttribute("no");

        Date reportDate = new Date();
        if(parameters.get("date") != null)
            reportDate = (Date) DataTypes.getCastObject(DataTypes.DATE, parameters.get("date"));

        ISearchResult result = new PaginableSearchResult();
        List<BaseEntity> entityList = new ArrayList<>();
        result.setData(entityList);
        SearchPagination pagination = new SearchPagination(0);
        result.setPagination(pagination);

        if(parameters.get("pDate").length() < 1 && parameters.get("no").length() < 1) {

            Select inner = context.select(EAV_BE_STRING_VALUES.ENTITY_ID)
                    .from(EAV_BE_STRING_VALUES)
                    .where(EAV_BE_STRING_VALUES.CREDITOR_ID.eq(creditorId))
                    .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(noAttribute.getId()))
                    .orderBy(DSL.rand());

            Select select = context.select(DSL.field("ENTITY_ID"))
                    .from(inner)
                    .where(DSL.field("rownum").eq(1));

            List<Long> primaryContracts = jdbcTemplate.queryForList(select.getSQL(), select.getBindValues().toArray(), Long.class);

            for (Long id : primaryContracts) {
                String keyString = id + Errors.SEPARATOR + creditorId;

                select = context.select(EAV_OPTIMIZER.ENTITY_ID)
                        .from(EAV_OPTIMIZER)
                        .where(EAV_OPTIMIZER.CREDITOR_ID.eq(creditorId))
                        .and(EAV_OPTIMIZER.META_ID.eq(59L))
                        .and(EAV_OPTIMIZER.KEY_STRING.eq(keyString));


                Long creditId = jdbcTemplate.queryForLong(select.getSQL(), select.getBindValues().toArray());
                entityList.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(creditId, reportDate));
            }

            pagination.setTotalCount(1);
        }

        return result;
    }
}
