package kz.bsbnb.usci.eav_persistance.test;

import static org.jooq.impl.Factory.fieldByName;
import static org.jooq.impl.Factory.tableByName;

import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.Executor;

/**
 *
 */
public class JooqTest {

   public void generateSQLTest() throws Exception
   {
       Query query;
       Executor create = new Executor(SQLDialect.ORACLE);

       query = create.select(fieldByName("BOOK","TITLE"), fieldByName("AUTHOR","FIRST_NAME"), fieldByName("AUTHOR","LAST_NAME"))
               .from(tableByName("BOOK"))
               .join(tableByName("AUTHOR")).on(fieldByName("BOOK", "AUTHOR_ID").equal(fieldByName("AUTHOR", "ID")))
               .where(fieldByName("BOOK", "PUBLISHED_IN").equal(1948));
       System.out.println("SELECT_SQL: " + query.getSQL());
       System.out.println("SELECT_SQL_BINDS: " + query.getBindValues());

       query = create.update(tableByName("EAV_BE_DATE_VALUES"))
               .set(fieldByName("EAV", "EAV_BE_DATE_VALUES", "BATCH_ID"), 1L)
               .set(fieldByName("EAV_BE_DATE_VALUES", "INDEX"), 1000L)
               .where(fieldByName("EAV_BE_DATE_VALUES", "ID").equal(5000));
       System.out.println("UPDATE_SQL: " + query.getSQL());
       System.out.println("UPDATE_SQL_BINDS: " + query.getBindValues());
   }

}
