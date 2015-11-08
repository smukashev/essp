package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.PaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

@Component
public class OrgFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    DSLContext context;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    private final Logger logger = LoggerFactory.getLogger(OrgFormImpl.class);

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass organization = metaClassRepository.getMetaClass("organization_info");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(organization.getId(), organization.getClassName(), "организация (по наименованию)"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass,String prefix) {
        return "Наименование: <input type='text' name='name' style='width: 95%; margin: 5px'></input>" +
                "<div style='text-align:center; width:100%'><img src='/static-usci/ext/resources/ext-theme-classic/images/grid/loading.gif' id='form-loading' style='display:none'/></div>";
    }

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {
        ISearchResult ret;

        if(parameters.get("pageNo") != null)
            ret = new PaginableSearchResult();
        else
            ret = new NonPaginableSearchResult();

        List<BaseEntity> entities = new LinkedList<>();
        ret.setData(entities);

        String name = parameters.get("name");
        if(name.trim().length() < 1)
            return ret;
        Date reportDate = null;
        if(parameters.get("date")!=null)
            reportDate = (Date) DataTypes.fromString(DataTypes.DATE, parameters.get("date"));


        MetaClass orgNameMeta = metaClassRepository.getMetaClass("organization_name");
        MetaClass orgMeta = metaClassRepository.getMetaClass("organization_info");

        IMetaAttribute attribute = orgNameMeta.getMetaAttribute("name");
        IMetaAttribute namesAttribute = orgMeta.getMetaAttribute("names");


        Select nameSelect = context.select(EAV_BE_STRING_VALUES.ENTITY_ID)
                .from(EAV_BE_STRING_VALUES)
                .where(EAV_BE_STRING_VALUES.VALUE.lower().like("%" + name.toLowerCase() + "%"))
                .and(EAV_BE_STRING_VALUES.ATTRIBUTE_ID.eq(attribute.getId()));

        Select setSelect = context.select(EAV_BE_COMPLEX_SET_VALUES.SET_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.in(nameSelect));

        Select orgSelect = context.select(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .where(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.in(setSelect))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(namesAttribute.getId()));


        logger.debug(orgSelect.toString());


        List<Long> orgIds = jdbcTemplate.queryForList(orgSelect.getSQL(), orgSelect.getBindValues().toArray(), Long.class);

        if(reportDate != null) {
            for (Long id : orgIds) {
                entities.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, reportDate));
            }
        } else {
            for(Long id : orgIds)
                entities.add((BaseEntity) baseEntityLoadDao.load(id));
        }

        return ret;
    }
}
