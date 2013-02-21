package kz.bsbnb.usci.eav.tool.generator.data;

import kz.bsbnb.usci.eav.model.metadata.DataTypes;

import java.util.Date;
import java.util.Random;

/**
 * @author k.tulbassiyev
 */
public abstract class AbstractDataGenerator
{
    protected Random rand = new Random(10000);

    protected Object getCastObject(DataTypes typeCode)
    {
        switch(typeCode)
        {
            case INTEGER:
                return rand.nextInt(1000);
            case DATE:
                return new Date();
            case STRING:
                return "string_" + rand.nextInt(1000);
            case BOOLEAN:
                return rand.nextBoolean();
            case DOUBLE:
                return rand.nextDouble()*10000;
            default:
                throw new IllegalArgumentException("Unknown type. Can not be returned an appropriate class.");
        }
    }
}
