import base.EavTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Bauyrzhan.Makhambeto on 12.01.2015.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(locations = {"classpath:cliApplicationContextTest.xml"})
public class EavSimpleSetDistinctTest extends EavTest {

    private final Logger logger = LoggerFactory.getLogger(EavSimpleSetDistinctTest.class);

    long setId;

    public EavSimpleSetDistinctTest() {
        meta = new String[]{
            "meta create --name=person",
            "meta add --name=person --attribute=details --type string --array",
            "meta add --name=person --attribute=no --type string",
            "meta key --name=person --attribute=no"};
        //skipMeta = true;
    }

    @Override
    public void beforeTesting() {
        super.beforeTesting();

        testingBaseEntites = new String[5];
        testingDates = new String[5];

        testingBaseEntites[0] =
                "<entity class=\"person\">" +
                        " <details>" +
                        "   <item>a</item>" +
                        "   <item>b</item>" +
                        " </details>" +
                        " <no>key_128</no>" +
                        "</entity>";
        testingDates[0]= "01.01.2015";

        testingBaseEntites[1] =
                "<entity class=\"person\">" +
                        " <details>" +
                        "   <item>a</item>" +
                        "   <item>c</item>" +
                        " </details>" +
                        " <no>key_128</no>" +
                        "</entity>";
        testingDates[1]= "01.02.2015";

        testingBaseEntites[2] =
                "<entity class=\"person\">" +
                        " <details>" +
                        "   <item>a</item>" +
                        "   <item>d</item>" +
                        " </details>" +
                        " <no>key_128</no>" +
                        "</entity>";
        testingDates[2]= "01.03.2015";

        nextKey();
    }

    @Override
    public void readInDb() {
        super.readInDb();
        setId = jdbcTemplate.queryForLong("select seq_eav_be_sets_id.currval from dual");
        System.out.println("set id : " + setId);
        //logger.debug("test expectation baseEntityId: " + baseEntityId + "\n setId: " + setId);
    }


    /**
     +====+========+==========+========+=============+=======+===========+=========+
     | ID | SET_ID | BATCH_ID | INDEX_ | REPORT_DATE | VALUE | IS_CLOSED | IS_LAST |
     +====+========+==========+========+=============+=======+===========+=========+
     | 55 |   19   |   230    |   1    | 01.01.2015  |   b   |     0     |    0    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 56 |   19   |   229    |   1    | 01.01.2015  |   a   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 57 |   19   |   231    |   1    | 01.02.2015  |   c   |     0     |    0    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 58 |   19   |   230    |   1    | 01.02.2015  |   b   |     1     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 59 |   19   |   231    |   1    | 01.03.2015  |   d   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 60 |   19   |   231    |   1    | 01.03.2015  |   c   |     1     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     */
    @Test
    public void test1() {

        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        cli.readEntityFromXMLString(testingBaseEntites[2],testingDates[2]);

        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test;

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.setAnswer(6);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        System.out.println(setId);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 0);
        test.addCond(VALUE, "b");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 1);
        test.addCond(VALUE, "a");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 0);
        test.addCond(VALUE, "c");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(IS_CLOSED, 1);
        test.addCond(IS_LAST, 1);
        test.addCond(VALUE, "b");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 1);
        test.addCond(VALUE, "d");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.addCond(IS_CLOSED, 1);
        test.addCond(IS_LAST, 1);
        test.addCond(VALUE, "c");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        int status =testHolder.runBatch();
        assertTrue(testHolder.lastWrong, status == 0);
    }

    /**
     +====+========+==========+========+=============+=======+===========+=========+
     | ID | SET_ID | BATCH_ID | INDEX_ | REPORT_DATE | VALUE | IS_CLOSED | IS_LAST |
     +====+========+==========+========+=============+=======+===========+=========+
     | 73 |   22   |   239    |   1    | 01.01.2015  |   b   |     0     |    0    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 74 |   22   |   238    |   1    | 01.01.2015  |   a   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 75 |   22   |   239    |   1    | 01.03.2015  |   d   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 76 |   22   |   240    |   1    | 01.02.2015  |   b   |     1     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 77 |   22   |   240    |   1    | 01.02.2015  |   c   |     0     |    0    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 78 |   22   |   240    |   1    | 01.03.2015  |   c   |     1     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     */
    @Test
    public void test2(){

        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        cli.readEntityFromXMLString(testingBaseEntites[2],testingDates[2]);
        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);

        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test;

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.setAnswer(6);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(IS_LAST, 0);
        test.addCond(IS_CLOSED, 0);
        test.addCond(VALUE, "b");
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(VALUE, "a");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.addCond(VALUE, "d");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(VALUE, "b");
        test.addCond(IS_CLOSED, 1);
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(VALUE, "c");
        test.addCond(IS_CLOSED, 0);
        test.addCond(IS_LAST, 0);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-03-01'");
        test.addCond(VALUE, "c");
        test.addCond(IS_CLOSED, 1);
        test.addCond(IS_LAST, 1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        int status =testHolder.runBatch();

        //int count = jdbcTemplate.queryForInt("select count(1) from eav_be_string_set_values where set_id = " + setId);
        assertTrue(testHolder.lastWrong, status == 0);
    }

    /**
     +====+========+==========+========+=============+=======+===========+=========+
     | ID | SET_ID | BATCH_ID | INDEX_ | REPORT_DATE | VALUE | IS_CLOSED | IS_LAST |
     +====+========+==========+========+=============+=======+===========+=========+
     | 88 |   25   |   247    |   1    | 01.02.2015  |   c   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 89 |   25   |   248    |   1    | 01.01.2015  |   a   |     0     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 90 |   25   |   248    |   1    | 01.01.2015  |   b   |     0     |    0    |
     +====+========+==========+========+=============+=======+===========+=========+
     | 91 |   25   |   248    |   1    | 01.02.2015  |   b   |     1     |    1    |
     +====+========+==========+========+=============+=======+===========+=========+
     */
    @Test
    public void test3(){

        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);

        readInDb();

        TestHolder testHolder = context.getBean(TestHolder.class);
        SingleTest test;

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.setAnswer(4);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(VALUE, "c");
        test.addCond(IS_CLOSED,0);
        test.addCond(IS_LAST,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(VALUE, "c");
        test.addCond(IS_CLOSED,0);
        test.addCond(IS_LAST,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-01-01'");
        test.addCond(VALUE, "b");
        test.addCond(IS_CLOSED,0);
        test.addCond(IS_LAST,0);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        test = new SingleTest();
        test.addCond(SET_ID, setId);
        test.addCond(REPORT_DATE, "date '2015-02-01'");
        test.addCond(VALUE, "b");
        test.addCond(IS_CLOSED,1);
        test.addCond(IS_LAST,1);
        test.setAnswer(1);
        test.setTable(EAV_BE_STRING_SET_VALUES);
        testHolder.addTest(test);

        int status = testHolder.runBatch();

        assertTrue(testHolder.lastWrong, status == 0);
    }

    /**
     * NULL pointer exception bug  in BaseEntityProcessorDaoImpl
     * when batch resend to the same report date
     */
    @Test
    public void test4() throws Exception {
        cli.readEntityFromXMLString(testingBaseEntites[0], testingDates[0]);
        long id1 = getBEid();
        checkException();
        cli.readEntityFromXMLString(testingBaseEntites[0], testingDates[0]);
        long id2 = getBEid();
        checkException();
        assertTrue("ids must be equal", id1 == id2);
    }
}
