package kz.bsbnb.usci.receiver

import kz.bsbnb.usci.cr.model.Creditor
import kz.bsbnb.usci.eav.manager.IBaseEntityManagerHistory
import kz.bsbnb.usci.eav.model.Batch
import kz.bsbnb.usci.eav.model.base.IBaseEntity
import kz.bsbnb.usci.eav.model.output.BaseToShortTool
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityLoadDao
import kz.bsbnb.usci.eav.util.Errors
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.sql.DataSource
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Created by emles on 06.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class LoadFileTest {

    protected Logger logger = LoggerFactory.getLogger(LoadFileTest.class)

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

        String zipPath = "/opt/projects/info/batches/in/"

        AntBuilder ant = new AntBuilder()

        ant.delete(dir: zipPath)
        ant.mkdir(dir: zipPath)

        logger.info "Cleaning db..."
        def clean = new clean_db()
        clean.run()

        logger.info "Generating test batches..."
        def batches_gen = new test_batches_gen()
        batches_gen.run()

        logger.info "Starting tests..."

        baseEntityManagerHistoryRmiServiceExporter = (IBaseEntityManagerHistory) rmiProxyFactoryBean.getObject()

        ["-CASE-1", "-CASE-2", "-CASE-3", "-CASE-4"].each { String caseFraze ->

            String[] reportDates = [
                    "01.07.2017",
                    "01.08.2017",
                    "01.09.2017",
                    "01.10.2017"
            ]

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



