package kz.bsbnb;

import kz.bsbnb.testing.BaseUnitTest;
import kz.bsbnb.usci.eav.util.DataUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DataEntityTest extends BaseUnitTest {

    @Test(expected = RuntimeException.class)
    public void incorrectAttribute() {
        DataEntity credit = new DataEntity(metaCredit);

        credit.setDataValue("invalid", new DataStringValue("invalid"));
        fail("should have thrown Exception");

    }

    @Test
    public void testSimpleObject() throws Exception {
        DataEntity credit = new DataEntity(metaCredit);

        credit.setDataValue("amount", new DataDoubleValue(500.0));

        DataEntity primaryContract = new DataEntity(metaPrimaryContract);

        primaryContract.setDataValue("no", new DataStringValue("KZT-001"));
        primaryContract.setDataValue("date", new DataDateValue(DataUtils.getDate("01.01.2018")));

        credit.setDataValue("primary_contract", new DataComplexValue(primaryContract));

        Assert.assertEquals(500.0, credit.getEl("amount"));
        Assert.assertEquals("KZT-001", credit.getEl("primary_contract.no"));
        Assert.assertEquals(DataUtils.getDate("01.01.2018"), credit.getEl("primary_contract.date"));
    }
}