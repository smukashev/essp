import base.EavTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Bauyrzhan.Makhambeto on 09.01.2015.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(locations = {"classpath:cliApplicationContextTest.xml"})
public class EavSimpleDistinctTest extends EavTest {

    public EavSimpleDistinctTest(){
        meta = new String[]{
                "meta create --name person",
                "meta add --name=person --attribute=name --type=string",
                "meta add --name=person --attribute=doc_no --type=string",
                "meta key --name=person --attribute=doc_no"
        };

        //skipMeta = true;
    }

    @Override
    public void beforeTesting() {
        super.beforeTesting();

        testingBaseEntites = new String[5];
        testingDates = new String[5];

        testingBaseEntites[0] =
                "<entity class=\"person\">" +
                        "  <name>a</name>" +
                        "  <doc_no>key_127</doc_no>" +
                        "</entity>";
        testingDates[0]= "01.01.2015";

        testingBaseEntites[1] =
                "<entity class=\"person\">" +
                        "  <name>b</name>" +
                        "  <doc_no>key_127</doc_no>" +
                        "</entity>";
        testingDates[1]= "01.02.2015";

        testingBaseEntites[2] =
                "<entity class=\"person\">" +
                        "  <name>c</name>" +
                        "  <doc_no>key_127</doc_no>" +
                        "</entity>";
        testingDates[2]= "01.03.2015";
    }

    /**
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | ID | ENTITY_ID | BATCH_ID | ATTRIBUTE_ID | INDEX_ | REPORT_DATE |  VALUE  | IS_CLOSED | IS_LAST |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 1  |     1     |    9     |      1       |   1    | 01.01.2015  | key_123 |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 2  |     1     |    10    |      2       |   1    | 01.01.2015  |    a    |     0     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 3  |     1     |    10    |      2       |   1    | 01.02.2015  |    b    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     */
    @Test
    public void firstTest(){

        nextKey();

        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);

        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test = new SingleTest();
        test.addCond("report_date","date '2015-01-01'");
        test.addCond("entity_id",baseEntityId);
        test.addCond("value","a");
        test.setTable("eav_be_string_values");
        test.setAnswer(1);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond("report_date","date '2015-02-01'");
        test.addCond("entity_id",baseEntityId);
        test.addCond("value","b");
        test.addCond("is_last",1);
        test.setTable("eav_be_string_values");
        test.setAnswer(1);
        testHolder.addTest(test);
        int status = testHolder.runBatch();

        assertTrue(testHolder.lastWrong, status == 0);

        int r = jdbcTemplate.queryForInt("SELECT count(DISTINCT attribute_id) from EAV_BE_STRING_VALUES where entity_id = " + baseEntityId);
        assertTrue("number of records incorrect", r==2);

    }


    /**
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | ID | ENTITY_ID | BATCH_ID | ATTRIBUTE_ID | INDEX_ | REPORT_DATE |  VALUE  | IS_CLOSED | IS_LAST |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 4  |     2     |    11    |      1       |   1    | 01.01.2015  | key_124 |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 6  |     2     |    12    |      2       |   1    | 01.01.2015  |    a    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 5  |     2     |    11    |      2       |   1    | 01.02.2015  |    b    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 7  |     2     |    12    |      2       |   1    | 01.02.2015  |    a    |     1     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     */
    @Test
    public void secondTest(){
        nextKey();
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);

        SingleTest test;
        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE,"a");
        test.setAnswer(2);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(REPORT_DATE,"date '2015-02-01'");
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE,"b");
        test.addCond(IS_LAST,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.setAnswer(4);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(REPORT_DATE,"date '2015-02-01'");
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE,"a");
        test.addCond(IS_CLOSED,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);


        int status = testHolder.runBatch();
        assertTrue(testHolder.lastWrong, status == 0 );

        int r = jdbcTemplate.queryForInt("select max(count(*)) from " + EAV_BE_STRING_VALUES + " where entity_id= "
                + baseEntityId + " group by attribute_id");

        assertTrue(NUMBER_OF_ROWS_INCORRECT, r == 3);

    }

    /**
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | ID | ENTITY_ID | BATCH_ID | ATTRIBUTE_ID | INDEX_ | REPORT_DATE |  VALUE  | IS_CLOSED | IS_LAST |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 8  |     3     |    13    |      1       |   1    | 01.01.2015  | key_125 |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 9  |     3     |    13    |      2       |   1    | 01.03.2015  |    c    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 10 |     3     |    14    |      2       |   1    | 01.02.2015  |    b    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 11 |     3     |    14    |      2       |   1    | 01.03.2015  |    b    |     1     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 12 |     3     |    15    |      2       |   1    | 01.01.2015  |    a    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 13 |     3     |    15    |      2       |   1    | 01.02.2015  |    a    |     1     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     */
    @Test
    public void thirdTest(){
        nextKey();

        cli.readEntityFromXMLString(testingBaseEntites[2],testingDates[2]);
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);

        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test;
        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.setAnswer(6);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(IS_CLOSED,1);
        test.setAnswer(2);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE,"a");
        test.setAnswer(2);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE,"b");
        test.setAnswer(2);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(VALUE, "c");
        test.addCond(IS_LAST,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID,baseEntityId);
        test.addCond(REPORT_DATE,"date '2015-03-01'");
        test.addCond(VALUE,"b");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);


        int status = testHolder.runBatch();
        assertTrue(testHolder.lastWrong, status == 0 );

        assertTrue(true);
    }

    /**
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | ID | ENTITY_ID | BATCH_ID | ATTRIBUTE_ID | INDEX_ | REPORT_DATE |  VALUE  | IS_CLOSED | IS_LAST |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 14 |     4     |    16    |      1       |   1    | 01.01.2015  | key_126 |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 15 |     4     |    16    |      2       |   1    | 01.03.2015  |    c    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 16 |     4     |    18    |      2       |   1    | 01.01.2015  |    a    |     0     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 17 |     4     |    17    |      2       |   1    | 01.03.2015  |    a    |     1     |    0    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     | 18 |     4     |    18    |      2       |   1    | 01.02.2015  |    b    |     0     |    1    |
     +====+===========+==========+==============+========+=============+=========+===========+=========+
     */
    @Test
    public void test4(){
        nextKey();
        cli.readEntityFromXMLString(testingBaseEntites[2],testingDates[2]);
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test;
        test = new SingleTest();
        test.addCond(ENTITY_ID, baseEntityId);
        test.setAnswer(5);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID, baseEntityId);
        test.addCond(VALUE, "a");
        test.setAnswer(2);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID, baseEntityId);
        test.addCond(VALUE, "a");
        test.addCond(IS_CLOSED, 1);
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID, baseEntityId);
        test.addCond(VALUE, "b");
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(ENTITY_ID, baseEntityId);
        test.addCond(VALUE, "c");
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_VALUES);
        testHolder.addTest(test);

        int status = testHolder.runBatch();

        assertTrue(testHolder.lastWrong, status == 0);
    }
}
