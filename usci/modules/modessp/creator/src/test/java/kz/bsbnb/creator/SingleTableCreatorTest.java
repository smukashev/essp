package kz.bsbnb.creator;

import kz.bsbnb.testing.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

public class SingleTableCreatorTest extends BaseUnitTest {

    @Test
    public void creditTableCreation() throws Exception {
        SingleTableCreator creator = new SingleTableCreator(metaCredit);

        String ddl = creator.getDDL().getTableCreationPart();

        Assert.assertTrue(ddl.contains("CREATE TABLE CREDIT"));

        ddl = creator.getDDL().getColumnsPart();

        //system columns
        Assert.assertTrue(ddl.contains("CREDITOR_ID NUMBER(14) NOT NULL"));
        Assert.assertTrue(ddl.contains("REPORT_DATE DATE NOT NULL"));
        Assert.assertTrue(ddl.contains("ENTITY_ID NUMBER(14) NOT NULL"));
        Assert.assertTrue(ddl.contains("SYSTEM_DATE DATE DEFAULT SYSDATE NOT NULL"));

        //table columns
        Assert.assertTrue(ddl.contains("AMOUNT NUMBER(17,3)"));
        Assert.assertTrue(ddl.contains("PRIMARY_CONTRACT_ID NUMBER(14)"));
        Assert.assertTrue(ddl.contains("CURRENCY_ID NUMBER(14)"));
        Assert.assertTrue(ddl.contains("PLEDGES_IDS TNUMBER"));
        Assert.assertTrue(ddl.contains("HAS_CURRENCY_EARN VARCHAR2(1) DEFAULT '0'"));

        ddl = creator.getDDL().getPrimaryKeyPart();
        Assert.assertTrue(ddl.contains("ALTER TABLE CREDIT ADD CONSTRAINT PK_CREDIT PRIMARY KEY (CREDITOR_ID, REPORT_DATE, ENTITY_ID);"));

        ddl = creator.getDDL().getForeignKeyPart();
        Assert.assertTrue(ddl.contains("ALTER TABLE CREDIT ADD CONSTRAINT CREDIT_R1 FOREIGN KEY (CREDITOR_ID, ENTITY_ID) REFERENCES EAV_BE_ENTITIES (CREDITOR_ID, ENTITY_ID);"));

        //System.out.println(creator.getDDL().getCompact());
    }

    @Test
    public void simpleArrayCreation() throws Exception {
        SingleTableCreator creator = new SingleTableCreator(metaContact);
        String ddl = creator.getDDL().getColumnsPart();
        Assert.assertTrue(ddl.contains("DETAILS TVARCHAR2"));

    }
}