package kz.bsbnb.usci.receiver

import kz.bsbnb.usci.receiver.tools.PrintUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * Created by emles on 17.09.17
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
class LoadEntityTest {

    @Autowired
    PrintUtils printUtils

    @Test
    void loadEntity() {

        long batchId = 728

        String[] reportDates = [
                "01.07.2017",
                "01.08.2017",
                "01.09.2017",
                "01.10.2017",
        ]

        printUtils.printBatchEntitiesInfoDetailed(batchId, reportDates)

    }

}



