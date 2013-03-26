package kz.bsbnb.usci.batch.test.helper;

import junit.framework.Assert;
import kz.bsbnb.usci.batch.common.Global;
import kz.bsbnb.usci.batch.helper.impl.ParserHelper;
import kz.bsbnb.usci.eav.model.type.DataTypes;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author k.tulbassiyev
 */
public class ParserHelperTest
{
    ParserHelper parserHelper = new ParserHelper();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Global.DATE_FORMAT);

    @Test
    public void testInteger()
    {
        // test integer
        String integer1 = "123";
        String integer2 = "456";
        String integer3 = "789";

        Assert.assertEquals(123, parserHelper.getCastObject(DataTypes.INTEGER, integer1));
        Assert.assertEquals(456, parserHelper.getCastObject(DataTypes.INTEGER, integer2));
        Assert.assertEquals(789, parserHelper.getCastObject(DataTypes.INTEGER, integer3));

        Assert.assertFalse(new Integer(123).equals(parserHelper.getCastObject(DataTypes.INTEGER, integer3)));
    }

    @Test
    public void testDate() throws ParseException
    {
        // test integer
        String date1 = "2013-01-01";
        String date2 = "2012-05-05";
        String date3 = "2011-03-03";

        Assert.assertEquals(simpleDateFormat.parse(date1), parserHelper.getCastObject(DataTypes.DATE, date1));
        Assert.assertEquals(simpleDateFormat.parse(date2), parserHelper.getCastObject(DataTypes.DATE, date2));
        Assert.assertEquals(simpleDateFormat.parse(date3), parserHelper.getCastObject(DataTypes.DATE, date3));

        Assert.assertFalse(simpleDateFormat.parse(date1).equals(date2));
    }
}
