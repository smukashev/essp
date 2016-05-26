package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;
import kz.bsbnb.usci.eav.persistance.dao.ISQLGenerator;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.repository.IMetaClassRepository;
import kz.bsbnb.usci.eav.util.Errors;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static kz.bsbnb.eav.persistance.generated.Tables.*;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_DATE_VALUES;
import static kz.bsbnb.eav.persistance.generated.Tables.EAV_BE_ENTITIES;

@Repository
public class SQLGeneratorImpl extends JDBCSupport implements ISQLGenerator {
    @Qualifier("metaClassRepositoryImpl")
    @Autowired
    private IMetaClassRepository metaClassRepository;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private DSLContext context;

    @SuppressWarnings("all")
    @Override
    public Select getSimpleSelect(long metaId, boolean onlyKey) {
        final String ebe = "ebe";
        int counter = 1;

        List<Map> refList = new LinkedList<>();

        MetaClass metaClass = metaClassRepository.getMetaClass(metaId);
        final String metaClassName = metaClass.getClassName().toUpperCase();

        SelectSelectStep select = context.select(EAV_BE_ENTITIES.as(ebe).ID.as(metaClassName + "_ID"));

        TreeSet<String> names = new TreeSet<>();

        for (String attribute : metaClass.getAttributeNames()) {
            names.add(attribute);
        }

        for (String attribute : names) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if (onlyKey && !metaAttribute.isKey() && !metaAttribute.isOptionalKey())
                continue;

            String attrName = metaAttribute.getName().toUpperCase();

            if (metaType.isSet()) {
                if (metaType.isComplex()) {
                    MetaSet metaSet = (MetaSet) metaType;
                    MetaClass childMetaClass = (MetaClass) metaSet.getMemberType();
                    select.select(EAV_BE_ENTITY_COMPLEX_SETS.as("cs" + counter).ID.as(attrName + "_ID"));
                    select.select(EAV_BE_COMPLEX_SET_VALUES.as("csv" + counter).ENTITY_VALUE_ID.as(childMetaClass.getClassName().toUpperCase() + "_ID"));
                } else {
                    throw new IllegalStateException("Not yet implemented");
                }
            } else {
                if (metaType.isComplex()) {
                    select.select(EAV_BE_COMPLEX_VALUES.as("cv" + counter).ENTITY_VALUE_ID.as(attrName + "_ID"));
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
                    switch (metaValue.getTypeCode()) {
                        case STRING:
                            select.select(EAV_BE_STRING_VALUES.as("sv" + counter).VALUE.as(attrName));
                            break;
                        case INTEGER:
                            select.select(EAV_BE_INTEGER_VALUES.as("iv" + counter).VALUE.as(attrName));
                            break;
                        case BOOLEAN:
                            select.select(EAV_BE_BOOLEAN_VALUES.as("bv" + counter).VALUE.as(attrName));
                            break;
                        case DOUBLE:
                            select.select(EAV_BE_DOUBLE_VALUES.as("dov" + counter).VALUE.as(attrName));
                            break;
                        case DATE:
                            select.select(EAV_BE_DATE_VALUES.as("dav" + counter).VALUE.as(attrName));
                            break;
                        default:
                            throw new IllegalArgumentException(Errors.compose(Errors.E127));
                    }
                }
            }

            counter++;
        }

        SelectJoinStep selectJoinStep = select.from(EAV_BE_ENTITIES.as(ebe));

        counter = 1;
        for (String attribute : names) {
            IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attribute);
            IMetaType metaType = metaAttribute.getMetaType();

            if (onlyKey && !metaAttribute.isKey() && !metaAttribute.isOptionalKey())
                continue;

            if (metaType.isSet()) {
                if (metaType.isComplex()) {
                    selectJoinStep.leftOuterJoin(EAV_BE_ENTITY_COMPLEX_SETS.as("cs" + counter))
                            .on(EAV_BE_ENTITY_COMPLEX_SETS.as("cs" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                            .and(EAV_BE_ENTITY_COMPLEX_SETS.as("cs" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()))
                            .join(EAV_BE_COMPLEX_SET_VALUES.as("csv" + counter))
                            .on(EAV_BE_COMPLEX_SET_VALUES.as("csv" + counter).SET_ID.eq(EAV_BE_ENTITY_COMPLEX_SETS.as("cs" + counter).ID));
                } else {
                    throw new IllegalStateException("Not yet implemented");
                }
            } else {
                if (metaType.isComplex()) {
                    selectJoinStep.leftOuterJoin(EAV_BE_COMPLEX_VALUES.as("cv" + counter))
                            .on(EAV_BE_COMPLEX_VALUES.as("cv" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                            .and(EAV_BE_COMPLEX_VALUES.as("cv" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                } else {
                    MetaValue metaValue = (MetaValue) metaType;
                    switch (metaValue.getTypeCode()) {
                        case STRING:
                            selectJoinStep.leftOuterJoin(EAV_BE_STRING_VALUES.as("sv" + counter))
                                    .on(EAV_BE_STRING_VALUES.as("sv" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                                    .and(EAV_BE_STRING_VALUES.as("sv" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                            break;
                        case INTEGER:
                            selectJoinStep.leftOuterJoin(EAV_BE_INTEGER_VALUES.as("iv" + counter))
                                    .on(EAV_BE_INTEGER_VALUES.as("iv" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                                    .and(EAV_BE_INTEGER_VALUES.as("iv" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                            break;
                        case BOOLEAN:
                            selectJoinStep.leftOuterJoin(EAV_BE_BOOLEAN_VALUES.as("bv" + counter))
                                    .on(EAV_BE_BOOLEAN_VALUES.as("bv" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                                    .and(EAV_BE_BOOLEAN_VALUES.as("bv" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                            break;
                        case DOUBLE:
                            selectJoinStep.leftOuterJoin(EAV_BE_DOUBLE_VALUES.as("dov" + counter))
                                    .on(EAV_BE_DOUBLE_VALUES.as("dov" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                                    .and(EAV_BE_DOUBLE_VALUES.as("dov" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                            break;
                        case DATE:
                            selectJoinStep.leftOuterJoin(EAV_BE_DATE_VALUES.as("dav" + counter))
                                    .on(EAV_BE_DATE_VALUES.as("dav" + counter).ENTITY_ID.eq(EAV_BE_ENTITIES.as(ebe).ID))
                                    .and(EAV_BE_DATE_VALUES.as("dav" + counter).ATTRIBUTE_ID.eq(metaAttribute.getId()));
                            break;
                        default:
                            throw new IllegalArgumentException(Errors.compose(Errors.E127));
                    }
                }
            }

            counter++;
        }

        selectJoinStep.where(EAV_BE_ENTITIES.as(ebe).CLASS_ID.eq(metaId));

        return selectJoinStep;
    }

    @Override
    public List<Map<String, Object>> getSimpleResult(long metaId, boolean onlyKey) {
        Select select = getSimpleSelect(metaId, onlyKey);
        return queryForListWithStats(select.getSQL(), select.getBindValues().toArray());
    }

}
