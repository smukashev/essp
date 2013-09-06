package kz.bsbnb.usci.bconv.cr.parser.util;

/**
 *
 * @author a.motov
 */
public class ParserUtils {
    
    public static boolean parseBoolean(String stringValue) 
    {
        int intValue = 0;
        boolean booleanValue = false;

        try {
            intValue = Integer.parseInt(stringValue);
        } catch(Exception ex) {
            // Do nothing
        }

        try {
            booleanValue = Boolean.parseBoolean(stringValue);
        } catch(Exception ex) {
            // Do nothing
        }

        return (intValue == 1 || booleanValue == true) ? true : false;
    }
    
}
