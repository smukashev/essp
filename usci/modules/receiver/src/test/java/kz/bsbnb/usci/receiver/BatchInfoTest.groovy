package kz.bsbnb.usci.receiver

import com.google.gson.GsonBuilder
import kz.bsbnb.usci.receiver.tools.JustDao
import kz.bsbnb.usci.receiver.tools.PrintUtils
import kz.bsbnb.usci.receiver.tools.ShowCaseUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by emles on 17.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class BatchInfoTest {

    Logger logger = LoggerFactory.getLogger(BatchInfoTest.class)

    @Autowired
    PrintUtils printUtils

    @Autowired
    JustDao justDao

    @Test
    void loadEntity() {

        long batchId = 530

        printUtils.printBatchInfo$creditIds$subjectIds(batchId)

    }

    @Test
    void getTables() {

        List<String> tables = justDao.getShowcaseTables()

        println tables

    }

    @Test
    void splitShowcases() {

        def keys = ["CREDIT_ID", "SUBJECT_ID"]

        Map<String, List<ShowCaseUtils.Table>> splitShowcases = justDao.splitShowcases(keys as String[])

        splitShowcases.each { key, value ->
            println """
################################################################
key: $key
value: ${
                /*JsonBuilder.newInstance(value).toPrettyString()*/
                /*JsonOutput.toJson(value)*/
                GsonBuilder.newInstance().setPrettyPrinting().create().toJson(value)
            }
"""
        }

        println splitShowcases

    }

    @Test
    void getDataForShowcases() {

        final Long batchId = 535
        final def keys = ["CREDIT_ID", "SUBJECT_ID"] as String[]
        final Map<Long, Set<Long>> map = printUtils.getBatchInfo$creditIds$subjectIds(batchId)

        Map<Long, List<Map<String, Object>>> rowsByCredit = justDao.getDataForShowcases(map, keys)

        println GsonBuilder.newInstance().setPrettyPrinting().create().toJson(rowsByCredit)

    }

}



