package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;
import kz.bsbnb.usci.eav.model.meta.impl.MetaSet;
import kz.bsbnb.usci.eav.model.meta.impl.MetaValue;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleTableCreator extends BaseTableCreator {

    DDL ddl;

    public SingleTableCreator(MetaClass metaCredit) {
        super(metaCredit);
        //this.ddl = new DDL(metaCredit, namingSequence);
    }

    public DDL getDDL() {
        if(ddl == null) {
            ddl = new DDL(metaClass, namingSequence);
        }
        return ddl;
    }

    static class DDL {
        String tableCreationPrefix;
        String tableCreationSuffix;
        String columnCreation;
        private String dropTable;
        private String primaryKey;
        private String foreignKey;

        public DDL(MetaClass metaClass, AtomicInteger namingSequence) {
            String tableName = metaClass.getClassName().toUpperCase();

            tableCreationPrefix = String.format("CREATE TABLE %s (\n", tableName);
            tableCreationSuffix = ")\n";
            dropTable = String.format("DROP TABLE %s", tableName);

            StringBuilder builder = new StringBuilder();
            Set<String> attributeNames = metaClass.getAttributeNames();

            builder.append("  CREDITOR_ID NUMBER(14) NOT NULL\n");
            builder.append(", REPORT_DATE DATE NOT NULL\n");
            builder.append(", ENTITY_ID NUMBER(14) NOT NULL\n");
            builder.append(", IS_DELETED VARCHAR2(1) DEFAULT '0' NOT NULL\n");
            builder.append(", SYSTEM_DATE DATE DEFAULT SYSDATE NOT NULL\n");

            for (String attributeName : attributeNames) {
                //system table duplicate
                if(attributeName.equals("creditor"))
                    continue;

                IMetaAttribute metaAttribute = metaClass.getMetaAttribute(attributeName);
                IMetaType metaType = metaAttribute.getMetaType();
                builder.append(", ");
                String columnName = metaAttribute.getName().toUpperCase();
                if (metaType.isComplex()) {
                    if(metaType.isSet()) {
                        builder.append(columnName + "_IDS TNUMBER\n");
                        tableCreationSuffix += String.format("NESTED TABLE %s_IDS STORE AS NT_%s%d\n", columnName, columnName, namingSequence.incrementAndGet());
                    } else {
                        builder.append(columnName + "_ID NUMBER(14)\n");
                    }
                } else {
                    if(metaType.isSet()) {
                        MetaSet metaSet = (MetaSet) metaType;
                        MetaValue metaValue = (MetaValue) metaSet.getMemberType();
                        builder.append(columnName + " ");
                        tableCreationSuffix += String.format("NESTED TABLE %s STORE AS NT_%s%d\n", columnName, columnName, namingSequence.incrementAndGet());
                        switch (metaValue.getTypeCode()) {
                            case STRING:
                                builder.append("TVARCHAR2\n");
                                break;
                        }
                    } else {
                        MetaValue metaValue = (MetaValue) metaType;
                        if(columnName.equals("DATE"))
                            columnName += "_";
                        builder.append(columnName + " ");
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
                                break;
                            case INTEGER:
                                builder.append("NUMBER(14)\n");
                                break;
                        }
                    }
                }
            }

            columnCreation = builder.toString();
            primaryKey = String.format("ALTER TABLE %s ADD CONSTRAINT PK_%s PRIMARY KEY (CREDITOR_ID, REPORT_DATE, ENTITY_ID)",
                    tableName, tableName);
            foreignKey = String.format("ALTER TABLE %s ADD CONSTRAINT %s_R1 FOREIGN KEY (CREDITOR_ID, ENTITY_ID) REFERENCES EAV_BE_ENTITIES (CREDITOR_ID, ENTITY_ID)",
                    tableName, tableName);
        }


        public String getTableCreationPart() {
            return tableCreationPrefix + columnCreation + tableCreationSuffix;
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

        public String dropIfExists(){
            return dropTable;
        }

        public String getCompact() {
            return tableCreationPrefix + "\n" + columnCreation + tableCreationSuffix + "\n" +
               primaryKey + "\n" + foreignKey;
        }
    }

    @Override
    public void execute(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        try {
            if(performDrops) {
                statement.executeUpdate(getDDL().dropIfExists());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            statement.executeUpdate(getDDL().getTableCreationPart());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        statement.executeUpdate(getDDL().getPrimaryKeyPart());
        statement.executeUpdate(getDDL().getForeignKeyPart());
        connection.close();
    }


}
