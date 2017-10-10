package kz.bsbnb.usci.receiver

import org.junit.Test

/**
 * Created by emles on 10.10.17
 */
class TestReg {

    @Test
    void regTest() {

        def test = { showcase, table ->
            print "showcase: $showcase, table: $table "
            println table ==~ /(?i)$showcase(_his)?/ ? "true" : "false"
        }

        test("table_1", "table_1")
        test("table_1", "table_1_his")
        test("table_1_his", "table_1")
        test("table_1_his", "table_1_his")

    }

}



