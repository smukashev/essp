package kz.bsbnb.usci.eav.test;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class JooqTest {
    @Test
    public void generateSQLTest() throws Exception {
        Query query;
        DSLContext context = DSL.using(SQLDialect.POSTGRES);

        query = context.select(DSL.fieldByName("BOOK", "TITLE"), DSL.fieldByName("AUTHOR", "FIRST_NAME"), DSL.fieldByName("AUTHOR", "LAST_NAME"))
                .from(DSL.tableByName("BOOK"))
                .join(DSL.tableByName("AUTHOR")).on(DSL.fieldByName("BOOK", "AUTHOR_ID").equal(DSL.fieldByName("AUTHOR", "ID")))
                .where(DSL.fieldByName("BOOK", "PUBLISHED_IN").equal(1948));

        query = context.update(DSL.tableByName("EAV_BE_DATE_VALUES"))
                .set(DSL.fieldByName("EAV", "EAV_BE_DATE_VALUES", "BATCH_ID"), 1L)
                .set(DSL.fieldByName("EAV_BE_DATE_VALUES", "INDEX"), 1000L)
                .where(DSL.fieldByName("EAV_BE_DATE_VALUES", "ID").equal(5000));
    }
}
