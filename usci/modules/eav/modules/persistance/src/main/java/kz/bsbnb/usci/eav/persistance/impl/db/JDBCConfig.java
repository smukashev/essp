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

    @Value("${ds.schema}")
    protected String schema;

    private String classesTableName;
    private String arrayKeyFilterTableName;
    private String arrayKeyFilterValuesTableName;
    private String attributesTableName;
    private String simpleAttributesTableName;
    private String complexAttributesTableName;
    private String setTableName;
    private String complexSetTableName;
    private String setOfSetsTableName;
    private String simpleSetTableName;
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
    private String baseEntitySetsTableName;
    private String baseEntitySimpleSetsTableName;
    private String baseEntityComplexSetsTableName;
    private String baseEntitySetOfSetsTableName;
    private String baseSetOfSetsTableName;
    private String baseSetOfSimpleSetsTableName;
    private String baseSetOfComplexSetsTableName;

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
        setTableName = tablePrefix + "set";
        complexSetTableName = tablePrefix + "complex_set";
        setOfSetsTableName = tablePrefix + "set_of_sets";
        simpleSetTableName = tablePrefix + "simple_set";
        batchesTableName = tablePrefix + "batches";

        baseValuesTableName = tablePrefix + "be_values";
        baseDateValuesTableName = tablePrefix + "be_date_values";
        baseDoubleValuesTableName = tablePrefix + "be_double_values";
        baseIntegerValuesTableName = tablePrefix + "be_integer_values";
        baseBooleanValuesTableName = tablePrefix + "be_boolean_values";
        baseStringValuesTableName = tablePrefix + "be_string_values";
        baseComplexValuesTableName = tablePrefix + "be_complex_values";

        baseSetsTableName = tablePrefix + "be_sets";
        baseEntitySetsTableName = tablePrefix + "be_entity_sets";
        baseEntitySimpleSetsTableName = tablePrefix + "be_entity_simple_sets";
        baseEntityComplexSetsTableName = tablePrefix + "be_entity_complex_sets";
        baseEntitySetOfSetsTableName = tablePrefix + "be_entity_set_of_sets";
        baseSetOfSetsTableName = tablePrefix + "be_set_of_sets";
        baseSetOfSimpleSetsTableName = tablePrefix + "be_set_of_simple_sets";
        baseSetOfComplexSetsTableName = tablePrefix + "be_set_of_complex_sets";

        baseSetValuesTableName = tablePrefix + "be_set_values";
        baseDateSetValuesTableName = tablePrefix + "be_date_set_values";
        baseDoubleSetValuesTableName = tablePrefix + "be_double_set_values";
        baseIntegerSetValuesTableName = tablePrefix + "be_integer_set_values";
        baseBooleanSetValuesTableName = tablePrefix + "be_boolean_set_values";
        baseStringSetValuesTableName = tablePrefix + "be_string_set_values";
        baseComplexSetValuesTableName = tablePrefix + "be_complex_set_values";
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

    public String getSetTableName() {
        return setTableName;
    }

    public String getComplexSetTableName() {
        return complexSetTableName;
    }

    public String getSimpleSetTableName() {
        return simpleSetTableName;
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

    public String getBaseEntitySetsTableName() {
        return baseEntitySetsTableName;
    }

    public String getBaseEntitySimpleSetsTableName() {
        return baseEntitySimpleSetsTableName;
    }

    public String getBaseEntityComplexSetsTableName() {
        return baseEntityComplexSetsTableName;
    }

    public String getBaseEntitySetOfSetsTableName() {
        return baseEntitySetOfSetsTableName;
    }

    public String getBaseSetOfSetsTableName() {
        return baseSetOfSetsTableName;
    }

    public String getBaseSetOfSimpleSetsTableName() {
        return baseSetOfSimpleSetsTableName;
    }

    public String getBaseSetOfComplexSetsTableName() {
        return baseSetOfComplexSetsTableName;
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

    public String getSetOfSetsTableName() {
        return setOfSetsTableName;
    }

    public String getMetaObjectTableName() {
        return metaObjectTableName;
    }

    public String getTablePrefix()
    {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix)
    {
        this.tablePrefix = tablePrefix;
    }

    public String getSchema()
    {
        return schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }
}
