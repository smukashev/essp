package kz.bsbnb.creator;

import kz.bsbnb.testing.FunctionalTest;
import org.junit.Test;

import java.sql.Statement;

public class SingleTableCreatorFunctionalTest extends FunctionalTest {

    @Test
    public void createTable() throws Exception {
        SingleTableCreator creator = new SingleTableCreator(metaCredit);

        Statement statement = dataSource.getConnection().createStatement();
        statement.executeUpdate(creator.getDDL().getTableCreationPart());
        statement.executeUpdate(creator.getDDL().getPrimaryKeyPart());
        statement.executeUpdate(creator.getDDL().getForeignKeyPart());
        dataSource.getConnection().close();
    }

}
