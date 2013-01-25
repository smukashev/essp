package kz.bsbnb.usci.eav.persistance.impl.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Scope(value = "singleton")
public class JDBCConfig {
    @Value("${ds.table.prefix}")
    protected String tablePrefix;

    protected String classesTableName;
    protected String arrayKeyFilterTableName;
    protected String arrayKeyFilterValuesTableName;
    protected String attributesTableName;
    protected String simpleAttributesTableName;
    protected String complexAttributesTableName;
    protected String arrayTableName;
    protected String complexArrayTableName;
    protected String simpleArrayTableName;
    protected String entitiesTableName;


    public static final int classNameLength = 64;
    public static final int attributeNameLength = 64;
    public static final int typeCodeLength = 16;
    public static final int arrayKeyTypeCodeLength = 16;
    public static final int complexKeyTypeCodeLength = 16;
    public static final int arrayKeyFilterValueLength = 128;

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
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        init();
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

    public static int getClassNameLength() {
        return classNameLength;
    }

    public static int getAttributeNameLength() {
        return attributeNameLength;
    }

    public static int getTypeCodeLength() {
        return typeCodeLength;
    }

    public static int getArrayKeyTypeCodeLength() {
        return arrayKeyTypeCodeLength;
    }

    public static int getComplexKeyTypeCodeLength() {
        return complexKeyTypeCodeLength;
    }

    public static int getArrayKeyFilterValueLength() {
        return arrayKeyFilterValueLength;
    }
}
