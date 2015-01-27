package deletion;

import base.CreditTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Created by Bauyrzhan.Makhambeto on 16.01.2015.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(locations = {"classpath:cliApplicationContextTest.xml"})
public class CreditTest extends CreditTestBase{

    public CreditTest() {
        super();
        skipMeta = true;
    }
    String creditor;
    String docType;

    @Override
    public void beforeTesting() {
        super.beforeTesting();

        creditor = wrap("<entity class=\"ref_creditor\">" +
                "<name>АО \"Народный сберегательный банк Казахстана\"</name><code>601</code>" +
                "<docs>" +
                "  <item>" +
                "     <no>940101000000</no>" +
                "     <doc_type>" +
                "        <name_ru>Бизнес - идентификационный номер </name_ru>" +
                "        <name_kz>Бизнес - идентификационный номер </name_kz>" +
                "        <is_identification>1</is_identification><is_organization_doc>1</is_organization_doc>" +
                "        <is_person_doc>0</is_person_doc><weight>10</weight>" +
                "        <code>07</code></doc_type>" +
                "   </item>" +
                "</docs>" +
                "<subject_type>" +
                "  <kind_id>31</kind_id>" +
                "  <name_kz>Банк второго уровня</name_kz>" +
                "   <name_ru>Банк второго уровня</name_ru>" +
                "  <code>0001</code><report_period_duration_months>1</report_period_duration_months></subject_type>" +
                "</entity>");
        docType = wrap("<entity class=\"ref_doc_type\"><name_ru>Бизнес - идентификационный номер </name_ru><name_kz>Бизнес - идентификационный номер </name_kz><is_organization_doc>1</is_organization_doc><is_person_doc>0</is_person_doc><weight>10</weight><code>07</code> </entity>");

        testingBaseEntites[0] = "<entity class=\"credit\" >" +
                " <primary_contract><date>01.01.2015</date><no>key_1</no></primary_contract>" +
                "<creditor>" +
                "  <docs>" +
                "    <item>" +
                "       <doc_type>" +
                "          <code>07</code>" +
                "       </doc_type>" +
                "       <no>940101000000</no>" +
                "    </item>" +
                "  </docs>" +
                "</creditor>" +
                " <amount>1</amount>" +
                "</entity>";

        testingBaseEntites[1] = "<entity class=\"credit\" operation=\"delete\">" +
                " <primary_contract><date>01.01.2015</date><no>key_1</no></primary_contract>" +
                "<creditor>" +
                "  <docs>" +
                "    <item>" +
                "       <doc_type>" +
                "          <code>07</code>" +
                "       </doc_type>" +
                "       <no>940101000000</no>" +
                "    </item>" +
                "  </docs>" +
                "</creditor>" +
                " <amount>1</amount>" +
                "</entity>";

        testingBaseEntites[2] = "<entity class=\"credit\" >" +
                " <primary_contract><date>01.01.2015</date><no>key_1</no></primary_contract>" +
                "<creditor>" +
                "  <docs>" +
                "    <item>" +
                "       <doc_type>" +
                "          <code>07</code>" +
                "       </doc_type>" +
                "       <no>940101000000</no>" +
                "    </item>" +
                "  </docs>" +
                "</creditor>" +
                " <amount>1</amount>" +
                "</entity>";

        nextKey();
    }

    @Test
    public void test1() throws Exception {

        cli.readEntityFromXMLString(docType, testingDates[0]);
        cli.readEntityFromXMLString(creditor, testingDates[0]);
        long id1,id2;
        cli.readEntityFromXMLString(testingBaseEntites[0],testingDates[0]);
        id1 = getBEid();
        checkNoException();

        cli.readEntityFromXMLString(testingBaseEntites[1],testingDates[1]);
        checkNoException();
        cli.readEntityFromXMLString(testingBaseEntites[2],testingDates[2]);
        id2 = getBEid();
        checkNoException();

        assertTrue("two new instances must be created",id1+1 == id2);
    }
}
