package kz.bsbnb.usci.eav.tool;

import java.sql.Date;

/**
 * @author a.motov
 */
public interface Constants
{

    public static final int HISTORY_ALGORITHM_FILL_BIG_DATE = 1;
    public static final int HISTORY_ALGORITHM_NOT_FILL = 2;
    public static final Date HISTORY_MAX_DATE = new Date(new Long("4102423200000"));

}
