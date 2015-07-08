package kz.bsbnb.usci.core.service.form.searcher.impl.cr;

import edu.emory.mathcs.backport.java.util.Arrays;
import kz.bsbnb.usci.core.service.IMetaFactoryService;
import kz.bsbnb.usci.core.service.form.searcher.ISearcherForm;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityProcessorDao;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.persistance.searcher.IBaseEntitySearcher;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Pair;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    private final Logger logger = LoggerFactory.getLogger(PersonFormImpl.class);

    @Override
    public List<BaseEntity> search(HashMap<String, String> parameters, MetaClass metaClass) {
        String firstName = parameters.get("firstName");
        String lastName = parameters.get("lastName");
        String middleName = parameters.get("middleName");
        List<BaseEntity> ret = new ArrayList<>();

        MetaClass personNameClass = metaClassRepository.getMetaClass("person_name");
        MetaClass personClass = metaClassRepository.getMetaClass("person");

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
                    .and(EAV_BE_STRING_VALUES.as("t2").VALUE.eq(vals.get(1)));

        if(vals.size() > 2)
            select = select.join(EAV_BE_STRING_VALUES.as("t3"))
                    .on(EAV_BE_STRING_VALUES.as("t1").ENTITY_ID.eq(EAV_BE_STRING_VALUES.as("t3").ENTITY_ID))
                    .and(EAV_BE_STRING_VALUES.as("t3").ATTRIBUTE_ID.eq(attributeIds.get(2)))
                    .and(EAV_BE_STRING_VALUES.as("t3").VALUE.eq(vals.get(2)))
                    .and(EAV_BE_STRING_VALUES.as("t2").ENTITY_ID.eq(EAV_BE_STRING_VALUES.as("t3").ENTITY_ID));

        Select fioSelect = select.where(EAV_BE_STRING_VALUES.as("t1").ATTRIBUTE_ID.eq(attributeIds.get(0)))
                .and(EAV_BE_STRING_VALUES.as("t1").VALUE.eq(vals.get(0)));


        Select setSelect = context.select(EAV_BE_COMPLEX_SET_VALUES.SET_ID)
                .from(EAV_BE_COMPLEX_SET_VALUES)
                .where(EAV_BE_COMPLEX_SET_VALUES.ENTITY_VALUE_ID.in(
                        fioSelect
                ));

        Select entitySelect = context.select(EAV_BE_ENTITY_COMPLEX_SETS.ENTITY_ID)
                .from(EAV_BE_ENTITY_COMPLEX_SETS)
                .where(EAV_BE_ENTITY_COMPLEX_SETS.SET_ID.in(setSelect))
                .and(EAV_BE_ENTITY_COMPLEX_SETS.ATTRIBUTE_ID.eq(namesAttribute.getId()));

        logger.debug(entitySelect.toString());

        List<Long> personIds = jdbcTemplate.queryForList(entitySelect.getSQL(), entitySelect.getBindValues().toArray(), Long.class);

        for(Long id : personIds) {
            ret.add((BaseEntity) baseEntityProcessorDao.load(id));
        }

        return ret;
    }

    @Override
    public List<Pair> getMetaClasses(long userId) {
        MetaClass person = metaClassRepository.getMetaClass("person");
        List<Pair> ret = new LinkedList<>();
        ret.add(new Pair(person.getId(), person.getClassName(), "физ лицо (по ФИО)"));
        return ret;
    }

    @Override
    public String getDom(long userId, IMetaClass metaClass) {
        return "Фамилия: <input type='text' name='lastName' style='width: 95%; margin: 5px'></input><br/>" +
                "Имя: <input type='text' name='firstName' style='width: 95%; margin: 5px'></input><br/>" +
                "Отчество: <input type='text' name='middleName' style='width: 95%; margin: 5px'></input></form>";
    }
}
