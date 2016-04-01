package base;

import kz.bsbnb.usci.cli.app.CLI;
import kz.bsbnb.usci.eav.persistance.db.JDBCSupport;
import kz.bsbnb.usci.eav.util.Errors;
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
    protected ApplicationContext context;

    protected String[] testingBaseEntites;
    protected String[] testingDates;
    protected final String ENTITY_ID = "entity_id";
    protected final String SET_ID = "set_id";
    protected final String REPORT_DATE = "report_date";
    protected final String VALUE = "value";
    protected final String NUMBER_OF_ROWS_INCORRECT = "number of rows incorrect";
    protected final String IS_LAST = "IS_LAST";
    protected final String EAV_BE_STRING_VALUES = "eav_be_string_values";
    protected final String EAV_BE_STRING_SET_VALUES = "eav_be_string_set_values";
    protected final String IS_CLOSED = "IS_CLOSED";
    protected long baseEntityId;
    protected String[] meta;
    protected int seed;
    protected boolean skipMeta;

    @Before
    public void beforeTesting(){
        //LogManager.getRootLogger().setLevel(Level.DEBUG);
        seed = jdbcTemplate.queryForInt("SELECT SEQ_EAV_FOR_TEST.NEXTVAL FROM DUAL");

        testingBaseEntites= new String[5];
        testingDates = new String[]{"01.01.2015","01.02.2015","01.03.2015","01.04.2015","01.05.2015"};

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

    public String wrap(String s){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entities>" + s + "</entities>";
    }

    public void nextKey(){
        for(int i=0;i<testingBaseEntites.length;i++) {
            testingBaseEntites[i] = wrap(testingBaseEntites[i]);
            testingBaseEntites[i]= testingBaseEntites[i].replaceFirst("key_\\d+","key_" + seed);
            //System.out.println(testingBaseEntites[i]);
        }
    }

    public long getBEid(){
        return jdbcTemplate.queryForLong("select max(id) from eav_be_entities");
    }

    public void checkNoException(){
        if(cli.getLastException() != null)
            throw new RuntimeException(Errors.compose(Errors.E211));
    }

}
