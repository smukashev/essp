package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import kz.bsbnb.eav.persistance.generated.tables.EavBeStringValues;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.searchForm.ISearchResult;
import kz.bsbnb.usci.eav.model.searchForm.SearchPagination;
import kz.bsbnb.usci.eav.model.searchForm.impl.NonPaginableSearchResult;
import kz.bsbnb.usci.eav.model.searchForm.impl.PaginableSearchResult;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import kz.bsbnb.usci.eav.util.DataUtils;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static kz.bsbnb.eav.persistance.generated.Tables.*;

/**
 * Created by Bauyrzhan.Makhambeto on 01/07/2015.
 */
@Component
public class PersonFormImpl extends AbstractSubjectForm {

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
            reportDate = (Date) DataTypes.getCastObject(DataTypes.DATE, parameters.get("date"));

        if(parameters.get("pageNo") != null)
            ret = new PaginableSearchResult();
        else
            ret = new NonPaginableSearchResult();

        ret.setData(entities);

        MetaClass personNameClass = metaClassRepository.getMetaClass("person_name");
        MetaClass personClass = metaClassRepository.getMetaClass("person_info");
        MetaClass subjectClass = metaClassRepository.getMetaClass("subject");

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

        EavBeStringValues t1 = EAV_BE_STRING_VALUES.as("t1");
        EavBeStringValues t2 = EAV_BE_STRING_VALUES.as("t2");
        EavBeStringValues t3 = EAV_BE_STRING_VALUES.as("t3");

        SelectJoinStep select = context.select(t1.ENTITY_ID)
                .from(t1);

        SelectOnConditionStep tempOnSelect;

        if(vals.size() > 1) {
            tempOnSelect = select.join(t2)
                    .on(t1.ENTITY_ID.eq(t2.ENTITY_ID))
                    .and(t2.ATTRIBUTE_ID.eq(attributeIds.get(1)))
                    .and(t2.VALUE.lower().like("%" + vals.get(1).toLowerCase() + "%"));


            if(creditorId > 0)
                tempOnSelect = tempOnSelect.and(t2.CREDITOR_ID.eq(creditorId));

            if(reportDate != null)
                tempOnSelect = tempOnSelect.and(t2.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

            select = tempOnSelect;
        }


        if(vals.size() > 2) {
            tempOnSelect = select.join(t3)
                    .on(t1.ENTITY_ID.eq(t3.ENTITY_ID))
                    .and(t3.ATTRIBUTE_ID.eq(attributeIds.get(2)))
                    .and(t3.VALUE.lower().like("%" + vals.get(2).toLowerCase() + "%"))
                    .and(t2.ENTITY_ID.eq(t3.ENTITY_ID));

            if(creditorId > 0)
                tempOnSelect = tempOnSelect.and(t3.CREDITOR_ID.eq(creditorId));

            if(reportDate != null)
                tempOnSelect = tempOnSelect.and(t3.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

            select = tempOnSelect;
        }

        SelectConditionStep fioSelect = select.where(t1.ATTRIBUTE_ID.eq(attributeIds.get(0)))
                .and(t1.VALUE.lower().like("%" + vals.get(0).toLowerCase() + "%"));

        if(creditorId > 0)
            fioSelect = fioSelect.and(t1.CREDITOR_ID.eq(creditorId));

        if(reportDate != null)
            fioSelect = fioSelect.and(t1.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

        Select setSelect = context.select(EAV_BE_COMPLEX_SET_VALUES.SET_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.in(
                        fioSelect
                ));

        Select personInfoSelect = context.select(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .where(EAV_BE_ENTITY_COMPLEX_SETS.ID.in(setSelect))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(namesAttribute.getId()));

        IMetaAttribute personInfoAttribute = subjectClass.getElAttribute("person_info");

        SelectConditionStep subjectSelectWhere = context.select(EAV_BE_COMPLEX_VALUES.ENTITY_ID)
                .from(EAV_BE_COMPLEX_VALUES)
                .where(EAV_BE_COMPLEX_VALUES.ENTITY_VALUE_ID.in(personInfoSelect))
                .and(EAV_BE_COMPLEX_VALUES.ATTRIBUTE_ID.eq(personInfoAttribute.getId()));

        Select subjectSelect = subjectSelectWhere;
        if(reportDate != null)
            subjectSelect = subjectSelectWhere.and(EAV_BE_COMPLEX_VALUES.REPORT_DATE.lessOrEqual(DataUtils.convert(reportDate)));

        /*if(parameters.get("pageNo") != null) {
            Select countSelect = context.select(DSL.count()).from(personInfoSelect);
            int cnt = jdbcTemplate.queryForInt(countSelect.getSQL(), countSelect.getBindValues().toArray());
            SearchPagination pagination = new SearchPagination(cnt);
            ret.setPagination(pagination);
        }*/

        logger.debug(subjectSelect.toString());

        Long pageNo = 1L;

        if(parameters.get("pageNo") != null) {
            pageNo = Long.parseLong(parameters.get("pageNo"));
        }

        List<Long> subjectIds = jdbcTemplate.queryForList(subjectSelect.getSQL(), subjectSelect.getBindValues().toArray(), Long.class);

        SearchPagination pagination = new SearchPagination(subjectIds.size());
        ret.setPagination(pagination);
        prepareByPageNo(subjectIds, entities, reportDate, pageNo);
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
