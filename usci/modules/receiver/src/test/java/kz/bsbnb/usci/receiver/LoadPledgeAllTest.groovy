package kz.bsbnb.usci.receiver

import kz.bsbnb.usci.eav.manager.IBaseEntityManagerHistory
import kz.bsbnb.usci.eav.model.Batch
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao
import kz.bsbnb.usci.receiver.monitor.ZipFilesMonitor
import kz.bsbnb.usci.receiver.queue.JobLauncherQueue
import kz.bsbnb.usci.receiver.tools.PrintUtils
import kz.bsbnb.usci.tools.clean_db
import kz.bsbnb.usci.tools.test_batches_gen
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by emles on 06.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class LoadPledgeAllTest {

    protected Logger logger = LoggerFactory.getLogger(LoadPledgeAllTest.class)

    @Autowired
    protected ZipFilesMonitor filesMonitor

    @Autowired
    private JobLauncherQueue jobLauncherQueue

    @Autowired
    private IBaseEntityLoadDao baseEntityLoadDao

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier(value = "remoteEntityManagerHistoryService")
    private RmiProxyFactoryBean rmiProxyFactoryBean

    private IBaseEntityManagerHistory baseEntityManagerHistoryRmiServiceExporter

    @Autowired
    PrintUtils printUtils

    @Test
    void loadFile() throws InterruptedException {

        final String[] reportDates = [
                "01.07.2017"
        ]

        final String zipPath = "/opt/projects/info/batches/in/"

        final def batchsInfo = [

                [
                        caseFraze   : "-CASE-1",
                        xml  : """
<?xml version="1.0" encoding="utf-8"?>
<batch xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <info>
    <creditor>
      <docs>
        <doc doc_type="15">
                    <no>KINCKZKA</no>
        </doc>
      </docs>
    </creditor>
    <account_date>2017-06-09</account_date>
    <report_date>2017-07-01</report_date>
    <actual_credit_count>27689</actual_credit_count>
  </info>
  <packages>
    <package operation_type="insert" no="1">
      <primary_contract>
        <no>000.095695</no>
        <date>2016-05-19</date>
      </primary_contract>
      <credit credit_type="10">
        <currency>KZT</currency>
        <interest_rate_yearly>36</interest_rate_yearly>
        <actual_issue_date>2016-05-19</actual_issue_date>
        <credit_purpose>01</credit_purpose>
        <credit_object>03</credit_object>
        <amount>202000</amount>
        <finance_source>01</finance_source>
        <has_currency_earn>false</has_currency_earn>
        <creditor_branch>
          <docs>
            <doc doc_type="11">
              <no>600400607703</no>
            </doc>
          </docs>
        </creditor_branch>
        <portfolio>
          <portfolio_msfo>0933</portfolio_msfo>
        </portfolio>
      </credit>
      <subjects>
        <subject>
          <person>
            <country>398</country>
            <bank_relations>
              <bank_relation>50</bank_relation>
            </bank_relations>
            <addresses>
              <address type="FA">
                <region>75</region>
                <details>АЛМАТЫ г, ТУРКСИБСКИЙ р-н, АЙНАБУЛАК-3 мкр, дом 108, к-ра 51</details>
              </address>
            </addresses>
            <names>
              <name lang="RU">
                <firstname>ЗАИДА</firstname>
                <lastname>ЖАНТУРЕЕВА</lastname>
                <middlename>АБИЛЬБАЕВНА</middlename>
              </name>
            </names>
            <docs>
              <doc doc_type="06">
                <no>670213402297</no>
              </doc>
            </docs>
          </person>
        </subject>
      </subjects>
      <change>
        <turnover>
          <issue>
            <debt>
              <amount>195376</amount>
              <amount_currency>0</amount_currency>
            </debt>
            <interest>
              <amount>0</amount>
              <amount_currency>0</amount_currency>
            </interest>
          </issue>
        </turnover>
        <remains>
          <debt>
            <current>
              <value>195376</value>
              <value_currency>0</value_currency>
              <balance_account>1403191</balance_account>
            </current>
          </debt>
          <limit>
            <value>6624</value>
            <balance_account>6625</balance_account>
          </limit>
        </remains>
      </change>
    </package>
  </packages>
</batch>



""",
                ],

        ]


        AntBuilder ant = new AntBuilder()

        ant.delete(dir: zipPath)
        ant.mkdir(dir: zipPath)

        logger.info "Cleaning db..."
        def clean = new clean_db()
        clean.run()

        logger.info "Generating test batches..."
        test_batches_gen.newInstance()
                .genAllBatches(zipPath, batchsInfo)

        logger.info "Starting tests..."

        baseEntityManagerHistoryRmiServiceExporter = (IBaseEntityManagerHistory) rmiProxyFactoryBean.getObject()

        batchsInfo.collect { it.caseFraze }.each { String caseFraze ->

            String year = "2017"

            String zipFileName = zipPath + year + caseFraze + ".ZIP"

            logger.info("Loading file...")
            Batch batch = filesMonitor.readFiles(zipFileName, 10196L, false)
            if (batch == null) {
                logger.info("Finished loading, no batches...")
                return
            }
            logger.info("Finished loading, batchId = " + batch.getId() + ".")

            while (true) {
                String status = jobLauncherQueue.getStatus()
                logger.info("Status: " + status)
                Thread.sleep(2000)
                if (status.contains("(empty)")) break
            }

            List<String> history = baseEntityManagerHistoryRmiServiceExporter.getHistory()


            printUtils.printCreditorInfo(batch.getCreditor())

            printUtils.printCreditorInfo(batch.getCreditor().getId())

            printUtils.printBatchInfo(batch.getId())

            printUtils.printEntitiesInfo(batch.getId())

            if (true)
                printUtils.printBatchEntitiesInfoDetailed(batch.getId(), reportDates)
            else
                printUtils.printBatchEntitiesInfo(batch.getId(), reportDates)

            printUtils.printHistory(history)

        }

        logger.info "Finished tests."

    }

}



