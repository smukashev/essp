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
    private String simpleArrayTableName;
    private String entitiesTableName;
    private String batchesTableName;
    private String valuesTableName;
    private String dateValuesTableName;
    private String dateArrayValuesTableName;
    private String doubleValuesTableName;
    private String doubleArrayValuesTableName;
    private String integerValuesTableName;
    private String integerArrayValuesTableName;
    private String booleanValuesTableName;
    private String booleanArrayValuesTableName;
    private String stringValuesTableName;
    private String stringArrayValuesTableName;
    private String complexValuesTableName;
    private String complexArrayValuesTableName;

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
        classesTableName = tablePrefix + "classes";
        attributesTableName = tablePrefix + "attributes";
        simpleAttributesTableName = tablePrefix + "simple_attributes";
        entitiesTableName = tablePrefix + "entities";
        arrayKeyFilterTableName = tablePrefix + "array_key_filter";
        arrayKeyFilterValuesTableName = tablePrefix + "array_key_filter_values";
        complexAttributesTableName = tablePrefix + "complex_attributes";
        arrayTableName = tablePrefix + "array";
        complexArrayTableName = tablePrefix + "complex_array";
        simpleArrayTableName = tablePrefix + "simple_array";
        batchesTableName = tablePrefix + "batches";
        valuesTableName = tablePrefix + "values";
        dateValuesTableName = tablePrefix + "date_values";
        doubleValuesTableName = tablePrefix + "double_values";
        integerValuesTableName = tablePrefix + "integer_values";
        booleanValuesTableName  = tablePrefix + "boolean_values";
        stringValuesTableName = tablePrefix + "string_values";
        complexValuesTableName = tablePrefix + "complex_values";
        dateArrayValuesTableName = tablePrefix + "date_array_values";
        doubleArrayValuesTableName = tablePrefix + "double_array_values";
        integerArrayValuesTableName = tablePrefix + "integer_array_values";
        booleanArrayValuesTableName  = tablePrefix + "boolean_array_values";
        stringArrayValuesTableName = tablePrefix + "string_array_values";
        complexArrayValuesTableName = tablePrefix + "complex_array_values";
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

    public String getValuesTableName() {
        return valuesTableName;
    }

    public String getDateValuesTableName() {
        return dateValuesTableName;
    }

    public String getDoubleValuesTableName() {
        return doubleValuesTableName;
    }

    public String getIntegerValuesTableName() {
        return integerValuesTableName;
    }

    public String getBooleanValuesTableName() {
        return booleanValuesTableName;
    }

    public String getStringValuesTableName() {
        return stringValuesTableName;
    }

    public String getComplexValuesTableName() {
        return complexValuesTableName;
    }

    public String getDateArrayValuesTableName() {
        return dateArrayValuesTableName;
    }

    public String getDoubleArrayValuesTableName() {
        return doubleArrayValuesTableName;
    }

    public String getIntegerArrayValuesTableName() {
        return integerArrayValuesTableName;
    }

    public String getBooleanArrayValuesTableName() {
        return booleanArrayValuesTableName;
    }

    public String getStringArrayValuesTableName() {
        return stringArrayValuesTableName;
    }

    public String getComplexArrayValuesTableName() {
        return complexArrayValuesTableName;
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

}
