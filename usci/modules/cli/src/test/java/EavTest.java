import kz.bsbnb.usci.cli.app.CLI;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Bauyrzhan.Makhambeto on 12.01.2015.
 */
public class EavTest extends JDBCSupport {

    @Autowired
    protected CLI cli;

    @Autowired
    ApplicationContext context;

    String[] testingBaseEntites;
    String[] testingDates;
    final String ENTITY_ID = "entity_id";
    final String SET_ID = "set_id";
    final String REPORT_DATE = "report_date";
    final String VALUE = "value";
    final String NUMBER_OF_ROWS_INCORRECT = "number of rows incorrect";
    final String IS_LAST = "IS_LAST";
    final String EAV_BE_STRING_VALUES = "eav_be_string_values";
    final String EAV_BE_STRING_SET_VALUES = "eav_be_string_set_values";
    final String IS_CLOSED = "IS_CLOSED";
    protected long baseEntityId;
    protected String[] meta;
    protected int seed;
    protected boolean skipMeta;

    @Before
    public void beforeTesting(){
        //LogManager.getRootLogger().setLevel(Level.DEBUG);
        seed = jdbcTemplate.queryForInt("SELECT SEQ_EAV_FOR_TEST.NEXTVAL FROM DUAL");

        if(!skipMeta) {
            for (int i = 0; i < meta.length; i++) {
                meta[i] = meta[i].replaceFirst("meta ", "");
                cli.setArgs(new ArrayList<String>(Arrays.asList(meta[i].split("\\s+"))));
                cli.commandMeta();
            }
        cli.resetMetaCache();
        }
    }

    public void readInDb(){
        baseEntityId = jdbcTemplate.queryForLong("SELECT SEQ_EAV_BE_ENTITIES_ID.CURRVAL FROM DUAL");
    }

    public void nextKey(){
        for(int i=0;i<testingBaseEntites.length;i++) {
            testingBaseEntites[i] = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entities>"
                    + testingBaseEntites[i] + "</entities>";
            testingBaseEntites[i]= testingBaseEntites[i].replaceFirst("key_\\d+","key_" + seed);
            //System.out.println(testingBaseEntites[i]);
        }
    }

}
