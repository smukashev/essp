package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.PaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Created by Bauyrzhan.Makhambeto on 01/07/2015.
 */
@Component
public class PersonFormImpl extends JDBCSupport implements ISearcherForm {

    @Autowired
    IMetaClassRepository metaClassRepository;

    @Autowired
    DSLContext context;

    @Autowired
    IBaseEntityProcessorDao baseEntityProcessorDao;

    @Autowired
    IBaseEntityLoadDao baseEntityLoadDao;

    final private static int maxPageSize = 50;
    final private static int maxResultSize = 500;

    private final Logger logger = LoggerFactory.getLogger(PersonFormImpl.class);

    @Override
    public ISearchResult search(HashMap<String, String> parameters, MetaClass metaClass, String prefix, long creditorId) {

        ISearchResult ret;
        String firstName = parameters.get("firstName");
        String lastName = parameters.get("lastName");
        String middleName = parameters.get("middleName");
        //List<BaseEntity> ret = new ArrayList<>();
        List<BaseEntity> entities = new ArrayList<>();
        Date reportDate = null;

        if(parameters.get("date")!=null)
            reportDate = (Date) DataTypes.fromString(DataTypes.DATE, parameters.get("date"));

        if(parameters.get("pageNo") != null)
            ret = new PaginableSearchResult();
        else
            ret = new NonPaginableSearchResult();

        ret.setData(entities);

        MetaClass personNameClass = metaClassRepository.getMetaClass("person_name");
        MetaClass personClass = metaClassRepository.getMetaClass("person_info");

        IMetaAttribute firstNameAttribute = personNameClass.getMetaAttribute("firstname");
        IMetaAttribute lastNameAttribute = personNameClass.getMetaAttribute("lastname");
        IMetaAttribute middleNameAttribute = personNameClass.getMetaAttribute("middlename");

        IMetaAttribute namesAttribute = personClass.getMetaAttribute("names");

        List<String> vals = new LinkedList<>();
        List<Long> attributeIds = new LinkedList<>();

        if(firstName != null && firstName.trim().length() > 0) {
            vals.add(firstName);
            attributeIds.add(firstNameAttribute.getId());
        }
        if(lastName != null && lastName.trim().length() > 0) {
            vals.add(lastName);
            attributeIds.add(lastNameAttribute.getId());
        }
        if(middleName != null && middleName.trim().length() > 0) {
            vals.add(middleName);
            attributeIds.add(middleNameAttribute.getId());
        }

        if(firstName == null && lastName == null && middleName == null)
            return ret;

        SelectJoinStep select = context.select(EAV_BE_STRING_VALUES.as("t1").ENTITY_ID)
                .from(EAV_BE_STRING_VALUES.as("t1"));

        if(vals.size() > 1)
            select = select.join(EAV_BE_STRING_VALUES.as("t2"))
                    .on(EAV_BE_STRING_VALUES.as("t1").ENTITY_ID.eq(EAV_BE_STRING_VALUES.as("t2").ENTITY_ID))
                    .and(EAV_BE_STRING_VALUES.as("t2").ATTRIBUTE_ID.eq(attributeIds.get(1)))
                    .and(EAV_BE_STRING_VALUES.as("t2").VALUE.lower().like("%" + vals.get(1).toLowerCase() + "%"));

        if(vals.size() > 2)
            select = select.join(EAV_BE_STRING_VALUES.as("t3"))
                    .on(EAV_BE_STRING_VALUES.as("t1").ENTITY_ID.eq(EAV_BE_STRING_VALUES.as("t3").ENTITY_ID))
                    .and(EAV_BE_STRING_VALUES.as("t3").ATTRIBUTE_ID.eq(attributeIds.get(2)))
                    .and(EAV_BE_STRING_VALUES.as("t3").VALUE.lower().like("%" + vals.get(2).toLowerCase() + "%"))
                    .and(EAV_BE_STRING_VALUES.as("t2").ENTITY_ID.eq(EAV_BE_STRING_VALUES.as("t3").ENTITY_ID));

        Select fioSelect = select.where(EAV_BE_STRING_VALUES.as("t1").ATTRIBUTE_ID.eq(attributeIds.get(0)))
                .and(EAV_BE_STRING_VALUES.as("t1").VALUE.lower().like("%" + vals.get(0).toLowerCase() + "%"));


        Select setSelect = context.select(EAV_BE_COMPLEX_SET_VALUES.SET_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.in(
                        fioSelect
                ));

        Select entitySelect = context.select(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .where(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.in(setSelect))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(namesAttribute.getId()));

        if(parameters.get("pageNo") != null) {
            Select countSelect = context.select(DSL.count()).from(entitySelect);
            int cnt = jdbcTemplate.queryForInt(countSelect.getSQL(), countSelect.getBindValues().toArray());
            SearchPagination pagination = new SearchPagination(cnt);
            ret.setPagination(pagination);
        }

        logger.debug(entitySelect.toString());

        List<Long> personIds = jdbcTemplate.queryForList(entitySelect.getSQL(), entitySelect.getBindValues().toArray(), Long.class);

        if(reportDate != null) {
            for (Long id : personIds) {
                entities.add((BaseEntity) baseEntityLoadDao.loadByMaxReportDate(id, reportDate));
            }
        } else {
            for(Long id : personIds)
                entities.add((BaseEntity) baseEntityLoadDao.load(id));
        }

        return ret;
    }

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass person = metaClassRepository.getMetaClass("person_info");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(person.getId(), person.getClassName(), "физ лицо (по ФИО)"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass, String prefix) {
        return "Фамилия: <input type='text' name='lastName' style='width: 95%; margin: 5px'></input><br/>" +
                "Имя: <input type='text' name='firstName' style='width: 95%; margin: 5px'></input><br/>" +
                "Отчество: <input type='text' name='middleName' style='width: 95%; margin: 5px'></input></form>" +
                "<div style='text-align:center; width:100%'><img src='/static-usci/ext/resources/ext-theme-classic/images/grid/loading.gif' id='form-loading' style='display:none'/></div>";
    }
}
