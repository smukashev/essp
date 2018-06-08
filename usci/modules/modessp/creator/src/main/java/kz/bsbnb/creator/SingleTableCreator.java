package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;

import java.util.Set;

public class SingleTableCreator {

    DDL ddl;

    public SingleTableCreator(MetaClass metaCredit) {
        this.ddl = new DDL(metaCredit);
    }

    public DDL getDDL() {
        return ddl;
    }

    static class DDL {
        String tableCreationPrefix;
        String tableCreationSuffix;
        String columnCreation;
        private String primaryKey;
        private String foreignKey;

        public DDL(MetaClass metaClass) {
            String tableName = metaClass.getClassName().toUpperCase();

            tableCreationPrefix = String.format("CREATE TABLE %s (", tableName);
            tableCreationSuffix = ");";

            StringBuilder builder = new StringBuilder();
            Set<String> attributeNames = metaClass.getAttributeNames();

            builder.append("  CREDITOR_ID NUMBER(14) NOT NULL\n");
            builder.append(", REPORT_DATE DATE NOT NULL\n");
            builder.append(", ENTITY_ID NUMBER(14) NOT NULL\n");
            builder.append(", SYSTEM_DATE DATE DEFAULT SYSDATE NOT NULL\n");

            for (String attributeName : attributeNames) {
                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attributeName);
                IMetaType metaType = metaAttribute.getMetaType();
                builder.append(", ");
                if (metaType.isComplex()) {
                    if(metaType.isSet()) {
                        builder.append(metaAttribute.getName().toUpperCase() + "_IDS TNUMBER\n");
                    } else {
                        builder.append(metaAttribute.getName().toUpperCase() + "_ID NUMBER(14)\n");
                    }
                } else {
                    if(metaType.isSet()) {
                        MetaSet metaSet = (MetaSet) metaType;
                        MetaValue metaValue = (MetaValue) metaSet.getMemberType();
                        builder.append(metaAttribute.getName().toUpperCase() + " ");
                        switch (metaValue.getTypeCode()) {
                            case STRING:
                                builder.append("TVARCHAR2\n");
                                break;
                        }
                    } else {
                        MetaValue metaValue = (MetaValue) metaType;
                        builder.append(metaAttribute.getName().toUpperCase() + " ");
                        switch (metaValue.getTypeCode()) {
                            case DOUBLE:
                                builder.append("NUMBER(17,3)\n");
                                break;
                            case STRING:
                                builder.append("VARCHAR2(1024)\n");
                                break;
                            case DATE:
                                builder.append("DATE\n");
                                break;
                            case BOOLEAN:
                                builder.append("VARCHAR2(1) DEFAULT '0'\n");
                        }
                    }
                }
            }

            columnCreation = builder.toString();
            primaryKey = String.format("ALTER TABLE %s ADD CONSTRAINT PK_%s PRIMARY KEY (CREDITOR_ID, REPORT_DATE, ENTITY_ID);",
                    tableName, tableName);
            foreignKey = String.format("ALTER TABLE %s ADD CONSTRAINT %s_R1 FOREIGN KEY (CREDITOR_ID, ENTITY_ID) REFERENCES EAV_BE_ENTITIES (CREDITOR_ID, ENTITY_ID);",
                    tableName, tableName);
        }


        public String getTableCreationPart() {
            return tableCreationPrefix + tableCreationSuffix;
        }

        public String getColumnsPart() {
            return columnCreation;
        }

        public String getPrimaryKeyPart() {
            return primaryKey;
        }

        public String getForeignKeyPart() {
            return foreignKey;
        }

        public String getCompact() {
            return tableCreationPrefix + "\n" + columnCreation + tableCreationSuffix + "\n" +
               primaryKey + "\n" + foreignKey;
        }
    }
}
