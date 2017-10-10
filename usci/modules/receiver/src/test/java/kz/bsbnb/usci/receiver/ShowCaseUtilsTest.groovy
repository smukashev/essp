package kz.bsbnb.usci.receiver

import groovy.json.JsonBuilder
import kz.bsbnb.usci.receiver.tools.ShowCaseUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by emles on 05.10.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class ShowCaseUtilsTest {

    protected Logger logger = LoggerFactory.getLogger(ShowCaseUtilsTest.class)

    @Autowired
    ShowCaseUtils showCaseUtils

    ShowCaseUtilsTest() {
        super()
    }


    @Test
    void test$genTables() {

        List<ShowCaseUtils.Table> tables = showCaseUtils.genTables()

        println "tables size: ${tables.size()}"

        tables.each { ShowCaseUtils.Table table ->
            println """%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
${new JsonBuilder(table).toPrettyString()}
"""
        }

    }

}



