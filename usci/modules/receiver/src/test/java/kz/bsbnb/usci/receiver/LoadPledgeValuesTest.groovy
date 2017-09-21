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
class LoadPledgeValuesTest {

    protected Logger logger = LoggerFactory.getLogger(LoadPledgeValuesTest.class)

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
                "01.07.2017",
                "01.08.2017",
                "01.09.2017",
                "01.10.2017"
        ]

        final String zipPath = "/opt/projects/info/batches/in/"

        final def batchsInfo = [

                [
                        contractNo  : "test-8",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-1",
                        reportDate  : "2017-07-01",
                        action      : "insert",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                                [pledgeType: 10, contractNo: "pledge-3", value: "222111"],*/
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-2",
                        reportDate  : "2017-08-01",
                        action      : "update",
                        pledgesList : [
                                /*[pledgeType: 22, contractNo: "pledge-1", value: "444333"],*/
                                [pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                                [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-3",
                        reportDate  : "2017-09-01",
                        action      : "update",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],*/
                                [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-4",
                        reportDate  : "2017-10-01",
                        action      : "update",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                /*[pledgeType: 10, contractNo: "pledge-2", value: "333222"],*/
                                [pledgeType: 10, contractNo: "pledge-3", value: "222111"],
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-5",
                        reportDate  : "2017-08-01",
                        action      : "update",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                [pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                                /*[pledgeType: 10, contractNo: "pledge-3", value: "222111"],*/
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-6",
                        reportDate  : "2017-07-01",
                        action      : "update",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                [pledgeType: 10, contractNo: "pledge-2", value: "333222"],
                                /*[pledgeType: 10, contractNo: "pledge-3", value: "222111"],*/
                                /*[pledgeType: 47],*/
                        ]
                ],

                [
                        contractNo  : "test-7",
                        contractDate: "2017-03-20",
                        caseFraze   : "-CASE-7",
                        reportDate  : "2017-08-01",
                        action      : "update",
                        pledgesList : [
                                [pledgeType: 22, contractNo: "pledge-1", value: "444333"],
                                [pledgeType: 10, contractNo: "pledge-2", value: "666555"],
                                /*[pledgeType: 10, contractNo: "pledge-3", value: "222111"],*/
                                /*[pledgeType: 47],*/
                        ]
                ]

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



