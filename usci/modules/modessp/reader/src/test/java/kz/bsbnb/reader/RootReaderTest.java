package kz.bsbnb.reader;

import kz.bsbnb.DataEntity;
import kz.bsbnb.testing.FunctionalTest;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;


public class RootReaderTest extends FunctionalTest {
    RootReader reader = new RootReader();

    @Test
    public void mustReadSimpleValues() throws Exception {
        InputStream inputStream =  Thread.currentThread().
                getContextClassLoader().getResourceAsStream("kz/bsbnb/reader/credit.xml");
        reader.withSource(inputStream)
                .withMeta(metaCredit);
        DataEntity entity = reader.read();
        Assert.assertNotNull(entity);
        Assert.assertEquals(5000.0, entity.getEl("amount"));
        Assert.assertEquals(DataUtils.getDate("01.01.2018"), entity.getEl("maturity_date"));
    }
}