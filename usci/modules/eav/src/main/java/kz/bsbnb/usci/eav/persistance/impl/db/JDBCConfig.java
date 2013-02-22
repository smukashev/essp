package kz.bsbnb.usci.eav.persistance.impl.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope(value = "singleton")
public class JDBCConfig {
    @Value("${ds.table.prefix}")
    protected String tablePrefix = "eav_";

    private String classesTableName;
    private String arrayKeyFilterTableName;
    private String arrayKeyFilterValuesTableName;
    private String attributesTableName;
    private String simpleAttributesTableName;
    private String complexAttributesTableName;
    private String arrayTableName;
    private String complexArrayTableName;
    private String arrayArrayTableName;
    private String simpleArrayTableName;
    private String entitiesTableName;

    private String metaObjectTableName;

    private String batchesTableName;

    private String baseValuesTableName;
    private String baseDateValuesTableName;
    private String baseDoubleValuesTableName;
    private String baseIntegerValuesTableName;
    private String baseBooleanValuesTableName;
    private String baseStringValuesTableName;
    private String baseComplexValuesTableName;
    
    private String baseSetsTableName;
    private String baseSimpleSetsTableName;
    private String baseComplexSetsTableName;
    private String baseSetValuesTableName;
    private String baseIntegerSetValuesTableName;
    private String baseDoubleSetValuesTableName;
    private String baseDateSetValuesTableName;
    private String baseBooleanSetValuesTableName;
    private String baseStringSetValuesTableName;
    private String baseComplexSetValuesTableName;

    private final int classNameLength = 64;
    private final int attributeNameLength = 64;
    private final int typeCodeLength = 16;
    private final int arrayKeyTypeCodeLength = 16;
    private final int complexKeyTypeCodeLength = 16;
    private final int arrayKeyFilterValueLength = 128;
    private final int stringValueLength = 1024;

    @PostConstruct
    public void init()
    {
        metaObjectTableName = tablePrefix + "meta_object";
        classesTableName = tablePrefix + "classes";
        attributesTableName = tablePrefix + "attributes";
        simpleAttributesTableName = tablePrefix + "simple_attributes";
        entitiesTableName = tablePrefix + "entities";
        arrayKeyFilterTableName = tablePrefix + "array_key_filter";
        arrayKeyFilterValuesTableName = tablePrefix + "array_key_filter_values";
        complexAttributesTableName = tablePrefix + "complex_attributes";
        arrayTableName = tablePrefix + "array";
        complexArrayTableName = tablePrefix + "complex_array";
        arrayArrayTableName = tablePrefix + "array_array";
        simpleArrayTableName = tablePrefix + "simple_array";
        batchesTableName = tablePrefix + "batches";

        baseValuesTableName = tablePrefix + "b_values";
        baseDateValuesTableName = tablePrefix + "b_date_values";
        baseDoubleValuesTableName = tablePrefix + "b_double_values";
        baseIntegerValuesTableName = tablePrefix + "b_integer_values";
        baseBooleanValuesTableName = tablePrefix + "b_boolean_values";
        baseStringValuesTableName = tablePrefix + "b_string_values";
        baseComplexValuesTableName = tablePrefix + "b_complex_values";

        baseSetsTableName = tablePrefix + "b_sets";
        baseSimpleSetsTableName = tablePrefix + "b_simple_sets";
        baseComplexSetsTableName = tablePrefix + "b_complex_sets";
        baseSetValuesTableName = tablePrefix + "b_set_values";
        baseDateSetValuesTableName = tablePrefix + "b_date_set_values";
        baseDoubleSetValuesTableName = tablePrefix + "b_double_set_values";
        baseIntegerSetValuesTableName = tablePrefix + "b_integer_set_values";
        baseBooleanSetValuesTableName = tablePrefix + "b_boolean_set_values";
        baseStringSetValuesTableName = tablePrefix + "b_string_set_values";
        baseComplexSetValuesTableName = tablePrefix + "b_complex_set_values";
    }

    public String getClassesTableName() {
        return classesTableName;
    }

    public String getArrayKeyFilterTableName() {
        return arrayKeyFilterTableName;
    }

    public String getArrayKeyFilterValuesTableName() {
        return arrayKeyFilterValuesTableName;
    }

    public String getAttributesTableName() {
        return attributesTableName;
    }

    public String getSimpleAttributesTableName() {
        return simpleAttributesTableName;
    }

    public String getComplexAttributesTableName() {
        return complexAttributesTableName;
    }

    public String getArrayTableName() {
        return arrayTableName;
    }

    public String getComplexArrayTableName() {
        return complexArrayTableName;
    }

    public String getSimpleArrayTableName() {
        return simpleArrayTableName;
    }

    public String getEntitiesTableName() {
        return entitiesTableName;
    }

    public String getBatchesTableName() {
        return batchesTableName;
    }

    public String getBaseValuesTableName() {
        return baseValuesTableName;
    }

    public String getBaseDateValuesTableName() {
        return baseDateValuesTableName;
    }

    public String getBaseDoubleValuesTableName() {
        return baseDoubleValuesTableName;
    }

    public String getBaseIntegerValuesTableName() {
        return baseIntegerValuesTableName;
    }

    public String getBaseBooleanValuesTableName() {
        return baseBooleanValuesTableName;
    }

    public String getBaseStringValuesTableName() {
        return baseStringValuesTableName;
    }

    public String getBaseComplexValuesTableName() {
        return baseComplexValuesTableName;
    }

    public String getBaseSetsTableName() {
        return baseSetsTableName;
    }

    public String getBaseSimpleSetsTableName() {
        return baseSimpleSetsTableName;
    }

    public String getBaseSetValuesTableName() {
        return baseSetValuesTableName;
    }

    public String getBaseDateSetValuesTableName() {
        return baseDateSetValuesTableName;
    }

    public String getBaseDoubleSetValuesTableName() {
        return baseDoubleSetValuesTableName;
    }

    public String getBaseIntegerSetValuesTableName() {
        return baseIntegerSetValuesTableName;
    }

    public String getBaseBooleanSetValuesTableName() {
        return baseBooleanSetValuesTableName;
    }

    public String getBaseStringSetValuesTableName() {
        return baseStringSetValuesTableName;
    }

    public String getBaseComplexSetValuesTableName() {
        return baseComplexSetValuesTableName;
    }

    public int getClassNameLength() {
        return classNameLength;
    }

    public int getAttributeNameLength() {
        return attributeNameLength;
    }

    public int getTypeCodeLength() {
        return typeCodeLength;
    }

    public int getArrayKeyTypeCodeLength() {
        return arrayKeyTypeCodeLength;
    }

    public int getComplexKeyTypeCodeLength() {
        return complexKeyTypeCodeLength;
    }

    public int getArrayKeyFilterValueLength() {
        return arrayKeyFilterValueLength;
    }

    public int getStringValueLength() {
        return stringValueLength;
    }

    public String getArrayArrayTableName() {
        return arrayArrayTableName;
    }

    public String getMetaObjectTableName() {
        return metaObjectTableName;
    }
}
