package kz.bsbnb.creator;

import kz.bsbnb.testing.FunctionalTest;
import org.junit.Test;

public class RecursiveTableCreatorFunctionalTest extends FunctionalTest {
    @Test
    public void testCreationAll() throws Exception {
        try {
            BaseTableCreator creator = new RecursiveTableCreator(metaCredit)
                    .withStaticTables()
                    .performDrops(true);

            creator.execute(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }
}