import base.EavTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Created by Bauyrzhan.Makhambeto on 22.01.2015.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(locations = {"classpath:cliApplicationContextTest.xml"})
public class EavComplexSetTest extends EavTest {
    public EavComplexSetTest() {
        meta = new String[]{
                "meta create --name=person",
                "meta create --name=house",
                "meta add --name=house --attribute=address --type=string",
                "meta add --name=person --attribute=houses --type=meta_class --array --childname=house",
                "meta add --name=person --attribute=no --type=string",
                "meta key --name=person --attribute=no"};

        skipMeta = true;
    };

    @Override
    public void beforeTesting() {
        super.beforeTesting();
        testingBaseEntites[0] = "<entity class=\"person\">" +
                " <no>key_1</no>" +
                "   <houses>" +
                "    <item>" +
                "      <address>a</address>" +
                "    </item>" +
                "   </houses>" +
                "</entity>";
        nextKey();
    }

    @Test
    public void test1() throws Exception {
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        long id1 = getBEid();
        checkNoException();
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        long id2 = getBEid();
        checkNoException();
        assertTrue("must be same id ", id1 == id2);
    }
}
