import groovy.json.StringEscapeUtils
import org.junit.Test

/**
 * Created by emles on 04.09.17
 */
class test_unicode {

    @Test
    void testUnescapeJava() {

        String sJava = "Helow java тест"
        String esJava = StringEscapeUtils.escapeJava(sJava)
        String unesJava = StringEscapeUtils.unescapeJava(esJava)

        println("A:\n" + sJava)
        println("B:\n" + esJava)
        println("C:\n" + unesJava)

    }

}



