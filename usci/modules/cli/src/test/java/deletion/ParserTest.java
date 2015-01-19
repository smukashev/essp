package deletion;

import base.EavTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Bauyrzhan.Makhambeto on 14.01.2015.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(locations = {"classpath:cliApplicationContextTest.xml"})
public class ParserTest extends EavTest {
    public ParserTest() {
        meta = new String[]{
            "meta create --name=person",
            "meta add --name=person --attribute=doc_no --type=string",
            "meta add --name=person --attribute=name --type=string",
            "meta key --name=person --attribute=doc_no",
            "meta create --name=deposit",
            "meta add --name=deposit --attribute=amount --type=double",
            "meta add --name=deposit --attribute=person --type=meta_class --childname=person",
            "meta add --name=deposit --attribute=contract_no --type=string",
            "meta key --name=deposit --attribute=contract_no"
        };
        //skipMeta = true;
    }

    @Override
    public void beforeTesting() {
        super.beforeTesting();

        testingBaseEntites[0] = "<entity class=\"deposit\" >" +
                " <contract_no>key_1</contract_no>" +
                "    <person>" +
                "       <name>a</name>" +
                "       <doc_no>001</doc_no>" +
                "    </person>" +
                " <amount>100000</amount>" +
                "</entity>";

        testingBaseEntites[1] = "<entity class=\"deposit\" operation=\"delete\">" +
                " <contract_no>key_1</contract_no>" +
                "    <person>" +
                "       <name>a</name>" +
                "       <doc_no>001</doc_no>" +
                "    </person>" +
                " <amount>100000</amount>" +
                "</entity>";

        testingBaseEntites[2] = "<entity class=\"deposit\">" +
                " <contract_no>key_1</contract_no>" +
                "    <person>" +
                "       <name>c</name>" +
                "       <doc_no>001</doc_no>" +
                "    </person>" +
                " <amount>100000</amount>" +
                "</entity>";

        nextKey();
    }

    @Test
    public void test1() throws Exception {
        cli.readEntityFromXMLString(testingBaseEntites[0], testingDates[0]);
        long id1 = getBEid();
        cli.readEntityFromXMLString(testingBaseEntites[1], testingDates[1]);

        cli.readEntityFromXMLString(testingBaseEntites[2], testingDates[2]);
        long id2 = getBEid();
        assertTrue("new instance must be added after delete",id1 + 1 == id2);

        readInDb();
    }
}
