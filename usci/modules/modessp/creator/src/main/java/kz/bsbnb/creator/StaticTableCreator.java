package kz.bsbnb.creator;

import kz.bsbnb.usci.eav.model.meta.impl.MetaClass;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StaticTableCreator extends BaseTableCreator {

    private String ddlTable;
    private String ddlPrimaryKey;

    public StaticTableCreator(MetaClass metaClass) {
        super(metaClass);

        this.ddlTable = "CREATE TABLE EAV_BE_ENTITIES\n" +
                "(\n" +
                "  CREDITOR_ID NUMBER(14) NOT NULL,\n" +
                "  ENTITY_ID   NUMBER(14) NOT NULL,\n" +
                "  ENTITY_KEY  VARCHAR2(4000) NOT NULL,\n" +
                "  CLASS_ID    NUMBER(14) NOT NULL,\n" +
                "  IS_DELETED  VARCHAR2(1) NOT NULL,\n" +
                "  SYSTEM_DATE DATE DEFAULT SYSDATE NOT NULL\n" +
                ")";

        this.ddlPrimaryKey = "ALTER TABLE EAV_BE_ENTITIES\n" +
                "  ADD CONSTRAINT EAV_BE_ENTITIES_P1 PRIMARY KEY (CREDITOR_ID, ENTITY_ID)";


    }

    @Override
    public void execute(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        try {
            if(performDrops) {
                statement.executeUpdate("DROP TABLE EAV_BE_ENTITIES");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        statement.executeQuery(ddlTable);
        statement.executeQuery(ddlPrimaryKey);
        connection.close();
    }
}
